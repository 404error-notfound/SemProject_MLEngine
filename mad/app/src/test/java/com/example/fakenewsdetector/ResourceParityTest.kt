package com.example.fakenewsdetector

import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards the localisation promise: every user-facing English string has a
 * Swahili counterpart. `app_name` is intentionally not translated.
 */
class ResourceParityTest {

    private fun stringsFile(path: String): File {
        val candidates = listOf(File(path), File("app/$path"), File("../app/$path"))
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find $path (looked in ${candidates.joinToString()})")
    }

    private fun keys(file: File): Set<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length)
            .mapNotNull { nodes.item(it) as? Element }
            .mapNotNull { it.getAttribute("name").takeIf { name -> name.isNotEmpty() } }
            .toSet()
    }

    @Test
    fun `swahili strings cover english keys`() {
        val english = keys(stringsFile("src/main/res/values/strings.xml"))
        val swahili = keys(stringsFile("src/main/res/values-sw/strings.xml"))
        val missing = english - swahili - setOf("app_name")
        assertTrue("Missing Swahili strings: ${missing.sorted()}", missing.isEmpty())
    }

    @Test
    fun `correction templates exist in both languages`() {
        val required = setOf(
            "correction_verdict_false",
            "correction_verdict_unverified",
            "correction_verdict_true",
            "correction_message"
        )
        val english = keys(stringsFile("src/main/res/values/strings.xml"))
        val swahili = keys(stringsFile("src/main/res/values-sw/strings.xml"))
        assertTrue(required.all { it in english })
        assertTrue(required.all { it in swahili })
    }
}
