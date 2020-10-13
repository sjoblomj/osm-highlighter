package org.sjoblomj.osmhighlighterdatagenerator.service

import de.topobyte.osm4j.core.access.DefaultOsmHandler
import de.topobyte.osm4j.core.model.iface.OsmEntity
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.core.model.util.OsmModelUtil

class InterestingFeatureFilter : DefaultOsmHandler() {
	val todoNodes = HashSet<OsmNode>()
	val todoWays = HashSet<OsmWay>()
	val todoRelations = HashSet<OsmRelation>()

	val namelessNodes = HashSet<OsmNode>()
	val namelessWays = HashSet<OsmWay>()
	val namelessRelations = HashSet<OsmRelation>()


	fun getNodes() = listOf(todoNodes, namelessNodes).flatten()
	fun getWays() = listOf(todoWays, namelessWays).flatten()
	fun getRelations() = listOf(todoRelations, namelessRelations).flatten()


	override fun handle(entity: OsmNode) {
		handle(entity, todoNodes, namelessNodes)
	}

	override fun handle(entity: OsmWay) {
		handle(entity, todoWays, namelessWays)
	}

	override fun handle(entity: OsmRelation) {
		handle(entity, todoRelations, namelessRelations)
	}

	private fun <T: OsmEntity> handle(entity: T, todoSet: HashSet<T>, namelessSet: HashSet<T>) {
		val tags = OsmModelUtil.getTagsAsMap(entity)

		if (tags["fixme"] != null || tags["todo"] != null)
			todoSet.add(entity)

		if (lacksName(tags))
			namelessSet.add(entity)
	}
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
private fun Map<String, String>.hasAllowedValueForKey(key: String, allowedValues: List<String>) =
	this.containsKey(key) && allowedValues.contains(this[key])

fun lacksName(tags: Map<String, String>) =
	tags.containsKey("highway") &&
		!tags.containsKey("ref") &&
		!tags.containsKey("name") &&
		!tags.containsKeyValue("junction", "roundabout") &&
		!tags.hasAllowedValueForKey("highway", allowedHighwayValues)
