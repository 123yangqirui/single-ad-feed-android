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
    private var lastPlayState: Boolean = false

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
        // 如果点击同一个视频 → 根据当前状态切换：播放/暂停
        if (currentPlayingId == itemId) {
            if (currentVideoView?.isPlaying == true) {
                // 当前在播放 → 暂停
                pauseCurrent()
            } else {
                // 当前已暂停 → 恢复播放
                resumeCurrent()
            }
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
        // 注意：暂停时保留 videoView 仍然可见（保留画面），但按钮图标恢复播放图标
        currentCoverView?.visibility = android.view.View.VISIBLE
        currentPlayButton?.visibility = android.view.View.VISIBLE
    }

    fun resumeCurrent() {
        currentVideoView?.let { videoView ->
            videoView.resume()
        }
        currentPlayButton?.setImageResource(android.R.drawable.ic_media_pause)
        currentCoverView?.visibility = android.view.View.GONE
    }

    fun stop() {
        pauseCurrent()
        currentPlayingId = null
        currentVideoView = null
        currentPlayButton = null
        currentCoverView = null
    }

    fun release() {
        stop()
    }

    fun getCurrentPlayingId(): String? = currentPlayingId
}