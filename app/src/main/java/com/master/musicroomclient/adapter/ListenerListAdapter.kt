package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Listener

class ListenerListAdapter(
        private val values: List<Listener>
) : RecyclerView.Adapter<ListenerListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_room_listener_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listener = values[position]
        holder.listenerNameText.text = listener.name
        holder.listenerTimeText.text = listener.connectedAt
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listenerNameText: TextView = view.findViewById(R.id.listener_name)
        val listenerTimeText: TextView = view.findViewById(R.id.listener_time)
    }
}