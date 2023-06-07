package com.memad.moviesmix.ui.main.upcoming

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.flaviofaria.kenburnsview.KenBurnsView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.databinding.UpcomingItemBinding
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.DoubleClickListener
import javax.inject.Inject

class UpcomingAdapter @Inject constructor() :
    ListAdapter<MovieEntity, UpcomingAdapter.UpcomingViewHolder>(UpcomingDiffCallBack) {

    lateinit var upcomingMovieClickListener: OnMoviesClickListener

    var favouriteList: MutableList<Boolean> = mutableListOf()
    fun submitFavouritesList(it: MutableList<Boolean>) {
        favouriteList = it
    }

    override fun submitList(list: MutableList<MovieEntity>?) {
        list?.remove(MovieEntity(null, Constants.TRENDING, -122, null))
        val oldSize = currentList.size
        list?.add(MovieEntity(null, Constants.TRENDING, -122, null))
        super.submitList(list)
        notifyItemChanged(oldSize - 1)
        notifyItemRangeInserted(oldSize - 1, list?.size!!)
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
        if (currentList[position].moviePage == -122) {
            holder.itemBinding.upcomingLoadingVeilLayout.veil()
            return
        } else {
            holder.itemBinding.upcomingLoadingVeilLayout.unVeil()
        }
        ViewCompat.setTransitionName(holder.itemBinding.movieImage, position.toString() + "poster")
        holder.itemBinding.movieImage
            .load(
                Constants.POSTER_BASE_URL +
                        currentList[position].movie?.poster_path
            ) {
                crossfade(true)
                placeholder(R.drawable.start_img_min_blur)
                error(R.drawable.start_img_min_broken)
                allowHardware(false)
            }

        holder.itemBinding.movieTitle.text = currentList[position].movie?.title
        holder.itemBinding.ratingBar.rating = currentList[position].movie?.vote_average?.toFloat()!!
        holder.itemBinding.movieDescription.text = currentList[position].movie?.overview
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
    inner class UpcomingViewHolder(val itemBinding: UpcomingItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.upcomingCardView.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick() {
                    upcomingMovieClickListener.onMovieDoubleClicked(absoluteAdapterPosition)
                }

                override fun onSingleClick() {
                    upcomingMovieClickListener.onMovieClicked(
                        absoluteAdapterPosition,
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
        fun onMovieClicked(position: Int, imageView: KenBurnsView)

        fun onMovieDoubleClicked(position: Int)
    }
}

object UpcomingDiffCallBack : DiffUtil.ItemCallback<MovieEntity>() {
    override fun areItemsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return newItem.movieId == oldItem.movieId
    }
}