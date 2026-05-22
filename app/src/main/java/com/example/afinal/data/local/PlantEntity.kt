package com.example.afinal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity mapping to the "plants" table.
 * Expanded to cover all requested Trefle API properties for precise UI display.
 */
@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey
    val id: String,

    val slug: String = "",
    val commonName: String = "",
    val scientificName: String = "",
    val family: String = "",
    val genus: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val desc: String = "",
    val isDetailLoaded: Boolean = false,

    // --- Morphology ---
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
    val spread: Float? = null,

    val averageHeight: Float? = null,
    val maximumHeight: Float? = null,

    val toxicity: String = "",
    val edible: Boolean = false,
    val ediblePart: String = "",

    val nativeDistribution: String = "",
    val introducedDistribution: String = "",

    // --- Environment / Growth parameters ---
    val light: Int? = null,
    val phMinimum: Float? = null,
    val phMaximum: Float? = null,
    val minimumTemperature: Int? = null,
    val maximumTemperature: Int? = null,
    val soilHumidity: Int? = null,
    val soilTexture: Int? = null,
    val soilNutrients: Int? = null,
    val soilSalinity: Int? = null,

    val growthMonths: String = "",
    val bloomMonths: String = "",
    val fruitMonths: String = "",

    // --- Local Extended Fields ---
    val season: String = "",
    val name: String = "",
    val alias: String = "",
    val feature: String = "",
    val habit: String = "",
    val care: String = "",

    val lastUpdate: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)