package org.sjoblomj.osmhighlighterdatagenerator

import org.sjoblomj.osmhighlighterdatagenerator.service.Apa
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OsmhighlighterdatageneratorApplication(private val apa: Apa) : CommandLineRunner {

	override fun run(vararg args: String?) {
		apa.consumeSweden()
		apa.consumeNorway()
	}


	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<OsmhighlighterdatageneratorApplication>(*args)
		}
	}
}
