package com.memad.moviesmix.ui.main.viewer

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.fragment.navArgs
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.load
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.FragmentViewerBinding
import com.memad.moviesmix.ui.main.viewer.worker.DownloadImageWorker
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.checkmPermission
import com.memad.moviesmix.utils.unAvailableFeature
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Duration


@AndroidEntryPoint
class ViewerFragment : Fragment() {
    private var _binding: FragmentViewerBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ViewerFragmentArgs>()

    private lateinit var oneTimeWork: OneTimeWorkRequest

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    private val player: ExoPlayer? by lazy {
        ExoPlayer.Builder(requireContext()).build()
    }


    private val saveImageUnavailableMessage = "Save Image feature is unavailable " +
            "because you denied the Permission, you can Grant us the Permission to save the image " +
            "to your Device."

    private val saveImageNeededMessage =
        "This permission is needed to write to your contacts. to help you to hide them from Social Media apps\n" +
                "this permission is primary in the app the whole app is relying on it."

    private lateinit var saveImagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var url: String
    private val urlsList = mutableListOf<String>()


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
        if (args.isVideo) {
            binding.buttonDownload.visibility = View.GONE
            binding.imageView.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE

        } else {
            binding.playerView.visibility = View.GONE
            binding.buttonDownload.visibility = View.VISIBLE
            binding.imageView.visibility = View.VISIBLE
            url = Constants.POSTER_BASE_URL + args.url
            initForImage()
        }

        return binding.root
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun initForVideo() {
        urlsList.clear()

        args.url.split("**").forEach { urlsList.add( it) }
        player?.also {
            binding.playerView.player = it
        }
        val mediaSources = mutableListOf<MediaSource>()
        urlsList.forEach{
            viewLifecycleOwner.lifecycleScope.launch {
                val ytFiles = YTExtractor(requireContext()).getYtFile(it)
                val streamUrl = ytFiles?.get(22)?.url
                val video = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                    .createMediaSource(MediaItem.fromUri(streamUrl!!))
                mediaSources.add(video)
                player?.addMediaSource(video)
            }
        }
        player?.playWhenReady = playWhenReady
        player?.prepare()

    }

    private fun releasePlayer() {
        player?.let { player ->
            playbackPosition = player.currentPosition
            mediaItemIndex = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            player.release()
        }
        player?.clearMediaItems()
        player?.clearVideoSurface()
        urlsList.clear()
    }

    private fun initForImage() {
        ViewCompat.setTransitionName(binding.imageView, args.movieId)
        initWorker()
        binding.imageView.load(url) {
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
                    WorkManager.getInstance(requireContext()).enqueue(oneTimeWork)
                } else {
                    requireContext().unAvailableFeature(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        saveImageUnavailableMessage,
                        saveImagePermissionLauncher
                    )
                }
            }

        binding.buttonDownload.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                requireContext().checkmPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    {
                        WorkManager.getInstance(requireContext()).enqueue(oneTimeWork)
                    },
                    saveImageNeededMessage,
                    saveImagePermissionLauncher
                )
            } else {
                WorkManager.getInstance(requireContext()).enqueue(oneTimeWork)
            }
        }
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

    private fun initWorker() {
        val data = Data.Builder()
            .putString("imageUrl", url)
            .build()
        oneTimeWork = OneTimeWorkRequestBuilder<DownloadImageWorker>()
            .setInputData(data)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(10)
            )
            .build()
    }


    override fun onStart() {
        super.onStart()
        if (args.isVideo) {
            initForVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (player == null && args.isVideo) {
            initForVideo()
        }
    }

    override fun onStop() {
        super.onStop()
        if (args.isVideo) {
            releasePlayer()
        }
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(
            requireActivity().window,
            binding.playerView
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}