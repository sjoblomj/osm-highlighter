package org.sjoblomj.osmhighlighterdataprovider.service

import org.sjoblomj.osmhighlighterdataprovider.db.CategoryRepository
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
class FeatureService(private val databaseRepository: DatabaseRepository,
										 private val tagRepository: TagRepository,
										 categoryRepository: CategoryRepository) {

	private val dbCategories: Map<Int, String> = categoryRepository.findAll().map { it.categoryid to it.name }.toMap()


	@GetMapping("/feature", produces = [APPLICATION_JSON_VALUE])
	fun getFeatures(@RequestParam bbox: String, @RequestParam showNotes: Boolean = false): String {

		val boundingBox = bbox.split(",").map { it.toFloat() }
		val queryStart = System.currentTimeMillis()
		val features = databaseRepository.getFeatures(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3])

		println("Found ${features.size} features for $bbox in ${System.currentTimeMillis() - queryStart} ms")
		val conversionStart = System.currentTimeMillis()
		val json = features.map { featureToJson(it) }.toString()
		println("getFeatures(): Json created in ${System.currentTimeMillis() - conversionStart} ms")
		return json
	}

	@GetMapping("/featurecount", produces = [APPLICATION_JSON_VALUE])
	fun getFeatureCount(@RequestParam bbox: String): FeatureCount {

		val boundingBox = bbox.split(",").map { it.toFloat() }
		val queryStart = System.currentTimeMillis()
		val numberOfFeatures = databaseRepository.getFeatureCount(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3])

		println("Returning a count of $numberOfFeatures features for $bbox ${System.currentTimeMillis() - queryStart} in ms")
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
		fun createGeoJson(entityType: String) = geoEntity.geom.replaceFirst("{", "\n{${createProperties(geoEntity.id, entityType, geoEntity.category)}, ")

		return when {
			geoEntity.geom.startsWith("{\"type\":\"Point\"") -> createGeoJson("node")
			geoEntity.geom.startsWith("{\"type\":\"LineString\"") -> createGeoJson("way")
			else -> createGeoJson("relation")
		}
	}

	private fun createProperties(id: Long, entityType: String, categories: Collection<Int>): String {
		val cats = categories.mapNotNull { catId -> dbCategories[catId] }.joinToString(",") { "\"$it\"" }
		val url = "https://www.openstreetmap.org/$entityType/$id"
		return "\"properties\": {\"entityId\": \"$id\", \"url\": \"$url\", \"category\":[$cats]}"
	}
}
