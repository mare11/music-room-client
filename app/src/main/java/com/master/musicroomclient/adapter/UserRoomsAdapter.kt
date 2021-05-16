package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R


class UserRoomsAdapter(
        private val listener: OnItemClickListener,
        private val roomList: List<String>
) : RecyclerView.Adapter<UserRoomsAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_room_item_layout, parent, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = roomList[position]
        holder.userRoomCodeText.text = room
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    fun getRooms(): List<String> {
        return roomList
    }

    class ViewHolder(view: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
        val userRoomCodeText: TextView = view.findViewById(R.id.user_room_name)

        init {
            view.setOnClickListener {
                onItemClickListener.onItemClick(bindingAdapterPosition)
            }
        }
    }


}