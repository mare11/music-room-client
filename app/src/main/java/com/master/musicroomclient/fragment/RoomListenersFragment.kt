package com.master.musicroomclient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.musicroomclient.R
import com.master.musicroomclient.adapter.ListenerListAdapter
import com.master.musicroomclient.model.Listener
import com.master.musicroomclient.utils.ApiUtils.gson
import com.master.musicroomclient.utils.ApiUtils.musicRoomStompClient
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.dto.StompMessage

class RoomListenersFragment : Fragment() {

    private lateinit var roomCode: String
    private lateinit var listeners: List<Listener>
    private lateinit var adapter: ListenerListAdapter
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getString(ARG_ROOM_CODE)?.also { roomCode = it }
            bundle.getParcelableArrayList<Listener>(ARG_LISTENERS)?.also { listeners = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_room_listeners, container, false)

        if (!this::roomCode.isInitialized || !this::listeners.isInitialized) {
            // TODO: show some error message
            return view
        }
        adapter = ListenerListAdapter(listeners.toMutableList())
        if (view is RecyclerView && this::listeners.isInitialized) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@RoomListenersFragment.adapter
            }
        }
        connectToListenerTopics()
        return view
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun connectToListenerTopics() {
        val connectTopicDisposable =
            musicRoomStompClient.topic("/topic/room/$roomCode/listener/connect")
                .subscribe { topicMessage: StompMessage ->
                    val newListener = gson.fromJson(topicMessage.payload, Listener::class.java)
                    requireActivity().runOnUiThread {
                        adapter.addListener(newListener)
                        println("Listener '${newListener.name}' connected at '${newListener.connectedAt}'!")
                    }
                }
        compositeDisposable.add(connectTopicDisposable)

        val disconnectTopicDisposable =
            musicRoomStompClient.topic("/topic/room/$roomCode/listener/disconnect")
                .subscribe { topicMessage: StompMessage ->
                    val listenerName = topicMessage.payload
                    requireActivity().runOnUiThread {
                        adapter.removeListener(listenerName)
                        println("Listener '$listenerName' disconnected!")
                    }
                }
        compositeDisposable.add(disconnectTopicDisposable)
    }

    companion object {
        private const val ARG_ROOM_CODE = "roomCode"
        private const val ARG_LISTENERS = "listeners"

        @JvmStatic
        fun newInstance(roomCode: String, listeners: List<Listener>) =
            RoomListenersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROOM_CODE, roomCode)
                    putParcelableArrayList(ARG_LISTENERS, ArrayList(listeners))
                }
            }
    }
}