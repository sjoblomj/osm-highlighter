package org.sjoblomj.osmhighlighterdataprovider.db

import org.sjoblomj.osmhighlighterdataprovider.dtos.GeoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DatabaseRepository : CrudRepository<GeoEntity, Long> {
	@Query("select count(*) " +
		"from $geoentryTableName f " +
		"where ST_INTERSECTS(ST_MakeEnvelope(?1, ?2, ?3, ?4), f.geom);", nativeQuery = true)
	fun getFeatureCount(a: Float, b: Float, c: Float, d: Float): Int

	@Query("select f.id as id, ST_AsGeoJSON(f.geom) as geom " +
		"from $geoentryTableName f " +
		"where ST_INTERSECTS(ST_MakeEnvelope(?1, ?2, ?3, ?4), f.geom) " +
		"limit $maxNumberOfFeaturesReturned;", nativeQuery = true)
	fun getFeatures(a: Float, b: Float, c: Float, d: Float): List<GeoEntity>
}

const val geoentryTableName = "geoentries"
const val maxNumberOfFeaturesReturned = 512
