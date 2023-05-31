package com.memad.moviesmix.ui.main.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.SearchResultItemBinding
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.ui.main.description.MoviesSimilarDiffCallBack
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.GenresUtils
import com.memad.moviesmix.utils.addChip
import javax.inject.Inject

class SearchAdapter @Inject constructor() :
    ListAdapter<MoviesResponse.Result, SearchAdapter.SearchViewHolder>(MoviesSimilarDiffCallBack) {
    var movieClickListener: OnMoviesClickListener? = null
    private var favouriteList: MutableList<Boolean> = mutableListOf()

    override fun submitList(list: MutableList<MoviesResponse.Result>?) {
        var oldSize = currentList.size
        val newSize = list!!.size
        if (newSize < oldSize) {
            oldSize = 0
        }
        super.submitList(list)
        notifyItemRangeInserted(oldSize, newSize - oldSize)
    }


    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            SearchResultItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        ViewCompat.setTransitionName(holder.itemBinding.searchItemPoster, position.toString())
        val movie = getItem(position)!!
        holder.itemBinding.searchItemPoster.load(
            Constants.POSTER_BASE_URL +
                    movie.poster_path
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }
        holder.itemBinding.searchItemTitle.text = movie.original_title
        holder.itemBinding.searchItemRate.text = movie.vote_average.toString()
        holder.itemBinding.searchItemReleaseDate.text = movie.release_date
        movie.genre_ids.forEach {
            holder.itemBinding.searchItemGenre.addChip(
                holder.itemBinding.searchItemGenre.context,
                GenresUtils.getGenre(it)!!
            )
        }
        if (favouriteList.isNotEmpty() && favouriteList[position]) {
            holder.itemBinding.buttonFavorite.isActivated = true
            holder.itemBinding.buttonFavorite.isChecked = true
        } else {
            holder.itemBinding.buttonFavorite.isActivated = false
            holder.itemBinding.buttonFavorite.isChecked = false
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    fun submitFavouritesList(it: MutableList<Boolean>) {
        favouriteList = it
        favouriteList.forEachIndexed { index, b ->
            if (b) {
                notifyItemChanged(index)
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class SearchViewHolder(val itemBinding: SearchResultItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.cardSearchItem.setOnClickListener {
                movieClickListener?.onMovieClicked(
                    absoluteAdapterPosition,
                    itemBinding.searchItemPoster
                )
            }
            itemBinding.searchItemPoster.setOnClickListener {
                movieClickListener?.onMovieClicked(
                    absoluteAdapterPosition,
                    itemBinding.searchItemPoster
                )
            }
            itemBinding.buttonFavorite.setOnClickListener {
                itemBinding.buttonFavorite.isActivated = !itemBinding.buttonFavorite.isActivated
                movieClickListener?.onMovieFavouriteClicked(
                    absoluteAdapterPosition,
                    itemBinding.buttonFavorite.isActivated
                )
                if (itemBinding.buttonFavorite.isActivated) {
                    itemBinding.buttonFavorite.isChecked = true
                    itemBinding.buttonFavorite.playAnimation()
                } else {
                    itemBinding.buttonFavorite.isChecked = false
                }
            }
        }


    }

    interface OnMoviesClickListener {
        fun onMovieClicked(position: Int, imageView: ShapeableImageView)
        fun onMovieFavouriteClicked(position: Int, isActivated: Boolean)
    }

}