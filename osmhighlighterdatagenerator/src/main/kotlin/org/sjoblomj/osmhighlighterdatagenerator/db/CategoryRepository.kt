package org.sjoblomj.osmhighlighterdatagenerator.db

import org.sjoblomj.osmhighlighterdatagenerator.dto.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, Long>
