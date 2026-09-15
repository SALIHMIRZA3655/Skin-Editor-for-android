package com.example.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import java.io.ByteArrayOutputStream

object SkinTextureHelper {
    const val SKIN_WIDTH = 64
    const val SKIN_HEIGHT = 64
    const val TOTAL_PIXELS = SKIN_WIDTH * SKIN_HEIGHT

    fun getPartRect(
        part: CharacterPart,
        face: PartFace,
        layer: LayerType,
        isSlim: Boolean
    ): PartRect {
        val isOverlay = layer == LayerType.OVERLAY
        return when (part) {
            CharacterPart.HEAD -> {
                val baseX = if (isOverlay) 32 else 0
                val baseY = 0
                when (face) {
                    PartFace.TOP -> PartRect(baseX + 8, baseY + 0, 8, 8)
                    PartFace.BOTTOM -> PartRect(baseX + 16, baseY + 0, 8, 8)
                    PartFace.RIGHT -> PartRect(baseX + 0, baseY + 8, 8, 8)
                    PartFace.FRONT -> PartRect(baseX + 8, baseY + 8, 8, 8)
                    PartFace.LEFT -> PartRect(baseX + 16, baseY + 8, 8, 8)
                    PartFace.BACK -> PartRect(baseX + 24, baseY + 8, 8, 8)
                }
            }
            CharacterPart.TORSO -> {
                val yOffset = if (isOverlay) 16 else 0
                when (face) {
                    PartFace.TOP -> PartRect(20, 16 + yOffset, 8, 4)
                    PartFace.BOTTOM -> PartRect(28, 16 + yOffset, 8, 4)
                    PartFace.RIGHT -> PartRect(16, 20 + yOffset, 4, 12)
                    PartFace.FRONT -> PartRect(20, 20 + yOffset, 8, 12)
                    PartFace.LEFT -> PartRect(28, 20 + yOffset, 4, 12)
                    PartFace.BACK -> PartRect(32, 20 + yOffset, 8, 12)
                }
            }
            CharacterPart.RIGHT_ARM -> {
                val armW = if (isSlim) 3 else 4
                val yOffset = if (isOverlay) 16 else 0
                when (face) {
                    PartFace.TOP -> PartRect(44, 16 + yOffset, armW, 4)
                    PartFace.BOTTOM -> PartRect(44 + armW, 16 + yOffset, armW, 4)
                    PartFace.RIGHT -> PartRect(40, 20 + yOffset, 4, 12)
                    PartFace.FRONT -> PartRect(44, 20 + yOffset, armW, 12)
                    PartFace.LEFT -> PartRect(44 + armW, 20 + yOffset, 4, 12)
                    PartFace.BACK -> PartRect(44 + armW + 4, 20 + yOffset, armW, 12)
                }
            }
            CharacterPart.LEFT_ARM -> {
                val armW = if (isSlim) 3 else 4
                if (!isOverlay) {
                    when (face) {
                        PartFace.TOP -> PartRect(36, 48, armW, 4)
                        PartFace.BOTTOM -> PartRect(36 + armW, 48, armW, 4)
                        PartFace.RIGHT -> PartRect(32, 52, 4, 12)
                        PartFace.FRONT -> PartRect(36, 52, armW, 12)
                        PartFace.LEFT -> PartRect(36 + armW, 52, 4, 12)
                        PartFace.BACK -> PartRect(36 + armW + 4, 52, armW, 12)
                    }
                } else {
                    when (face) {
                        PartFace.TOP -> PartRect(52, 48, armW, 4)
                        PartFace.BOTTOM -> PartRect(52 + armW, 48, armW, 4)
                        PartFace.RIGHT -> PartRect(48, 52, 4, 12)
                        PartFace.FRONT -> PartRect(52, 52, armW, 12)
                        PartFace.LEFT -> PartRect(52 + armW, 52, 4, 12)
                        PartFace.BACK -> PartRect(52 + armW + 4, 52, armW, 12)
                    }
                }
            }
            CharacterPart.RIGHT_LEG -> {
                val yOffset = if (isOverlay) 16 else 0
                when (face) {
                    PartFace.TOP -> PartRect(4, 16 + yOffset, 4, 4)
                    PartFace.BOTTOM -> PartRect(8, 16 + yOffset, 4, 4)
                    PartFace.RIGHT -> PartRect(0, 20 + yOffset, 4, 12)
                    PartFace.FRONT -> PartRect(4, 20 + yOffset, 4, 12)
                    PartFace.LEFT -> PartRect(8, 20 + yOffset, 4, 12)
                    PartFace.BACK -> PartRect(12, 20 + yOffset, 4, 12)
                }
            }
            CharacterPart.LEFT_LEG -> {
                if (!isOverlay) {
                    when (face) {
                        PartFace.TOP -> PartRect(20, 48, 4, 4)
                        PartFace.BOTTOM -> PartRect(24, 48, 4, 4)
                        PartFace.RIGHT -> PartRect(16, 52, 4, 12)
                        PartFace.FRONT -> PartRect(20, 52, 4, 12)
                        PartFace.LEFT -> PartRect(24, 52, 4, 12)
                        PartFace.BACK -> PartRect(28, 52, 4, 12)
                    }
                } else {
                    when (face) {
                        PartFace.TOP -> PartRect(4, 48, 4, 4)
                        PartFace.BOTTOM -> PartRect(8, 48, 4, 4)
                        PartFace.RIGHT -> PartRect(0, 52, 4, 12)
                        PartFace.FRONT -> PartRect(4, 52, 4, 12)
                        PartFace.LEFT -> PartRect(8, 52, 4, 12)
                        PartFace.BACK -> PartRect(12, 52, 4, 12)
                    }
                }
            }
        }
    }

