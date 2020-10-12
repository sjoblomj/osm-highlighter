package org.sjoblomj.osmhighlighterdataprovider.dtos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PointTests {

	@Test
	fun `Creates correct Json`() {
		val feature = Point(
			Coordinate(-0.08, 51.49),
			Properties("Coord field", "Floorball stadium", "Trycksvärta och förblindanden"))

		assertEquals("{\n" +
			"  \"coordinates\" : [ -0.08, 51.49 ],\n" +
			"  \"properties\" : {\n" +
			"    \"name\" : \"Coord field\",\n" +
			"    \"amenity\" : \"Floorball stadium\",\n" +
			"    \"description\" : \"Trycksvärta och förblindanden\"\n" +
			"  },\n" +
			"  \"type\" : \"Point\"\n" +
			"}", feature.toString())
	}
}
