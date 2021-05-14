package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.master.musicroomclient.R


class UserRoomsAdapter(private val listener: OnItemClickListener, private val roomList: List<String>) : RecyclerView.Adapter<ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_room_item_layout, parent, false)
        return UserRoomHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = roomList[position]
        (holder as UserRoomHolder).bind(room)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    fun getRooms(): List<String> {
        return roomList
    }

    private class UserRoomHolder(itemView: View, onItemClickListener: OnItemClickListener) : ViewHolder(itemView) {
        val userRoomCodeText: TextView = itemView.findViewById(R.id.user_room_name)

        init {
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

        fun bind(room: String) {
            userRoomCodeText.text = room
        }
    }


}