    fun createEmptyPixels(): IntArray {
        return IntArray(TOTAL_PIXELS) { Color.TRANSPARENT }
    }

    fun pixelsToBitmap(pixels: IntArray): Bitmap {
        val bitmap = Bitmap.createBitmap(SKIN_WIDTH, SKIN_HEIGHT, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, SKIN_WIDTH, 0, 0, SKIN_WIDTH, SKIN_HEIGHT)
        return bitmap
    }

    fun bitmapToPixels(bitmap: Bitmap): IntArray {
        val scaled = if (bitmap.width == SKIN_WIDTH && bitmap.height == SKIN_HEIGHT) {
            bitmap
        } else if (bitmap.width == 64 && bitmap.height == 32) {
            // Convert legacy 64x32 to 64x64 by copying right arm/leg to left arm/leg
            convertLegacy64x32To64x64(bitmap)
        } else {
            Bitmap.createScaledBitmap(bitmap, SKIN_WIDTH, SKIN_HEIGHT, false)
        }
        val pixels = IntArray(TOTAL_PIXELS)
        scaled.getPixels(pixels, 0, SKIN_WIDTH, 0, 0, SKIN_WIDTH, SKIN_HEIGHT)
        return pixels
    }

    private fun convertLegacy64x32To64x64(legacy: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(SKIN_WIDTH, SKIN_HEIGHT, Bitmap.Config.ARGB_8888)
        val legPixels = IntArray(64 * 32)
        legacy.getPixels(legPixels, 0, 64, 0, 0, 64, 32)
        result.setPixels(legPixels, 0, 64, 0, 0, 64, 32)

        // Copy right leg (0,16, 16,16) to left leg (16,48)
        val legPart = IntArray(16 * 16)
        legacy.getPixels(legPart, 0, 16, 0, 16, 16, 16)
        result.setPixels(legPart, 0, 16, 16, 48, 16, 16)

        // Copy right arm (40,16, 16,16) to left arm (32,48)
        val armPart = IntArray(16 * 16)
        legacy.getPixels(armPart, 0, 16, 40, 16, 16, 16)
        result.setPixels(armPart, 0, 16, 32, 48, 16, 16)

        return result
    }

