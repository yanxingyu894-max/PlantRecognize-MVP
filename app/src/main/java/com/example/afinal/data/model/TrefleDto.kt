package com.example.afinal.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Trefle Plant Library List API Response
 */
data class TrefleResponse(
    val data: List<TreflePlantData>,
    val meta: TrefleMeta
)

/**
 * Trefle Plant Detail API Response
 */
data class TrefleDetailResponse(
    val data: TreflePlantData
)

/**
 * Trefle Single Plant Basic Data
 */
data class TreflePlantData(
    val id: Int,
    val slug: String?,
    @SerializedName("common_name") val commonName: String?,
    @SerializedName("scientific_name") val scientificName: String,
    @SerializedName("family_common_name") val familyCommonName: String?,
    val family: JsonElement?,
    val genus: JsonElement?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("main_species") val mainSpecies: TrefleMainSpecies?
)

/**
 * Main Species Detail Information
 */
data class TrefleMainSpecies(
    val id: Int?,
    @SerializedName("common_name") val commonName: String?,
    @SerializedName("scientific_name") val scientificName: String?,
    val observations: String?,
    val duration: List<String>?,
    val edible: Boolean?,
    @SerializedName("edible_part") val ediblePart: List<String>?,
    val specifications: TrefleSpecifications?,
    val growth: TrefleGrowth?,
    val flower: TrefleFlower?,
    val foliage: TrefleFoliage?,
    @SerializedName("fruit_or_seed") val fruitOrSeed: TrefleFruitOrSeed?,
    val distributions: TrefleDistributions?
)

data class TrefleDistributions(
    val native: List<TrefleLocation>?,
    val introduced: List<TrefleLocation>?
)

/**
 * Location Data
 */
data class TrefleLocation(
    val name: String?
)

/**
 * Plant Specification Information
 */
data class TrefleSpecifications(
    @SerializedName("average_height") val averageHeight: TrefleMeasurement?,
    @SerializedName("maximum_height") val maximumHeight: TrefleMeasurement?,
    @SerializedName("growth_habit") val growthHabit: String?,
    @SerializedName("growth_rate") val growthRate: String?,
    val toxicity: String?,
    @SerializedName("ligneous_type") val ligneousType: String?
)

/**
 * Growth Environment Parameters
 */
data class TrefleGrowth(
    val light: Int?,
    @SerializedName("ph_minimum") val phMinimum: Float?,
    @SerializedName("ph_maximum") val phMaximum: Float?,
    @SerializedName("minimum_temperature") val minTemp: TrefleTemperature?,
    @SerializedName("maximum_temperature") val maxTemp: TrefleTemperature?,
    @SerializedName("soil_humidity") val soilHumidity: JsonElement?,
    @SerializedName("soil_texture") val soilTexture: JsonElement?,
    @SerializedName("soil_nutriments") val soilNutrients: JsonElement?,
    @SerializedName("soil_salinity") val soilSalinity: JsonElement?,
    @SerializedName("growth_months") val growthMonths: List<String>?,
    @SerializedName("bloom_months") val bloomMonths: List<String>?,
    @SerializedName("fruit_months") val fruitMonths: List<String>?,
    val spread: TrefleMeasurement?
)

/**
 * Temperature Data (℃/℉)
 */
data class TrefleTemperature(
    @SerializedName("deg_c") val deg_c: Int?,
    @SerializedName("deg_f") val deg_f: Int?
)

/**
 * Height/Width Measurement Data (cm/ft)
 */
data class TrefleMeasurement(
    val cm: Float?,
    val ft: Float?
)

/**
 * Flower Information
 */
data class TrefleFlower(
    val color: List<String>?,
    val conspicuous: Boolean?
)

/**
 * Foliage Information
 */
data class TrefleFoliage(
    val color: List<String>?,
    val texture: String?,
    @SerializedName("leaf_retention") val leafRetention: Boolean?
)

/**
 * Fruit/Seed Information
 */
data class TrefleFruitOrSeed(
    val color: List<String>?,
    val shape: String?
)

/**
 * Pagination Metadata
 */
data class TrefleMeta(
    val total: Int
)
