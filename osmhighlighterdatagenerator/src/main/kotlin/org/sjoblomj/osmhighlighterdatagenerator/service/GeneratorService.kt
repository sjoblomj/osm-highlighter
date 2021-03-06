package org.sjoblomj.osmhighlighterdatagenerator.service

import de.topobyte.osm4j.core.access.OsmHandler
import de.topobyte.osm4j.core.model.iface.OsmEntity
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.core.model.util.OsmModelUtil
import de.topobyte.osm4j.geometry.GeometryBuilder
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy
import de.topobyte.osm4j.geometry.MissingWayNodeStrategy
import de.topobyte.osm4j.pbf.seq.PbfReader
import org.sjoblomj.osmhighlighterdatagenerator.db.CategoryRepository
import org.sjoblomj.osmhighlighterdatagenerator.db.GeometryRepository
import org.sjoblomj.osmhighlighterdatagenerator.db.NativeQueryRepository
import org.sjoblomj.osmhighlighterdatagenerator.db.TagRepository
import org.sjoblomj.osmhighlighterdatagenerator.dto.CategoryEntity
import org.sjoblomj.osmhighlighterdatagenerator.dto.GeoEntity
import org.sjoblomj.osmhighlighterdatagenerator.dto.Tag
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.FileInputStream
import kotlin.system.measureTimeMillis

@Service
class GeneratorService(private val geoRepo: GeometryRepository,
											 private val nativeQueryRepository: NativeQueryRepository,
											 private val tagRepo: TagRepository,
											 private val categoryRepository: CategoryRepository) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun consumeFile(filename: String) {
		logger.info("Starting to consume $filename")

		val interestingFeatures = InterestingFeatureFilter()
		val timetaken = measureTimeMillis {
			readPbfFileWithHandler(filename, interestingFeatures)
		}
		logger.info("Consumed in $timetaken ms")

		saveCategoriesToDatabase(interestingFeatures)
		val categories = categoryRepository.findAll()
		saveTagsToDatabase(interestingFeatures)

		val featureExtractor = FeatureExtractor(interestingFeatures.getNodes(), interestingFeatures.getWays(), interestingFeatures.getRelations())
		for (i in 0..10) {
			logger.info("Iteration $i at finding interesting features")
			readPbfFileWithHandler(filename, featureExtractor)
			if (featureExtractor.hasUpdated())
				featureExtractor.resetUpdateStatus()
			else
				break
		}

		logger.info("Finished finding interesting features; creating GeoEntities")
		val geometryBuilder = GeometryBuilder().also {
			it.missingWayNodeStrategy = MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE
			it.missingEntitiesStrategy = MissingEntitiesStrategy.BUILD_PARTIAL
		}

		fun <T: OsmEntity> createGeoEntities(entities: Map<T, Set<Category>>) = entities
			.map { (entity, categorySet) -> Triple(entity.id, buildGeometry(entity, geometryBuilder, featureExtractor), categorySet) }
			.filter { it.second != null }
			.map { GeoEntity(it.first, it.second!!, it.third.map { category -> categories.first { categoryEntry -> categoryEntry.name == category }.categoryid })}

		logger.info("Saving GeoEntities to database")
		val dbTimeTaken = measureTimeMillis {
			geoRepo.saveAll(createGeoEntities(interestingFeatures.relations))
			geoRepo.saveAll(createGeoEntities(interestingFeatures.ways))
			geoRepo.saveAll(createGeoEntities(interestingFeatures.nodes))
		}
		logger.info("Took $dbTimeTaken ms to save GeoEntities to database.")


		val nativeQueryTime = measureTimeMillis {
			nativeQueryRepository.homogenizeAllGeoms()
			nativeQueryRepository.createGeoEntryCategoryIndex()
			nativeQueryRepository.createHideIdsTable()
		}
		logger.info("Took $nativeQueryTime ms to perform native queries.")
	}


	private fun saveCategoriesToDatabase(interestingFeatures: InterestingFeatureFilter) {
		val timeTaken = measureTimeMillis {
			val previouslySavedCategories = categoryRepository.findAll().map { it.name }
			interestingFeatures.categories
				.filter { !previouslySavedCategories.contains(it) }
				.map { categoryRepository.save(CategoryEntity(0, it)) }
		}
		logger.info("Saved categories to database in $timeTaken ms")
	}

	private fun saveTagsToDatabase(interestingFeatures: InterestingFeatureFilter) {
		fun saveTags(entities: Collection<OsmEntity>) {
			val tags = entities.flatMap { entity ->
				val tags = OsmModelUtil.getTagsAsMap(entity)
				tags.map { tag -> Tag(entity.id, tag.key, tag.value) }
			}
			tagRepo.saveAll(tags)
		}

		logger.info("About to save tags to database")
		val timeTaken = measureTimeMillis {
			saveTags(interestingFeatures.getNodes())
			saveTags(interestingFeatures.getWays())
			saveTags(interestingFeatures.getRelations())
		}
		logger.info("Saved tags in $timeTaken ms")
	}


	private fun buildGeometry(entity: OsmEntity, geometryBuilder: GeometryBuilder, featureExtractor: FeatureExtractor) =
		when (entity) {
			is OsmNode -> buildGeom(entity, geometryBuilder)
			is OsmWay -> buildGeom(entity, geometryBuilder, featureExtractor)
			is OsmRelation -> buildGeom(entity, geometryBuilder, featureExtractor)
			else -> null
		}

	private fun buildGeom(entity: OsmNode, geometryBuilder: GeometryBuilder) =
		try {
			geometryBuilder.build(entity)
		} catch (e: Exception) {
			logger.error("Exception for node ${entity.id}: $e")
			null
		}

	private fun buildGeom(entity: OsmWay, geometryBuilder: GeometryBuilder, featureExtractor: FeatureExtractor) =
		try {
			geometryBuilder.build(entity, featureExtractor)
		} catch (e: Exception) {
			logger.error("Exception for way ${entity.id}: $e")
			null
		}

	private fun buildGeom(entity: OsmRelation, geometryBuilder: GeometryBuilder, featureExtractor: FeatureExtractor) =
		try {
			geometryBuilder.build(entity, featureExtractor)
		} catch (e: Exception) {
			logger.error("Exception for relation ${entity.id}: $e")
			null
		}

	private fun readPbfFileWithHandler(file: String, handler: OsmHandler) {
		FileInputStream(file).use {
			val reader = PbfReader(it, false)
			reader.setHandler(handler)
			reader.read()
		}
	}
}
