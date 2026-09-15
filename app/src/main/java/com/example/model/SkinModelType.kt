package com.example.model

enum class SkinModelType(
    val title: String,
    val description: String,
    val armWidth: Int,
    val geometryKey: String
) {
    CLASSIC(
        title = "Klasik (Steve)",
        description = "4 Piksel Geniş Kollu Model",
        armWidth = 4,
        geometryKey = "geometry.humanoid.custom"
    ),
    SLIM(
        title = "İnce / Slim (Alex)",
        description = "3 Piksel Zarif Kollu Model",
        armWidth = 3,
        geometryKey = "geometry.humanoid.customSlim"
    );

    companion object {
        fun fromIsSlim(isSlim: Boolean): SkinModelType = if (isSlim) SLIM else CLASSIC
    }
}

enum class CharacterPart(val displayName: String) {
    HEAD("Kafa"),
    TORSO("Gövde"),
    RIGHT_ARM("Sağ Kol"),
    LEFT_ARM("Sol Kol"),
    RIGHT_LEG("Sağ Bacak"),
    LEFT_LEG("Sol Bacak")
}

enum class PartFace(val displayName: String) {
    FRONT("Ön"),
    BACK("Arka"),
    LEFT("Sol"),
    RIGHT("Sağ"),
    TOP("Üst"),
    BOTTOM("Alt")
}

enum class LayerType(val displayName: String) {
    BASE("Ana Katman"),
    OVERLAY("Dış Katman (Şapka/Ceket)")
}

data class PartRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
