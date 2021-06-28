package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Listener
import com.master.musicroomclient.utils.Constants.formatDurationToHoursAndMinutes
import java.time.Duration
import java.time.LocalDateTime

class ListenerListAdapter(
    private val values: MutableList<Listener>
) : RecyclerView.Adapter<ListenerListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_room_listener_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listener = values[position]
        holder.listenerNameText.text = listener.name

        val connectedTime = LocalDateTime.parse(listener.connectedAt)
        val connectedDuration = Duration.between(connectedTime, LocalDateTime.now())
        holder.listenerTimeText.text = formatDurationToHoursAndMinutes(connectedDuration.toMillis())
    }

    override fun getItemCount(): Int = values.size

    fun addListener(listener: Listener) {
        values.add(listener)
        notifyItemInserted(values.size - 1)
    }

    fun removeListener(listenerName: String) {
        val index = values.indexOfFirst { it.name == listenerName }
        if (index > -1) {
            values.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listenerNameText: TextView = view.findViewById(R.id.listener_name)
        val listenerTimeText: TextView = view.findViewById(R.id.listener_time)
    }
}