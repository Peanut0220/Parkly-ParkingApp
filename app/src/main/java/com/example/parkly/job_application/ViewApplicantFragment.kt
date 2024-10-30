package com.example.parkly.job_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.databinding.FragmentViewApplicantBinding
import com.example.parkly.util.JobApplicationState

class ViewApplicantFragment : Fragment() {

    companion object {
        fun newInstance() = ViewApplicantFragment()
    }

    private lateinit var binding: FragmentViewApplicantBinding
    private val jobID by lazy { arguments?.getString("jobID") ?: "" }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_view_applicant, container, false)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = MyPagerAdapter(childFragmentManager, jobID)
        binding.tabContent.adapter = adapter
        binding.tab.setupWithViewPager(binding.tabContent)
    }

    class MyPagerAdapter(fm: FragmentManager, id: String) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        val id = id
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TabApplicantFragment(id,JobApplicationState.NEW)
                1 -> TabApplicantFragment(id,JobApplicationState.ACCEPTED)
                2 -> TabApplicantFragment(id,JobApplicationState.REJECTED)
                else -> throw Exception()
            }
        }

        override fun getCount(): Int {
            return 3  // Number of tabs
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "New"
                1 -> "Accepted"
                2 -> "Rejected"
                else -> null
            }
        }
    }
}