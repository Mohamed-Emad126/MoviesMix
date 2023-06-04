package com.memad.moviesmix.ui.main.trending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.MovieTrendItemBinding
import com.memad.moviesmix.utils.Constants
import javax.inject.Inject

class TrendingAdapter @Inject constructor() :
    ListAdapter<MovieEntity, TrendingAdapter.TrendingViewHolder>(TrendingDiffCallBack) {

    lateinit var trendingMovieClickListener: OnMoviesClickListener

    var favouriteList: MutableList<Boolean> = mutableListOf()
    fun submitFavouritesList(it: MutableList<Boolean>) {
        favouriteList = it
    }

    override fun submitList(list: MutableList<MovieEntity>?) {
        list?.remove(MovieEntity(null, Constants.TRENDING, -122, null))
        val oldSize = currentList.size
        list?.add(MovieEntity(null, Constants.TRENDING, -122, null))
        super.submitList(list)
        notifyItemRemoved(oldSize)
        notifyItemRangeInserted(oldSize, list?.size!!)
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
        if (currentList[position].moviePage == -122) {
            holder.itemBinding.itemVeilLayout.veil()
            return
        } else {
            holder.itemBinding.itemVeilLayout.unVeil()
        }
        holder.itemBinding.posterImage
            .load(
                Constants.POSTER_BASE_URL +
                        currentList[position].movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
                allowHardware(false)
            }
        ViewCompat.setTransitionName(holder.itemBinding.posterImage, position.toString())
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].movieId?.toLong()!!
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
        fun onMovieClicked(position: Int, imageView: ShapeableImageView)
    }
}

object TrendingDiffCallBack : DiffUtil.ItemCallback<MovieEntity>() {
    override fun areItemsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem.movieId == oldItem.movieId
    }
}