package com.example.myapplication.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.dataprocess.AdItem

class MessageAdapter(
    private val messages: List<Message>,
    private val onAdItemClick: (AdItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1
        private const val VIEW_TYPE_AI_WITH_SEARCH = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            MessageType.USER -> VIEW_TYPE_USER
            MessageType.AI -> VIEW_TYPE_AI
            MessageType.AI_WITH_SEARCH -> VIEW_TYPE_AI_WITH_SEARCH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_AI_WITH_SEARCH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai_with_search, parent, false)
                AiWithSearchViewHolder(view, onAdItemClick)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai, parent, false)
                AiMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AiMessageViewHolder -> holder.bind(message)
            is AiWithSearchViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.tv_message_content)

        fun bind(message: Message) {
            contentText.text = message.content
        }
    }

    class AiMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.tv_message_content)

        fun bind(message: Message) {
            contentText.text = message.content
        }
    }

    class AiWithSearchViewHolder(
        itemView: View,
        private val onAdItemClick: (AdItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.tv_message_content)
        private val searchResultsLayout: LinearLayout = itemView.findViewById(R.id.search_results_layout)

        fun bind(message: Message) {
            contentText.text = message.content

            searchResultsLayout.removeAllViews()

            if (message.searchResults.isEmpty()) {
                val noResultView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_search_no_result, searchResultsLayout, false)
                searchResultsLayout.addView(noResultView)
            } else {
                message.searchResults.take(3).forEach { adItem ->
                    val searchItemView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_search_result, searchResultsLayout, false)

                    val title = searchItemView.findViewById<TextView>(R.id.tv_title)
                    val desc = searchItemView.findViewById<TextView>(R.id.tv_desc)
                    val image = searchItemView.findViewById<ImageView>(R.id.iv_image)
                    val tagsLayout = searchItemView.findViewById<LinearLayout>(R.id.tags_layout)

                    title.text = adItem.title
                    desc.text = adItem.desc

                    Glide.with(itemView.context)
                        .load(adItem.imgUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(image)

                    tagsLayout.removeAllViews()
                    adItem.label.take(3).forEach { tag ->
                        val tagView = LayoutInflater.from(itemView.context)
                            .inflate(R.layout.item_tag, tagsLayout, false)
                        val tagText = tagView.findViewById<TextView>(R.id.tv_tag)
                        tagText.text = tag
                        tagsLayout.addView(tagView)
                    }

                    searchItemView.setOnClickListener {
                        onAdItemClick(adItem)
                    }

                    searchResultsLayout.addView(searchItemView)
                }
            }
        }
    }
}
