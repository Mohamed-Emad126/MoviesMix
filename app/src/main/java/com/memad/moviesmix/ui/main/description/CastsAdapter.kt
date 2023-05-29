package com.memad.moviesmix.ui.main.description

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.ItemCastBinding
import com.memad.moviesmix.models.Cast
import com.memad.moviesmix.utils.Constants
import javax.inject.Inject

class CastsAdapter @Inject constructor() :
    ListAdapter<Cast, CastsAdapter.CastViewHolder>(MoviesCastDiffCallBack) {
    var castClickListener: OnCastClickListener? = null
    override fun submitList(list: MutableList<Cast>?) {
        val oldSize = list?.size ?: 0
        super.submitList(list)
        notifyItemRangeInserted(oldSize, 20)
    }


    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        return CastViewHolder(
            ItemCastBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        ViewCompat.setTransitionName(holder.itemBinding.posterDialogImage, position.toString())
        val cast = getItem(position)
        holder.itemBinding.posterDialogImage.load(
            Constants.POSTER_BASE_URL +
                    cast.profile_path
        ) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
        }
        holder.itemBinding.castName.text = cast.name


    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class CastViewHolder(val itemBinding: ItemCastBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.posterDialogImage.setOnClickListener {
                castClickListener?.onCastClick(
                    absoluteAdapterPosition,
                    getItem(absoluteAdapterPosition),
                    itemBinding.posterDialogImage
                )
            }
        }


    }

    interface OnCastClickListener {
        fun onCastClick(position: Int, cast: Cast, imageView: ShapeableImageView)
    }

}

object MoviesCastDiffCallBack : DiffUtil.ItemCallback<Cast>() {
    override fun areItemsTheSame(oldItem: Cast, newItem: Cast): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: Cast, newItem: Cast): Boolean {
        return newItem.id == oldItem.id
    }
}