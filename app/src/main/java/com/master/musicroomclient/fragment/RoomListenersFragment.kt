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

class RoomListenersFragment : Fragment() {

    private lateinit var listeners: List<Listener>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getParcelableArrayList<Listener>(ARG_LISTENERS)?.also { listeners = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_room_listeners, container, false)

        if (view is RecyclerView && this::listeners.isInitialized) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ListenerListAdapter(listeners)
            }
        }
        return view
    }

    companion object {
        private const val ARG_LISTENERS = "listeners"

        @JvmStatic
        fun newInstance(listeners: List<Listener>) =
            RoomListenersFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_LISTENERS, ArrayList(listeners))
                }
            }
    }
}