package org.sjoblomj.osmhighlighterdataprovider.db

import org.sjoblomj.osmhighlighterdataprovider.dtos.GeoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface DatabaseRepository : CrudRepository<GeoEntity, Long> {

	@Query("SELECT f.id AS id, ST_AsGeoJSON(f.geom) AS geom " +
		"FROM $geoentryTableName f, " +
		"ST_MakeEnvelope(?1, ?2, ?3, ?4) AS bbox " +
		"WHERE ST_INTERSECTS(bbox, f.geom) AND " +
		"NOT EXISTS (" +
		"   SELECT " +
		"   FROM  hide_ids " +
		"   WHERE id = f.id " +
		"   ) " +
		"LIMIT $maxNumberOfFeaturesReturned;", nativeQuery = true)
	fun getFeatures(a: Float, b: Float, c: Float, d: Float): List<GeoEntity>

	@Transactional
	@Modifying
	@Query("INSERT INTO hide_ids(id) VALUES (?1) ON CONFLICT DO NOTHING;", nativeQuery = true)
	fun hide(id: Long)
}

const val geoentryTableName = "geoentries"
const val maxNumberOfFeaturesReturned = 512
