package com.example.parkly.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.databinding.FragmentPdfViewerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

class PdfViewerFragment : Fragment() {

    lateinit var binding: FragmentPdfViewerBinding
    val nav by lazy { findNavController() }
    private val fileName by lazy { arguments?.getString("fileName") ?: "" }
    private val url by lazy { arguments?.getString("url") ?: "" }
    lateinit var  downloadManager: DownloadManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        binding.topAppBar.title = fileName
        binding.topAppBar.setOnClickListener {
            nav.popBackStack()
        }

        downloadManager =
            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream = URL(url).openStream()
            withContext(Dispatchers.Main) {
                binding.PDFView.fromStream(inputStream).onRender { pages, _, _ ->
                    if (pages >= 1) {
                        binding.prograss.visibility = View.GONE
                    }
                }.load()
            }
        }

        binding.floatingActionButton.setOnClickListener { download() }
        return binding.root
    }

    private fun download() {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setMimeType("application/pdf")

            downloadManager.enqueue(request)
        } catch (e: Exception) {
            toast(e.message.toString())
        }
    }


}