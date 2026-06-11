package com.example.myapplication

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.dataprocess.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.util.VideoPlaybackManager

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESC = "extra_desc"
        const val EXTRA_DETAIL_CONTENT = "extra_detail_content"
        const val EXTRA_LABELS = "extra_labels"
        const val EXTRA_IMG_URL = "extra_img_url"
        const val EXTRA_VIDEO_URL = "extra_video_url"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_LIKE = "extra_like"
        const val EXTRA_STAR = "extra_star"
        const val EXTRA_LIKE_COUNT = "extra_like_count"
        const val EXTRA_STAR_COUNT = "extra_star_count"
    }

    private lateinit var btnBack: ImageView
    private lateinit var titleView: TextView
    private lateinit var descView: TextView
    private lateinit var detailContentView: TextView
    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var videoPlayBtn: ImageView
    private lateinit var videoControlBar: LinearLayout
    private lateinit var vidPlayBtn: ImageView
    private lateinit var videoSeekBar: SeekBar
    private lateinit var videoDuration: TextView
    private lateinit var vidVolumeBtn: ImageView
    private lateinit var vidFullscreenBtn: ImageView
    private lateinit var labelContainer: LinearLayout

    // 操作按钮
    private lateinit var likeBtn: ImageView
    private lateinit var likeCount: TextView
    private lateinit var starBtn: ImageView
    private lateinit var starCount: TextView
    private lateinit var shareBtn: ImageView
    private lateinit var shareCount: TextView

    private var isPlaying = false
    private var isMuted = false
    private var isFullscreen = false
    private var isLiked = false
    private var isStarred = false
    private var likeCountNum = 0
    private var starCountNum = 0
    private var itemId = ""

    private val handler = Handler(Looper.getMainLooper())
    private val dao = AppDatabase.instance.adItemDao()
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            if (isPlaying && videoView.isPlaying) {
                val current = videoView.currentPosition
                val duration = videoView.duration
                if (duration > 0) {
                    videoSeekBar.progress = (current * 100 / duration)
                    videoDuration.text = formatTime(current) + "/" + formatTime(duration)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 绑定视图
        btnBack = findViewById(R.id.btnBack)
        titleView = findViewById(R.id.detailTitle)
        descView = findViewById(R.id.detailDesc)
        detailContentView = findViewById(R.id.detailContent)
        imageView = findViewById(R.id.detailImage)
        videoView = findViewById(R.id.detailVideo)
        videoPlayBtn = findViewById(R.id.detailVideoPlayBtn)
        videoControlBar = findViewById(R.id.videoControlBar)
        vidPlayBtn = findViewById(R.id.vidPlayBtn)
        videoSeekBar = findViewById(R.id.videoSeekBar)
        videoDuration = findViewById(R.id.videoDuration)
        vidVolumeBtn = findViewById(R.id.vidVolumeBtn)
        vidFullscreenBtn = findViewById(R.id.vidFullscreenBtn)
        labelContainer = findViewById(R.id.detailLabelContainer)

        // 操作按钮
        likeBtn = findViewById(R.id.detailLikeBtn)
        likeCount = findViewById(R.id.detailLikeCount)
        starBtn = findViewById(R.id.detailStarBtn)
        starCount = findViewById(R.id.detailStarCount)
        shareBtn = findViewById(R.id.detailShareBtn)
//        shareCount = findViewById(R.id.detailShareCount)

        // 获取数据
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val desc = intent.getStringExtra(EXTRA_DESC) ?: ""
        val detailContent = intent.getStringExtra(EXTRA_DETAIL_CONTENT) ?: ""
        val labels = intent.getStringArrayExtra(EXTRA_LABELS) ?: emptyArray()
        val imgUrl = intent.getStringExtra(EXTRA_IMG_URL) ?: ""
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        val type = intent.getIntExtra(EXTRA_TYPE, 0)
        itemId = intent.getStringExtra(EXTRA_ID) ?: ""
        isLiked = intent.getBooleanExtra(EXTRA_LIKE, false)
        isStarred = intent.getBooleanExtra(EXTRA_STAR, false)
        likeCountNum = intent.getIntExtra(EXTRA_LIKE_COUNT, 0)
        starCountNum = intent.getIntExtra(EXTRA_STAR_COUNT, 0)

        // 设置数据
        titleView.text = title
        descView.text = desc
        detailContentView.text = detailContent

        // 设置标签
        bindLabels(labels.toList())

        // 设置操作按钮状态
        updateLikeState()
        updateStarState()

        // 根据类型显示图片或视频
        if (type == 2 && !videoUrl.isNullOrEmpty()) {
            // 视频类型 - 使用缓存代理 URL
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoPlayBtn.visibility = View.GONE
            videoControlBar.visibility = View.VISIBLE

            val proxyUrl = VideoPlaybackManager.getProxyUrl(this, videoUrl)
            videoView.setVideoURI(Uri.parse(proxyUrl))

            // 进入页面自动播放视频
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = false
                videoView.start()
                isPlaying = true
                vidPlayBtn.setImageResource(android.R.drawable.ic_media_pause)
                videoPlayBtn.visibility = View.GONE
            }

            videoPlayBtn.setOnClickListener {
                if (isPlaying) {
                    videoView.pause()
                    videoPlayBtn.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    videoView.start()
                    videoPlayBtn.setImageResource(android.R.drawable.ic_media_pause)
                    videoPlayBtn.visibility = View.GONE
                }
                isPlaying = !isPlaying
            }

            // 点击视频显示/隐藏播放按钮和控制栏
            videoView.setOnClickListener {
                if (videoControlBar.visibility == View.VISIBLE) {
                    videoControlBar.visibility = View.GONE
                } else {
                    videoControlBar.visibility = View.VISIBLE
                }
            }

            // 视频控制栏按钮
            vidPlayBtn.setOnClickListener {
                togglePlay()
            }

            // 进度条拖动
            videoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && videoView.duration > 0) {
                        videoView.seekTo(progress * videoView.duration / 100)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // 音量控制
            vidVolumeBtn.setOnClickListener {
                toggleMute()
            }

            // 全屏控制
            vidFullscreenBtn.setOnClickListener {
                toggleFullscreen()
            }

            // 视频播放完成回调 - 显示封面图
            videoView.setOnCompletionListener {
                isPlaying = false
                vidPlayBtn.setImageResource(android.R.drawable.ic_media_play)
                videoView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(imageView)
                videoControlBar.visibility = View.GONE
            }
        } else {
            // 图片类型
            videoView.visibility = View.GONE
            videoPlayBtn.visibility = View.GONE
            videoControlBar.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imageView)
        }

        // 点赞按钮
        likeBtn.setOnClickListener {
            isLiked = !isLiked
            likeCountNum += if (isLiked) 1 else -1
            updateLikeState()
            // 添加点击动画
            animateButton(likeBtn)
            // 保存到数据库
            saveLikeState()
        }

        // 收藏按钮
        starBtn.setOnClickListener {
            isStarred = !isStarred
            starCountNum += if (isStarred) 1 else -1
            updateStarState()
            // 添加点击动画
            animateButton(starBtn)
            // 保存到数据库
            saveStarState()
        }

        // 分享按钮
        shareBtn.setOnClickListener {
            shareContent(title, desc)
        }

        // 返回按钮 - 设置在所有逻辑之后，确保不会被覆盖
        btnBack.isClickable = true
        btnBack.isFocusable = true
        btnBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (isFullscreen) {
                    toggleFullscreen()
                } else {
                    returnResult()
                }
            }
        })

        // 开始更新进度
        handler.post(updateProgressRunnable)
    }

    private fun togglePlay() {
        if (isPlaying) {
            videoView.pause()
            vidPlayBtn.setImageResource(android.R.drawable.ic_media_play)
            videoPlayBtn.visibility = View.VISIBLE
        } else {
            videoView.start()
            vidPlayBtn.setImageResource(android.R.drawable.ic_media_pause)
            videoPlayBtn.visibility = View.GONE
        }
        isPlaying = !isPlaying
    }

    private fun toggleMute() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (isMuted) {
            // 取消静音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
            vidVolumeBtn.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
        } else {
            // 静音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            vidVolumeBtn.setImageResource(android.R.drawable.ic_lock_silent_mode)
        }
        isMuted = !isMuted
    }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            // 退出全屏
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            supportActionBar?.show()
            vidFullscreenBtn.setImageResource(android.R.drawable.ic_menu_add)

            // 恢复视频尺寸
            val layoutParams = videoView.layoutParams
            layoutParams.height = dpToPx(240)
            videoView.layoutParams = layoutParams
        } else {
            // 进入全屏
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            supportActionBar?.hide()
            vidFullscreenBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)

            // 视频铺满屏幕
            val layoutParams = videoView.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            videoView.layoutParams = layoutParams
        }
        isFullscreen = !isFullscreen
    }

    private fun updateLikeState() {
        likeBtn.setColorFilter((if (isLiked) 0xFFFF4081 else 0xFF999999).toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
        likeCount.text = formatCount(likeCountNum)
    }

    private fun updateStarState() {
        starBtn.setColorFilter((if (isStarred) 0xFFFFD700 else 0xFF999999).toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
        starCount.text = formatCount(starCountNum)
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> "${count / 10000}万"
            count >= 1000 -> "${count / 1000}k"
            else -> count.toString()
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun shareContent(title: String, desc: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n$desc")
        }
        startActivity(Intent.createChooser(intent, "分享到"))
    }

    private fun saveLikeState() {
        if (itemId.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.updateLikeAndCount(itemId, isLiked, likeCountNum)
            }
        }
    }

    private fun saveStarState() {
        if (itemId.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                dao.updateStarAndCount(itemId, isStarred, starCountNum)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
            isPlaying = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressRunnable)
        videoView.stopPlayback()
    }

    private fun bindLabels(labels: List<String>) {
        labelContainer.removeAllViews()
        for (label in labels) {
            val labelView = TextView(this).apply {
                text = label
                textSize = 12f
                setTextColor(0xFF2196F3.toInt())
                setBackgroundResource(R.drawable.bg_item_label)
                val paddingH = dpToPx(8)
                val paddingV = dpToPx(4)
                setPadding(paddingH, paddingV, paddingH, paddingV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dpToPx(6)
                    topMargin = dpToPx(4)
                    bottomMargin = dpToPx(4)
                }
            }
            labelContainer.addView(labelView)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // 按钮点击动画
    private fun animateButton(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.scale_bounce)
        view.startAnimation(animation)
    }

    override fun onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen()
        } else {
            returnResult()
        }
    }

    private fun returnResult() {
        val result = Intent().apply {
            putExtra(EXTRA_ID, itemId)
            putExtra(EXTRA_LIKE, isLiked)
            putExtra(EXTRA_STAR, isStarred)
            putExtra(EXTRA_LIKE_COUNT, likeCountNum)
            putExtra(EXTRA_STAR_COUNT, starCountNum)
        }
        setResult(android.app.Activity.RESULT_OK, result)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}