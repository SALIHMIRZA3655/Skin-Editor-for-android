package com.example.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.SkinTextureHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ImportedSkinData(
    val name: String,
    val isSlim: Boolean,
    val pixels: IntArray
)

data class ImportedPackData(
    val name: String,
    val description: String,
    val version: String,
    val manifestUuid: String,
    val moduleUuid: String,
    val skins: List<ImportedSkinData>
)

data class ImportResult(
    val success: Boolean,
    val pack: ImportedPackData? = null,
    val errorMessage: String? = null
)

object McPackImporter {

    fun importFromUri(context: Context, uri: Uri, fallbackName: String? = null): ImportResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult(success = false, errorMessage = "Dosya açılamadı")
            inputStream.use { stream ->
                importFromStream(stream, fallbackName)
            }
        } catch (e: Exception) {
            ImportResult(success = false, errorMessage = e.localizedMessage ?: "İçe aktarma hatası oluştu")
        }
    }

    fun importFromStream(inputStream: InputStream, fallbackName: String? = null): ImportResult {
        return try {
            val entries = readZipEntries(inputStream)
            if (entries.isEmpty()) {
                return ImportResult(success = false, errorMessage = "Paket dosyası boş veya geçersiz .mcpack arşivi.")
            }

            // 1. Locate manifest.json
            val manifestEntry = entries.entries.find { (path, _) ->
                path.equals("manifest.json", ignoreCase = true) || path.endsWith("/manifest.json", ignoreCase = true)
            }

            var packName = fallbackName ?: "İçe Aktarılan Paket"
            var packDescription = "İçe aktarılan Minecraft skin paketi"
            var packVersion = "1.0.0"
            var manifestUuid = UUID.randomUUID().toString()
            var moduleUuid = UUID.randomUUID().toString()

            if (manifestEntry != null) {
                try {
                    val manifestStr = String(manifestEntry.value, Charsets.UTF_8)
                    val json = JSONObject(manifestStr)
                    if (json.has("header")) {
                        val header = json.getJSONObject("header")
                        if (header.has("name")) {
                            packName = header.getString("name").ifEmpty { packName }
                        }
                        if (header.has("description")) {
                            packDescription = header.getString("description")
                        }
                        if (header.has("uuid")) {
                            manifestUuid = header.getString("uuid")
                        }
                        if (header.has("version")) {
                            val verArr = header.optJSONArray("version")
                            if (verArr != null && verArr.length() > 0) {
                                val verList = mutableListOf<Int>()
                                for (i in 0 until verArr.length()) {
                                    verList.add(verArr.getInt(i))
                                }
                                packVersion = verList.joinToString(".")
                            }
                        }
                    }
                    if (json.has("modules")) {
                        val modules = json.getJSONArray("modules")
                        for (i in 0 until modules.length()) {
                            val mod = modules.getJSONObject(i)
                            if (mod.optString("type") == "skin_pack" && mod.has("uuid")) {
                                moduleUuid = mod.getString("uuid")
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore manifest parse errors and fallback
                }
            }

            // 2. Parse language files (texts/*.lang)
            val langMap = mutableMapOf<String, String>()
            for ((path, bytes) in entries) {
                if (path.endsWith(".lang", ignoreCase = true)) {
                    val langText = String(bytes, Charsets.UTF_8)
                    langText.lineSequence().forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                            val parts = trimmed.split("=", limit = 2)
                            if (parts.size == 2) {
                                langMap[parts[0].trim()] = parts[1].trim()
                            }
                        }
                    }
                }
            }

            // If lang map has skinpack display name, use it
            for ((k, v) in langMap) {
                if (k.startsWith("skinpack.")) {
                    packName = v
                    break
                }
            }

            // 3. Locate skins.json
            val skinsJsonEntry = entries.entries.find { (path, _) ->
                path.equals("skins.json", ignoreCase = true) || path.endsWith("/skins.json", ignoreCase = true)
            }

            val importedSkins = mutableListOf<ImportedSkinData>()

            if (skinsJsonEntry != null) {
                try {
                    val skinsStr = String(skinsJsonEntry.value, Charsets.UTF_8)
                    val json = JSONObject(skinsStr)
                    val serializeName = json.optString("serialize_name", "")
                    val skinsArray = json.optJSONArray("skins") ?: JSONArray()

                    for (i in 0 until skinsArray.length()) {
                        val skinObj = skinsArray.getJSONObject(i)
                        val locName = skinObj.optString("localization_name", "skin_$i")
                        val geometry = skinObj.optString("geometry", "")
                        val texturePath = skinObj.optString("texture", "")

                        val isSlim = geometry.contains("slim", ignoreCase = true) ||
                                geometry.contains("alex", ignoreCase = true)

                        // Resolve skin name from langMap or locName
                        val resolvedName = resolveSkinName(langMap, serializeName, locName, texturePath, i)

                        // Find texture bytes
                        val textureBytes = findEntryBytes(entries, texturePath)
                        if (textureBytes != null) {
                            val pixels = decodeSkinPixels(textureBytes)
                            if (pixels != null) {
                                importedSkins.add(
                                    ImportedSkinData(
                                        name = resolvedName,
                                        isSlim = isSlim,
                                        pixels = pixels
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to direct PNG scan
                }
            }

            // Fallback: If no skins parsed from skins.json, scan for all PNG textures
            if (importedSkins.isEmpty()) {
                val pngEntries = entries.filter { (path, _) ->
                    path.endsWith(".png", ignoreCase = true) &&
                            !path.endsWith("pack_icon.png", ignoreCase = true)
                }

                pngEntries.entries.toList().forEachIndexed { index, entry ->
                    val path = entry.key
                    val bytes = entry.value
                    val pixels = decodeSkinPixels(bytes)
                    if (pixels != null) {
                        val fileName = path.substringAfterLast("/").substringBeforeLast(".")
                        importedSkins.add(
                            ImportedSkinData(
                                name = fileName.replace("_", " ").replaceFirstChar { it.uppercase() }.ifEmpty { "Karakter ${index + 1}" },
                                isSlim = fileName.contains("slim", ignoreCase = true) || fileName.contains("alex", ignoreCase = true),
                                pixels = pixels
                            )
                        )
                    }
                }
            }

            if (importedSkins.isEmpty()) {
                return ImportResult(
                    success = false,
                    errorMessage = "Paket içinde geçerli Minecraft karakter dokusu (.png) bulunamadı."
                )
            }

            val packData = ImportedPackData(
                name = packName,
                description = packDescription,
                version = packVersion,
                manifestUuid = manifestUuid,
                moduleUuid = moduleUuid,
                skins = importedSkins
            )

            ImportResult(success = true, pack = packData)
        } catch (e: Exception) {
            ImportResult(success = false, errorMessage = "İçe aktarma hatası: ${e.localizedMessage}")
        }
    }

    private fun readZipEntries(inputStream: InputStream): Map<String, ByteArray> {
        val map = mutableMapOf<String, ByteArray>()
        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                val normalizedName = entry.name.replace('\\', '/')
                val buffer = ByteArrayOutputStream()
                val temp = ByteArray(4096)
                var read: Int
                while (zis.read(temp).also { read = it } != -1) {
                    buffer.write(temp, 0, read)
                }
                map[normalizedName] = buffer.toByteArray()
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
        return map
    }

    private fun findEntryBytes(entries: Map<String, ByteArray>, targetPath: String): ByteArray? {
        if (targetPath.isEmpty()) return null
        val normalizedTarget = targetPath.replace('\\', '/')
        val filename = normalizedTarget.substringAfterLast("/")

        // Exact match
        entries[normalizedTarget]?.let { return it }

        // Suffix match
        for ((path, bytes) in entries) {
            if (path.equals(normalizedTarget, ignoreCase = true) ||
                path.endsWith("/$normalizedTarget", ignoreCase = true) ||
                path.substringAfterLast("/").equals(filename, ignoreCase = true)
            ) {
                return bytes
            }
        }
        return null
    }

    private fun resolveSkinName(
        langMap: Map<String, String>,
        serializeName: String,
        locName: String,
        texturePath: String,
        index: Int
    ): String {
        // Common lang keys:
        // skin.packname.skin_0
        // skin.skin_0
        // skinpack.packname.skin_0
        val candidates = listOf(
            "skin.$serializeName.$locName",
            "skin.$locName",
            "skinpack.$serializeName.$locName",
            locName
        )
        for (key in candidates) {
            val found = langMap[key]
            if (!found.isNullOrBlank()) {
                return found
            }
        }

        // Check if locName is a readable name itself
        if (locName.isNotEmpty() && !locName.startsWith("skin_")) {
            return locName.replace("_", " ").replaceFirstChar { it.uppercase() }
        }

        // Extract from texture name
        val textureName = texturePath.substringAfterLast("/").substringBeforeLast(".")
        if (textureName.isNotEmpty() && !textureName.startsWith("skin_")) {
            return textureName.replace("_", " ").replaceFirstChar { it.uppercase() }
        }

        return "Karakter ${index + 1}"
    }

    private fun decodeSkinPixels(bytes: ByteArray): IntArray? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val width = bitmap.width
            val height = bitmap.height

            // Valid skin dimensions: 64x64 or legacy 64x32
            val processedBitmap = when {
                width == 64 && height == 64 -> bitmap
                width == 64 && height == 32 -> {
                    // Convert legacy 64x32 to 64x64
                    val newBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(newBitmap)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    newBitmap
                }
                else -> {
                    // Scale to 64x64
                    Bitmap.createScaledBitmap(bitmap, 64, 64, false)
                }
            }

            val pixels = IntArray(64 * 64)
            processedBitmap.getPixels(pixels, 0, 64, 0, 0, 64, 64)
            pixels
        } catch (e: Exception) {
            null
        }
    }
}
