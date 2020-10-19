package org.sjoblomj.osmhighlighterdatagenerator.db

import org.sjoblomj.osmhighlighterdatagenerator.dto.GeoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GeometryRepository : JpaRepository<GeoEntity, Long>
