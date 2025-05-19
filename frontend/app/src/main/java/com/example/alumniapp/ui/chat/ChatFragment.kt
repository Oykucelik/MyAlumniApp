package com.example.alumniapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alumniapp.R
import com.example.alumniapp.data.model.Chat
import com.example.alumniapp.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadDummyChats()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { chat ->
            // Navigate to ChatDetailFragment
            val action = ChatFragmentDirections.actionChatFragmentToChatDetailFragment(chat.id)
            findNavController().navigate(action)
            // Toast.makeText(requireContext(), "Clicked on ${chat.userName}", Toast.LENGTH_SHORT).show()
        }
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadDummyChats() {
        val dummyChats = listOf(
            Chat("1", "Alice Wonderland", "Hey, how are you?", "10:00 AM"),
            Chat("2", "Bob The Builder", "Can we fix it? Yes we can!", "11:30 AM"),
            Chat("3", "Charlie Brown", "Good grief!", "Yesterday"),
            Chat("4", "Diana Prince", "Wondering about the project.", "Mon"),
            Chat("5", "Edward Scissorhands", "Just finished trimming the hedges.", "Sun")
        )
        chatAdapter.submitList(dummyChats)

        if (dummyChats.isEmpty()) {
            binding.chatRecyclerView.visibility = View.GONE
            binding.emptyChatsText.visibility = View.VISIBLE
        } else {
            binding.chatRecyclerView.visibility = View.VISIBLE
            binding.emptyChatsText.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}