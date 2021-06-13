package com.master.musicroomclient.adapter

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.master.musicroomclient.R
import com.master.musicroomclient.fragment.RoomChatFragment
import com.master.musicroomclient.fragment.RoomListenersFragment
import com.master.musicroomclient.fragment.RoomPlaylistFragment
import com.master.musicroomclient.model.Room

class TabAdapter(
    private val room: Room,
    private val userName: String,
    private val context: Context,
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            2 -> RoomListenersFragment.newInstance(room.listeners)
            1 -> RoomPlaylistFragment.newInstance(room.playlist)
            else -> RoomChatFragment.newInstance(room.code, userName)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }

    companion object {
        @StringRes
        private val TAB_TITLES =
            arrayOf(R.string.tab_chat_text, R.string.tab_playlist_text, R.string.tab_listeners_text)
    }
}