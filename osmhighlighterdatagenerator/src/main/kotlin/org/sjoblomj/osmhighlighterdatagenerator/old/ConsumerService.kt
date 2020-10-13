package org.sjoblomj.osmhighlighterdatagenerator.old

import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8


class ConsumerService {

	fun consume(): Pair<HashMap<String, Node>, HashMap<String, Way>> {
		val nodes = HashMap<String, Node>()
		val ways = HashMap<String, Way>()
		var lines = mutableListOf<String>()
		var consumingMultiLine = false

		val file = File(this.javaClass.getResource("/oslo.osm").toURI())
		FileUtils.lineIterator(file, UTF_8.name()).use {
			while (it.hasNext()) {
				val line = it.nextLine()

				if (line.startsWith("  <node ") && !line.endsWith(" />") || line.startsWith("  <way ") && !line.endsWith(" />"))
					consumingMultiLine = true
				if (consumingMultiLine)
					lines.add(line)

				if (line == "  </node>") {
					consumingMultiLine = false
					lines.add(line)

					val (id, node) = createNode(lines)
					nodes[id] = node

					lines = mutableListOf()
				}
				if (line.startsWith("  <node ") && line.endsWith(" />")) {
					val (id, node) = createNode(line)
					nodes[id] = node
				}

				if (line == "  </way>") {
					consumingMultiLine = false
					lines.add(line)

					val (id, way) = createWay(lines, nodes)
					ways[id] = way

					lines = mutableListOf()
				}
			}
		}
		println("0")

		return nodes to ways
	}

	private fun createNode(data: List<String>): Pair<String, Node> {
		val (id, node) = createNode(data[0])

		val properties = createProperties(data)
		return id to Node(node.lat, node.lon, properties)
	}

	private fun createProperties(data: List<String>): HashMap<String, String> {
		val properties = hashMapOf<String, String>()
		for (keyLine in data.filter { it.startsWith("    <tag ") }) {
			val key = getValue(keyLine, "k")
			val value = getValue(keyLine, "v")
			properties[key] = value
		}
		return properties
	}

	private fun createNode(data: String): Pair<String, Node> {
		val id = getValue(data, "id")
		val lat = getValue(data, "lat")
		val lon = getValue(data, "lon")

		return id to Node(lat, lon, emptyMap())
	}

	private fun createWay(data: List<String>, nodes: Map<String, Node>): Pair<String, Way> {
		val id = getValue(data[0], "id")

		val nodesInWay = data
			.filter { it.startsWith("    <nd ") }
			.map { getValue(it, "ref") }
			.map { nodes[it] }
		if (nodesInWay.contains(null))
			throw NullPointerException("Could not create Node for way with id $id")

		return id to Way(nodesInWay.filterNotNull(), createProperties(data))
	}

	private fun getValue(data: String, key: String): String {
		val k = " $key='"
		val keyPos = data.indexOf(k)
		return data.substring(keyPos + k.length, data.indexOf("'", keyPos + k.length))
	}
}
