package com.memad.moviesmix.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.os.LocaleListCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.memad.moviesmix.R
import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.databinding.FragmentSettingsBinding
import com.memad.moviesmix.ui.start.StartActivity
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.Constants.DARK
import com.memad.moviesmix.utils.Constants.LANG_PREF
import com.memad.moviesmix.utils.Constants.LIGHT
import com.memad.moviesmix.utils.SharedPreferencesHelper
import com.memad.moviesmix.utils.recreateTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    @Inject
    lateinit var gson: Gson
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dao: MoviesDao
    private val locales = arrayOf("العربية", "English")

    @Inject
    lateinit var preferencesHelper: SharedPreferencesHelper

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
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val currentLang = preferencesHelper.read(LANG_PREF, "1")?.toInt()!!

        binding.languageTextView.text = locales[currentLang]
        if (currentLang == 0) {
            binding.settingsToolbar.navigationIcon =
                getDrawable(requireContext(), R.drawable.ic_arrow_back)
        }
        setThemeChoice(preferencesHelper.darkMode)
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.lightRadioButton.id -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    preferencesHelper.darkMode = LIGHT
                }

                binding.darkRadioButton.id -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    preferencesHelper.darkMode = DARK
                }
            }
            requireActivity().recreate()
        }

        binding.languageCardView.setOnClickListener {
            openLanguageSelectorDialog(currentLang)
        }
        binding.removeCache.setOnClickListener {
            preferencesHelper.remove(
                Constants.SESSION,
            )
            viewLifecycleOwner.lifecycleScope.launch {
                dao.deleteAll()
            }
            startActivity(Intent(context, StartActivity::class.java))
            requireActivity().finish()
        }
        return binding.root
    }


    private fun setThemeChoice(it: Int) {
        when (it) {
            LIGHT -> {
                binding.lightRadioButton.isChecked = true
            }

            DARK -> {
                binding.darkRadioButton.isChecked = true

            }
        }
    }

    private fun collapseCard() {
        TransitionManager.beginDelayedTransition(binding.root)
        binding.themeTextView.setCompoundDrawablesWithIntrinsicBounds(
            getDrawable(requireContext(), R.drawable.ic_theme), null,
            getDrawable(requireContext(), R.drawable.ic_arrow_down), null
        )
        binding.themeRadioGroup.visibility = View.GONE
    }

    private fun expandCard() {
        TransitionManager.beginDelayedTransition(binding.root)
        binding.themeTextView.setCompoundDrawablesWithIntrinsicBounds(
            getDrawable(requireContext(), R.drawable.ic_theme), null,
            getDrawable(requireContext(), R.drawable.ic_arrow_up), null
        )
        binding.themeRadioGroup.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openLanguageSelectorDialog(currentLang: Int) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.language))


        builder.setSingleChoiceItems(locales, currentLang) { _, which ->
            when (which) {
                0 -> {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("ar")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                    preferencesHelper.save(LANG_PREF, 0)
                    requireActivity().recreate()
                }

                1 -> {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                    preferencesHelper.save(LANG_PREF, 1)
                    requireActivity().recreateTask()
                }
            }
        }
        builder.create().apply {
            show()
        }
    }
}