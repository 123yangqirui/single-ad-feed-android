package com.example.myapplication

import android.net.Uri
import android.view.LayoutInflater
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.dataprocess.AdItem
import com.example.myapplication.dataprocess.AppDatabase
import com.example.myapplication.dataprocess.UserManager
import com.example.myapplication.util.VideoPlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FeedAdapter : ListAdapter<AdItem, RecyclerView.ViewHolder>(diffCallback) {

    // 点击回调接口
    interface OnItemClickListener {
        fun onItemClick(item: AdItem)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<AdItem>() {
            override fun areItemsTheSame(oldItem: AdItem, newItem: AdItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: AdItem, newItem: AdItem): Boolean {
                return oldItem == newItem
            }
        }

        const val TYPE_BIG = 0
        const val TYPE_SMALL = 1
        const val TYPE_VIDEO = 2
        const val TYPE_FOOTER = 3
    }

    private var showFooter = false

    fun setShowFooter(show: Boolean) {
        showFooter = show
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (showFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (showFooter && position == itemCount - 1) {
            return TYPE_FOOTER
        }
        return getItem(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BIG -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_big_image, parent, false)
                BigImageViewHolder(view)
            }
            TYPE_SMALL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_small_image, parent, false)
                SmallImageViewHolder(view)
            }
            TYPE_VIDEO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video, parent, false)
                VideoViewHolder(view)
            }
            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_footer, parent, false)
                FooterViewHolder(view)
            }
            else -> throw Exception("未知类型")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BigImageViewHolder -> holder.bind(getItem(position))
            is SmallImageViewHolder -> holder.bind(getItem(position))
            is VideoViewHolder -> holder.bind(getItem(position))
            is FooterViewHolder -> holder.bind()
        }
    }

    // 大图
    inner class BigImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val desc = itemView.findViewById<TextView>(R.id.desc)
        private val image = itemView.findViewById<ImageView>(R.id.image)
        private val labelContainer = itemView.findViewById<LinearLayout>(R.id.labelContainer)
        private val likeBtn = itemView.findViewById<ImageView>(R.id.likeBtn)
        private val likeCount = itemView.findViewById<TextView>(R.id.likeCount)
        private val starBtn = itemView.findViewById<ImageView>(R.id.starBtn)
        private val starCount = itemView.findViewById<TextView>(R.id.starCount)

        fun bind(item: AdItem) {
            title.text = item.title
            desc.text = item.desc
            Glide.with(itemView.context)
                .load(item.imgUrl) // 传入什么就加载什么：http、file://、/sdcard/、R.drawable
                .placeholder(R.drawable.ic_launcher_background) // 加载中
                .error(R.drawable.ic_launcher_background)       // 加载失败
                .centerCrop()
                .into(image)
            bindLabels(labelContainer, item.label)

            // 设置点赞状态
            updateLikeState(item)
            // 设置收藏状态
            updateStarState(item)

            // 点赞按钮
            likeBtn.setOnClickListener {
                item.like = !item.like
                item.likeCount += if (item.like) 1 else -1
                updateLikeState(item)
                // 添加点击动画（放在状态更新之后）
                animateButton(likeBtn)
                // 保存到数据库
                saveLikeToDatabase(item.id, item.like, item.likeCount)
            }

            // 收藏按钮
            starBtn.setOnClickListener {
                item.star = !item.star
                item.starCount += if (item.star) 1 else -1
                updateStarState(item)
                // 添加点击动画（放在状态更新之后）
                animateButton(starBtn)
                // 保存到数据库
                saveStarToDatabase(item.id, item.star, item.starCount)
            }

            // 点击事件（排除按钮区域）
            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
        }

        private fun updateLikeState(item: AdItem) {
            likeBtn.setColorFilter((if (item.like) 0xFFFF4081 else 0xFF999999).toInt(), PorterDuff.Mode.SRC_IN)
            likeCount.text = formatCount(item.likeCount)
        }

        private fun updateStarState(item: AdItem) {
            starBtn.setColorFilter((if (item.star) 0xFFFFD700 else 0xFF999999).toInt(), PorterDuff.Mode.SRC_IN)
            starCount.text = formatCount(item.starCount)
        }
    }

    // 小图
    inner class SmallImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val desc = itemView.findViewById<TextView>(R.id.desc)
        private val image = itemView.findViewById<ImageView>(R.id.image)
        private val labelContainer = itemView.findViewById<LinearLayout>(R.id.labelContainer)
        private val likeBtn = itemView.findViewById<ImageView>(R.id.likeBtn)
        private val likeCount = itemView.findViewById<TextView>(R.id.likeCount)
        private val starBtn = itemView.findViewById<ImageView>(R.id.starBtn)
        private val starCount = itemView.findViewById<TextView>(R.id.starCount)

        fun bind(item: AdItem) {
            title.text = item.title
            desc.text = item.desc
            Glide.with(itemView.context)
                .load(item.imgUrl) // 传入什么就加载什么：http、file://、/sdcard/、R.drawable
                .placeholder(R.drawable.ic_launcher_background) // 加载中
                .error(R.drawable.ic_launcher_background)       // 加载失败
                .centerCrop()
                .into(image)
            bindLabels(labelContainer, item.label)

            // 设置点赞状态
            updateLikeState(item)
            // 设置收藏状态
            updateStarState(item)

            // 点赞按钮
            likeBtn.setOnClickListener {
                item.like = !item.like
                item.likeCount += if (item.like) 1 else -1
                updateLikeState(item)
                // 添加点击动画（放在状态更新之后）
                animateButton(likeBtn)
                // 保存到数据库
                saveLikeToDatabase(item.id, item.like, item.likeCount)
            }

            // 收藏按钮
            starBtn.setOnClickListener {
                item.star = !item.star
                item.starCount += if (item.star) 1 else -1
                updateStarState(item)
                // 添加点击动画（放在状态更新之后）
                animateButton(starBtn)
                // 保存到数据库
                saveStarToDatabase(item.id, item.star, item.starCount)
            }

            // 点击事件
            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
        }

        private fun updateLikeState(item: AdItem) {
            likeBtn.setColorFilter((if (item.like) 0xFFFF4081 else 0xFF999999).toInt(), PorterDuff.Mode.SRC_IN)
            likeCount.text = formatCount(item.likeCount)
        }

        private fun updateStarState(item: AdItem) {
            starBtn.setColorFilter((if (item.star) 0xFFFFD700 else 0xFF999999).toInt(), PorterDuff.Mode.SRC_IN)
            starCount.text = formatCount(item.starCount)
        }
    }

    // 视频
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val desc = itemView.findViewById<TextView>(R.id.desc)
        private val videoCover = itemView.findViewById<ImageView>(R.id.videoCover)
        private val videoView = itemView.findViewById<VideoView>(R.id.videoView)
        private val playBtn = itemView.findViewById<ImageView>(R.id.playBtn)
        private val labelContainer = itemView.findViewById<LinearLayout>(R.id.labelContainer)
        private val likeBtn = itemView.findViewById<ImageView>(R.id.likeBtn)
        private val likeCount = itemView.findViewById<TextView>(R.id.likeCount)
        private val starBtn = itemView.findViewById<ImageView>(R.id.starBtn)
        private val starCount = itemView.findViewById<TextView>(R.id.starCount)

        fun bind(item: AdItem) {
            title.text = item.title
            desc.text = item.desc
            bindLabels(labelContainer, item.label)

            // 重置视频状态（封面显示，视频隐藏，按钮显示播放图标）
            videoCover.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            playBtn.setImageResource(android.R.drawable.ic_media_play)
            
            // 如果当前有其他视频正在播放且是同一个视频，恢复播放状态
            val isCurrentlyPlaying = VideoPlaybackManager.getCurrentPlayingId() == item.id
            if (isCurrentlyPlaying && videoView.isPlaying) {
                videoCover.visibility = View.GONE
                videoView.visibility = View.VISIBLE
                playBtn.setImageResource(android.R.drawable.ic_media_pause)
            }

            // 设置视频封面图
            Glide.with(itemView.context)
                .load(item.imgUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(videoCover)

            // 设置视频路径
            if (!item.videoUrl.isNullOrEmpty()) {
                videoView.setVideoURI(Uri.parse(item.videoUrl))
            }

            // 设置播放/暂停点击事件
            playBtn.setOnClickListener {
                // 使用视频播放管理器处理播放逻辑（带缓存支持）
                if (!item.videoUrl.isNullOrEmpty()) {
                    VideoPlaybackManager.play(item.id, videoView, playBtn, videoCover, item.videoUrl)
                }
            }

            // 设置点赞状态
            updateLikeState(item)
            // 设置收藏状态
            updateStarState(item)

            // 点赞按钮
            likeBtn.setOnClickListener {
                item.like = !item.like
                item.likeCount += if (item.like) 1 else -1
                updateLikeState(item)
                // 添加点击动画（放在状态更新之后）
                animateButton(likeBtn)
                // 保存到数据库
                saveLikeToDatabase(item.id, item.like, item.likeCount)
            }

            // 收藏按钮
            starBtn.setOnClickListener {
                item.star = !item.star
                item.starCount += if (item.star) 1 else -1
                updateStarState(item)
                // 添加点击动画（放在状态更新之后）
                animateButton(starBtn)
                // 保存到数据库
                saveStarToDatabase(item.id, item.star, item.starCount)
            }

            // 点击整个条目跳转详情页（排除播放按钮区域）
            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }

            // 视频播放完成回调
            videoView.setOnCompletionListener {
                // 播放完成后回到开头并显示封面
                videoView.seekTo(0)
                VideoPlaybackManager.stop()
            }
        }

        private fun updateLikeState(item: AdItem) {
            likeBtn.setColorFilter((if (item.like) 0xFFFF4081 else 0xFF999999).toInt(), PorterDuff.Mode.SRC_IN)
            likeCount.text = formatCount(item.likeCount)
        }

        private fun updateStarState(item: AdItem) {
            starBtn.setColorFilter((if (item.star) 0xFFFFD700 else 0xFF999999).toInt(), PorterDuff.Mode.SRC_IN)
            starCount.text = formatCount(item.starCount)
        }
    }

    // 底部提示
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            // 底部提示不需要绑定数据
        }
    }

    // 格式化数字（如 1000 -> 1k）
    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> "${count / 10000}万"
            count >= 1000 -> "${count / 1000}k"
            else -> count.toString()
        }
    }

    private fun bindLabels(container: LinearLayout, labels: List<String>) {
        container.removeAllViews()
        for (label in labels) {
            val isSelected = FilterManager.isTagSelected(label)
            val labelView = TextView(container.context).apply {
                text = label
                textSize = 11f
                // 选中状态用不同颜色
                if (isSelected) {
                    setTextColor(ContextCompat.getColor(container.context, R.color.white))
                    setBackgroundColor(ContextCompat.getColor(container.context, R.color.primary))
                } else {
                    setTextColor(ContextCompat.getColor(container.context, R.color.primary))
                    setBackgroundResource(R.drawable.bg_item_label)
                }
                val paddingH = dpToPx(6, container.context)
                val paddingV = dpToPx(2, container.context)
                setPadding(paddingH, paddingV, paddingH, paddingV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dpToPx(4, container.context)
                }
                // 标签点击事件
                setOnClickListener {
                    FilterManager.toggleTag(label)
                }
            }
            container.addView(labelView)
        }
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // 更新单个 item 的状态（用于详情页返回后同步）
    fun updateItemState(itemId: String, isLiked: Boolean, isStarred: Boolean, likeCount: Int, starCount: Int) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                like = isLiked,
                star = isStarred,
                likeCount = likeCount,
                starCount = starCount
            )
            submitList(currentList)
        }
    }

    // 保存点赞状态到数据库
    private fun saveLikeToDatabase(itemId: String, isLiked: Boolean, likeCount: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.instance.adItemDao()
            dao.updateLikeAndCount(itemId, isLiked, likeCount)
            // 更新用户数据
            if (UserManager.isLoggedIn()) {
                if (isLiked) {
                    UserManager.addLike(itemId)
                } else {
                    UserManager.removeLike(itemId)
                }
            }
        }
    }

    // 保存收藏状态到数据库
    private fun saveStarToDatabase(itemId: String, isStarred: Boolean, starCount: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.instance.adItemDao()
            dao.updateStarAndCount(itemId, isStarred, starCount)
            // 更新用户数据
            if (UserManager.isLoggedIn()) {
                if (isStarred) {
                    UserManager.addStar(itemId)
                } else {
                    UserManager.removeStar(itemId)
                }
            }
        }
    }

    // 按钮点击动画
    private fun animateButton(view: View) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.scale_bounce)
        view.startAnimation(animation)
    }
}