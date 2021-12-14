package com.memad.moviesmix.ui.start.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.memad.moviesmix.databinding.LayoutScreenBinding


class OnBoardingAdapter(
    private var mContext: Context,
    private var images: Array<Int>,
    private var descText: Array<String>,
    private var titleText: Array<String>
): PagerAdapter() {
    private var inflater: LayoutInflater? = null

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val binding = LayoutScreenBinding.inflate(inflater!!, container, false)

        binding.introImg.setImageResource(images[position])
        binding.introTitle.text = titleText[position]
        binding.introDescription.text = descText[position]
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}