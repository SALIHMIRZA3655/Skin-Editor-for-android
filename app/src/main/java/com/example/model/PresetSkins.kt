package com.example.model

import android.graphics.Bitmap
import android.graphics.Color

data class PresetSkin(
    val name: String,
    val isSlim: Boolean,
    val category: String,
    val description: String,
    val generator: () -> IntArray
)

object PresetSkinLibrary {

    private fun hex(hexStr: String): Int {
        return Color.parseColor(hexStr)
    }

    private fun fillRect(pixels: IntArray, x0: Int, y0: Int, w: Int, h: Int, color: Int) {
        for (y in y0 until (y0 + h)) {
            for (x in x0 until (x0 + w)) {
                if (x in 0 until 64 && y in 0 until 64) {
                    pixels[y * 64 + x] = color
                }
            }
        }
    }

    fun getStevePixels(): IntArray {
        val p = IntArray(64 * 64) { Color.TRANSPARENT }
        val skin = hex("#B88764")
        val hair = hex("#4A3324")
        val eyes = hex("#4B4A8D")
        val eyeWhite = hex("#FFFFFF")
        val mouth = hex("#633F2E")
        val shirt = hex("#009193")
        val pants = hex("#253B82")
        val shoes = hex("#565656")

        // Head Base
        fillRect(p, 8, 8, 8, 8, skin) // front
        fillRect(p, 8, 0, 8, 8, hair) // top
        fillRect(p, 0, 8, 8, 8, hair) // right
        fillRect(p, 16, 8, 8, 8, hair) // left
        fillRect(p, 24, 8, 8, 8, hair) // back
        fillRect(p, 16, 0, 8, 8, skin) // bottom
        // Steve Face details
        fillRect(p, 8, 8, 8, 3, hair) // hair fringe
        p[10 * 64 + 10] = eyeWhite; p[10 * 64 + 11] = eyes
        p[10 * 64 + 12] = eyeWhite; p[10 * 64 + 13] = eyes
        fillRect(p, 10, 12, 4, 1, mouth) // beard/mouth

        // Torso Base (Shirt)
        fillRect(p, 20, 20, 8, 12, shirt) // front
        fillRect(p, 20, 16, 8, 4, shirt) // top
        fillRect(p, 28, 16, 8, 4, shirt) // bottom
        fillRect(p, 16, 20, 4, 12, shirt) // right
        fillRect(p, 28, 20, 4, 12, shirt) // left
        fillRect(p, 32, 20, 8, 12, shirt) // back
        // Shirt neck cutout
        fillRect(p, 23, 20, 2, 2, skin)

        // Arms Base (Classic 4px)
        // Right Arm
        fillRect(p, 40, 20, 4, 12, shirt) // upper
        fillRect(p, 44, 20, 4, 4, shirt)
        fillRect(p, 44, 24, 4, 8, skin) // arm lower
        fillRect(p, 48, 20, 4, 12, shirt)
        fillRect(p, 52, 20, 4, 12, shirt)
        // Left Arm
        fillRect(p, 32, 52, 4, 12, shirt)
        fillRect(p, 36, 52, 4, 4, shirt)
        fillRect(p, 36, 56, 4, 8, skin)
        fillRect(p, 40, 52, 4, 12, shirt)
        fillRect(p, 44, 52, 4, 12, shirt)

        // Legs (Pants & Shoes)
        // Right Leg
        fillRect(p, 4, 20, 4, 10, pants)
        fillRect(p, 4, 30, 4, 2, shoes)
        fillRect(p, 0, 20, 4, 10, pants); fillRect(p, 0, 30, 4, 2, shoes)
        fillRect(p, 8, 20, 4, 10, pants); fillRect(p, 8, 30, 4, 2, shoes)
        fillRect(p, 12, 20, 4, 10, pants); fillRect(p, 12, 30, 4, 2, shoes)
        // Left Leg
        fillRect(p, 20, 52, 4, 10, pants)
        fillRect(p, 20, 62, 4, 2, shoes)
        fillRect(p, 16, 52, 4, 10, pants); fillRect(p, 16, 62, 4, 2, shoes)
        fillRect(p, 24, 52, 4, 10, pants); fillRect(p, 24, 62, 4, 2, shoes)
        fillRect(p, 28, 52, 4, 10, pants); fillRect(p, 28, 62, 4, 2, shoes)

        return p
    }