    fun pixelsToBase64(pixels: IntArray): String {
        val bitmap = pixelsToBitmap(pixels)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun base64ToPixels(base64: String): IntArray {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                bitmapToPixels(bitmap)
            } else {
                createEmptyPixels()
            }
        } catch (e: Exception) {
            createEmptyPixels()
        }
    }

    fun base64ToBitmap(base64: String): Bitmap {
        val pixels = base64ToPixels(base64)
        return pixelsToBitmap(pixels)
    }

    fun createCompositePreview(
        pixels: IntArray,
        isSlim: Boolean,
        isBackView: Boolean = false,
        scale: Int = 4
    ): Bitmap {
        // Character composite dimensions in pixels:
        // Width: arm(3 or 4) + torso(8) + arm(3 or 4) = 14 or 16 pixels
        // Height: head(8) + torso(12) + legs(12) = 32 pixels
        val armWidth = if (isSlim) 3 else 4
        val charWidth = armWidth + 8 + armWidth
        val charHeight = 32

        val canvasBitmap = Bitmap.createBitmap(charWidth, charHeight, Bitmap.Config.ARGB_8888)

        fun drawFace(rect: PartRect, destX: Int, destY: Int, destW: Int, destH: Int) {
            for (dy in 0 until destH) {
                for (dx in 0 until destW) {
                    val px = rect.x + dx
                    val py = rect.y + dy
                    if (px in 0 until SKIN_WIDTH && py in 0 until SKIN_HEIGHT) {
                        val color = pixels[py * SKIN_WIDTH + px]
                        if (Color.alpha(color) > 10) {
                            canvasBitmap.setPixel(destX + dx, destY + dy, color)
                        }
                    }
                }
            }
        }

        val targetFace = if (isBackView) PartFace.BACK else PartFace.FRONT

        // 1. Head (Base + Overlay) - centered at (charWidth - 8)/2
        val headX = (charWidth - 8) / 2
        val headBase = getPartRect(CharacterPart.HEAD, targetFace, LayerType.BASE, isSlim)
        val headOverlay = getPartRect(CharacterPart.HEAD, targetFace, LayerType.OVERLAY, isSlim)
        drawFace(headBase, headX, 0, 8, 8)
        drawFace(headOverlay, headX, 0, 8, 8)

        // 2. Torso (Base + Overlay) - centered at headX, Y = 8
        val torsoBase = getPartRect(CharacterPart.TORSO, targetFace, LayerType.BASE, isSlim)
        val torsoOverlay = getPartRect(CharacterPart.TORSO, targetFace, LayerType.OVERLAY, isSlim)
        drawFace(torsoBase, headX, 8, 8, 12)
        drawFace(torsoOverlay, headX, 8, 8, 12)

        // 3. Arms
        val leftArmPart = if (isBackView) CharacterPart.RIGHT_ARM else CharacterPart.LEFT_ARM
        val rightArmPart = if (isBackView) CharacterPart.LEFT_ARM else CharacterPart.RIGHT_ARM

        // Left arm position (screen left)
        val arm1Base = getPartRect(if (isBackView) CharacterPart.LEFT_ARM else CharacterPart.RIGHT_ARM, targetFace, LayerType.BASE, isSlim)
        val arm1Overlay = getPartRect(if (isBackView) CharacterPart.LEFT_ARM else CharacterPart.RIGHT_ARM, targetFace, LayerType.OVERLAY, isSlim)
        drawFace(arm1Base, 0, 8, armWidth, 12)
        drawFace(arm1Overlay, 0, 8, armWidth, 12)

        // Right arm position (screen right)
        val arm2Base = getPartRect(if (isBackView) CharacterPart.RIGHT_ARM else CharacterPart.LEFT_ARM, targetFace, LayerType.BASE, isSlim)
        val arm2Overlay = getPartRect(if (isBackView) CharacterPart.RIGHT_ARM else CharacterPart.LEFT_ARM, targetFace, LayerType.OVERLAY, isSlim)
        drawFace(arm2Base, headX + 8, 8, armWidth, 12)
        drawFace(arm2Overlay, headX + 8, 8, armWidth, 12)

        // 4. Legs (Base + Overlay) - Y = 20
        val rLegBase = getPartRect(if (isBackView) CharacterPart.LEFT_LEG else CharacterPart.RIGHT_LEG, targetFace, LayerType.BASE, isSlim)
        val rLegOverlay = getPartRect(if (isBackView) CharacterPart.LEFT_LEG else CharacterPart.RIGHT_LEG, targetFace, LayerType.OVERLAY, isSlim)
        drawFace(rLegBase, headX, 20, 4, 12)
        drawFace(rLegOverlay, headX, 20, 4, 12)

        val lLegBase = getPartRect(if (isBackView) CharacterPart.RIGHT_LEG else CharacterPart.LEFT_LEG, targetFace, LayerType.BASE, isSlim)
        val lLegOverlay = getPartRect(if (isBackView) CharacterPart.RIGHT_LEG else CharacterPart.LEFT_LEG, targetFace, LayerType.OVERLAY, isSlim)
        drawFace(lLegBase, headX + 4, 20, 4, 12)
        drawFace(lLegOverlay, headX + 4, 20, 4, 12)

        // Scale cleanly with nearest neighbor
        return Bitmap.createScaledBitmap(canvasBitmap, charWidth * scale, charHeight * scale, false)
    }
}
