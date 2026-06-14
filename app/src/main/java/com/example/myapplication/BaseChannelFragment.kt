package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.util.VideoPlaybackManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.dataprocess.AdItem
import com.example.myapplication.dataprocess.AppDatabase
import com.example.myapplication.dataprocess.UserManager
import kotlinx.coroutines.Dispatchers


open class BaseChannelFragment(private val channelType: Int) : Fragment(), FilterManager.OnFilterChangedListener {

    //下拉刷新
    private lateinit var swipeRefresh: SwipeRefreshLayout
    //列表空控件
    private lateinit var recyclerView: RecyclerView
    //列表适配器
    private lateinit var adapter: FeedAdapter
    //数据列表
    private val dataList = mutableListOf<AdItem>()

    //数据加载页码数
    private var page = 0
    //页码大小（每次加载11个）
    private val pageSize = 11
    //是否正在加载
    private var isLoading = false
    //是否还有更多数据可加载
    private var hasMore = true
    //首次加载标志
    private var isFirstLoad = true

    //数据库访问对象
    private val dao = AppDatabase.instance.adItemDao()

    //详情页返回数据处理，更像相关的状态
    private val detailResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                val itemId = it.getStringExtra(DetailActivity.EXTRA_ID) ?: ""
                val isLiked = it.getBooleanExtra(DetailActivity.EXTRA_LIKE, false)
                val isStarred = it.getBooleanExtra(DetailActivity.EXTRA_STAR, false)
                val likeCount = it.getIntExtra(DetailActivity.EXTRA_LIKE_COUNT, 0)
                val starCount = it.getIntExtra(DetailActivity.EXTRA_STAR_COUNT, 0)
                