    fun getAlexPixels(): IntArray {
        val p = IntArray(64 * 64) { Color.TRANSPARENT }
        val skin = hex("#DDB28D")
        val ginger = hex("#C36224")
        val eyes = hex("#45795C")
        val eyeWhite = hex("#FFFFFF")
        val tunic = hex("#5E7643")
        val belt = hex("#3E2B1E")
        val pants = hex("#594C42")
        val boots = hex("#403024")

        // Head Base
        fillRect(p, 8, 8, 8, 8, skin)
        fillRect(p, 8, 0, 8, 8, ginger) // top
        fillRect(p, 0, 8, 8, 8, ginger)
        fillRect(p, 16, 8, 8, 8, ginger)
        fillRect(p, 24, 8, 8, 8, ginger)
        // Fringe & Eyes
        fillRect(p, 8, 8, 8, 3, ginger)
        p[10 * 64 + 10] = eyeWhite; p[10 * 64 + 11] = eyes
        p[10 * 64 + 12] = eyes; p[10 * 64 + 13] = eyeWhite

        // Torso Base (Green Tunic)
        fillRect(p, 20, 20, 8, 10, tunic)
        fillRect(p, 20, 30, 8, 2, belt)
        fillRect(p, 20, 16, 8, 4, tunic)
        fillRect(p, 16, 20, 4, 12, tunic)
        fillRect(p, 28, 20, 4, 12, tunic)
        fillRect(p, 32, 20, 8, 12, tunic)
        // V-neck
        fillRect(p, 23, 20, 2, 2, skin)

        // Arms Base (Slim 3px)
        // Right Arm (x=44, w=3)
        fillRect(p, 44, 20, 3, 4, tunic)
        fillRect(p, 44, 24, 3, 8, skin)
        fillRect(p, 40, 20, 4, 12, tunic)
        fillRect(p, 47, 20, 4, 12, tunic)
        // Left Arm (x=36, w=3)
        fillRect(p, 36, 52, 3, 4, tunic)
        fillRect(p, 36, 56, 3, 8, skin)
        fillRect(p, 32, 52, 4, 12, tunic)
        fillRect(p, 39, 52, 4, 12, tunic)

        // Legs
        fillRect(p, 4, 20, 4, 8, pants)
        fillRect(p, 4, 28, 4, 4, boots)
        fillRect(p, 20, 52, 4, 8, pants)
        fillRect(p, 20, 60, 4, 4, boots)
        fillRect(p, 0, 20, 4, 8, pants); fillRect(p, 0, 28, 4, 4, boots)
        fillRect(p, 12, 20, 4, 8, pants); fillRect(p, 12, 28, 4, 4, boots)
        fillRect(p, 16, 52, 4, 8, pants); fillRect(p, 16, 60, 4, 4, boots)
        fillRect(p, 28, 52, 4, 8, pants); fillRect(p, 28, 60, 4, 4, boots)

        return p
    }

    fun getEnderWarriorPixels(): IntArray {
        val p = IntArray(64 * 64) { Color.TRANSPARENT }
        val dark = hex("#161616")
        val purple = hex("#8E24AA")
        val glow = hex("#D500F9")
        val eyes = hex("#BA68C8")

        // Full Body Dark Base
        fillRect(p, 0, 0, 64, 64, dark)

        // Head Eyes (Glowing purple Ender eyes)
        p[10 * 64 + 10] = eyes; p[10 * 64 + 11] = glow
        p[10 * 64 + 12] = glow; p[10 * 64 + 13] = eyes

        // Torso Runes
        fillRect(p, 23, 22, 2, 8, purple)
        fillRect(p, 21, 25, 6, 2, glow)

        // Arm Highlights
        fillRect(p, 44, 28, 4, 2, purple)
        fillRect(p, 36, 60, 4, 2, purple)

        // Leg Greaves
        fillRect(p, 4, 28, 4, 3, purple)
        fillRect(p, 20, 60, 4, 3, purple)

        return p
    }

    fun getCyberNinjaPixels(): IntArray {
        val p = IntArray(64 * 64) { Color.TRANSPARENT }
        val dark = hex("#1E293B")
        val cyan = hex("#00E5FF")
        val neon = hex("#18FFFF")
        val gray = hex("#475569")

        // Cybernetic Dark Armor
        fillRect(p, 0, 0, 64, 64, dark)

        // Visor on Head
        fillRect(p, 9, 10, 6, 2, cyan)
        p[10 * 64 + 11] = neon; p[10 * 64 + 12] = neon

        // Torso Cyber Core
        fillRect(p, 22, 23, 4, 4, cyan)
        fillRect(p, 23, 24, 2, 2, neon)
        fillRect(p, 20, 28, 8, 2, gray)

        // Slim Arms (w=3)
        fillRect(p, 44, 22, 3, 2, cyan)
        fillRect(p, 36, 54, 3, 2, cyan)

        // Neon Boots
        fillRect(p, 4, 29, 4, 3, gray)
        p[31 * 64 + 5] = neon; p[31 * 64 + 6] = neon
        fillRect(p, 20, 61, 4, 3, gray)
        p[63 * 64 + 21] = neon; p[63 * 64 + 22] = neon

        return p
    }

