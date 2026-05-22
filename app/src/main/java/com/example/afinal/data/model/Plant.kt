package com.example.afinal.data.model

/**
 * Application Layer Data Class - Used by Business Logic and UI
 * Represents the complete plant model containing basic info, morphological features,
 * growth parameters, environmental needs, and local favorite state.
 */
data class Plant(
    val id: String,
    val name: String,
    val alias: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val genus: String = "",
    val category: String,
    val imageUrl: String,
    val desc: String,
    val feature: String,
    val habit: String,
    val care: String,
    val season: String = "",
    val isFavorite: Boolean,
    val confidence: Double? = null,

    // ==================== Morphological Attributes ====================
    val flowerColor: String = "",
    val flowerConspicuous: Boolean? = null,

    val foliageColor: String = "",
    val foliageTexture: String = "",
    val leafRetention: Boolean? = null,

    val fruitColor: String = "",
    val fruitShape: String = "",

    val duration: String = "",
    val ligneousType: String = "",
    val growthHabit: String = "",
    val growthRate: String = "",
    val spread: Float? = null, // in cm

    val averageHeight: Float? = null,
    val maximumHeight: Float? = null,

    val toxicity: String = "",
    val edible: Boolean = false,
    val ediblePart: String = "",

    val nativeDistribution: String = "",
    val introducedDistribution: String = "",

    // ==================== Environmental & Growth Parameters ====================
    val light: Int? = null,
    val phMinimum: Float? = null,
    val phMaximum: Float? = null,
    val minTemp: Int? = null,
    val maxTemp: Int? = null,
    val soilHumidity: Int? = null,
    val soilTexture: Int? = null,
    val soilNutrients: Int? = null,
    val soilSalinity: Int? = null,

    val growthMonths: String = "",
    val bloomMonths: String = "",
    val fruitMonths: String = ""
)