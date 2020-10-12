package org.sjoblomj.osmhighlighterdataprovider.dtos

import com.fasterxml.jackson.annotation.JsonValue

interface GeoJson {
	val coordinates: List<Coordinate>
	val properties: Properties
}

data class Properties(val name: String, val amenity: String, val description: String)

data class Coordinate(val lat: Double, val lon: Double) {

	@JsonValue
	override fun toString() = "[$lat, $lon]"
}
