package org.sjoblomj.osmhighlighterdataprovider.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

data class Point(override val coordinates: List<Coordinate>, override val properties: Properties) : GeoJson {
	constructor(coordinate: Coordinate, properties: Properties) : this(listOf(coordinate), properties)

	@SuppressWarnings("unused")
	val type = "Point"

	@JsonProperty("coordinates")
	@SuppressWarnings("unused")
	fun formatCoordinates() = listOf(coordinates[0].lat, coordinates[0].lon)

	override fun toString(): String = ObjectMapper()
		.findAndRegisterModules()
		.writerWithDefaultPrettyPrinter()
		.writeValueAsString(this)
}
