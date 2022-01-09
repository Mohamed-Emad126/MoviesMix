package com.memad.moviesmix.ui.main.popular

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.MoviePopularItemBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.DoubleClickListener
import javax.inject.Inject

class PopularAdapter @Inject constructor() :
    RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {
    lateinit var popularMovieClickListener: OnMoviesClickListener
    var popularMoviesList: MutableList<MovieEntity> = mutableListOf()
        set(value) {
            if (field.isNullOrEmpty()) {
                field = value
            } else {
                val lastFinish = field.size
                field = value
                notifyItemRangeInserted(lastFinish, 20)
            }
        }


    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        return PopularViewHolder(
            MoviePopularItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.itemBinding.posterImage
            .load(
                Constants.POSTER_BASE_URL +
                        popularMoviesList[position].movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
            }
        holder.itemBinding.movieRate.text =
            popularMoviesList[position].movie?.vote_average.toString()
    }

    override fun getItemCount(): Int {
        return popularMoviesList.size
    }

    override fun getItemId(position: Int): Long {
        return popularMoviesList[position].movieId?.toLong()!!
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class PopularViewHolder(val itemBinding: MoviePopularItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.movieCard.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick() {
                    popularMovieClickListener.onMovieDoubleClicked(
                        bindingAdapterPosition,
                        itemBinding.posterImage
                    )
                }

                override fun onSingleClick() {
                    popularMovieClickListener.onMovieClicked(
                        bindingAdapterPosition,
                        itemBinding.posterImage
                    )
                }
            })
            itemBinding.movieCard.setOnLongClickListener {
                popularMovieClickListener.onMovieHoldDown(bindingAdapterPosition)
                true
            }
        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ImageView?)
        fun onMovieDoubleClicked(position: Int, imageView: ImageView?)
        fun onMovieHoldDown(position: Int)
    }
}