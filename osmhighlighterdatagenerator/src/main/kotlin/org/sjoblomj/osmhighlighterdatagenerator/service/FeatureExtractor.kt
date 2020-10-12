package org.sjoblomj.osmhighlighterdatagenerator.service

import de.topobyte.osm4j.core.access.DefaultOsmHandler
import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.iface.EntityType.*
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.core.resolve.OsmEntityProvider

class FeatureExtractor(interestingNodes: List<OsmNode>,
											 interestingWays: List<OsmWay>,
											 interestingRelations: List<OsmRelation>) : DefaultOsmHandler(), OsmEntityProvider {

	private var hasUpdated = false

	private val nodes = hashMapOf<Long, OsmNode>()
	private val ways = hashMapOf<Long, OsmWay>()
	private val relations = hashMapOf<Long, OsmRelation>()

	private val wantedNodes: HashSet<Long>
	private val wantedWays: HashSet<Long>
	private val wantedRelations: HashSet<Long>

	init {
		wantedRelations = getIdsFromRelationOfType(interestingRelations, Relation)

		wantedWays = getIdsFromRelationOfType(interestingRelations, Way)

		wantedNodes = interestingWays.flatMap { way -> (0 until way.numberOfNodes).map { way.getNodeId(it) } }.toHashSet()
		wantedNodes.addAll(getIdsFromRelationOfType(interestingRelations, Node))
		wantedNodes.addAll(interestingNodes.map { it.id })
	}


	override fun handle(entity: OsmNode) {
		val id = entity.id
		if (wantedNodes.contains(id)) {
			if (nodes[id] == null) {

				nodes[id] = entity
				hasUpdated = true
			}
		}
	}

	override fun handle(entity: OsmWay) {
		val id = entity.id
		if (wantedWays.contains(id)) {
			if (ways[id] == null) {

				ways[id] = entity
				wantedNodes.addAll((0 until entity.numberOfNodes).map { entity.getNodeId(it) })
				hasUpdated = true
			}
		}
	}

	override fun handle(entity: OsmRelation) {
		val id = entity.id
		if (wantedRelations.contains(id)) {
			if (relations[id] == null) {

				relations[id] = entity
				wantedRelations.addAll(getIdsOfType(entity, Relation))
				wantedWays.addAll(getIdsOfType(entity, Way))
				wantedNodes.addAll(getIdsOfType(entity, Node))
				hasUpdated = true
			}
		}
	}

	override fun getNode(id: Long) = getEntity(id, nodes, "node")
	override fun getWay(id: Long) = getEntity(id, ways, "way")
	override fun getRelation(id: Long) = getEntity(id, relations, "relation")

	private fun <T> getEntity(id: Long, map: Map<Long, T>, type: String): T? {
		val entity = map[id]
		if (entity == null)
			println("Found no $type with id $id")
		return entity
	}


	private fun getIdsFromRelationOfType(relations: List<OsmRelation>, entityType: EntityType) =
		relations.flatMap { getIdsOfType(it, entityType) }.toHashSet()

	private fun getIdsOfType(relation: OsmRelation, entityType: EntityType) =
		(0 until relation.numberOfMembers)
			.filter { index -> relation.getMember(index).type == entityType }
			.map { index -> relation.getMember(index).id }


	fun resetUpdateStatus() {
		hasUpdated = false
	}

	fun hasUpdated() = hasUpdated
}
