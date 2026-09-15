package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.SkinEntity
import com.example.data.SkinPackEntity
import com.example.model.SkinModelType
import com.example.model.SkinTextureHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object McPackExporter {

    data class ExportResult(
        val success: Boolean,
        val file: File? = null,
        val uri: Uri? = null,
        val errorMessage: String? = null
    )

    fun createMcPack(
        context: Context,
        pack: SkinPackEntity,
        skins: List<SkinEntity>
    ): ExportResult {
        if (skins.isEmpty()) {
            return ExportResult(
                success = false,
                errorMessage = "Pakette en az 1 karakter bulunmalıdır!"
            )
        }

        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val sanitizedPackName = pack.name
                .replace(Regex("[^a-zA-Z0-9_]"), "_")
                .lowercase()
                .ifEmpty { "custom_pack" }

            // Always generate fresh UUIDs on every export or share so Minecraft Bedrock never detects duplicate packs!
            val freshManifestUuid = UUID.randomUUID().toString()
            val freshModuleUuid = UUID.randomUUID().toString()

            val mcPackFile = File(exportDir, "${sanitizedPackName}_${System.currentTimeMillis()}.mcpack")
            if (mcPackFile.exists()) {
                mcPackFile.delete()
            }

            ZipOutputStream(FileOutputStream(mcPackFile)).use { zos ->
                // 1. manifest.json with fresh unique UUIDs
                val manifestContent = buildManifestJson(pack, freshManifestUuid, freshModuleUuid)
                addZipEntry(zos, "manifest.json", manifestContent.toByteArray(Charsets.UTF_8))

                // 2. skins.json
                val skinsJsonContent = buildSkinsJson(sanitizedPackName, skins)
                addZipEntry(zos, "skins.json", skinsJsonContent.toByteArray(Charsets.UTF_8))

                // 3. texts/en_US.lang
                val langContent = buildLangContent(sanitizedPackName, pack.name, skins)
                addZipEntry(zos, "texts/en_US.lang", langContent.toByteArray(Charsets.UTF_8))

                // 4. pack_icon.png
                val iconBytes = createPackIconBytes(skins.firstOrNull())
                addZipEntry(zos, "pack_icon.png", iconBytes)

                // 5. Skin PNG files
                skins.forEachIndexed { index, skin ->
                    val skinFilename = "skin_$index.png"
                    val skinBitmap = SkinTextureHelper.base64ToBitmap(skin.textureBase64)
                    val skinStream = ByteArrayOutputStream()
                    skinBitmap.compress(Bitmap.CompressFormat.PNG, 100, skinStream)
                    addZipEntry(zos, skinFilename, skinStream.toByteArray())
                }
            }

            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    mcPackFile
                )
            } catch (e: Exception) {
                Uri.fromFile(mcPackFile)
            }

            ExportResult(success = true, file = mcPackFile, uri = uri)
        } catch (e: Exception) {
            ExportResult(success = false, errorMessage = e.localizedMessage ?: "Dışa aktarma hatası")
        }
    }

    private fun addZipEntry(zos: ZipOutputStream, entryName: String, data: ByteArray) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(data)
        zos.closeEntry()
    }

    private fun buildManifestJson(
        pack: SkinPackEntity,
        manifestUuid: String = UUID.randomUUID().toString(),
        moduleUuid: String = UUID.randomUUID().toString()
    ): String {
        return """
{
  "format_version": 2,
  "header": {
    "name": "${escapeJson(pack.name)}",
    "description": "${escapeJson(pack.description)}",
    "uuid": "$manifestUuid",
    "version": [1, 0, 0],
    "min_engine_version": [1, 16, 0]
  },
  "modules": [
    {
      "type": "skin_pack",
      "uuid": "$moduleUuid",
      "version": [1, 0, 0]
    }
  ]
}
        """.trimIndent()
    }

    private fun buildSkinsJson(sanitizedPackName: String, skins: List<SkinEntity>): String {
        val skinsArrayEntries = skins.mapIndexed { index, skin ->
            val modelType = SkinModelType.fromIsSlim(skin.isSlim)
            val locKey = "skin_${index}"
            """
    {
      "localization_name": "$locKey",
      "geometry": "${modelType.geometryKey}",
      "texture": "skin_$index.png",
      "type": "free"
    }
            """.trimIndent()
        }.joinToString(",\n")

        return """
{
  "serialize_name": "$sanitizedPackName",
  "localization_name": "$sanitizedPackName",
  "skins": [
$skinsArrayEntries
  ]
}
        """.trimIndent()
    }

    private fun buildLangContent(
        sanitizedPackName: String,
        packDisplayName: String,
        skins: List<SkinEntity>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("skinpack.$sanitizedPackName=$packDisplayName")
        skins.forEachIndexed { index, skin ->
            sb.appendLine("skin.$sanitizedPackName.skin_$index=${skin.name}")
        }
        return sb.toString()
    }

    private fun createPackIconBytes(firstSkin: SkinEntity?): ByteArray {
        val iconBitmap = if (firstSkin != null) {
            val pixels = SkinTextureHelper.base64ToPixels(firstSkin.textureBase64)
            SkinTextureHelper.createCompositePreview(pixels, firstSkin.isSlim, isBackView = false, scale = 4)
        } else {
            Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        }
        val stream = ByteArrayOutputStream()
        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .replace("\r", "")
    }

    fun isMinecraftInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo("com.mojang.minecraftpe", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openInMinecraft(context: Context, uri: Uri): Boolean {
        // Try opening directly with Minecraft first
        val mcIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/octet-stream")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.mojang.minecraftpe")
        }

        return try {
            context.startActivity(mcIntent)
            true
        } catch (e: Exception) {
            // If direct Minecraft intent fails, try generic ACTION_VIEW chooser
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/octet-stream")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(genericIntent, "Minecraft ile Aç veya İçe Aktar").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun shareMcPack(context: Context, uri: Uri, packName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$packName - Minecraft Skin Paketi")
            putExtra(Intent.EXTRA_TEXT, "$packName Minecraft Bedrock skin paketi (.mcpack)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Skin Paketini Paylaş / Kaydet").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
