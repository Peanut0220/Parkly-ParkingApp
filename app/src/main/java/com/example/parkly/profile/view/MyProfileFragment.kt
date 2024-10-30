package com.example.parkly.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.example.parkly.profile.tab.TabMyJobFragment
import com.example.parkly.profile.tab.TabMyPostListFragment
import com.google.android.material.tabs.TabLayoutMediator
import io.getstream.avatarview.coil.loadImage

class MyProfileFragment : Fragment() {

    companion object {
        fun newInstance() = MyProfileFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentMyProfileBinding
    private var tabItems = arrayOf(
        "My Job",
        "My Post"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)

        userVM.getUserLD().observe(viewLifecycleOwner) { user ->
            val avatar =
                if (user.avatar.toBytes().isEmpty())
                    R.drawable.round_account_circle_24
                else
                    user.avatar.toBitmap()

            binding.txtName.text = user.name
            binding.avatarView.loadImage(avatar)
            if (userVM.isCompanyRegistered()) {
                binding.avatarView.indicatorEnabled = true
            }
            if (userVM.isVerified()) {
                binding.btnVerify.visibility = View.GONE
            } else {
                binding.btnVerify.visibility = View.VISIBLE
            }


            val adapter =
                ViewPagerAdapter(
                    requireActivity().supportFragmentManager,
                    lifecycle,
                    if (userVM.isEnterprise()) arrayOf("My Post") else tabItems
                )
            binding.viewPager.adapter = adapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = if (userVM.isEnterprise()) "My Post" else tabItems[position]
            }.attach()
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
                "My Job" -> TabMyJobFragment()
                "My Post" -> TabMyPostListFragment()
                else -> throw IllegalArgumentException("Invalid tab item: ${items[position]}")
            }
        }
    }


}