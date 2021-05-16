package com.master.musicroomclient.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.master.musicroomclient.fragment.RoomChatFragment
import com.master.musicroomclient.fragment.RoomListenersFragment
import com.master.musicroomclient.fragment.RoomPlaylistFragment
import com.master.musicroomclient.model.Room

class TabAdapter(
    private val mContext: Context,
    private val fm: FragmentManager,
    private val room: Room,
    private val userName: String
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            2 -> RoomListenersFragment.newInstance(room.listeners)
            1 -> RoomPlaylistFragment.newInstance("", "")
            else -> RoomChatFragment.newInstance(room.code!!, userName) // FIXME
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
//        return mContext.getResources().getString(TAB_TITLES[position]);
        return TAB_TITLES[position]
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }

    companion object {
        //    @StringRes
        //    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
        private val TAB_TITLES = arrayOf("Chat", "Playlist", "Listeners")
    }
}