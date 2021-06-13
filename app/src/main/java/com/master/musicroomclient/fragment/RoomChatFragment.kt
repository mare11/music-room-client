package com.master.musicroomclient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.MessageListAdapter
import com.master.musicroomclient.model.Message
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import com.master.musicroomclient.utils.SnackBarUtils
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.dto.StompMessage
import java.time.Instant

class RoomChatFragment : Fragment() {

    private lateinit var roomCode: String
    private lateinit var userName: String
    private lateinit var messageView: RecyclerView
    private lateinit var messageListAdapter: MessageListAdapter

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        arguments?.let { bundle ->
            bundle.getString(ARG_ROOM_CODE)?.also { roomCode = it }
            bundle.getString(ARG_USER_NAME)?.also { userName = it }
        }

        if (!this::roomCode.isInitialized || !this::userName.isInitialized) {
            // TODO: show some error message
            return view
        }

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.stackFromEnd = true
        messageView = view.findViewById(R.id.message_view)
        messageView.layoutManager = linearLayoutManager
        this.messageListAdapter = MessageListAdapter(this.userName)
        messageView.adapter = messageListAdapter

        val sendMessageText = view.findViewById<EditText>(R.id.send_message_text)
        val sendMessageButton = view.findViewById<Button>(R.id.send_message_button)
        sendMessageButton.setOnClickListener {
            val messageText = sendMessageText.text.toString()
            if (messageText.isNotBlank()) {
                val sentMessage = Message(messageText, this.userName, Instant.now().toString())
                val sendMessageDisposable = musicRoomStompClient.send(
                    "/app/room/${this.roomCode}/chat",
                    gson.toJson(sentMessage)
                ).subscribe({ sendMessageText.text.clear() },
                    {
                        SnackBarUtils.showSnackBar(
                            requireView().findViewById(android.R.id.content),
                            "Error sending message"
                        )
                    })
                compositeDisposable.add(sendMessageDisposable)
            }
        }
        connectToChatTopic()

        return view
    }

    private fun connectToChatTopic() {
        val topicDisposable = musicRoomStompClient.topic("/topic/room/${this.roomCode}/chat")
            .subscribe { topicMessage: StompMessage ->
                val receivedMessage = gson.fromJson(topicMessage.payload, Message::class.java)
                requireActivity().runOnUiThread {
                    messageListAdapter.addMessage(receivedMessage)
                }
            }
        compositeDisposable.add(topicDisposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        private const val ARG_ROOM_CODE = "roomCode"
        private const val ARG_USER_NAME = "userName"

        @JvmStatic
        fun newInstance(roomCode: String, userName: String) =
            RoomChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROOM_CODE, roomCode)
                    putString(ARG_USER_NAME, userName)
                }
            }
    }
}