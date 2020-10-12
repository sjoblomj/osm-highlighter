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
import org.sjoblomj.osmhighlighterdatagenerator.OsmhighlighterdatageneratorApplication
import org.sjoblomj.osmhighlighterdatagenerator.db.DatabaseRepository
import org.sjoblomj.osmhighlighterdatagenerator.db.TagRepository
import org.sjoblomj.osmhighlighterdatagenerator.dto.GeoEntity
import org.sjoblomj.osmhighlighterdatagenerator.dto.Tag
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import kotlin.system.measureTimeMillis

@Service
class Apa(private val geoRepo: DatabaseRepository, private val tagRepo: TagRepository) {

	fun consumeSweden() = consumeFile("/sweden-latest.osm.pbf")
	fun consumeNorway() = consumeFile("/norway-latest.osm.pbf")

	private fun consumeFile(filename: String) {

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
//		geoRepo.saveAll(createGeoEntities(interestingFeatures.todoRelations, "todo"))
		geoRepo.saveAll(createGeoEntities(interestingFeatures.namelessNodes, "nameless"))
		geoRepo.saveAll(createGeoEntities(interestingFeatures.namelessWays, "nameless"))
//		geoRepo.saveAll(createGeoEntities(interestingFeatures.namelessRelations, "nameless"))


		/**
		Nodes with todos: 4338
		Ways with todos: 7113
		Relations with todos: 197
		Nameless Nodes: 4339
		Nameless Ways: 176066
		Nameless Relations: 174
		Nodes needed for ways: 3433658


		== Values for highways of Nodes ==
		'rcn_ref': 1
		'sign': 1
		'traffic_signals;crossing': 1
		'street_light': 1
		'construction': 1
		'noexit': 1
		'mine': 1
		'stop;crossing': 1
		'stone': 1
		'service': 1
		'gate': 1
		'bus_stop;crossing': 2
		'abandoned': 2
		'lift': 2
		'roundabout': 2
		'speed_display': 2
		'incline': 3
		'stepping_stones': 3
		'unclassified': 3
		'traffic_sign': 3
		'residential': 3
		'junction': 4
		'path': 4
		'proposed': 4
		'raceway': 4
		'track': 4
		'emergency_access_point': 5
		'toll_gantry': 5
		'services': 6
		'incline_steep': 6
		'trailhead': 7
		'yes': 7
		'signpost': 8
		'traffic_mirror': 9
		'footway': 10
		'meeting': 11
		'steps': 11
		'milestone': 39
		'emergency_bay': 78
		'priority': 97
		'elevator': 169
		'mini_roundabout': 337
		'rest_area': 484
		'passing_place': 1003
		'turning_loop': 1019
		'stop': 1321
		'speed_camera': 1404
		'platform': 1491
		'motorway_junction': 1642
		'give_way': 4453
		'traffic_signals': 4700
		'street_lamp': 10404
		'crossing': 36041
		'bus_stop': 46676
		'turning_circle': 56028


		== Values for highways of Ways ==
		'junction': 1
		'iceroad': 1
		'footpath': 1
		'crossing': 1
		'residential;path': 1
		'unsurfaced': 1
		'escape': 1
		'informal': 1
		'fence': 1
		'unspecified': 2
		'turning_circle': 2
		'traffic_island': 3
		'escalator': 4
		'desire_path': 5
		'access': 6
		'yes': 13
		'vehicletesting': 16
		'services': 16
		'disused': 16
		'razed': 21
		'dismantled': 26
		'planned': 28
		'elevator': 33
		'bus_stop': 50
		'corridor': 114
		'rest_area': 153
		'abandoned': 178
		'proposed': 307
		'tertiary_link': 315
		'raceway': 846
		'secondary_link': 975
		'construction': 1124
		'primary_link': 1313
		'bridleway': 1323
		'road': 2365
		'trunk_link': 2919
		'pedestrian': 3452
		'motorway_link': 4717
		'motorway': 5078
		'living_street': 5496
		'primary': 10632
		'steps': 11450
		'trunk': 12552
		'platform': 16509
		'secondary': 30317
		'cycleway': 60383
		'tertiary': 61736
		'unclassified': 151185
		'footway': 155386
		'path': 172643
		'track': 175856
		'residential': 199578
		'service': 306455


		== Values for highways of Relations ==
		'yes': 1
		'bridleway': 1
		'trunk': 1
		'steps': 1
		'tertiary': 2
		'living_street': 2
		'raceway': 3
		'residential': 4
		'path': 6
		'unclassified': 10
		'services': 12
		'platform': 15
		'service': 52
		'footway': 54
		'rest_area': 58
		'pedestrian': 318



		////////// NORWAY //////////



		Nodes with todos: 5948
		Ways with todos: 24738
		Relations with todos: 557
		Nameless Nodes: 953
		Nameless Ways: 61697
		Nameless Relations: 91
		Nodes needed for ways: 1768759


		== Values for highways of Nodes ==
		'junction': 1
		'road': 1
		'bus_stop;crossing': 1
		'traffic_mirror;street_lamp': 1
		'via_ferrata': 1
		'locality': 1
		'weighbridge': 1
		'trailhead': 1
		'unclassified': 1
		'foothpath': 1
		'street_lamp;traffic_mirror': 1
		'yield': 1
		'track': 1
		'was:rest_area': 1
		'speed_display': 1
		'x-bus_stop': 2
		'disused_bus_stop': 2
		'residential': 2
		'bridge': 3
		'traffic_sign': 3
		'raceway': 4
		'layby': 4
		'yes': 4
		'proposed': 5
		'traffic_mirror': 5
		'services': 9
		'steps': 12
		'emergency_access_point': 17
		'milestone': 17
		'sign': 18
		'platform': 20
		'path': 27
		'construction': 28
		'emergency_bay': 42
		'mini_roundabout': 61
		'stop': 107
		'elevator': 108
		'turning_loop': 116
		'speed_camera': 402
		'toll_gantry': 425
		'motorway_junction': 559
		'traffic_signals': 1347
		'rest_area': 1465
		'give_way': 1666
		'passing_place': 3156
		'turning_circle': 6489
		'street_lamp': 21481
		'crossing': 25589
		'bus_stop': 98651


		== Values for highways of Ways ==
		'collapsed_steps': 1
		'draft': 1
		'ex-distribution-substation': 1
		'shieling': 1
		'unused': 1
		'proposed cycleway': 1
		'virtual': 1
		'minor road': 1
		'bus_guideway': 1
		'emergency_access_point': 1
		'footway-disabled': 1
		'resw': 1
		'ladder': 1
		'TR': 1
		'abandoned': 2
		'turning_circle': 2
		'SER': 2
		'emergency_access': 2
		'underwater_way': 2
		'proposed_cycleway': 2
		'winter_road': 2
		'disused_path': 3
		'trd': 3
		'awe': 3
		'industrial': 3
		'proposal_alternative': 4
		'razed': 4
		'trail': 5
		'crossing': 9
		'elevator': 10
		'planned': 18
		'services': 19
		'corridor': 33
		'bus_stop': 44
		'tertiary_link': 51
		'via_ferrata': 67
		'secondary_link': 105
		'rest_area': 251
		'platform': 393
		'primary_link': 396
		'road': 491
		'raceway': 569
		'bridleway': 577
		'motorway_link': 961
		'proposed': 1061
		'construction': 1560
		'living_street': 1778
		'pedestrian': 2144
		'motorway': 2204
		'trunk_link': 2396
		'steps': 9314
		'tertiary': 12909
		'trunk': 19154
		'primary': 27372
		'secondary': 37321
		'cycleway': 49779
		'unclassified': 72933
		'footway': 100032
		'track': 103147
		'residential': 143134
		'path': 154018
		'service': 618814


		== Values for highways of Relations ==
		'raceway': 1
		'path': 1
		'tertiary': 1
		'steps': 1
		'track': 2
		'residential': 4
		'rest_area': 4
		'footway': 8
		'platform': 10
		'service': 48
		'pedestrian': 123

		 */

		printStatistics(interestingFeatures)
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

	private fun printStatistics(interestingFeatures: InterestingFeatureFilter) {
		println("Nodes with todos: ${interestingFeatures.todoNodes.size}")
		println("Ways with todos: ${interestingFeatures.todoWays.size}")
		println("Relations with todos: ${interestingFeatures.todoRelations.size}")

		println("Nameless Nodes: ${interestingFeatures.namelessNodes.size}")
		println("Nameless Ways: ${interestingFeatures.namelessWays.size}")
		println("Nameless Relations: ${interestingFeatures.namelessRelations.size}")

		println("Nodes needed for ways: ${interestingFeatures.namelessWays.plus(interestingFeatures.todoWays).map { way -> way.numberOfNodes }.sum()}")
		println("\n")

		println("== Values for highways of Nodes ==")
		interestingFeatures.nodeHighways.toList().sortedBy { (_, value) -> value }.forEach { (key, count) -> println("'$key': $count") }
		println("\n")

		println("== Values for highways of Ways ==")
		interestingFeatures.wayHighways.toList().sortedBy { (_, value) -> value }.forEach { (key, count) -> println("'$key': $count") }
		println("\n")

		println("== Values for highways of Relations ==")
		interestingFeatures.relationHighways.toList().sortedBy { (_, value) -> value }.forEach { (key, count) -> println("'$key': $count") }
		println("\n")
	}

	private fun readPbfFileWithHandler(file: File, handler: OsmHandler) {
		FileInputStream(file).use {
			val reader = PbfReader(it, false)
			reader.setHandler(handler)
			reader.read()
		}
	}
}
