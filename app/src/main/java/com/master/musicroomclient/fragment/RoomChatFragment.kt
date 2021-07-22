package com.master.musicroomclient.fragment

import android.app.Activity.RESULT_CANCELED
import android.content.Context
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
import com.master.musicroomclient.adapter.TabAdapter.Companion.TabIndexes.ROOM_CHAT
import com.master.musicroomclient.model.Message
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import com.master.musicroomclient.utils.Constants.ARG_ROOM_CODE
import com.master.musicroomclient.utils.Constants.ARG_USER_NAME
import com.master.musicroomclient.utils.SnackBarUtils.showSnackBar
import com.master.musicroomclient.utils.TabLayoutBadgeListener
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.dto.StompMessage
import java.time.Instant

class RoomChatFragment : Fragment() {

    private lateinit var roomCode: String
    private lateinit var userName: String
    private lateinit var messageView: RecyclerView
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var tabListener: TabLayoutBadgeListener

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getString(ARG_ROOM_CODE)?.also { roomCode = it }
            bundle.getString(ARG_USER_NAME)?.also { userName = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        if (!this::roomCode.isInitialized || !this::userName.isInitialized) {
            showSnackBar(view, "View could not be loaded")
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
                        showSnackBar(
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = requireActivity()
        if (activity is TabLayoutBadgeListener) {
            tabListener = activity
        } else {
            activity.setResult(RESULT_CANCELED)
            activity.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        tabListener.onTabResume(ROOM_CHAT.ordinal)
    }

    private fun connectToChatTopic() {
        val topicDisposable = musicRoomStompClient.topic("/topic/room/${this.roomCode}/chat")
            .subscribe { topicMessage: StompMessage ->
                val receivedMessage = gson.fromJson(topicMessage.payload, Message::class.java)
                requireActivity().runOnUiThread {
                    messageListAdapter.addMessage(receivedMessage)
                    tabListener.onTabEvent(ROOM_CHAT.ordinal)
                    messageView.scrollToPosition(messageListAdapter.itemCount - 1)
                }
            }
        compositeDisposable.add(topicDisposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
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