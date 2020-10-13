package org.sjoblomj.osmhighlighterdatagenerator.service

import de.topobyte.osm4j.core.access.OsmHandler
import de.topobyte.osm4j.core.model.iface.*
import de.topobyte.osm4j.core.model.util.OsmModelUtil
import de.topobyte.osm4j.geometry.GeometryBuilder
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy
import de.topobyte.osm4j.geometry.MissingWayNodeStrategy
import de.topobyte.osm4j.pbf.seq.PbfReader
import org.sjoblomj.osmhighlighterdatagenerator.OsmhighlighterdatageneratorApplication
import org.sjoblomj.osmhighlighterdatagenerator.db.DatabaseRepository
import org.sjoblomj.osmhighlighterdatagenerator.db.TagRepository
import org.sjoblomj.osmhighlighterdatagenerator.dto.GeoEntity
import org.sjoblomj.osmhighlighterdatagenerator.dto.Tag
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.util.HashSet
import kotlin.system.measureTimeMillis

@Service
class GeneratorService(private val geoRepo: DatabaseRepository, private val tagRepo: TagRepository) {

	fun consumeFile(filename: String) {

		val file = File(OsmhighlighterdatageneratorApplication::class.java.getResource(filename).toURI())

		val interestingFeatures = InterestingFeatureFilter()
		readPbfFileWithHandler(file, interestingFeatures)

		saveTagsToDatabase(interestingFeatures)

		val featureExtractor = FeatureExtractor(interestingFeatures.getNodes(), interestingFeatures.getWays(), interestingFeatures.getRelations())
		for (i in 0..10) {
			println("Iteration $i at finding interesting features")
			readPbfFileWithHandler(file, featureExtractor)
			if (featureExtractor.hasUpdated())
				featureExtractor.resetUpdateStatus()
			else
				break
		}

		val geometryBuilder = GeometryBuilder().also {
			it.missingWayNodeStrategy = MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE
			it.missingEntitiesStrategy = MissingEntitiesStrategy.BUILD_PARTIAL
		}

		fun createGeoEntities(entities: Set<OsmEntity>, category: String) = entities
			.map { it.id to buildGeometry(it, geometryBuilder, featureExtractor) }
			.filter { it.second != null }
			.map { GeoEntity(it.first, it.second!!, category)}

		geoRepo.saveAll(createGeoEntities(interestingFeatures.todoNodes, "todo"))
		geoRepo.saveAll(createGeoEntities(interestingFeatures.todoWays, "todo"))
		geoRepo.saveAll(createGeoEntities(relationsFilter(interestingFeatures.todoRelations), "todo"))
		geoRepo.saveAll(createGeoEntities(interestingFeatures.namelessNodes, "nameless"))
		geoRepo.saveAll(createGeoEntities(interestingFeatures.namelessWays, "nameless"))
		geoRepo.saveAll(createGeoEntities(relationsFilter(interestingFeatures.namelessRelations), "nameless"))
	}

	private fun relationsFilter(relations: HashSet<OsmRelation>): Set<OsmRelation> {
		return relations.filter { relation ->
			(0 until relation.numberOfMembers)
				.map { relation.getMember(it) }
				.none { it.type == EntityType.Relation }
		}.toSet()
	}


	private fun saveTagsToDatabase(interestingFeatures: InterestingFeatureFilter) {
		fun saveTags(list: List<OsmEntity>) {
			val tags = list.flatMap { entity ->
				val tags = OsmModelUtil.getTagsAsMap(entity)
				tags.map { tag -> Tag(entity.id, tag.key, tag.value) }
			}
			tagRepo.saveAll(tags)
		}

		val timeTaken = measureTimeMillis {
			saveTags(interestingFeatures.getNodes())
			saveTags(interestingFeatures.getWays())
			saveTags(interestingFeatures.getRelations())
		}
		println("Saved tags in $timeTaken ms")
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
			println("Exception for node ${entity.id}: $e")
			null
		}

	private fun buildGeom(entity: OsmWay, geometryBuilder: GeometryBuilder, featureExtractor: FeatureExtractor) =
		try {
			geometryBuilder.build(entity, featureExtractor)
		} catch (e: Exception) {
			println("Exception for way ${entity.id}: $e")
			null
		}

	private fun buildGeom(entity: OsmRelation, geometryBuilder: GeometryBuilder, featureExtractor: FeatureExtractor) =
		try {
			geometryBuilder.build(entity, featureExtractor)
		} catch (e: Exception) {
			println("Exception for relation ${entity.id}: $e")
			null
		}

	private fun readPbfFileWithHandler(file: File, handler: OsmHandler) {
		FileInputStream(file).use {
			val reader = PbfReader(it, false)
			reader.setHandler(handler)
			reader.read()
		}
	}
}
