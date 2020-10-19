package org.sjoblomj.osmhighlighterdatagenerator.dto

import javax.persistence.*

@Entity
@Table(name = "categories")
data class CategoryEntity(
	@Id
	@GeneratedValue
	val categoryid: Int,

	@Column(nullable = false)
	val name: String
)
