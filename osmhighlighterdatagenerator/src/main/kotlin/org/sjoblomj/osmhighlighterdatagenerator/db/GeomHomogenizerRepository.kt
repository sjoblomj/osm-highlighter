package org.sjoblomj.osmhighlighterdatagenerator.db

import org.sjoblomj.osmhighlighterdatagenerator.dto.GeoEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface GeomHomogenizerRepository : CrudRepository<GeoEntity, Long> {

	@Transactional
	@Modifying
	@Query("update geoentries g set id = g.id, geom = ST_CollectionHomogenize(g.geom);", nativeQuery = true)
	fun homogenizeAllGeoms()
}
