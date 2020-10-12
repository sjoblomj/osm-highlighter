package org.sjoblomj.osmhighlighterdataprovider.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.Entity
import javax.persistence.Table

data class LineString(override val coordinates: List<Coordinate>, override val properties: Properties) : GeoJson {

	@SuppressWarnings("unused")
	val type = "LineString"

	@JsonProperty("coordinates")
	@SuppressWarnings("unused")
	fun formatCoordinates() = coordinates.map { listOf(it.lat, it.lon) }

	override fun toString(): String = ObjectMapper()
		.findAndRegisterModules()
		.writerWithDefaultPrettyPrinter()
		.writeValueAsString(this)
}
