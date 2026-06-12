package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.dataprocess.AdItem
import com.example.myapplication.dataprocess.AppDatabase
import com.example.myapplication.dataprocess.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var btnBack: Button
    private lateinit var btnLogout: Button
    private lateinit var rvHistory: RecyclerView
    private lateinit var rvLikes: RecyclerView
    private lateinit var rvStars: RecyclerView
    private lateinit var historyAdapter: UserItemsAdapter
    private lateinit var likesAdapter: UserItemsAdapter
    private lateinit var starsAdapter: UserItemsAdapter
    private lateinit var emptyHistory: TextView
    private lateinit var emptyLikes: TextView
    private lateinit var emptyStars: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        tvUsername = findViewById(R.id.tv_username)
        btnBack = findViewById(R.id.btn_back)
        btnLogout = findViewById(R.id.btn_logout)
        rvHistory = findViewById(R.id.rv_history)
        rvLikes = findViewById(R.id.rv_likes)
        rvStars = findViewById(R.id.rv_stars)
        emptyHistory = findViewById(R.id.empty_history)
        emptyLikes = findViewById(R.id.empty_likes)
        emptyStars = findViewById(R.id.empty_stars)

        // 设置用户名
        val user = UserManager.getCurrentUser()
        if (user != null) {
            tvUsername.text = user.username
        }

        // 初始化列表
        historyAdapter = UserItemsAdapter { item ->
            val user = UserManager.getCurrentUser()
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_ID, item.id)
                putExtra(DetailActivity.EXTRA_TITLE, item.title)
                putExtra(DetailActivity.EXTRA_DESC, item.desc)
                putExtra(DetailActivity.EXTRA_DETAIL_CONTENT, item.detailContent)
                putExtra(DetailActivity.EXTRA_LABELS, item.label.toTypedArray())
                putExtra(DetailActivity.EXTRA_IMG_URL, item.imgUrl)
                putExtra(DetailActivity.EXTRA_VIDEO_URL, item.videoUrl)
                putExtra(DetailActivity.EXTRA_TYPE, item.type)
                // 根据用户的点赞/收藏列表设置状态
                putExtra(DetailActivity.EXTRA_LIKE, user?.likeList?.contains(item.id) ?: false)
                putExtra(DetailActivity.EXTRA_STAR, user?.starList?.contains(item.id) ?: false)
                putExtra(DetailActivity.EXTRA_LIKE_COUNT, item.likeCount)
                putExtra(DetailActivity.EXTRA_STAR_COUNT, item.starCount)
            }
            startActivity(intent)
        }
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        likesAdapter = UserItemsAdapter { item ->
            val user = UserManager.getCurrentUser()
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_ID, item.id)
                putExtra(DetailActivity.EXTRA_TITLE, item.title)
                putExtra(DetailActivity.EXTRA_DESC, item.desc)
                putExtra(DetailActivity.EXTRA_DETAIL_CONTENT, item.detailContent)
                putExtra(DetailActivity.EXTRA_LABELS, item.label.toTypedArray())
                putExtra(DetailActivity.EXTRA_IMG_URL, item.imgUrl)
                putExtra(DetailActivity.EXTRA_VIDEO_URL, item.videoUrl)
                putExtra(DetailActivity.EXTRA_TYPE, item.type)
                // 根据用户的点赞/收藏列表设置状态
                putExtra(DetailActivity.EXTRA_LIKE, user?.likeList?.contains(item.id) ?: false)
                putExtra(DetailActivity.EXTRA_STAR, user?.starList?.contains(item.id) ?: false)
                putExtra(DetailActivity.EXTRA_LIKE_COUNT, item.likeCount)
                putExtra(DetailActivity.EXTRA_STAR_COUNT, item.starCount)
            }
            startActivity(intent)
        }
        rvLikes.layoutManager = LinearLayoutManager(this)
        rvLikes.adapter = likesAdapter

        starsAdapter = UserItemsAdapter { item ->
            val user = UserManager.getCurrentUser()
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_ID, item.id)
                putExtra(DetailActivity.EXTRA_TITLE, item.title)
                putExtra(DetailActivity.EXTRA_DESC, item.desc)
                putExtra(DetailActivity.EXTRA_DETAIL_CONTENT, item.detailContent)
                putExtra(DetailActivity.EXTRA_LABELS, item.label.toTypedArray())
                putExtra(DetailActivity.EXTRA_IMG_URL, item.imgUrl)
                putExtra(DetailActivity.EXTRA_VIDEO_URL, item.videoUrl)
                putExtra(DetailActivity.EXTRA_TYPE, item.type)
                // 根据用户的点赞/收藏列表设置状态
                putExtra(DetailActivity.EXTRA_LIKE, user?.likeList?.contains(item.id) ?: false)
                putExtra(DetailActivity.EXTRA_STAR, user?.starList?.contains(item.id) ?: false)
                putExtra(DetailActivity.EXTRA_LIKE_COUNT, item.likeCount)
                putExtra(DetailActivity.EXTRA_STAR_COUNT, item.starCount)
            }
            startActivity(intent)
        }
        rvStars.layoutManager = LinearLayoutManager(this)
        rvStars.adapter = starsAdapter

        // 加载数据
        loadUserData()

        // 返回按钮
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // 添加返回动画
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        // 登出按钮
        btnLogout.setOnClickListener {
            UserManager.logout()
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次返回页面时刷新列表数据
        loadUserData()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // 添加返回动画
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun loadUserData() {
        val user = UserManager.getCurrentUser() ?: return

        GlobalScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.instance.adItemDao()

            // 加载浏览历史
            val historyItems = if (user.browseHistory.isNotEmpty()) {
                dao.getItemsByIds(user.browseHistory)
            } else {
                emptyList()
            }

            // 加载点赞列表
            val likeItems = if (user.likeList.isNotEmpty()) {
                dao.getItemsByIds(user.likeList)
            } else {
                emptyList()
            }

            // 加载收藏列表
            val starItems = if (user.starList.isNotEmpty()) {
                dao.getItemsByIds(user.starList)
            } else {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                if (historyItems.isEmpty()) {
                    rvHistory.visibility = View.GONE
                    emptyHistory.visibility = View.VISIBLE
                } else {
                    rvHistory.visibility = View.VISIBLE
                    emptyHistory.visibility = View.GONE
                    historyAdapter.submitList(historyItems)
                }

                if (likeItems.isEmpty()) {
                    rvLikes.visibility = View.GONE
                    emptyLikes.visibility = View.VISIBLE
                } else {
                    rvLikes.visibility = View.VISIBLE
                    emptyLikes.visibility = View.GONE
                    likesAdapter.submitList(likeItems)
                }

                if (starItems.isEmpty()) {
                    rvStars.visibility = View.GONE
                    emptyStars.visibility = View.VISIBLE
                } else {
                    rvStars.visibility = View.VISIBLE
                    emptyStars.visibility = View.GONE
                    starsAdapter.submitList(starItems)
                }
            }
        }
    }

    // 内部适配器类
    class UserItemsAdapter(private val onItemClick: (AdItem) -> Unit) :
        androidx.recyclerview.widget.ListAdapter<AdItem, UserItemsAdapter.ViewHolder>(
            object : androidx.recyclerview.widget.DiffUtil.ItemCallback<AdItem>() {
                override fun areItemsTheSame(oldItem: AdItem, newItem: AdItem): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: AdItem, newItem: AdItem): Boolean {
                    return oldItem == newItem
                }
            }
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title = itemView.findViewById<TextView>(R.id.item_title)
            private val desc = itemView.findViewById<TextView>(R.id.item_desc)
            private val image = itemView.findViewById<android.widget.ImageView>(R.id.item_image)
            private val labelContainer = itemView.findViewById<LinearLayout>(R.id.item_labels)

            fun bind(item: AdItem) {
                title.text = item.title
                desc.text = item.desc

                Glide.with(itemView.context)
                    .load(item.imgUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(image)

                labelContainer.removeAllViews()
                for (label in item.label.take(2)) {
                    val labelView = TextView(itemView.context).apply {
                        text = label
                        textSize = 10f
                        setTextColor(0xFF2196F3.toInt())
                        setBackgroundResource(R.drawable.bg_item_label)
                        val paddingH = (6 * itemView.context.resources.displayMetrics.density).toInt()
                        val paddingV = (2 * itemView.context.resources.displayMetrics.density).toInt()
                        setPadding(paddingH, paddingV, paddingH, paddingV)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = (4 * itemView.context.resources.displayMetrics.density).toInt()
                        }
                    }
                    labelContainer.addView(labelView)
                }

                itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}