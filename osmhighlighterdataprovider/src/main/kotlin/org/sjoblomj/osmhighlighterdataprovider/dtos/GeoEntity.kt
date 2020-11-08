package org.sjoblomj.osmhighlighterdataprovider.dtos

import org.sjoblomj.osmhighlighterdataprovider.db.geoentryTableName
import javax.persistence.*

@Entity
@Table(name = geoentryTableName)
data class GeoEntity(
	@Id
	val id: Long,

	@Column(nullable = false)
	val geom: String,

	@Column
	@ElementCollection
	val category: Collection<Int>
)
