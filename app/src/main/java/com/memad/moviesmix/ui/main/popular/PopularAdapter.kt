package com.memad.moviesmix.ui.main.popular

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.MoviePopularItemBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.DoubleClickListener
import com.varunest.sparkbutton.SparkButton
import javax.inject.Inject

class PopularAdapter @Inject constructor() :
    ListAdapter<MovieEntity, PopularAdapter.PopularViewHolder>(MoviesDiffCallBack) {

    var favouritesList: MutableList<Boolean> = mutableListOf()
    lateinit var popularMovieClickListener: OnMoviesClickListener

    override fun submitList(list: MutableList<MovieEntity>?) {
        val oldSize = list?.size ?: 0
        super.submitList(list)
        notifyItemRangeInserted(oldSize, 20)
    }

    fun submitFavouritesList(it: MutableList<Boolean>) {
        favouritesList = it
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        return PopularViewHolder(
            MoviePopularItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.itemBinding.posterImage.transitionName = position.toString() + "poster"

        holder.itemBinding.posterImage.load(
                Constants.POSTER_BASE_URL + getItem(position).movie?.poster_path!!
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
                allowHardware(false)
            }
        holder.itemBinding.movieRate.text = getItem(position).movie?.vote_average.toString()
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).movieId?.toLong()!!
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
                        absoluteAdapterPosition, itemBinding.buttonFavoritePopular
                    )
                }

                override fun onSingleClick() {
                    popularMovieClickListener.onMovieClicked(
                        absoluteAdapterPosition, itemBinding.posterImage
                    )
                }
            })
            itemBinding.movieCard.setOnLongClickListener {
                popularMovieClickListener.onMovieHoldDown(absoluteAdapterPosition)
                true
            }
        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ShapeableImageView)
        fun onMovieDoubleClicked(position: Int, favouriteButton: SparkButton)
        fun onMovieHoldDown(position: Int)
    }
}

object MoviesDiffCallBack : DiffUtil.ItemCallback<MovieEntity>() {
    override fun areItemsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem.movie == oldItem.movie
    }

    override fun areContentsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem.movieId == oldItem.movieId
    }
}