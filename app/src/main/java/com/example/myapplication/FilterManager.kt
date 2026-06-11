package com.example.myapplication

object FilterManager {
    private val selectedTags = mutableSetOf<String>()
    private val listeners = mutableListOf<OnFilterChangedListener>()

    interface OnFilterChangedListener {
        fun onFilterChanged(tags: Set<String>)
    }

    fun addTag(tag: String) {
        if (selectedTags.add(tag)) {
            notifyListeners()
        }
    }

    fun removeTag(tag: String) {
        if (selectedTags.remove(tag)) {
            notifyListeners()
        }
    }

    fun toggleTag(tag: String) {
        if (selectedTags.contains(tag)) {
            removeTag(tag)
        } else {
            addTag(tag)
        }
    }

    fun clearTags() {
        if (selectedTags.isNotEmpty()) {
            selectedTags.clear()
            notifyListeners()
        }
    }

    fun getSelectedTags(): Set<String> = selectedTags.toSet()

    fun hasTags(): Boolean = selectedTags.isNotEmpty()

    fun isTagSelected(tag: String): Boolean = selectedTags.contains(tag)

    fun addListener(listener: OnFilterChangedListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: OnFilterChangedListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        val tags = selectedTags.toSet()
        listeners.forEach { it.onFilterChanged(tags) }
    }
}