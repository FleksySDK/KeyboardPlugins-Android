package co.thingthing.fleksyapps.giphy

import co.thingthing.fleksyapps.giphy.models.CategoriesResponse
import co.thingthing.fleksyapps.giphy.models.GifResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyService {

    @GET("/v1/gifs/trending")
    fun trending(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("rating") rating: String
    ): Single<GifResponse>

    @GET("/v1/gifs/search")
    fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("lang") language: String,
        @Query("rating") rating: String
    ): Single<GifResponse>

    @GET("/v1/gifs/categories")
    fun categories(): Single<CategoriesResponse>

}
