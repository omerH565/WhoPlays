package com.example.whoplays.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whoplays.R
import com.example.whoplays.ui.adapters.MessageAdapter
import com.example.whoplays.viewmodels.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : BaseActivity() {

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var gameId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        gameId = intent.getStringExtra("EXTRA_GAME_ID") ?: ""
        val sportType = intent.getStringExtra("EXTRA_SPORT_TYPE") ?: "Chat"
        
        setupToolbar("$sportType Group", showBackButton = true)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val rvMessages = findViewById<RecyclerView>(R.id.rvMessages)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)

        messageAdapter = MessageAdapter(emptyList(), currentUserId)
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        chatViewModel.getMessages(gameId).observe(this) { messages ->
            messageAdapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                rvMessages.post {
                    rvMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(gameId, text)
                etMessage.setText("")
            }
        }
    }
}
