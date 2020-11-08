package org.sjoblomj.osmhighlighterdataprovider.db

import org.sjoblomj.osmhighlighterdataprovider.dtos.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, Long>
