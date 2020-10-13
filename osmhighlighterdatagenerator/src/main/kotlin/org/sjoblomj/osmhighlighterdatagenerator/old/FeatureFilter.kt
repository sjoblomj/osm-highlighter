package org.sjoblomj.osmhighlighterdatagenerator.old

class FeatureFilter {

	private val allowedHighwayValues = listOf("service", "speed_camera", "give_way", "footway", "steps", "motorway_link", "crossing", "cycleway", "path", "track", "traffic_signals", "passing_place", "street_lamp", "mini_roundabout", "turning_circle", "turning_loop", "trunk_link", "bridleway")

	fun filter() {
		val (nodes, ways) = ConsumerService().consume()

		val nodeTodos = nodes.filter { it.value.properties.containsKey("todo") || it.value.properties.containsKey("fixme") }
		val wayTodos  = ways .filter { it.value.properties.containsKey("todo") || it.value.properties.containsKey("fixme") }

		val poisonousAndEvilWays = filterWays(ways.values)
	}

	private fun Map<String, String>.containsKeyValue(key: String, value: String) = this.containsKey(key) && this.containsValue(value)
	private fun Map<String, String>.hasAllowedValueForKey(key: String, allowedValues: List<String>) =
		this.containsKey(key) && allowedValues.contains(this[key])

	private fun filterWays(ways: Collection<Way>) = ways
			.filter {
				it.properties.containsKey("highway") &&
				!it.properties.containsKey("ref") &&
				!it.properties.containsKey("name") &&
				!it.properties.containsKeyValue("junction", "roundabout") &&
				!(it.properties.containsKeyValue("highway", "construction") && it.properties.containsKeyValue("construction", "service")) &&
				!it.properties.hasAllowedValueForKey("highway", allowedHighwayValues)
			}
}
