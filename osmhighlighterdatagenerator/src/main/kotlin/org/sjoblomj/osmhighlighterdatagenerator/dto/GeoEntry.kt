package org.sjoblomj.osmhighlighterdatagenerator.dto

import org.locationtech.jts.geom.Geometry
import javax.persistence.*

@Entity
@Table(name = "bepa")
data class GeoEntity(
	@Id
	val id: Long,

	@Column(nullable = false)
	val geom: Geometry,

	@Column(nullable = false)
	val category: String
)
