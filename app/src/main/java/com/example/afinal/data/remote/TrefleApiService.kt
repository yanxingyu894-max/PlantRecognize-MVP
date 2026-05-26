package com.example.afinal.data.remote

import com.example.afinal.data.model.TrefleDetailResponse
import com.example.afinal.data.model.TrefleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Trefle Public Plant Database API Interface.
 * Connects to the Trefle open platform to retrieve authoritative plant data globally.
 * Supports paginated lists, keyword search, and detailed queries.
 * All requests must include a token for authorization.
 */
interface TrefleApiService {

    /**
     * GET Request: Fetch paginated plant list.
     * Endpoint: api/v1/plants
     * Supports pagination, defaults to page 1 with 20 items.
     * @param token Trefle authorization token.
     * @param page Page number, starting from 1.
     * @param pageSize Number of items per page.
     * @return TrefleResponse containing plant list and metadata.
     */
    @GET("api/v1/plants")
    suspend fun getPlants(
        @Query("token") token: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): TrefleResponse

    /**
     * GET Request: Search plants by keywords.
     * Endpoint: api/v1/plants/search
     * Supports fuzzy matching for common names and scientific names.
     * @param token Authorization token.
     * @param query Search query keyword.
     * @return TrefleResponse with matching plants.
     */
    @GET("api/v1/plants/search")
    suspend fun searchPlants(
        @Query("token") token: String,
        @Query("q") query: String
    ): TrefleResponse


    /**
     * Fetch complete plant details by Slug.
     * @param slug User-friendly URL identifier (e.g., "rosa-chinensis").
     * @param token Trefle authorization token.
     */
    @GET("api/v1/plants/{slug}")
    suspend fun getPlantBySlug(
        @Path("slug") slug: String,
        @Query("token") token: String
    ): TrefleDetailResponse
}
