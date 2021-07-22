package com.master.musicroomclient.utils

interface TabLayoutBadgeListener {

    fun onTabEvent(tabIndex: Int)

    fun onTabResume(tabIndex: Int)
}