package com.example.afinal.data.remote

import com.example.afinal.data.model.PlantNetResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * PlantNet Plant Image Recognition API Interface
 * Purpose: Connects to the third-party PlantNet service to implement AI-based plant identification.
 * Supports uploading multiple images + organ types, returning identification results and confidence scores.
 * Provides network support for the app's core AI recognition capability.
 */
interface PlantNetApiService {

    /**
     * POST Request: Execute plant identification.
     * Uses Multipart form to upload images, suitable for file upload scenarios.
     * Endpoint: v2/identify/all
     *
     * @param images List of images, must be encapsulated in MultipartBody.Part.
     * @param organs List of plant organs (flower/leaf/fruit/bark), supports multi-value parameters.
     * @param apiKey Authorization key requested from the PlantNet platform.
     * @param includeRelatedImages Whether to return related reference images, default is false.
     * @param noReject Whether to NOT filter low-confidence results, default is true.
     * @param lang The language of the returned results, default is "en" (English).
     * @return PlantNetResponse containing species, confidence, scientific names, etc.
     */
    @Multipart
    @POST("v2/identify/all")
    suspend fun identifyPlant(
        @Part images: @JvmSuppressWildcards List<MultipartBody.Part>,

        @Part organs: @JvmSuppressWildcards List<MultipartBody.Part>,

        @Query("api-key") apiKey: String,

        @Query("include-related-images") includeRelatedImages: Boolean = false,

        @Query("no-reject") noReject: Boolean = true,

        @Query("lang") lang: String = "en"

    ): PlantNetResponse
}
