package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import android.widget.VideoView
import com.danikula.videocache.HttpProxyCacheServer

object VideoPlaybackManager {
    private var currentPlayingId: String? = null
    private var currentVideoView: VideoView? = null
    private var currentPlayButton: ImageView? = null
    private var currentCoverView: ImageView? = null
    private var proxyCacheServer: HttpProxyCacheServer? = null

    private fun getProxy(context: Context): HttpProxyCacheServer {
        if (proxyCacheServer == null) {
            synchronized(this) {
                if (proxyCacheServer == null) {
                    proxyCacheServer = HttpProxyCacheServer.Builder(context)
                        .cacheDirectory(context.cacheDir)
                        .maxCacheSize(512 * 1024 * 1024)
                        .maxCacheFilesCount(20)
                        .build()
                }
            }
        }
        return proxyCacheServer!!
    }

    fun getProxyUrl(context: Context, originalUrl: String): String {
        return getProxy(context).getProxyUrl(originalUrl)
    }

    fun isCached(context: Context, url: String): Boolean {
        return getProxy(context).isCached(url)
    }

    fun play(
        itemId: String,
        videoView: VideoView,
        playButton: ImageView,
        coverView: ImageView,
        videoUrl: String
    ) {
        if (currentPlayingId == itemId) {
            return
        }

        pauseCurrent()

        currentPlayingId = itemId
        currentVideoView = videoView
        currentPlayButton = playButton
        currentCoverView = coverView

        val context = videoView.context
        val proxyUrl = getProxyUrl(context, videoUrl)
        videoView.setVideoURI(Uri.parse(proxyUrl))

        coverView.visibility = android.view.View.GONE
        videoView.visibility = android.view.View.VISIBLE

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false
            videoView.start()
            playButton.setImageResource(android.R.drawable.ic_media_pause)
        }

        videoView.setOnErrorListener { _, _, _ ->
            videoView.setVideoURI(Uri.parse(videoUrl))
            videoView.start()
            true
        }
    }

    fun pauseCurrent() {
        currentVideoView?.let { videoView ->
            if (videoView.isPlaying) {
                videoView.pause()
            }
        }
        currentPlayButton?.setImageResource(android.R.drawable.ic_media_play)
        currentCoverView?.visibility = android.view.View.VISIBLE
        currentVideoView?.visibility = android.view.View.GONE
        currentPlayingId = null
        currentVideoView = null
        currentPlayButton = null
        currentCoverView = null
    }

    fun stop() {
        pauseCurrent()
    }

    fun getCurrentPlayingId(): String? = currentPlayingId
}