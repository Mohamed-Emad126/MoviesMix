package com.memad.moviesmix.ui.main.trending

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.MovieTrendItemBinding
import com.memad.moviesmix.utils.Constants
import javax.inject.Inject

class TrendingAdapter @Inject constructor() :
    RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder>() {

    lateinit var trendingMovieClickListener: OnMoviesClickListener
    var trendingMoviesList: MutableList<MovieEntity> = mutableListOf()
        set(value) {
            if (field.isNullOrEmpty()) {
                field = value
                notifyDataSetChanged()
            } else {
                val lastFinish = field.size
                field = value
                notifyItemRangeInserted(lastFinish, 20)
            }
        }


    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        return TrendingViewHolder(
            MovieTrendItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        holder.itemBinding.itemVeilLayout.unVeil()
        holder.itemBinding.posterImage
            .load(
                Constants.POSTER_BASE_URL +
                        trendingMoviesList[position].movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
                allowHardware(false)
            }
        /*when (trendingMoviesList[position].movieType) {
            LOADING_STATUS_ID -> {
                holder.itemBinding.itemVeilLayout.veil()
            }
            ERROR_STATUS_ID -> {
                holder.itemBinding.itemVeilLayout.unVeil()
                holder.itemBinding.posterImage.load(R.drawable.ic_robot_broken) {
                    crossfade(true)
                }
            }
            else -> {
                holder.itemBinding.itemVeilLayout.unVeil()
                holder.itemBinding.posterImage
                    .load(
                        Constants.POSTER_BASE_URL +
                                trendingMoviesList[position].movie?.poster_path
                    ) {
                        crossfade(true)
                        placeholder(R.drawable.start_img_min_blur)
                        error(R.drawable.start_img_min_broken)
                    }
            }
        }*/
    }

    override fun getItemCount(): Int {
        return trendingMoviesList.size
    }

    override fun getItemId(position: Int): Long {
        return trendingMoviesList[position].movieId?.toLong()!!
    }

    /////////////////////////////////////////////////////////////////
/////////////////////////ViewHolder//////////////////////////////
/////////////////////////////////////////////////////////////////
    inner class TrendingViewHolder(val itemBinding: MovieTrendItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.materialCardView.setOnClickListener {
                trendingMovieClickListener.onMovieClicked(
                    bindingAdapterPosition,
                    itemBinding.posterImage
                )
            }


        }

    }

    /////////////////////////////////////////////////////////////////
///////////////////ClickListenerInterface////////////////////////
/////////////////////////////////////////////////////////////////
    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ImageView?)
    }
}