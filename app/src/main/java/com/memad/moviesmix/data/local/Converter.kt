package com.memad.moviesmix.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.memad.moviesmix.models.MoviesResponse


class Converter {
    @TypeConverter
    fun fromMovieToGson(movie: MoviesResponse.Result?): String? {
        return Gson().toJson(movie!!)
    }

    @TypeConverter
    fun fromGsonToMovie(string: String?): MoviesResponse.Result? {
        return Gson().fromJson(string!!, MoviesResponse.Result::class.java)
    }
}