package org.sjoblomj.osmhighlighterdatagenerator.service

import org.junit.Test
import org.mockito.Mockito.mock
import org.sjoblomj.osmhighlighterdatagenerator.db.DatabaseRepository
import org.sjoblomj.osmhighlighterdatagenerator.db.TagRepository
import org.sjoblomj.osmhighlighterdatagenerator.old.FeatureFilter

class ConsumerServiceTest {

	@Test
	fun `Can consume file`() {
		FeatureFilter().filter()
	}

	@Test
	fun `Test the pbf stuffz`() {
		Apa(mock(DatabaseRepository::class.java), mock(TagRepository::class.java)).consumeSweden()
	}
}