                // 同步 adapter 中的状态
                adapter.updateItemState(itemId, isLiked, isStarred, likeCount, starCount)
            }
        }
    }

    //生命周期
    //创建视图
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        return inflater.inflate(R.layout.fragment_channel_list, container, false)
    }

    //视图创建完成后
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView = view.findViewById(R.id.recyclerView)

        //初始化加载列表、刷新、加载更多功能
        initList()
        initRefresh()
        initLoadMore()

        // 注册标签过滤监听
        FilterManager.addListener(this)
    }
    //视图销毁
    override fun onDestroyView() {
        super.onDestroyView()
        // 取消标签过滤监听
        FilterManager.removeListener(this)
    }

    override fun onFilterChanged(tags: Set<String>) {
        // 标签变化时，重新加载数据
        refreshData()
    }
    //显示恢复的时候
    override fun onResume() {
        super.onResume()
        // 首次显示时加载数据
        if (isFirstLoad) {
            loadFirstPage()
            isFirstLoad = false
        }
    }

    //列表初始化：使用feedadapter作为列表适配器；点击事件点击跳转、使用intent传递数据、添加进入退出动画
    private fun initList() {
        adapter = FeedAdapter()
        // 设置点击事件监听
        adapter.setOnItemClickListener(object : FeedAdapter.OnItemClickListener {
            override fun onItemClick(item: AdItem) {
                // 跳转到详情页
                val user = UserManager.getCurrentUser()
                val userLiked = user?.likeList?.contains(item.id) ?: false
                val userStarred = user?.starList?.contains(item.id) ?: false
                val intent = Intent(activity, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_ID, item.id)
                    putExtra(DetailActivity.EXTRA_TITLE, item.title)
                    putExtra(DetailActivity.EXTRA_DESC, item.desc)
                    putExtra(DetailActivity.EXTRA_DETAIL_CONTENT, item.detailContent)
                    putExtra(DetailActivity.EXTRA_LABELS, item.label.toTypedArray())
                    putExtra(DetailActivity.EXTRA_IMG_URL, item.imgUrl)
                    putExtra(DetailActivity.EXTRA_VIDEO_URL, item.videoUrl)
                    putExtra(DetailActivity.EXTRA_TYPE, item.type)
                    putExtra(DetailActivity.EXTRA_LIKE, userLiked)
                    putExtra(DetailActivity.EXTRA_STAR, userStarred)
                    putExtra(DetailActivity.EXTRA_LIKE_COUNT, item.likeCount)
                    putExtra(DetailActivity.EXTRA_STAR_COUNT, item.starCount)
                }
                // 应用进入动画
                val options = activity?.let {
                    ActivityOptionsCompat.makeCustomAnimation(
                        it,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }
                if (options != null) {
                    detailResultLauncher.launch(intent, options)
                } else {
                    detailResultLauncher.launch(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    // 下拉刷新
    private fun initRefresh() {
        swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    //清空旧数据、重置更多数据标签、重新加载新的页面
    private fun refreshData() {
        if (isLoading) return
        isLoading = true
        page = 0 // 从第 0 页重新开始

        lifecycleScope.launch(Dispatchers.IO) {
            val tags = FilterManager.getSelectedTags()
            val (newItems, moreAvailable) = if (tags.isNotEmpty()) {
                val allItems = dao.getAllItemsByChannel(channelType)
                val filtered = allItems.filter { item ->
                    item.label.any { label -> tags.contains(label) }
                }
                filtered to false
            } else {
                val items = dao.getItemsSync(channelType, page * pageSize, pageSize)
                val more = !(items.isEmpty() || items.size < pageSize)
                items to more
            }

            lifecycleScope.launch(Dispatchers.Main) {
                hasMore = moreAvailable
                // 清空旧数据 + 加载新数据 + 提交给 adapter：全部在主线程同一个调用中完成
                dataList.clear()
                dataList.addAll(newItems)
                val finalList = dataList.toList()
                val showFooter = !hasMore && finalList.isNotEmpty()
                adapter.submitListAndFooter(finalList, showFooter)
                isLoading = false
                swipeRefresh.isRefreshing = false
            }
        }
    }

    // 上拉加载更多（倒数第3条触发）
    private fun initLoadMore() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                val total = adapter.itemCount

                //滚动到倒数第三个，触发加载更多
                if (!isLoading && hasMore && last >= total - 3) {
                    page++
                    loadNextPage()
                }
                
                // 检测滚动时是否有视频滑出屏幕，如果有则暂停
                checkVisibleVideoItems()
            }
            
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 滚动停止时也检查一下
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkVisibleVideoItems()
                }
            }

            //视频播放智能管理，超出可见范围暂停播放
            private fun checkVisibleVideoItems() {
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val firstVisible = lm.findFirstVisibleItemPosition()
                val lastVisible = lm.findLastVisibleItemPosition()
                
                // 检查当前可见的 item 中是否有视频正在播放
                val currentPlayingId = VideoPlaybackManager.getCurrentPlayingId()
                if (currentPlayingId != null) {
                    // 获取当前播放的视频在列表中的位置
                    var playingPosition = -1
                    for (i in firstVisible..lastVisible) {
                        val item = adapter.currentList.getOrNull(i)
                        if (item?.id == currentPlayingId) {
                            playingPosition = i
                            break
                        }
                    }
                    // 如果当前播放的视频不在可见范围内，暂停它
                    if (playingPosition < firstVisible || playingPosition > lastVisible) {
                        VideoPlaybackManager.pauseCurrent()
                    }
                }
            }
        })
    }

    private fun loadFirstPage() {
        lifecycleScope.launch {
            loadNextPage()
        }
    }

    //数据加载逻辑（所有数据/UI 修改都在主线程原子完成，避免 RecyclerView 不一致崩溃）
    private fun loadNextPage() {
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch(Dispatchers.IO) {
            val tags = FilterManager.getSelectedTags()
            val (newItems, moreAvailable) = if (tags.isNotEmpty()) {
                // 有过滤标签时，加载全部数据并过滤（不分页）
                val allItems = dao.getAllItemsByChannel(channelType)
                val filtered = allItems.filter { item ->
                    item.label.any { label -> tags.contains(label) }
                }
                filtered to false
            } else {
                // 无过滤时正常分页加载
                val items = dao.getItemsSync(channelType, page * pageSize, pageSize)
                val more = !(items.isEmpty() || items.size < pageSize)
                items to more
            }

            // 所有数据更新、UI 更新都切到主线程原子执行，防止 RecyclerView 读到不一致状态
            lifecycleScope.launch(Dispatchers.Main) {
                hasMore = moreAvailable
                // 对 dataList 的修改和对 adapter 的提交必须在同一个主线程调用中完成
                dataList.addAll(newItems)
                val finalList = dataList.toList()
                val showFooter = !hasMore && finalList.isNotEmpty()
                adapter.submitListAndFooter(finalList, showFooter)
                isLoading = false
                swipeRefresh.isRefreshing = false
            }
        }
    }
}