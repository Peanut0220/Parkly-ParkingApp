package com.example.parkly.profile.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.parkly.R
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentUserProfileBinding
import com.example.parkly.profile.tab.TabMyPostListFragment
import com.example.parkly.util.createChatroom
import com.example.parkly.util.isChatRoomExist
import com.example.parkly.util.message
import com.example.parkly.util.toBitmap
import com.google.android.material.tabs.TabLayoutMediator
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment() {

    companion object {
        fun newInstance() = UserProfileFragment()
    }

    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentUserProfileBinding
    private val tabItems = arrayOf(
        "Post"
    )
    private val userID by lazy { requireArguments().getString("userID", "") }
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        userVM.getUserLLD().observe(viewLifecycleOwner) { userList ->
            val user = userList.find { it.uid == userID } ?: return@observe
            user?.let {
                val avatar =
                    if (it.avatar.toBytes().isEmpty())
                        R.drawable.round_account_circle_24
                    else
                        it.avatar.toBitmap()
                if (user.isEnterprise && user.company_id != "") {
                    binding.avatarView.indicatorEnabled = true
                }
                binding.txtName.text = it.name
                binding.avatarView.loadImage(avatar)

            }

            binding.btnMessage.setOnClickListener {
                val userId = userVM.getAuth().uid
                val otherId = user.uid
                var chatRoomId = userId + "_" + otherId
                CoroutineScope(Dispatchers.Main).launch {
                    val isExist = withContext(Dispatchers.IO) {
                        isChatRoomExist(chatRoomId)
                    }

                    if (!isExist) {
                        chatRoomId = otherId + "_" + userId
                        createChatroom(chatRoomId)
                    }
                    message(chatRoomId, nav)
                }

            }

        }




        binding.topAppBar.setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle, userID)
        binding.viewPager.adapter = adapter


        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabItems[position]
        }.attach()
        return binding.root
    }

    class ViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val userID: String
    ) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return 1
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return TabMyPostListFragment(userID)
                else -> throw Exception()
            }
        }
    }
}
