package org.sjoblomj.osmhighlighterdatagenerator.db

import org.sjoblomj.osmhighlighterdatagenerator.dto.Tag
import org.sjoblomj.osmhighlighterdatagenerator.dto.TagId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, TagId>
