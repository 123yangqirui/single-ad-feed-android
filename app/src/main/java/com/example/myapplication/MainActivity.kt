package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.dataprocess.DataInitHelper
import com.example.myapplication.dataprocess.UserManager
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    //创建viewpager2和三个channel对应的变量
    private lateinit var viewPager2: ViewPager2
    private lateinit var btnChannel1: Button
    private lateinit var btnChannel2: Button
    private lateinit var btnChannel3: Button
    private lateinit var channelButtons: List<Button>
    
    // 搜索提示文字滚动相关
    private lateinit var searchHintText: TextView
    private val searchHints = listOf(
        "找运动相关的广告",
        "发现更多旅行资讯",
        "探索最近科技成果",
        "寻找热门运动装备",
        "发现最新运动资讯"
    )
    private var currentHintIndex = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val scrollInterval = 3000L // 切换间隔（毫秒）

    // 标签过滤相关
    private lateinit var filterHintText: TextView
    private lateinit var selectedTagsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 绑定 View
        viewPager2 = findViewById(R.id.viewPager2)
        btnChannel1 = findViewById(R.id.btn_channel_1)
        btnChannel2 = findViewById(R.id.btn_channel_2)
        btnChannel3 = findViewById(R.id.btn_channel_3)
        channelButtons = listOf(btnChannel1, btnChannel2, btnChannel3)
        searchHintText = findViewById(R.id.search_hint_text)

        // 初始化标签过滤视图
        filterHintText = findViewById(R.id.filter_hint_text)
        selectedTagsLayout = findViewById(R.id.selected_tags_layout)

        // 更新欢迎语
        updateWelcomeMessage()

        // 设置底部导航
        val btnMy = findViewById<Button>(R.id.btn_my)
        btnMy.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        // 缓存 3 个 Fragment，不销毁、不丢失位置
        viewPager2.offscreenPageLimit = 2

        // 等待数据初始化完成后再设置适配器
        lifecycleScope.launch {
            DataInitHelper.initTestData()
            // 数据初始化完成后再设置适配器，确保 Fragment 加载时数据已就绪
            viewPager2.adapter = ChannelPagerAdapter(this@MainActivity)
        }

        // 按钮点击切换
        btnChannel1.setOnClickListener { switchChannel(0) }
        btnChannel2.setOnClickListener { switchChannel(1) }
        btnChannel3.setOnClickListener { switchChannel(2) }

        // 滑动页面同步更新按钮
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtonState(position)
            }
        })

        // 启动搜索提示文字滚动
        startSearchHintScroll()

        // 监听标签过滤变化
        FilterManager.addListener(object : FilterManager.OnFilterChangedListener {
            override fun onFilterChanged(tags: Set<String>) {
                updateFilterTagUI(tags)
            }
        })
    }

    private fun updateFilterTagUI(tags: Set<String>) {
        if (tags.isEmpty()) {
            filterHintText.visibility = View.VISIBLE
            selectedTagsLayout.visibility = View.GONE
            selectedTagsLayout.removeAllViews()
        } else {
            filterHintText.visibility = View.GONE
            selectedTagsLayout.visibility = View.VISIBLE
            selectedTagsLayout.removeAllViews()
            for (tag in tags) {
                val tagView = createFilterTagView(tag)
                selectedTagsLayout.addView(tagView)
            }
        }
    }

    private fun createFilterTagView(tag: String): View {
        val context = this
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.bg_item_label)
            val paddingH = dpToPx(8)
            val paddingV = dpToPx(4)
            setPadding(paddingH, paddingV, paddingH, paddingV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(8)
            }
        }

        val tagText = TextView(context).apply {
            text = tag
            textSize = 12f
            setTextColor(0xFF2196F3.toInt())
        }
        container.addView(tagText)

        val closeBtn = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = LinearLayout.LayoutParams(dpToPx(14), dpToPx(14)).apply {
                marginStart = dpToPx(4)
            }
            setOnClickListener {
                FilterManager.removeTag(tag)
            }
        }
        container.addView(closeBtn)

        return container
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun updateWelcomeMessage() {
        val welcomeText = findViewById<TextView>(R.id.welcome)
        val username = UserManager.getCurrentUsername()
        if (username.isNotEmpty()) {
            welcomeText.text = "欢迎${username}用户，每次使用都是新的开始"
        } else {
            welcomeText.text = "欢迎使用，每次使用都是新的开始"
        }
    }

    // 启动搜索提示文字滚动
    private fun startSearchHintScroll() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                // 执行向上淡出动画
                val outAnimation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_up_out)
                outAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        // 更新文字
                        currentHintIndex = (currentHintIndex + 1) % searchHints.size
                        searchHintText.text = searchHints[currentHintIndex]
                        // 执行向下淡入动画
                        val inAnimation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_down_in)
                        searchHintText.startAnimation(inAnimation)
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                searchHintText.startAnimation(outAnimation)
                
                handler.postDelayed(this, scrollInterval)
            }
        }
        handler.postDelayed(runnable, scrollInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止滚动
        handler.removeCallbacks(runnable)
    }

    // 切换频道
    private fun switchChannel(position: Int) {
        viewPager2.setCurrentItem(position, true)
        updateButtonState(position)
    }

    // 更新按钮高亮
    private fun updateButtonState(selectedPosition: Int) {
        for (i in channelButtons.indices) {
            val btn = channelButtons[i]
            if (i == selectedPosition) {
                btn.setTextColor(Color.BLACK)
                btn.setBackgroundResource(R.drawable.bg_channel_btn_selected)
            } else {
                btn.setTextColor(Color.GRAY)
                btn.setBackgroundResource(R.drawable.bg_channel_btn_normal)
            }
        }
    }
}

