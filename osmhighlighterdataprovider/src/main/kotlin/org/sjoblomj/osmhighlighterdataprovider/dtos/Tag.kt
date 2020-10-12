package org.sjoblomj.osmhighlighterdataprovider.dtos

import javax.persistence.*

@Entity
@Table(name = "tags")
data class Tag(

	@Id
	val key: String,

	@Column(nullable = false)
	val value: String
)
