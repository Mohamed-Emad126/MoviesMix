package com.memad.moviesmix.ui.start.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentOnboardingBinding
import com.memad.moviesmix.ui.main.MainActivity


class OnBoardingFragment : Fragment(), ViewPager.OnPageChangeListener {

    private lateinit var images: Array<Int>
    private lateinit var onBoardingAdapter: OnBoardingAdapter

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        images = arrayOf(
            R.drawable.food_and_restaurant,
            R.drawable.cinema,
            R.drawable.clapperboard
        )
        val titleText = arrayOf(
            getString(R.string.app_name),
            getString(R.string.trending_movies),
            getString(R.string.upcoming_movies)
        )
        val descText = arrayOf(
            getString(R.string.welcome),
            getString(R.string.trending),
            getString(R.string.upcoming)
        )

        onBoardingAdapter = OnBoardingAdapter(requireContext(), images, descText, titleText)
        binding.screenViewpager.adapter = onBoardingAdapter
        binding.tabIndicator.setupWithViewPager(binding.screenViewpager)
        binding.screenViewpager.addOnPageChangeListener(this)
        binding.tabIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                checkLastScreen(tab!!.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        binding.skip.setOnClickListener { binding.screenViewpager.currentItem = images.size - 1 }
        binding.btnNext.setOnClickListener {
            var position: Int = binding.screenViewpager.currentItem
            if (position < images.size) {
                position++
                binding.screenViewpager.currentItem = position
            }
        }
        binding.btnGetStarted.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    MainActivity::class.java
                )
            )
            requireActivity().finish()
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageSelected(position: Int) {
        checkLastScreen(position)
    }

    private fun checkLastScreen(position: Int) {
        if (position == images.size - 1) {
            loadLastScreen()
        } else {
            unLoadLastScreen()
        }
    }

    private fun unLoadLastScreen() {
        binding.btnNext.visibility = View.VISIBLE
        binding.skip.visibility = View.VISIBLE
        binding.tabIndicator.visibility = View.VISIBLE
        binding.btnGetStarted.visibility = View.INVISIBLE
    }

    private fun loadLastScreen() {
        binding.btnNext.visibility = View.INVISIBLE
        binding.skip.visibility = View.INVISIBLE
        binding.tabIndicator.visibility = View.INVISIBLE
        binding.btnGetStarted.animation =
            AnimationUtils.loadAnimation(context, R.anim.button_animation)
        binding.btnGetStarted.visibility = View.VISIBLE
    }

}