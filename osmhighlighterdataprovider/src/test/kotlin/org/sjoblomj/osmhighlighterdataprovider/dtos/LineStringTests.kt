package org.sjoblomj.osmhighlighterdataprovider.dtos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LineStringTests {

	@Test
	fun `Creates correct Json`() {
		val lineString = LineString(
			listOf(Coordinate(-105.0, 40.0), Coordinate(-110.0, 45.0), Coordinate(-115.0, 55.0)),
			Properties("Main street", "highway", "Apa bepa"))

		assertEquals("{\n" +
			"  \"coordinates\" : [ [ -105.0, 40.0 ], [ -110.0, 45.0 ], [ -115.0, 55.0 ] ],\n" +
			"  \"properties\" : {\n" +
			"    \"name\" : \"Main street\",\n" +
			"    \"amenity\" : \"highway\",\n" +
			"    \"description\" : \"Apa bepa\"\n" +
			"  },\n" +
			"  \"type\" : \"LineString\"\n" +
			"}", lineString.toString())
	}
}
