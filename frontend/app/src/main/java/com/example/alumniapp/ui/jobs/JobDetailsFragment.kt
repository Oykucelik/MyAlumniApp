package com.example.alumniapp.ui.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alumniapp.databinding.FragmentJobDetailsBinding
import com.example.alumniapp.data.model.Job // Ensure this import is correct

class JobDetailsFragment : Fragment() {

    private var _binding: FragmentJobDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: JobDetailsFragmentArgs by navArgs()
    private lateinit var currentJob: Job

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentJob = args.job

        setupToolbar()
        displayJobDetails()
        setupApplyButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun displayJobDetails() {
        binding.detailJobTitle.text = currentJob.title
        binding.detailJobCompany.text = currentJob.company
        binding.detailJobLocation.text = currentJob.location
        binding.detailJobDescription.text = currentJob.description
        updateApplyButtonState()
    }

    private fun setupApplyButton() {
        binding.detailApplyButton.setOnClickListener {
            currentJob.isApplied = !currentJob.isApplied // Toggle state
            updateApplyButtonState()
            // Pass the updated job back to JobsFragment
            val result = Bundle().apply {
                putParcelable("updatedJob", currentJob)
            }
            parentFragmentManager.setFragmentResult("jobDetailsResult", result)
        }
    }

    private fun updateApplyButtonState() {
        if (currentJob.isApplied) {
            binding.detailApplyButton.visibility = View.GONE
            binding.detailAppliedBadge.visibility = View.VISIBLE
        } else {
            binding.detailApplyButton.visibility = View.VISIBLE
            binding.detailAppliedBadge.visibility = View.GONE
        }
        // Also update the result when the state is first loaded
        val result = Bundle().apply {
             putParcelable("updatedJob", currentJob)
        }
        parentFragmentManager.setFragmentResult("jobDetailsResult", result)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 