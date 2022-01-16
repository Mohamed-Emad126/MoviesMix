package com.memad.moviesmix.ui.main.settings

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentSettingsBinding
import com.memad.moviesmix.utils.ThemeState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel by activityViewModels<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.themeCardView.setOnClickListener {
            if (binding.themeRadioGroup.isVisible) {
                collapseCard()
            } else {
                expandCard()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeState.collect {
                    setThemeChoice(it)
                }
            }
        }
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.lightRadioButton.id -> {
                    settingsViewModel.changeTheme(ThemeState.Light)
                }
                binding.darkRadioButton.id -> {
                    settingsViewModel.changeTheme(ThemeState.Dark)
                }
                binding.batterySaverRadioButton.id -> {
                    settingsViewModel.changeTheme(ThemeState.BatterySaver)
                }
                binding.followSystemRadioButton.id -> {
                    settingsViewModel.changeTheme(ThemeState.SystemDefault)
                }
            }
        }

        return binding.root
    }

    private fun setThemeChoice(it: ThemeState) {
        when (it) {
            ThemeState.Light -> {
                binding.themeRadioGroup.check(binding.lightRadioButton.id)
            }
            ThemeState.Dark -> {
                binding.themeRadioGroup.check(binding.darkRadioButton.id)
            }
            ThemeState.BatterySaver -> {
                binding.themeRadioGroup.check(binding.batterySaverRadioButton.id)
            }
            ThemeState.SystemDefault -> {
                binding.themeRadioGroup.check(binding.followSystemRadioButton.id)
            }
        }
    }

    private fun collapseCard() {
        TransitionManager.beginDelayedTransition(binding.themeCardView)
        binding.themeRadioGroup.visibility = View.GONE
        binding.themeTextView.setCompoundDrawables(
            AppCompatResources.getDrawable(context!!, R.drawable.ic_theme), null,
            AppCompatResources.getDrawable(context!!, R.drawable.ic_arrow_down), null
        )
    }

    private fun expandCard() {
        TransitionManager.beginDelayedTransition(binding.themeCardView)
        binding.themeRadioGroup.visibility = View.VISIBLE
        binding.themeTextView.setCompoundDrawables(
            AppCompatResources.getDrawable(context!!, R.drawable.ic_theme), null,
            AppCompatResources.getDrawable(context!!, R.drawable.ic_arrow_up), null
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}