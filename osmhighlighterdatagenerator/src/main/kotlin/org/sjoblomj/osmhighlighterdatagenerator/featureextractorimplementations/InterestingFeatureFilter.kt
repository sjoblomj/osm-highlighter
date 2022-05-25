package org.sjoblomj.osmhighlighterdatagenerator.featureextractorimplementations

import de.topobyte.osm4j.core.model.iface.OsmEntity
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.core.model.util.OsmModelUtil
import org.sjoblomj.osmhighlighterdatagenerator.service.Category

class InterestingFeatureFilter {
	private val fixme = "Fixme"
	private val nameless = "Nameless"
	private val unknownHighwayValues = "Unknown highway values"

	val categories = listOf(fixme, nameless, unknownHighwayValues)

	val nodesAndCategories = mutableMapOf<OsmNode, Collection<Category>>()
	val waysAndCategories = mutableMapOf<OsmWay, Collection<Category>>()
	val relationsAndCategories = mutableMapOf<OsmRelation, Collection<Category>>()


	fun handle(entity: OsmNode) {
		handle(entity, nodesAndCategories)
	}

	fun handle(entity: OsmWay) {
		handle(entity, waysAndCategories)
	}

	fun handle(entity: OsmRelation) {
		handle(entity, relationsAndCategories)
	}

	private fun <T: OsmEntity> handle(entity: T, features: MutableMap<T, Collection<Category>>) {
		val tags = OsmModelUtil.getTagsAsMap(entity)

		if (hasUnknownHighwayValue(tags))
			features.addEntityWithTag(entity, unknownHighwayValues)
		else if (lacksName(tags))
			features.addEntityWithTag(entity, nameless)

		if (isFixme(tags))
			features.addEntityWithTag(entity, fixme)
	}
}

private fun <T: OsmEntity> MutableMap<T, Collection<Category>>.addEntityWithTag(entity: T, tag: Category) {
	if (this.containsKey(entity))
		this[entity] = this[entity]!!.plus(tag)
	else
		this[entity] = setOf(tag)
}


private val highwayValuesThatDontNeedNames = listOf(
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
	"milestone",
	"toll_gantry"
)

private val highwayValuesThatNeedNames = listOf(
	"residential",
	"unclassified",
	"tertiary",
	"bus_stop",
	"secondary",
	"platform",
	"trunk",
	"primary",
	"living_street",
	"motorway",
	"pedestrian",
	"motorway_junction",
	"raceway"
)


private fun Map<String, String>.containsKeyValue(key: String, value: String) = this.containsKey(key) && this.containsValue(value)
private fun Map<String, String>.hasAllowedValueForKey(key: String, allowedValues: Collection<String>) =
	this.containsKey(key) && allowedValues.contains(this[key])

private fun isFixme(tags: MutableMap<String, String>) =
	tags.keys.any {
		val key = it.lowercase()
		key.startsWith("fixme") || key.startsWith("todo")
	}

private fun lacksName(tags: Map<String, String>) =
	tags.containsKey("highway") &&
		!tags.containsKey("ref") &&
		!tags.containsKey("name") &&
		!tags.containsKeyValue("junction", "roundabout") &&
		!tags.hasAllowedValueForKey("highway", highwayValuesThatDontNeedNames)

private fun hasUnknownHighwayValue(tags: Map<String, String>) =
	tags.containsKey("highway") && !highwayValuesThatDontNeedNames.contains(tags["highway"]) && !highwayValuesThatNeedNames.contains(tags["highway"])
