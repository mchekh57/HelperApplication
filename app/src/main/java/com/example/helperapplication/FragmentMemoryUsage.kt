package com.example.helperapplication

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.helperapplication.services.MemoryUsageService
import com.example.helperapplication.services.ProcessStatsService

class FragmentMemoryUsage : Fragment(){
    private lateinit var memInfoTextView: TextView
    private lateinit var processStatsTextView: TextView
    private val handler = Handler()
    private var isUpdatingMemInfo = false
    private var isUpdatingProcessStats = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memory_usage, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        memInfoTextView = view.findViewById(R.id.memInfoTextView)
        processStatsTextView = view.findViewById(R.id.processStatsTextView)
        val memInfoStartButton: Button = view.findViewById(R.id.memInfoStartButton)
        val processStatsStartButton: Button = view.findViewById(R.id.processStatsStartButton)
        val stopButton: Button = view.findViewById(R.id.stopButton)
        setMemoryInfo()
        setProcessStatsInfo()

        memInfoStartButton.setOnClickListener {
            if (!isUpdatingMemInfo) {
                isUpdatingMemInfo = true
                updateInfo{
                    setMemoryInfo()
                }
            }
        }

        processStatsStartButton.setOnClickListener {
            if (!isUpdatingProcessStats) {
                isUpdatingProcessStats = true
                updateInfo{
                    setProcessStatsInfo()
                }
            }
        }

        stopButton.setOnClickListener {
            isUpdatingMemInfo = false
            isUpdatingProcessStats = false
            stopUpdating()
        }
    }
    private fun updateInfo(action: () -> Unit) {
            handler.postDelayed(object : Runnable {
                override fun run() {
                    action()
                    handler.postDelayed(this, 1000)
                }
            }, 0)
    }
    private fun stopUpdating() {
        handler.removeCallbacksAndMessages(null)
    }
    private fun setMemoryInfo() {
        memInfoTextView.text = MemoryUsageService.getMemoryInfo(requireContext())
    }
    private fun setProcessStatsInfo() {
        processStatsTextView.text = ProcessStatsService.getRunningProcessesStats()
    }
}