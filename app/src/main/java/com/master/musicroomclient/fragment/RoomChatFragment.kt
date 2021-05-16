package com.master.musicroomclient.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.MessageListAdapter
import com.master.musicroomclient.model.Message
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import java.time.Instant

/**
 * A fragment representing a list of Items.
 */
class RoomChatFragment(private val roomCode: String, private val userName: String) : Fragment() {

    private lateinit var messageView: RecyclerView
    private val messageListAdapter = MessageListAdapter(this.userName)

    private val stompClient: StompClient =
        Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.0.16:8008/music-rooms")
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val gson = GsonBuilder().create()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        messageView = view.findViewById(R.id.message_view)
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.stackFromEnd = true
        messageView.layoutManager = linearLayoutManager
        messageView.adapter = messageListAdapter

        val sendMessageText = view.findViewById<EditText>(R.id.send_message_text)
        val sendMessageButton = view.findViewById<Button>(R.id.send_message_button)
        sendMessageButton.setOnClickListener {
            val messageText = sendMessageText.text.toString()
            if (messageText.isNotBlank()) {
                val sentMessage = Message(messageText, this.userName, Instant.now().toString())
                val sendMessageDisposable =
                    stompClient.send("/app/room/${this.roomCode}", gson.toJson(sentMessage))
                        .subscribe({
                            sendMessageText.text.clear()
                        }, {
                            Toast.makeText(
                                activity,
                                "Error sending message",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                compositeDisposable.add(sendMessageDisposable)
            }
        }
        connectToWebSocket()

        return view
    }

    private fun connectToWebSocket() {
        stompClient.connect()
        val lifecycleDisposable: Disposable =
            stompClient.lifecycle().subscribe { lifecycleEvent: LifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.i("Lifecycle event", "Stomp connection opened")
                        Log.i("Lifecycle event", "Message received: " + lifecycleEvent.message)
                    }
                    LifecycleEvent.Type.ERROR -> Log.e(
                        "Lifecycle event",
                        "Error",
                        lifecycleEvent.exception
                    )
                    LifecycleEvent.Type.CLOSED -> Log.i(
                        "Lifecycle event",
                        "Stomp connection closed"
                    )
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> Log.w(
                        "Lifecycle event",
                        "Stomp connection failed heartbeat"
                    )
                    else -> Log.e("Lifecycle event", "Error!")
                }
            }
        compositeDisposable.add(lifecycleDisposable)

        val topicDisposable: Disposable = stompClient.topic("/topic/room/${this.roomCode}")
            .subscribe { topicMessage: StompMessage ->
                val receivedMessage = gson.fromJson(topicMessage.payload, Message::class.java)
                requireActivity().runOnUiThread { // FIXME
                    messageListAdapter.addMessage(receivedMessage)
                }
            }
        compositeDisposable.add(topicDisposable)
    }

    override fun onDestroy() {
        stompClient.disconnect()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(roomCode: String, userName: String) =
//            ChatFragment(userName).apply {
//                arguments = Bundle().apply {
//                    putInt(ARG_COLUMN_COUNT, columnCount)
//                }
//            }
            RoomChatFragment(roomCode, userName)
    }
}