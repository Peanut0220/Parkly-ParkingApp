package com.example.parkly.reservation.view

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
import com.example.parkly.databinding.FragmentEventBinding
import com.example.parkly.interview.TabHistoryInterviewFragment
import com.example.parkly.interview.TabUpcomingInterviewFragment
import com.example.parkly.reseration.view.TabPendingReservationFragment

class EventFragment : Fragment() {

    companion object {
        fun newInstance() = EventFragment()
    }

    private lateinit var binding: FragmentEventBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = MyPagerAdapter(childFragmentManager)
        binding.tabContent.adapter = adapter
        binding.tab.setupWithViewPager(binding.tabContent)
        binding.btnAddReservation.setOnClickListener{
            findNavController().navigate(
                R.id.action_eventFragment_to_addReservationFragment
            )
        }
    }

    class MyPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TabPendingReservationFragment.newInstance("Pending", "VIEW")
                1 -> TabPendingReservationFragment.newInstance("Accepted", "VIEW")
                2 -> TabPendingReservationFragment.newInstance("History", "VIEW")
                else -> throw Exception()
            }
        }

        override fun getCount(): Int {
            return 3  // Number of tabs
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Pending"
                1 -> "Accepted"
                2 -> "History"
                else -> null
            }
        }
    }

}