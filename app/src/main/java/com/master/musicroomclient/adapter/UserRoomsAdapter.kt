package com.master.musicroomclient.adapter

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Room


class UserRoomsAdapter(
    private val listener: OnItemClickListener,
    private val roomList: MutableList<Room>
) : RecyclerView.Adapter<UserRoomsAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(room: Room)

        fun onItemDeleted(room: Room)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_room_item_layout, parent, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = roomList[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    fun setRooms(rooms: List<Room>) {
        roomList.clear()
        roomList.addAll(rooms)
        notifyDataSetChanged()
    }

    fun removeRoom(room: Room) {
        val index = roomList.indexOf(room)
        roomList.remove(room)
        notifyItemRemoved(index)
    }

    class ViewHolder(private val view: View, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        private val userRoomNameText = view.findViewById<TextView>(R.id.user_room_name)
        private val userRoomListenersNumberText =
            view.findViewById<TextView>(R.id.user_room_listeners_number)
        private lateinit var room: Room

        fun bind(room: Room) {
            this.room = room
            userRoomNameText.text = room.name
            userRoomListenersNumberText.text = room.numberOfListeners.toString()
            view.setOnClickListener {
                this.listener.onItemClick(room)
            }
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
            menu.add("Remove from history").setOnMenuItemClickListener {
                listener.onItemDeleted(room)
                true
            }
        }
    }


}