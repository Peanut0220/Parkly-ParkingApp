package com.example.parkly.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.parkly.R
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentMyProfileBinding
import com.example.parkly.util.toBitmap
import com.example.parkly.profile.tab.TabRecordFragment
import com.example.parkly.profile.tab.TabVehicleFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.getstream.avatarview.coil.loadImage

class MyProfileFragment : Fragment() {

    companion object {
        fun newInstance() = MyProfileFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentMyProfileBinding
    private var tabItems = arrayOf(
        "My Vehicle",
        "My Record"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)


binding.btnAddVehicle.setOnClickListener{
    findNavController().navigate(
        R.id.action_profileFragment_to_addVehicleFragment
    )
}
        userVM.getUserLD().observe(viewLifecycleOwner) { user ->
            val avatar =
                if (user.avatar.toBytes().isEmpty())
                    R.drawable.round_account_circle_24
                else
                    user.avatar.toBitmap()

            binding.txtName.text = user.name
            binding.avatarView.loadImage(avatar)

            if (userVM.isVerified()) {
                binding.btnVerify.visibility = View.GONE
            } else {
                binding.btnVerify.visibility = View.VISIBLE
            }


            val adapter =
                ViewPagerAdapter(
                    requireActivity().supportFragmentManager,
                    lifecycle,
                    tabItems
                )
            binding.viewPager.adapter = adapter
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = tabItems[position]
            }.attach()


            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> {
                          binding.btnAddVehicle.visibility= VISIBLE
                        }
                        1 -> {
                            binding.btnAddVehicle.visibility= INVISIBLE
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // Optional: Handle any actions when the tab is unselected
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // Optional: Handle any actions when the tab is reselected
                }
            })
        }




        binding.btnVerify.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_emailVerificationFragment) }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.setting -> {
                    findNavController().navigate(R.id.action_profileFragment_to_settingFragment)
                    true
                }

                else -> false
            }
        }


        return binding.root
    }

    class ViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val items: Array<String>
    ) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return items.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (items[position]) {
                "My Vehicle" -> TabVehicleFragment()
                "My Record" -> TabRecordFragment()
                else -> throw IllegalArgumentException("Invalid tab item: ${items[position]}")
            }
        }
    }


}