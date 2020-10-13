package org.sjoblomj.osmhighlighterdataprovider.service

import org.sjoblomj.osmhighlighterdataprovider.db.DatabaseRepository
import org.sjoblomj.osmhighlighterdataprovider.db.TagRepository
import org.sjoblomj.osmhighlighterdataprovider.dtos.FeatureCount
import org.sjoblomj.osmhighlighterdataprovider.dtos.GeoEntity
import org.sjoblomj.osmhighlighterdataprovider.dtos.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class FeatureService(private val databaseRepository: DatabaseRepository, private val tagRepository: TagRepository) {

	@GetMapping("/feature", produces = [APPLICATION_JSON_VALUE])
	fun getFeature(@RequestParam bbox: String, @RequestParam showNotes: Boolean = false): String {

		val boundingBox = bbox.split(",").map { it.toFloat() }
//		databaseRepository.apa(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]).forEach { println(it) }
		val features = databaseRepository.getFeatures(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3])

		println("Found ${features.size} features for $bbox")
		val s = "[\n" + features.joinToString(",\n") { featureToJson(it) } + "\n]"
//		println(s)
		return s
	}

	@GetMapping("/featurecount", produces = [APPLICATION_JSON_VALUE])
	fun getFeatureCount(@RequestParam bbox: String): FeatureCount {

		val boundingBox = bbox.split(",").map { it.toFloat() }
		val numberOfFeatures = databaseRepository.getFeatureCount(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3])

		println("Returning a count of $numberOfFeatures features for $bbox")
		return FeatureCount(numberOfFeatures)
	}

	@GetMapping("/tags", produces = [APPLICATION_JSON_VALUE])
	fun getTags(@RequestParam id: Long): List<Tag> {

		println("Tags requested for $id")
		val tags = tagRepository.findAllById(id)

		println("Returning $tags for $id")
		return tags
	}


	fun featureToJson(geoEntity: GeoEntity): String {
		fun createGeoJson(entityType: String) = "{${createProperties(geoEntity.id, entityType, geoEntity.category)}, ${geoEntity.geom.substring(1)}"

		return when {
			geoEntity.geom.startsWith("{\"type\":\"Point\"") -> createGeoJson("node")
			geoEntity.geom.startsWith("{\"type\":\"LineString\"") -> createGeoJson("way")
			else -> createGeoJson("relation")
		}
	}

	private fun createProperties(id: Long, entityType: String, category: String): String {
		val url = "https://www.openstreetmap.org/$entityType/$id"
		return "\"properties\": {\"entityId\": \"$id\", \"url\": \"$url\", \"category\":\"$category\"}"
	}
}