package com.example.alumniapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alumniapp.R
import com.example.alumniapp.adapter.PostAdapter
import com.example.alumniapp.model.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var fabAddPost: FloatingActionButton
    private val postsList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts)
        fabAddPost = view.findViewById(R.id.fabAddPost)

        setupRecyclerView()
        loadDummyPosts()

        fabAddPost.setOnClickListener {
            // Handle FAB click - e.g., navigate to a create post screen
            Toast.makeText(context, "Add new post clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(postsList)
        recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun loadDummyPosts() {
        // Create some dummy posts
        // In a real app, you would fetch this data from a ViewModel, repository, or API
        postsList.add(
            Post(
                id = "1",
                username = "Alice Wonderland",
                location = "New York, USA",
                avatarUrl = null, // Or a URL to an image
                imageUrl = "placeholder_image_url_1", // Replace with actual image URLs or resource IDs
                description = "Having a great time exploring the city! Loving the views and the food. #travel #nyc"
            )
        )
        postsList.add(
            Post(
                id = "2",
                username = "Bob The Builder",
                location = "London, UK",
                avatarUrl = null,
                imageUrl = "placeholder_image_url_2",
                description = "Just finished a new project. It was challenging but rewarding. #construction #architecture"
            )
        )
        postsList.add(
            Post(
                id = "3",
                username = "Charlie Brown",
                location = "Springfield, USA",
                avatarUrl = null,
                imageUrl = "placeholder_image_url_3",
                description = "Good grief! Another beautiful day. Spending time with Snoopy. #comics #classic #doglover"
            )
        )

        postAdapter.notifyDataSetChanged() 
    }
}
