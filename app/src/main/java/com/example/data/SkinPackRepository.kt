package com.example.data

import android.content.Context
import com.example.export.ImportedPackData
import com.example.model.PresetSkinLibrary
import com.example.model.SkinTextureHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class SkinPackRepository(
    private val dao: SkinPackDao,
    private val context: Context
) {
    val allPacksWithSkins: Flow<List<SkinPackWithSkins>> = dao.getAllPacksWithSkins()
    val allPacks: Flow<List<SkinPackEntity>> = dao.getAllPacks()

    fun getPackWithSkins(packId: Long): Flow<SkinPackWithSkins?> = dao.getPackWithSkins(packId)

    suspend fun createPack(name: String, description: String, version: String = "1.0.0"): Long {
        val pack = SkinPackEntity(
            name = name.trim().ifEmpty { "Özel Skin Paketi" },
            description = description.trim().ifEmpty { "Skin Pack Maker ile oluşturuldu" },
            version = version.trim().ifEmpty { "1.0.0" },
            manifestUuid = UUID.randomUUID().toString(),
            moduleUuid = UUID.randomUUID().toString()
        )
        return dao.insertPack(pack)
    }

    suspend fun importPack(importedPack: ImportedPackData): Long {
        val pack = SkinPackEntity(
            name = importedPack.name.trim().ifEmpty { "İçe Aktarılan Paket" },
            description = importedPack.description.trim().ifEmpty { "İçe aktarılan skin paketi" },
            version = importedPack.version.trim().ifEmpty { "1.0.0" },
            manifestUuid = importedPack.manifestUuid.ifEmpty { UUID.randomUUID().toString() },
            moduleUuid = importedPack.moduleUuid.ifEmpty { UUID.randomUUID().toString() }
        )
        val packId = dao.insertPack(pack)
        for (skinData in importedPack.skins) {
            addSkinToPack(
                packId = packId,
                name = skinData.name,
                isSlim = skinData.isSlim,
                pixels = skinData.pixels
            )
        }
        return packId
    }

    suspend fun updatePack(pack: SkinPackEntity) {
        dao.updatePack(pack)
    }

    suspend fun deletePack(pack: SkinPackEntity) {
        dao.deletePack(pack)
    }

    suspend fun addSkinToPack(
        packId: Long,
        name: String,
        isSlim: Boolean,
        pixels: IntArray
    ): Long {
        val base64 = SkinTextureHelper.pixelsToBase64(pixels)
        val skin = SkinEntity(
            packId = packId,
            name = name.trim().ifEmpty { "Karakter" },
            isSlim = isSlim,
            textureBase64 = base64
        )
        return dao.insertSkin(skin)
    }

    suspend fun updateSkin(skinId: Long, packId: Long, name: String, isSlim: Boolean, pixels: IntArray) {
        val base64 = SkinTextureHelper.pixelsToBase64(pixels)
        val skin = SkinEntity(
            id = skinId,
            packId = packId,
            name = name.trim().ifEmpty { "Karakter" },
            isSlim = isSlim,
            textureBase64 = base64
        )
        dao.updateSkin(skin)
    }

    suspend fun renameSkin(skinId: Long, newName: String) {
        val existing = dao.getSkinById(skinId) ?: return
        dao.updateSkin(existing.copy(name = newName.trim().ifEmpty { "Karakter" }))
    }

    suspend fun deleteSkin(skinId: Long) {
        dao.deleteSkinById(skinId)
    }

    suspend fun getSkinById(skinId: Long): SkinEntity? {
        return dao.getSkinById(skinId)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllPacks().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val packId = createPack(
                name = "Efsanevi Kahramanlar",
                description = "Steve, Alex ve Özel Savaşçılar Paketi",
                version = "1.0.0"
            )
            // Add Steve (Classic)
            addSkinToPack(
                packId = packId,
                name = "Klasik Steve",
                isSlim = false,
                pixels = PresetSkinLibrary.getStevePixels()
            )
            // Add Alex (Slim)
            addSkinToPack(
                packId = packId,
                name = "Slim Alex",
                isSlim = true,
                pixels = PresetSkinLibrary.getAlexPixels()
            )
            // Add Ender Knight
            addSkinToPack(
                packId = packId,
                name = "Ender Savaşçısı",
                isSlim = false,
                pixels = PresetSkinLibrary.getEnderWarriorPixels()
            )
        }
    }
}
