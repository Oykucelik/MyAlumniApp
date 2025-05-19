package com.example.alumniapp.ui.alumni

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentAlumniListBinding
import com.example.alumniapp.models.Alumni
import com.example.alumniapp.utils.Resource

class AlumniListFragment : Fragment() {

    private var _binding: FragmentAlumniListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AlumniViewModel
    private lateinit var alumniAdapter: AlumniAdapter
    private var alumniList = listOf<Alumni>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlumniListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        setupSearch()
        
        viewModel.fetchAlumni()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AlumniViewModel::class.java]
        
        viewModel.alumniData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showLoading(false)
                    alumniList = resource.data
                    alumniAdapter.submitList(alumniList)
                    showEmptyView(alumniList.isEmpty())
                }
                is Resource.Loading -> {
                    showLoading(true)
                    showEmptyView(false)
                }
                is Resource.Error -> {
                    showLoading(false)
                    showEmptyView(true)
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupRecyclerView() {
        alumniAdapter = AlumniAdapter { alumni ->
            navigateToAlumniDetail(alumni.id)
        }
        
        binding.alumniRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alumniAdapter
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
        
        binding.searchBackButton.setOnClickListener {
            binding.searchEditText.text.clear()
            binding.searchEditText.clearFocus()
        }
    }
    
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAlumni(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun filterAlumni(query: String) {
        if (query.isEmpty()) {
            alumniAdapter.submitList(alumniList)
            showEmptyView(alumniList.isEmpty())
            return
        }
        
        val filteredList = alumniList.filter { alumni ->
            val fullName = "${alumni.firstName} ${alumni.lastName}".lowercase()
            fullName.contains(query.lowercase())
        }
        
        alumniAdapter.submitList(filteredList)
        showEmptyView(filteredList.isEmpty())
    }
    
    private fun showFilterDialog() {
        Toast.makeText(requireContext(), "Filter options will be implemented", Toast.LENGTH_SHORT).show()
        // TODO: Implement filter dialog
    }
    
    private fun navigateToAlumniDetail(alumniId: String) {
        val action = AlumniListFragmentDirections.actionAlumniListFragmentToAlumniDetailFragment(alumniId)
        findNavController().navigate(action)
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyView(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 