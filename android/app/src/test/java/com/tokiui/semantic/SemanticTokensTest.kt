package com.tokiui.semantic

import com.tokidiscover.app.ui.theme.schemeFromSeed
import com.tokidiscover.app.ui.theme.colorFromHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SemanticTokensTest {
    @Test
    fun snapshotMatchesTheTokiUiContract() {
        val tokens = SemanticTokens.parse(read("tokiui.semantic.tokens.json"))
        assertEquals("TokiUI", tokens.name)
        assertEquals("0.2.0-b1", tokens.version)
        assertTrue(tokens.typeScalesWithSystem)
        assertEquals(listOf("紫", "蓝", "青", "绿", "橙"), tokens.seeds.map { it.name })
        assertEquals(tokens.seed, tokens.seeds.first().value)
        assertEquals(16, tokens.space.getValue("lg"))
        assertTrue(tokens.motionShortMs < tokens.motionStandardMs)
        assertTrue(tokens.materialFallback.isNotBlank())
        assertEquals("bold", tokens.typeRoles.first { it.id == "title" }.weight)
        assertEquals("bold", tokens.typeRoles.first { it.id == "label" }.weight)
        assertEquals("bold", tokens.typeRoles.first { it.id == "bodyEmphasized" }.weight)
        assertEquals("regular", tokens.typeRoles.first { it.id == "body" }.weight)
        assertEquals("regular", tokens.typeRoles.first { it.id == "caption" }.weight)
        assertEquals("Inter", tokens.typeLatin)
        assertEquals("Source Han Sans", tokens.typeCjk)
        assertEquals("Noto Sans SC", tokens.typeCjkBundledAs)
        assertTrue(tokens.materialLightGlass.contains("浅色"))
        assertTrue(tokens.materialSolidBar.contains("描边"))
        assertTrue(tokens.materialScrollTitle.contains("描边"))
        assertEquals(SemanticTokens.colorRoles.toSet(), tokens.light.keys)
        assertEquals(SemanticTokens.colorRoles.toSet(), tokens.dark.keys)
        tokens.typeRoles.forEach { role ->
            assertTrue(role.androidStyle.isNotBlank())
            assertTrue(role.referenceSizeSp > 0)
        }
    }

    @Test
    fun showcaseInventoryCoversEveryScene() {
        val inventory = ShowcaseInventory.parse(read("showcase.inventory.json"))
        assertEquals("0.2.0-b1", inventory.version)
        assertEquals(ShowcaseSectionKind.entries.map { it.id }, inventory.sections.map { it.id })
        assertEquals("色彩", inventory.section("color").title)
    }

    @Test
    fun seedSchemeComesFromTheTokenSeedNotACopiedPalette() {
        val tokens = SemanticTokens.parse(read("tokiui.semantic.tokens.json"))
        val seed = colorFromHex(tokens.seed)
        val light = schemeFromSeed(seed, dark = false)
        val dark = schemeFromSeed(seed, dark = true)
        assertNotEquals(light.primary, dark.primary)
        assertNotEquals(light.primary, light.onPrimary)
        assertNotEquals(dark.primary, dark.onPrimary)
    }

    @Test
    fun androidMinSdkFollowsTokiUi() {
        val gradle = File("build.gradle.kts").readText()
        assertTrue(gradle.contains("minSdk = 36"))
        assertTrue(gradle.contains("\"0.1.6\""))
    }

    private fun read(name: String): String {
        val file = File("../tokiui/tokens/$name")
        assertTrue("missing $name", file.isFile)
        return file.readText()
    }
}
