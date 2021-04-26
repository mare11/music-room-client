package com.master.musicroomclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.master.musicroomclient.activity.RoomActivity
import com.master.musicroomclient.utils.Constants.ROOM_CODE_EXTRA

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val roomCodeText = findViewById<EditText>(R.id.room_code)

        val newRoomButton = findViewById<Button>(R.id.button)

        newRoomButton.setOnClickListener {
//            ApiUtils.musicRoomApi.createRoom()
            val snackBar = Snackbar.make(
                window.decorView.rootView,
                "Not yet implemented!",
                Snackbar.LENGTH_SHORT
            )
            snackBar.setAction("Dismiss") { snackBar.dismiss() }
            snackBar.show()
        }

        val joinRoomButton = findViewById<Button>(R.id.button2)
        joinRoomButton.setOnClickListener {
            val roomActivityIntent = Intent(this, RoomActivity::class.java)
            roomActivityIntent.putExtra(ROOM_CODE_EXTRA, roomCodeText.text.toString())
            startActivity(roomActivityIntent)
        }

    }
}