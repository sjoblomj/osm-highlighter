package org.sjoblomj.osmhighlighterdataprovider.db

import org.sjoblomj.osmhighlighterdataprovider.dtos.GeoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DatabaseRepository : JpaRepository<GeoEntity, Long> {
	@Query("select count(*) " +
		"from $tableName f " +
		"where ST_INTERSECTS(ST_MakeEnvelope(?1, ?2, ?3, ?4), f.geom);", nativeQuery = true)
	fun getFeatureCount(a: Float, b: Float, c: Float, d: Float): Int

	@Query("select f.id as id, ST_AsGeoJSON(f.geom) as geom, f.category as category " +
		"from $tableName f " +
		"where ST_INTERSECTS(ST_MakeEnvelope(?1, ?2, ?3, ?4), f.geom) " +
		"limit $maxNumberOfFeaturesReturned;", nativeQuery = true)
	fun getFeatures(a: Float, b: Float, c: Float, d: Float): List<GeoEntity>

	@Query("select f.id as id, f.geom as geom, f.category as category " +
		"from $tableName f " +
		"where ST_INTERSECTS(ST_MakeEnvelope(?1, ?2, ?3, ?4), f.geom) " +
		"limit $maxNumberOfFeaturesReturned;", nativeQuery = true)
	fun apa(a: Float, b: Float, c: Float, d: Float): List<GeoEntity>
}

private const val tableName = "bepa"
const val maxNumberOfFeaturesReturned = 512
