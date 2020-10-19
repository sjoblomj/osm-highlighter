package org.sjoblomj.osmhighlighterdatagenerator

import org.sjoblomj.osmhighlighterdatagenerator.service.GeneratorService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.time.ExperimentalTime

@SpringBootApplication
class OsmhighlighterdatageneratorApplication(private val generatorService: GeneratorService) : CommandLineRunner {

	@ExperimentalTime
	override fun run(vararg args: String?) {
		for (file in args)
			if (file != null)
				generatorService.consumeFile(file)
	}


	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<OsmhighlighterdatageneratorApplication>(*args)
		}
	}
}
