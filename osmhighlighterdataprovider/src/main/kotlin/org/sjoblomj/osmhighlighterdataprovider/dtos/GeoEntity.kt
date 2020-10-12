package org.sjoblomj.osmhighlighterdataprovider.dtos

import javax.persistence.*

@Entity
@Table(name = "bepa")
data class GeoEntity(
	@Id
	val id: Long,

	@Column(nullable = false)
	val geom: String,

	@Column(nullable = false)
	val category: String
)
