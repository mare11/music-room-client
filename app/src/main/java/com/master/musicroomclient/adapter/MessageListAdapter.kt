package com.master.musicroomclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.model.Message


class MessageListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // FIXME: 18-Apr-21  
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    private val messageList = mutableListOf<Message>()

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.sender == "ME") {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_sent_layout, parent, false)
            SentMessageHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_received_layout, parent, false)
            ReceivedMessageHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        if (holder.itemViewType == VIEW_TYPE_MESSAGE_SENT) {
            (holder as SentMessageHolder).bind(message)
        } else {
            (holder as ReceivedMessageHolder).bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun addMessage(message: Message) {
        messageList.add(message)
        notifyItemInserted(messageList.size - 1)
    }

    private class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.sent_message_text)
        val messageDate: TextView = itemView.findViewById(R.id.sent_message_date)
        val messageTime: TextView = itemView.findViewById(R.id.sent_message_time)

        fun bind(message: Message) {
            messageText.text = message.content
            val timestamp = message.timestamp
            messageDate.text = timestamp.month.name + " " + timestamp.dayOfMonth
            messageTime.text = "" + timestamp.hour + ":" + timestamp.minute
            // Format the stored timestamp into a readable String using method.
//            timeText.setText(Utils.formatDateTime(message.getCreatedAt()))
        }
    }

    private class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageSender: TextView = itemView.findViewById(R.id.received_message_name)
        val messageText: TextView = itemView.findViewById(R.id.received_message_text)
        val messageDate: TextView = itemView.findViewById(R.id.received_message_date)
        val messageTime: TextView = itemView.findViewById(R.id.received_message_time)

        fun bind(message: Message) {
            messageSender.text = message.sender
            messageText.text = message.content
            val timestamp = message.timestamp
            messageDate.text = timestamp.month.name + " " + timestamp.dayOfMonth
            messageTime.text = "" + timestamp.hour + ":" + timestamp.minute
            // Format the stored timestamp into a readable String using method.
//            timeText.setText(Utils.formatDateTime(message.getCreatedAt()))
        }
    }
}