package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.channels.Channel_1
import com.example.myapplication.channels.Channel_2
import com.example.myapplication.channels.Channel_3


class ChannelPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // 固定 3 个页面
    override fun getItemCount(): Int = 3

    // 根据位置创建对应的 Fragment
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Channel_1()
            1 -> Channel_2()
            2 -> Channel_3()
            else -> Channel_1()
        }
    }
}