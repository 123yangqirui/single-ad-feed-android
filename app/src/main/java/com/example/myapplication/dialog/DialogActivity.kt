package com.example.myapplication.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DetailActivity
import com.example.myapplication.R
import com.example.myapplication.dataprocess.AdItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 对话页面 Activity
 * 集成 AI 功能，支持与大模型对话并检索数据库
 */
class DialogActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SEARCH_QUERY = "search_query"

        fun start(activity: Activity, searchQuery: String = "") {
            val intent = Intent(activity, DialogActivity::class.java)
            intent.putExtra(EXTRA_SEARCH_QUERY, searchQuery)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.dialog_open_enter, R.anim.dialog_open_exit)
        }
    }

    private lateinit var closeButton: ImageButton
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesRecyclerView: RecyclerView

    private val messages = mutableListOf<Message>()
    private lateinit var messageAdapter: MessageAdapter
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        // 绑定 View
        closeButton = findViewById(R.id.btn_close_dialog)
        messageInput = findViewById(R.id.et_message_input)
        sendButton = findViewById(R.id.btn_send)
        messagesRecyclerView = findViewById(R.id.rv_messages)

        // 初始化消息列表
        initRecyclerView()

        // 设置关闭按钮点击事件
        closeButton.setOnClickListener {
            closeDialog()
        }

        // 设置发送按钮点击事件
        sendButton.setOnClickListener {
            sendMessage()
        }

        // 设置输入法动作
        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        // 如果有传入的搜索关键词，自动发送
        val searchQuery = intent.getStringExtra(EXTRA_SEARCH_QUERY) ?: ""
        if (searchQuery.isNotEmpty()) {
            messageInput.setText(searchQuery)
            sendMessage()
        }
    }

    /**
     * 初始化消息列表
     */
    private fun initRecyclerView() {
        messageAdapter = MessageAdapter(messages) { adItem ->
            // 点击搜索结果跳转到详情页
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("ad_id", adItem.id)
            startActivity(intent)
        }

        messagesRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@DialogActivity)
            setHasFixedSize(true)
        }
    }

    /**
     * 发送消息
     */
    private fun sendMessage() {
        val content = messageInput.text.toString().trim()
        if (content.isEmpty() || isSending) return

        // 添加用户消息到列表
        val userMessage = Message(
            id = "user_${System.currentTimeMillis()}",
            type = MessageType.USER,
            content = content
        )
        messages.add(userMessage)
        messageAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()

        // 清空输入框
        messageInput.text.clear()

        // 发送到 AI
        isSending = true
        sendButton.isEnabled = false

        AiClient.sendMessage(content) { result ->
            isSending = false
            sendButton.isEnabled = true

            result.onSuccess { aiResponse ->
                handleAiResponse(aiResponse)
            }.onFailure { error ->
                // 显示错误提示
                val errorMessage = Message(
                    id = "ai_${System.currentTimeMillis()}",
                    type = MessageType.AI,
                    content = "抱歉，暂时无法获取回复，请稍后重试。"
                )
                messages.add(errorMessage)
                messageAdapter.notifyItemInserted(messages.size - 1)
                scrollToBottom()

                Toast.makeText(this, "请求失败: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 处理 AI 响应
     */
    private fun handleAiResponse(aiResponse: AiResponse) {
        Log.d("DialogActivity", "handleAiResponse called")
        Log.d("DialogActivity", "AI Response: reply=${aiResponse.reply}")
        Log.d("DialogActivity", "AI Response: searchMethod=${aiResponse.searchMethod}")
        Log.d("DialogActivity", "AI Response: tags=${aiResponse.tags}")

        // 在后台线程执行数据库检索
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("DialogActivity", "Executing search in background thread")
            
            // 执行数据库检索
            val searchResults = if (aiResponse.searchMethod != "none" && aiResponse.tags.isNotEmpty()) {
                Log.d("DialogActivity", "Calling SearchService.executeSearch")
                SearchService.executeSearch(aiResponse.searchMethod, aiResponse.tags)
            } else {
                Log.d("DialogActivity", "No search needed (method=none or tags empty)")
                emptyList()
            }

            Log.d("DialogActivity", "Search results count: ${searchResults.size}")

            // 在主线程更新 UI
            withContext(Dispatchers.Main) {
                val messageType = if (searchResults.isNotEmpty()) {
                    MessageType.AI_WITH_SEARCH
                } else {
                    MessageType.AI
                }

                val aiMessage = Message(
                    id = "ai_${System.currentTimeMillis()}",
                    type = messageType,
                    content = aiResponse.reply,
                    searchResults = searchResults
                )

                messages.add(aiMessage)
                messageAdapter.notifyItemInserted(messages.size - 1)
                scrollToBottom()
            }
        }
    }

    /**
     * 滚动到列表底部
     */
    private fun scrollToBottom() {
        messagesRecyclerView.post {
            messagesRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    /**
     * 关闭对话框并返回主页面
     */
    private fun closeDialog() {
        finish()
        overridePendingTransition(R.anim.dialog_close_enter, R.anim.dialog_close_exit)
    }

    override fun onBackPressed() {
        closeDialog()
    }
}
