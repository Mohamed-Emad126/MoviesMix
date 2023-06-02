package com.memad.moviesmix.ui.main.favourites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.databinding.FavouriteItemBinding
import com.memad.moviesmix.utils.Constants
import javax.inject.Inject

class FavouritesAdapter @Inject constructor() :
    ListAdapter<FavouritesEntity, FavouritesAdapter.FavouriteViewHolder>(FavouritesDiffCallBack) {
    var favouriteClickListener: OnFavouriteEntityClickListener? = null

    override fun submitList(list: MutableList<FavouritesEntity>?) {
        val oldSize = currentList.size
        super.submitList(list)
        notifyItemRangeInserted(oldSize, list?.size!!)
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        return FavouriteViewHolder(
            FavouriteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        holder.itemBinding.favouriteImage.transitionName = position.toString()
        val movie = getItem(position).movie!!
        holder.itemBinding.favouriteTitle.text = movie.original_title
        holder.itemBinding.favouriteImage.load(
            Constants.POSTER_BASE_URL +
                    movie.poster_path
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }
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
    inner class FavouriteViewHolder(val itemBinding: FavouriteItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.favouriteImage.setOnClickListener {
                favouriteClickListener!!.onFavouriteEntityClicked(
                    absoluteAdapterPosition,
                    itemBinding.favouriteImage
                )
            }
        }

    }

    /////////////////////////////////////////////////////////////////
    ///////////////////ClickListenerInterface////////////////////////
    /////////////////////////////////////////////////////////////////
    interface OnFavouriteEntityClickListener {
        fun onFavouriteEntityClicked(position: Int, posterImageView: ShapeableImageView)
    }
}

object FavouritesDiffCallBack : DiffUtil.ItemCallback<FavouritesEntity>() {
    override fun areItemsTheSame(oldItem: FavouritesEntity, newItem: FavouritesEntity): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: FavouritesEntity, newItem: FavouritesEntity): Boolean {
        return newItem.movieId == oldItem.movieId
    }
}