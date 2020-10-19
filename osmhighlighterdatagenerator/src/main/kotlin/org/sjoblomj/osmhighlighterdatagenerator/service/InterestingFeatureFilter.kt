package org.sjoblomj.osmhighlighterdatagenerator.service

import de.topobyte.osm4j.core.access.DefaultOsmHandler
import de.topobyte.osm4j.core.model.iface.OsmEntity
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.core.model.util.OsmModelUtil

class InterestingFeatureFilter : DefaultOsmHandler() {
	private val fixme = "fixme"
	private val nameless = "nameless"

	val categories = listOf(fixme, nameless)

	val nodes = mutableMapOf<OsmNode, Set<Category>>()
	val ways = mutableMapOf<OsmWay, Set<Category>>()
	val relations = mutableMapOf<OsmRelation, Set<Category>>()


	fun getNodes() = nodes.keys
	fun getWays() = ways.keys
	fun getRelations() = relations.keys
	fun getNodes(category: Category) = nodes.filterValues { it.contains(category) }.keys
	fun getWays(category: Category) = ways.filterValues { it.contains(category) }.keys
	fun getRelations(category: Category) = relations.filterValues { it.contains(category) }.keys



	override fun handle(entity: OsmNode) {
		handle(entity, nodes)
	}

	override fun handle(entity: OsmWay) {
		handle(entity, ways)
	}

	override fun handle(entity: OsmRelation) {
		handle(entity, relations)
	}

	private fun <T: OsmEntity> handle(entity: T, features: MutableMap<T, Set<Category>>) {
		val tags = OsmModelUtil.getTagsAsMap(entity)

		if (isFixme(tags))
			features.addEntityWithTag(entity, fixme)

		if (lacksName(tags))
			features.addEntityWithTag(entity, nameless)
	}
}

private fun <T: OsmEntity> MutableMap<T, Set<Category>>.addEntityWithTag(entity: T, tag: Category) {
	if (this.containsKey(entity))
		this[entity] = this[entity]!!.plus(tag)
	else
		this[entity] = setOf(tag)
}


private val allowedHighwayValues = listOf(
	"service",
	"track",
	"path",
	"footway",
	"cycleway",
	"steps",
	"motorway_link",
	"trunk_link",
	"bridleway",
	"primary_link",
	"construction",
	"secondary_link",
	"tertiary_link",
	"proposed",
	"abandoned",
	"rest_area",
	"corridor",
	"elevator",
	"planned",

	"turning_circle",
	"crossing",
	"street_lamp",
	"traffic_signals",
	"give_way",
	"speed_camera",
	"stop",
	"turning_loop",
	"passing_place",
	"mini_roundabout",
	"emergency_bay",
	"milestone"
)

private fun Map<String, String>.containsKeyValue(key: String, value: String) = this.containsKey(key) && this.containsValue(value)
private fun Map<String, String>.hasAllowedValueForKey(key: String, allowedValues: Collection<String>) =
	this.containsKey(key) && allowedValues.contains(this[key])

private fun isFixme(tags: MutableMap<String, String>) =
	tags.keys.any {
		val key = it.toLowerCase()
		key.startsWith("fixme") || key.startsWith("todo")
	}

private fun lacksName(tags: Map<String, String>) =
	tags.containsKey("highway") &&
		!tags.containsKey("ref") &&
		!tags.containsKey("name") &&
		!tags.containsKeyValue("junction", "roundabout") &&
		!tags.hasAllowedValueForKey("highway", allowedHighwayValues)

typealias Category = String
