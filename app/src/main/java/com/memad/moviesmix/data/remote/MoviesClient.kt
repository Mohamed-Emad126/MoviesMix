package com.memad.moviesmix.data.remote

import com.memad.moviesmix.models.CastResponse
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.models.MovieTrailer
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.models.RateResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.*

interface MoviesClient {
    @FormUrlEncoded
    @POST("/3/movie/{movieId}/rating")
    suspend fun rateMovie(
        @Path("movieId") movieId: String?,
        @Field("value") value: String?,
        @Query("api_key") apiKey: String?,
        @Query("guest_session_id") sessionId: String?
    ): ApiResponse<RateResponse>

    @FormUrlEncoded
    @GET("/3/movie/{movie_id}/videos")
    suspend fun getMovieTrailer(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?
    ): ApiResponse<MovieTrailer>

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String?,
        @Query("api_key") apiKey: String?
    ): ApiResponse<Movie>

    @GET("/3/trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String?,
        @Query("page") page: Int
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String?,
        @Query("page") page: Int
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String?,
        @Query("page") page: Int
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/{movie_id}/recommendations")
    suspend fun getRecommendationsMovies(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?
    ): ApiResponse<MoviesResponse>

    @GET("/3/search/movie")
    suspend fun searchInMovies(
        @Query("api_key") apiKey: String?,
        @Query("query") query: String?,
        @Query("page") page: Int
    ): ApiResponse<MoviesResponse>


    @GET("/3/movie/{movieId}/similar?page=1")
    suspend fun getSimilarMovies(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/{movieId}/credits?")
    suspend fun getCastOfMovie(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?
    ): ApiResponse<CastResponse>


}