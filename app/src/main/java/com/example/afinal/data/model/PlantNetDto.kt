package com.example.afinal.data.model

import com.google.gson.annotations.SerializedName

/**
 * PlantNet Plant Recognition API Response Model
 * Purpose: Parses the results returned by the plant recognition API, 
 * including matches, confidence scores, and species information.
 */
data class PlantNetResponse(
    // List of recognition results
    val results: List<PlantNetResult>,

    // Query parameter info
    val query: PlantNetQuery,

    // Response language
    val language: String,

    // Preferred reference database
    val preferedReferential: String
)

/**
 * Single recognition match result
 */
data class PlantNetResult(
    // Recognition confidence score
    val score: Double,

    // Species detail information
    val species: PlantNetSpecies,

    // GBIF database info (nullable)
    val gbif: PlantNetGbif?
)

/**
 * Plant species information
 */
data class PlantNetSpecies(
    // Scientific name without authorship
    val scientificNameWithoutAuthor: String,

    // Scientific name authorship
    val scientificNameAuthorship: String,

    // Genus information
    val genus: PlantNetGenus,

    // Family information
    val family: PlantNetFamily,

    // List of common names
    val commonNames: List<String>,

    // Full scientific name
    val scientificName: String
)

/**
 * Plant genus information structure
 */
data class PlantNetGenus(
    val scientificNameWithoutAuthor: String,

    val scientificNameAuthorship: String,

    val scientificName: String
)

/**
 * Plant family information structure
 */
data class PlantNetFamily(
    val scientificNameWithoutAuthor: String,

    val scientificNameAuthorship: String,

    val scientificName: String
)

/**
 * GBIF database ID information
 */
data class PlantNetGbif(
    val id: String
)

/**
 * Recognition request query information
 */
data class PlantNetQuery(
    // Project identifier
    val project: String,

    // List of uploaded images
    val images: List<String>,

    // Plant organs (flower/leaf/fruit/etc)
    val organs: List<String>,

    // Whether related images are included
    val includeRelatedImages: Boolean
)
