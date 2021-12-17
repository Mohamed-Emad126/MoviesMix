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
    companion object {
        private const val ERROR_STATUS_ID = -2
        private const val LOADING_STATUS_ID = -1
    }

    lateinit var trendingMovieClickListener: OnMoviesClickListener
    var trendingMoviesList: MutableList<MovieEntity> = mutableListOf()
        set(value) {
            field.removeLastOrNull()
            notifyItemRemoved(field.size - 1)
            if (field.isNullOrEmpty()) {
                field = value
            } else {
                val lastFinish = field.size
                field = value
                notifyItemRangeInserted(lastFinish, 20)
            }
            field.add(field.size,
                MovieEntity(LOADING_STATUS_ID, 0, null)
            )
            notifyItemInserted(field.size)
        }

    fun addErrorItem() {
        trendingMoviesList.add(
            MovieEntity(ERROR_STATUS_ID, 0, null)
        )
        notifyItemChanged(trendingMoviesList.size-1)
    }

    fun addLoadingItem(position: Int) {
        trendingMoviesList.add(position,
            MovieEntity(LOADING_STATUS_ID, 0, null)
        )
        notifyItemInserted(trendingMoviesList.size)
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
        when (trendingMoviesList[position].movieType) {
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
        }
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
                if (trendingMoviesList[adapterPosition].movieId == ERROR_STATUS_ID) {
                    trendingMovieClickListener.onMovieErrorStateClicked(adapterPosition)
                } else {
                    trendingMovieClickListener.onMovieClicked(
                        adapterPosition,
                        itemBinding.posterImage
                    )
                }
            }


        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ImageView?)
        fun onMovieErrorStateClicked(position: Int)
    }
}