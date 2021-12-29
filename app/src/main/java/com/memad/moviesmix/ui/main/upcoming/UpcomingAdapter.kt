package com.memad.moviesmix.ui.main.upcoming

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.MoviePopularItemBinding
import com.memad.moviesmix.databinding.UpcomingItemBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.DoubleClickListener
import javax.inject.Inject

class UpcomingAdapter @Inject constructor() :
    RecyclerView.Adapter<UpcomingAdapter.UpcomingViewHolder>() {
    lateinit var popularMovieClickListener: OnMoviesClickListener
    var upcomingMoviesList: MutableList<MovieEntity> = mutableListOf()
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingViewHolder {
        return UpcomingViewHolder(
            UpcomingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: UpcomingViewHolder, position: Int) {
        holder.itemBinding.upcomingLoadingVeilLayout.unVeil()
        holder.itemBinding.movieImage
            .load(
                Constants.POSTER_BASE_URL +
                        upcomingMoviesList[position].movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
            }
        holder.itemBinding.ratingBar.rating =
            upcomingMoviesList[position].movie?.vote_average?.toFloat()!!
        holder.itemBinding.movieTitle.text = upcomingMoviesList[position].movie?.title
        holder.itemBinding.movieDescription.text = upcomingMoviesList[position].movie?.overview
    }

    override fun getItemCount(): Int {
        return upcomingMoviesList.size
    }

    override fun getItemId(position: Int): Long {
        return upcomingMoviesList[position].movieId?.toLong()!!
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class UpcomingViewHolder(val itemBinding: UpcomingItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.movieImage.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick() {
                    popularMovieClickListener.onMovieDoubleClicked(
                        bindingAdapterPosition,
                        itemBinding.movieImage
                    )
                }

                override fun onSingleClick() {
                    popularMovieClickListener.onMovieClicked(
                        bindingAdapterPosition,
                        itemBinding.movieImage
                    )
                }
            })


        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ImageView?)
        fun onMovieDoubleClicked(position: Int, imageView: ImageView?)
    }
}