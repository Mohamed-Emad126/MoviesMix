package com.memad.moviesmix.ui.start.start

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentStartBinding
import com.memad.moviesmix.databinding.LoadingDialogBinding
import com.memad.moviesmix.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartFragment : Fragment() {


    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var loadingDialog: Dialog
    private lateinit var networkStatus: NetworkStatus
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!
    private val startViewModel by viewModels<StartViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        loadingDialog =
            requireActivity().createDialog(LoadingDialogBinding.inflate(inflater).root, false)

        binding.startedButton.setOnClickListener {
            startViewModel.createSession()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                startViewModel.networkStatus.collectLatest {
                    networkStatus = it
                }
            }
        }
        startViewModel.createSessionStatus.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    sharedPreferencesHelper.save(
                        Constants.SESSION,
                        response.data
                    )
                    loadingDialog.dismiss()
                    findNavController().navigate(R.id.action_startFragment_to_onBoardingFragment)
                }

                is Resource.Error -> {
                    loadingDialog.dismiss()
                    handleErrorState()
                }

                is Resource.Loading -> {
                    loadingDialog.show()
                }
            }
        }
        return binding.root
    }

    private fun networkCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                startViewModel.networkStatus.collectLatest {
                    when (it) {
                        is NetworkStatus.Connected -> {
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.no_network),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun handleErrorState() {
        networkCheck()
        when (networkStatus) {
            is NetworkStatus.Connected -> {}
            else -> {
                Toast.makeText(activity, "No network connection!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
