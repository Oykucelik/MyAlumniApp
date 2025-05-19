package com.example.alumniapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alumniapp.data.model.Message
import com.example.alumniapp.databinding.FragmentChatDetailBinding
import androidx.navigation.fragment.navArgs // For receiving arguments

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val args: ChatDetailFragmentArgs by navArgs() // For receiving arguments

    private lateinit var chatId: String // Use lateinit now

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatId = args.chatId // Assign chatId from args

        setupRecyclerView()
        loadDummyMessages(chatId)

        binding.sendMessageButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        binding.messagesRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // To show latest messages at the bottom
            }
        }
    }

    private fun loadDummyMessages(currentChatId: String) {
        // Later, filter messages by currentChatId when fetching from API/repository
        val allMessages = listOf(
            Message("m1", "1", "user1", "Hello Alice!", System.currentTimeMillis() - 500000, false),
            Message("m2", "1", "currentUser", "Hi Bob! How are you?", System.currentTimeMillis() - 400000, true),
            Message("m3", "1", "user1", "I'm good, thanks! Working on the new project.", System.currentTimeMillis() - 300000, false),
            Message("m4", "2", "user2", "Let's build something amazing!", System.currentTimeMillis() - 600000, false),
            Message("m5", "2", "currentUser", "Sure, what do you have in mind?", System.currentTimeMillis() - 550000, true),
            Message("m6", "3", "user3", "Oh, good grief!", System.currentTimeMillis() - 700000, false),
            Message("m7", args.chatId, "user_placeholder", "This is for the selected chat.", System.currentTimeMillis() - 200000, false),
            Message("m8", args.chatId, "currentUser", "Replying in selected chat!", System.currentTimeMillis() - 100000, true)
        )
        // Filter messages for the current chat ID for this dummy implementation
        val chatMessages = allMessages.filter { it.chatId == currentChatId }
        messageAdapter.submitList(chatMessages)
        binding.messagesRecyclerView.scrollToPosition(messageAdapter.itemCount -1)
    }

    private fun sendMessage() {
        val text = binding.messageEditText.text.toString().trim()
        if (text.isNotEmpty()) {
            // For dummy implementation, just add to the adapter
            // In a real app, this would go to a ViewModel and then to a repository/API
            val newMessage = Message(
                id = "msg${System.currentTimeMillis()}",
                chatId = args.chatId, // Use chatId from args
                senderId = "currentUser", // Assume current user is sending
                text = text,
                timestamp = System.currentTimeMillis(),
                isSentByUser = true
            )
            
            val currentMessages = messageAdapter.currentList.toMutableList()
            currentMessages.add(newMessage)
            messageAdapter.submitList(currentMessages) {
                binding.messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }
            binding.messageEditText.text.clear()
        } else {
            Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 