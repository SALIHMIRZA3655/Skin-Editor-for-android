package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SkinEntity
import com.example.data.SkinPackEntity
import com.example.export.McPackExporter
import com.example.export.McPackImporter
import com.example.model.CharacterPart
import com.example.model.LayerType
import com.example.model.PartFace
import com.example.model.PresetSkinLibrary
import com.example.model.SkinModelType
import com.example.model.SkinTextureHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SkinPackLogicTest {

    @Test
    fun testClassicVsSlimModelGeometry() {
        assertEquals("geometry.humanoid.custom", SkinModelType.CLASSIC.geometryKey)
        assertEquals("geometry.humanoid.customSlim", SkinModelType.SLIM.geometryKey)
        assertEquals(4, SkinModelType.CLASSIC.armWidth)
        assertEquals(3, SkinModelType.SLIM.armWidth)

        assertEquals(SkinModelType.CLASSIC, SkinModelType.fromIsSlim(false))
        assertEquals(SkinModelType.SLIM, SkinModelType.fromIsSlim(true))
    }

    @Test
    fun testPartRectCalculation() {
        val headRect = SkinTextureHelper.getPartRect(
            CharacterPart.HEAD,
            PartFace.FRONT,
            LayerType.BASE,
            isSlim = false
        )
        assertEquals(8, headRect.x)
        assertEquals(8, headRect.y)
        assertEquals(8, headRect.width)
        assertEquals(8, headRect.height)

        val slimArmRect = SkinTextureHelper.getPartRect(
            CharacterPart.RIGHT_ARM,
            PartFace.FRONT,
            LayerType.BASE,
            isSlim = true
        )
        assertEquals(44, slimArmRect.x)
        assertEquals(20, slimArmRect.y)
        assertEquals(3, slimArmRect.width)
        assertEquals(12, slimArmRect.height)
    }

    @Test
    fun testPresetSkinGeneration() {
        val stevePixels = PresetSkinLibrary.getStevePixels()
        assertEquals(64 * 64, stevePixels.size)

        val alexPixels = PresetSkinLibrary.getAlexPixels()
        assertEquals(64 * 64, alexPixels.size)

        val bitmap = SkinTextureHelper.createCompositePreview(stevePixels, isSlim = false, isBackView = false, scale = 2)
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun testMcPackCreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pack = SkinPackEntity(
            name = "Test Kahramanlar",
            description = "Test skin paketi",
            version = "1.0.0",
            manifestUuid = UUID.randomUUID().toString(),
            moduleUuid = UUID.randomUUID().toString()
        )

        val steveBase64 = SkinTextureHelper.pixelsToBase64(PresetSkinLibrary.getStevePixels())
        val alexBase64 = SkinTextureHelper.pixelsToBase64(PresetSkinLibrary.getAlexPixels())

        val skins = listOf(
            SkinEntity(packId = 1L, name = "Steve", isSlim = false, textureBase64 = steveBase64),
            SkinEntity(packId = 1L, name = "Alex", isSlim = true, textureBase64 = alexBase64)
        )

        val result = McPackExporter.createMcPack(context, pack, skins)
        assertTrue(result.success)
        assertNotNull(result.file)
        assertTrue(result.file!!.exists())
        assertTrue(result.file!!.length() > 0)
        assertNotNull(result.uri)
    }

    @Test
    fun testMcPackImportAndExportRoundtrip() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val originalPack = SkinPackEntity(
            name = "Benim Ozel Paketim",
            description = "Duzenlenebilir mcpack testi",
            version = "2.1.0",
            manifestUuid = UUID.randomUUID().toString(),
            moduleUuid = UUID.randomUUID().toString()
        )

        val steveBase64 = SkinTextureHelper.pixelsToBase64(PresetSkinLibrary.getStevePixels())
        val alexBase64 = SkinTextureHelper.pixelsToBase64(PresetSkinLibrary.getAlexPixels())

        val originalSkins = listOf(
            SkinEntity(packId = 1L, name = "Kral Steve", isSlim = false, textureBase64 = steveBase64),
            SkinEntity(packId = 1L, name = "Savasci Alex", isSlim = true, textureBase64 = alexBase64)
        )

        val exportResult = McPackExporter.createMcPack(context, originalPack, originalSkins)
        assertTrue(exportResult.errorMessage ?: "Export failed", exportResult.success)
        val file = exportResult.file!!
        assertTrue(file.exists())

        // Now test import from file input stream
        file.inputStream().use { inputStream ->
            val importResult = McPackImporter.importFromStream(inputStream, "OzelPaket.mcpack")
            assertTrue(importResult.success)
            val importedPack = importResult.pack
            assertNotNull(importedPack)

            assertEquals("Benim Ozel Paketim", importedPack!!.name)
            assertEquals("Duzenlenebilir mcpack testi", importedPack.description)
            assertEquals(2, importedPack.skins.size)

            val importedSteve = importedPack.skins.find { it.name == "Kral Steve" }
            assertNotNull(importedSteve)
            assertEquals(false, importedSteve!!.isSlim)
            assertEquals(64 * 64, importedSteve.pixels.size)

            val importedAlex = importedPack.skins.find { it.name == "Savasci Alex" }
            assertNotNull(importedAlex)
            assertEquals(true, importedAlex!!.isSlim)
            assertEquals(64 * 64, importedAlex.pixels.size)
        }
    }

    @Test
    fun testExportGeneratesFreshUniqueUuidsToPreventCopyDetection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val originalPack = SkinPackEntity(
            id = 42L,
            name = "Test UUID Pack",
            description = "UUID degisim testi",
            version = "1.0.0",
            manifestUuid = UUID.randomUUID().toString(),
            moduleUuid = UUID.randomUUID().toString()
        )

        val steveBase64 = SkinTextureHelper.pixelsToBase64(PresetSkinLibrary.getStevePixels())
        val skins = listOf(
            SkinEntity(packId = 42L, name = "Steve", isSlim = false, textureBase64 = steveBase64)
        )

        // First export
        val firstExport = McPackExporter.createMcPack(
            context = context,
            pack = originalPack,
            skins = skins
        )
        assertTrue(firstExport.success)
        val firstFile = firstExport.file!!

        // Second export of the exact same pack
        val secondExport = McPackExporter.createMcPack(
            context = context,
            pack = originalPack,
            skins = skins
        )
        assertTrue(secondExport.success)
        val secondFile = secondExport.file!!

        // Read manifests from both exports
        val firstPack = firstFile.inputStream().use {
            McPackImporter.importFromStream(it, "first.mcpack").pack!!
        }
        val secondPack = secondFile.inputStream().use {
            McPackImporter.importFromStream(it, "second.mcpack").pack!!
        }

        // Verify UUIDs in exported packs are newly generated and never identical
        assertNotEquals(firstPack.manifestUuid, secondPack.manifestUuid)
        assertNotEquals(originalPack.manifestUuid, firstPack.manifestUuid)
        assertNotEquals(originalPack.manifestUuid, secondPack.manifestUuid)
    }

    @Test
    fun testSkinPackAndSkinRenaming() {
        val originalPack = SkinPackEntity(
            id = 10L,
            name = "Eski Paket Adi",
            description = "Eski Aciklama",
            version = "1.0.0"
        )
        val updatedPack = originalPack.copy(
            name = "Yeni Paket Adi (SALIHMIRZA3655)",
            description = "Guncellenmis Aciklama",
            version = "1.1.0"
        )
        assertEquals("Yeni Paket Adi (SALIHMIRZA3655)", updatedPack.name)
        assertEquals("Guncellenmis Aciklama", updatedPack.description)
        assertEquals("1.1.0", updatedPack.version)

        val originalSkin = SkinEntity(
            id = 5L,
            packId = 10L,
            name = "Eski Karakter",
            isSlim = false,
            textureBase64 = "abc"
        )
        val renamedSkin = originalSkin.copy(name = "Kral Steve V2")
        assertEquals("Kral Steve V2", renamedSkin.name)
        assertEquals(originalSkin.id, renamedSkin.id)
    }
}