    fun getDiamondKnightPixels(): IntArray {
        val p = IntArray(64 * 64) { Color.TRANSPARENT }
        val diamond = hex("#2ED573")
        val aqua = hex("#00D2D3")
        val deepArmor = hex("#2C3A47")
        val trim = hex("#70A1FF")
        val skin = hex("#B88764")

        fillRect(p, 0, 0, 64, 64, deepArmor)
        // Face opening
        fillRect(p, 10, 10, 4, 4, skin)
        p[11 * 64 + 11] = hex("#3742FA")
        p[11 * 64 + 12] = hex("#3742FA")

        // Diamond Chestplate
        fillRect(p, 21, 21, 6, 10, aqua)
        fillRect(p, 22, 23, 4, 4, diamond)
        fillRect(p, 20, 20, 8, 2, trim)

        // Diamond Arms
        fillRect(p, 44, 20, 4, 8, aqua)
        fillRect(p, 36, 52, 4, 8, aqua)

        // Diamond Boots
        fillRect(p, 4, 27, 4, 5, aqua)
        fillRect(p, 20, 59, 4, 5, aqua)

        return p
    }

    fun getNeonCreeperPixels(): IntArray {
        val p = IntArray(64 * 64) { Color.TRANSPARENT }
        val black = hex("#0F172A")
        val neonGreen = hex("#00FF66")
        val darkGreen = hex("#00B84D")

        fillRect(p, 0, 0, 64, 64, black)

        // Creeper Face on Chest
        fillRect(p, 22, 22, 4, 2, darkGreen) // top eye bridge
        p[23 * 64 + 21] = neonGreen; p[23 * 64 + 22] = neonGreen // eye L
        p[23 * 64 + 25] = neonGreen; p[23 * 64 + 26] = neonGreen // eye R
        fillRect(p, 23, 24, 2, 4, neonGreen) // nose
        fillRect(p, 21, 26, 6, 2, darkGreen) // mouth bottom

        // Head Mask
        fillRect(p, 9, 9, 6, 6, darkGreen)
        p[10 * 64 + 10] = black; p[10 * 64 + 13] = black
        fillRect(p, 11, 12, 2, 3, black)

        // Hood & Accent
        fillRect(p, 44, 20, 4, 2, neonGreen)
        fillRect(p, 36, 52, 4, 2, neonGreen)
        fillRect(p, 4, 30, 4, 2, neonGreen)
        fillRect(p, 20, 62, 4, 2, neonGreen)

        return p
    }

    val ALL_PRESETS = listOf(
        PresetSkin(
            name = "Steve (Orijinal)",
            isSlim = false,
            category = "Klasik",
            description = "Orijinal 4px kollu klasik Minecraft kahramanı",
            generator = { getStevePixels() }
        ),
        PresetSkin(
            name = "Alex (Orijinal)",
            isSlim = true,
            category = "Slim",
            description = "Orijinal 3px kollu zarif model Minecraft maceracısı",
            generator = { getAlexPixels() }
        ),
        PresetSkin(
            name = "Ender Savaşçısı",
            isSlim = false,
            category = "Savaşçı",
            description = "Ender dünyasının mistik mor parıltılı savaşçısı",
            generator = { getEnderWarriorPixels() }
        ),
        PresetSkin(
            name = "Siber Ajan",
            isSlim = true,
            category = "Fütüristik",
            description = "Neon mavi vizörlü yüksek teknolojili slim ninja",
            generator = { getCyberNinjaPixels() }
        ),
        PresetSkin(
            name = "Elmas Şövalye",
            isSlim = false,
            category = "Zırhlı",
            description = "Parlak elmas göğüslük ve koruyucu miğferli şövalye",
            generator = { getDiamondKnightPixels() }
        ),
        PresetSkin(
            name = "Neon Creeper",
            isSlim = false,
            category = "Kostüm",
            description = "Karanlık temalı neon yeşil creeper kapüşonlu",
            generator = { getNeonCreeperPixels() }
        )
    )
}
