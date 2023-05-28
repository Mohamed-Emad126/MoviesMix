package com.memad.moviesmix.ui.main.viewer

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentViewerBinding
import com.memad.moviesmix.utils.Downloader
import com.memad.moviesmix.utils.checkmPermission
import com.memad.moviesmix.utils.unAvailableFeature
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ViewerFragment : Fragment() {
    private var _binding: FragmentViewerBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ViewerFragmentArgs>()

    @Inject
    lateinit var downloadFile: Downloader


    private val saveImageUnavailableMessage = "Save Image feature is unavailable " +
            "because you denied the Permission, you can Grant us the Permission to save the image " +
            "to your Device."

    private val saveImageNeededMessage =
        "This permission is needed to write to your contacts. to help you to hide them from Social Media apps\n" +
                "this permission is primary in the app the whole app is relying on it."

    private lateinit var saveImagePermissionLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.main_nav_host_fragment
            scrimColor = Color.TRANSPARENT
            duration = 500
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentViewerBinding.inflate(inflater, container, false)

        ViewCompat.setTransitionName(binding.imageView, args.movieId)

        binding.imageView.load(args.url) {
            crossfade(true)
            placeholder(R.drawable.start_img_min_blur)
            error(R.drawable.start_img_min_broken)
            allowHardware(false)
            listener(
                onSuccess = { _, _ ->
                    startPostponedEnterTransition()
                })
        }

        saveImagePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    downloadFile.downloadFile(args.url)
                } else {
                    requireContext().unAvailableFeature(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        saveImageUnavailableMessage,
                        saveImagePermissionLauncher
                    )
                }
            }

        binding.buttonDownload.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                requireContext().checkmPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    {
                        downloadFile.downloadFile(args.url)
                    },
                    saveImageNeededMessage,
                    saveImagePermissionLauncher
                )
            } else {
                downloadFile.downloadFile(args.url)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        binding.imageView.doOnPreDraw { startPostponedEnterTransition() }
    }
}