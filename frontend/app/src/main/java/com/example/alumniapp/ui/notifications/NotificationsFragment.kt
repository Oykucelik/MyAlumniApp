package com.example.alumniapp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {
    
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationAdapter: NotificationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadNotifications()
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.notificationsRecyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(context)
            // Add item decoration for spacing if needed
            // addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }
    
    private fun loadNotifications() {
        val sampleNotifications = listOf(
            NotificationItem(
                id = "1",
                avatarText = "E",
                header = "New Event: Annual Gala",
                subhead = "Join us for an evening of celebration and networking on Dec 15th.",
                isRead = false
            ),
            NotificationItem(
                id = "2",
                avatarText = "J",
                header = "Job Alert: Software Engineer",
                subhead = "Tech Solutions Inc. is hiring. Fits your profile.",
                isRead = true
            ),
            NotificationItem(
                id = "3",
                avatarText = "C",
                header = "New Connection Request",
                subhead = "Sarah Miller wants to connect with you.",
                isRead = false
            ),
            NotificationItem(
                id = "4",
                avatarText = "M",
                header = "Message from John Doe",
                subhead = "Check out this interesting article...",
                isRead = true
            ),
            NotificationItem(
                id = "5",
                avatarText = "A",
                header = "Alumni Chapter Meeting",
                subhead = "Your local chapter is meeting next week. Don't miss out!",
                isRead = false
            )
        )

        if (sampleNotifications.isEmpty()) {
            binding.notificationsRecyclerView.visibility = View.GONE
            binding.emptyNotificationsText.visibility = View.VISIBLE
        } else {
            binding.notificationsRecyclerView.visibility = View.VISIBLE
            binding.emptyNotificationsText.visibility = View.GONE
            notificationAdapter.submitList(sampleNotifications)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 