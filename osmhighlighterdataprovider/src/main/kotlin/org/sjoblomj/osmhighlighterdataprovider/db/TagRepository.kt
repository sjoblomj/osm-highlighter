package org.sjoblomj.osmhighlighterdataprovider.db

import org.sjoblomj.osmhighlighterdataprovider.dtos.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, Long> {

	@Query("select tag.key as key, tag.value as value " +
		"from $tableName tag " +
		"where tag.id = ?1", nativeQuery = true)
	fun findAllById(id: Long): List<Tag>
}

private const val tableName = "tags"
