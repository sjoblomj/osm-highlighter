package org.sjoblomj.osmhighlighterdatagenerator.featureextractorimplementations

import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.iface.EntityType.*
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import org.sjoblomj.osmhighlighterdatagenerator.service.FeatureExtractorInterface
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FeatureExtractor : FeatureExtractorInterface {

	private val logger = LoggerFactory.getLogger(javaClass)

	private var hasUpdated = true
	private var isRunningFirstRound = true

	private val nodes = hashMapOf<Long, OsmNode>()
	private val ways = hashMapOf<Long, OsmWay>()
	private val relations = hashMapOf<Long, OsmRelation>()

	private var wantedNodes: HashSet<Long> = hashSetOf()
	private var wantedWays: HashSet<Long> = hashSetOf()
	private var wantedRelations: HashSet<Long> = hashSetOf()

	private val interestingFeatureFilter = InterestingFeatureFilter()

	override fun shouldRunConsumptionRound() = hasUpdated

	override fun startConsumptionRound() {
		hasUpdated = false
	}

	override fun finishConsumptionRound() {
		if (isRunningFirstRound) {
			val nodes = getNodesAndCategoriesToBePersisted().keys
			val ways = getWaysAndCategoriesToBePersisted().keys
			val relations = getRelationsAndCategoriesToBePersisted().keys

			wantedRelations = getIdsFromRelationOfType(relations, Relation)

			wantedWays = getIdsFromRelationOfType(relations, Way)

			wantedNodes = ways.flatMap { way -> (0 until way.numberOfNodes).map { way.getNodeId(it) } }.toHashSet()
			wantedNodes.addAll(getIdsFromRelationOfType(relations, Node))
			wantedNodes.addAll(nodes.map { it.id })

			isRunningFirstRound = false
			hasUpdated = true
		}
	}

	override fun getCategoriesToBePersisted() = interestingFeatureFilter.categories
	override fun getNodesAndCategoriesToBePersisted() = interestingFeatureFilter.nodesAndCategories
	override fun getWaysAndCategoriesToBePersisted() = interestingFeatureFilter.waysAndCategories
	override fun getRelationsAndCategoriesToBePersisted() = interestingFeatureFilter.relationsAndCategories


	override fun handle(entity: OsmNode) {
		if (isRunningFirstRound) {
			interestingFeatureFilter.handle(entity)

		} else {
			val id = entity.id
			if (wantedNodes.contains(id) && nodes[id] == null) {

				nodes[id] = entity
				hasUpdated = true
			}
		}
	}

	override fun handle(entity: OsmWay) {
		if (isRunningFirstRound) {
			interestingFeatureFilter.handle(entity)

		} else {
			val id = entity.id
			if (wantedWays.contains(id) && ways[id] == null) {

				ways[id] = entity
				wantedNodes.addAll((0 until entity.numberOfNodes).map { entity.getNodeId(it) })
				hasUpdated = true
			}
		}
	}

	override fun handle(entity: OsmRelation) {
		if (isRunningFirstRound) {
			interestingFeatureFilter.handle(entity)

		} else {
			val id = entity.id
			if (wantedRelations.contains(id) && relations[id] == null) {

				relations[id] = entity
				wantedRelations.addAll(getIdsOfType(entity, Relation))
				wantedWays.addAll(getIdsOfType(entity, Way))
				wantedNodes.addAll(getIdsOfType(entity, Node))
				hasUpdated = true
			}
		}
	}

	override fun getNode(id: Long) = getEntity(id, nodes)
	override fun getWay(id: Long) = getEntity(id, ways)
	override fun getRelation(id: Long) = getEntity(id, relations)

	private inline fun <reified T> getEntity(id: Long, map: Map<Long, T>): T? {
		val entity = map[id]
		if (entity == null)
			logger.warn("Found no ${T::class.java.simpleName} with id $id")
		return entity
	}


	private fun getIdsFromRelationOfType(relations: Collection<OsmRelation>, entityType: EntityType) =
		relations.flatMap { getIdsOfType(it, entityType) }.toHashSet()

	private fun getIdsOfType(relation: OsmRelation, entityType: EntityType) =
		(0 until relation.numberOfMembers)
			.filter { index -> relation.getMember(index).type == entityType }
			.map { index -> relation.getMember(index).id }
}
