package com.example.myapplication.dialog

import android.util.Log
import com.example.myapplication.BuildConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * AI 客户端 - 用于调用 DeepSeek API
 */
object AiClient {

    private const val BASE_URL = "https://api.deepseek.com/v1/"
    private const val API_KEY = BuildConfig.DEEPSEEK_API_KEY

    private val gson = Gson()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val aiService = retrofit.create(AiService::class.java)

    /**
     * 构建提示词，包含用户信息和数据库检索方式
     */
    fun buildPrompt(userMessage: String): String {
        val systemInfo = """
            你是一个智能助手，需要帮助用户通过标签检索数据库中的广告内容。
            
            可用的数据库检索方法：
            1. searchByTag(tag: String): 通过单个标签检索相关广告
            2. searchByMultipleTags(tags: List<String>): 通过多个标签检索相关广告
            3. searchByChannel(channel: Int): 通过频道类型检索（0=精选, 1=推荐, 2=本地）
            4. searchByKeyword(keyword: String): 通过关键词检索标题和描述
            
            用户可能的需求：
            - 搜索特定类型的广告（如运动、旅行、科技等）
            - 查找特定频道的内容
            - 获取推荐内容
            
            请按照以下格式输出：
            {
                "reply": "回复用户的自然语言内容",
                "search_method": "要调用的检索方法名",
                "tags": ["用于检索的标签列表"]
            }
            
            注意：
            - 如果不需要检索数据库，search_method 填 "none"，tags 填空数组
            - 回复要友好自然
            - 标签要准确匹配数据库中的标签（运动、旅行、科技等）
        """.trimIndent()

        return """
            $systemInfo
            
            用户消息：$userMessage
        """.trimIndent()
    }

    /**
     * 从文本中提取 JSON 字符串
     */
    private fun extractJsonFromText(text: String): String? {
        val startIndex = text.indexOf("{")
        val endIndex = text.lastIndexOf("}")
        
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return text.substring(startIndex, endIndex + 1)
        }
        return null
    }

    /**
     * 发送消息到 DeepSeek API
     */
    fun sendMessage(userMessage: String, callback: (Result<AiResponse>) -> Unit) {
        val prompt = buildPrompt(userMessage)
        
        val request = DeepSeekRequest(
            messages = listOf(
                MessageData("user", prompt)
            )
        )

        aiService.sendMessage(request).enqueue(object : retrofit2.Callback<DeepSeekResponse> {
            override fun onResponse(call: Call<DeepSeekResponse>, response: retrofit2.Response<DeepSeekResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val aiReply = response.body()!!.choices[0].message.content
                    Log.d("AiClient", "Raw AI reply: $aiReply")
                    
                    try {
                        // 尝试直接解析 JSON
                        val aiResponse = gson.fromJson(aiReply, AiResponse::class.java)
                        Log.d("AiClient", "Direct JSON parse success")
                        callback(Result.success(aiResponse))
                    } catch (e: Exception) {
                        Log.d("AiClient", "Direct parse failed, trying to extract JSON: ${e.message}")
                        // 如果直接解析失败，尝试从文本中提取 JSON
                        try {
                            val extractedJson = extractJsonFromText(aiReply)
                            if (extractedJson != null) {
                                Log.d("AiClient", "Extracted JSON: $extractedJson")
                                val aiResponse = gson.fromJson(extractedJson, AiResponse::class.java)
                                callback(Result.success(aiResponse))
                            } else {
                                // 如果无法提取 JSON，将整个内容作为回复
                                Log.d("AiClient", "No JSON found, using raw text")
                                callback(Result.success(AiResponse(aiReply, "none", emptyList())))
                            }
                        } catch (e2: Exception) {
                            // 所有解析都失败，将整个内容作为回复
                            Log.d("AiClient", "All parse attempts failed: ${e2.message}")
                            callback(Result.success(AiResponse(aiReply, "none", emptyList())))
                        }
                    }
                } else {
                    callback(Result.failure(Exception("API 请求失败: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<DeepSeekResponse>, t: Throwable) {
                Log.e("AiClient", "API 请求失败", t)
                callback(Result.failure(t))
            }
        })
    }
}
