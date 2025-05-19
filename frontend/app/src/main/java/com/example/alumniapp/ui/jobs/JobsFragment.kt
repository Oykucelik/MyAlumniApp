package com.example.alumniapp.ui.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alumniapp.R
import com.example.alumniapp.data.model.Job
import com.example.alumniapp.databinding.FragmentJobsBinding

class JobsFragment : Fragment() {

    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!

    private lateinit var jobAdapter: JobAdapter
    // Store jobs in a way that their state (isApplied) can be mutated and preserved
    private val jobsList = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        if (jobsList.isEmpty()) { // Load dummy jobs only if the list is empty
            loadDummyJobs()
        }
        // Use FragmentResultListener to get updates from JobDetailsFragment if needed
        parentFragmentManager.setFragmentResultListener("jobDetailsResult", viewLifecycleOwner) { _, bundle ->
            val updatedJob = bundle.getParcelable<Job>("updatedJob")
            updatedJob?.let { jobDetail ->
                val index = jobsList.indexOfFirst { it.id == jobDetail.id }
                if (index != -1) {
                    jobsList[index] = jobDetail
                    jobAdapter.submitList(jobsList.toList()) // Create a new list for DiffUtil
                    jobAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            onApplyClicked = { job ->
                // The state is already updated in the adapter, just need to ensure the list reflects it
                val index = jobsList.indexOfFirst { it.id == job.id }
                if (index != -1) {
                    jobsList[index] = job
                    // No need to submitList here as adapter's notifyItemChanged handles UI update for the item
                }
            },
            onItemClicked = { job ->
                val action = JobsFragmentDirections.actionJobsFragmentToJobDetailsFragment(job)
                findNavController().navigate(action)
            }
        )
        binding.jobsRecyclerView.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadDummyJobs() {
        jobsList.clear()
        jobsList.addAll(
            listOf(
                Job("1", "Software Engineer", "Google", "Mountain View, CA", "Description for SE at Google", false),
                Job("2", "Product Manager", "Facebook", "Menlo Park, CA", "Description for PM at Facebook", true),
                Job("3", "UX Designer", "Apple", "Cupertino, CA", "Description for UXD at Apple", false),
                Job("4", "Data Scientist", "Amazon", "Seattle, WA", "Description for DS at Amazon", false),
                Job("5", "Frontend Developer", "Netflix", "Los Gatos, CA", "Description for FED at Netflix", true)
            )
        )
        jobAdapter.submitList(jobsList.toList()) // Submit a new list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 