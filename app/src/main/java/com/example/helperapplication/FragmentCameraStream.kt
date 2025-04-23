package com.example.helperapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.view.TextureView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helperapplication.services.CameraStreamService
import android.Manifest
import androidx.core.content.ContextCompat

class FragmentCameraStream : Fragment() {
    private var cameraService: CameraStreamService? = null
    private var isBound = false
    private lateinit var textureView: TextureView
    private lateinit var cameraSpinner: Spinner
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private val CAMERA_PERMISSION_CODE = 101

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CameraStreamService.LocalBinder
            cameraService = binder.getService()
            isBound = true
            loadCameraList()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            cameraService = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureView = view.findViewById(R.id.textureView)
        cameraSpinner = view.findViewById(R.id.cameraSelectorSpinner)
        startButton = view.findViewById(R.id.startStreamButton)
        stopButton = view.findViewById(R.id.stopStreamButton)

        startButton.setOnClickListener {
            checkCameraPermissionAndStartStream()
        }

        stopButton.setOnClickListener {
            if (isBound) {
                cameraService?.stopCameraStream()
            }
        }
    }

    private fun checkCameraPermissionAndStartStream() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startCameraStream()
        }
    }

    private fun startCameraStream() {
        val selectedCameraId = cameraSpinner.selectedItem?.toString()
        if (isBound && textureView.isAvailable) {
            cameraService?.startCameraStream(selectedCameraId, textureView.surfaceTexture)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraStream()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), CameraStreamService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireContext().unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun loadCameraList() {
        if (!isBound || cameraService == null) {
            return
        }
        val cameraList = cameraService?.getCameraIdList()
        if (cameraList.isNullOrEmpty()) {
            return
        }
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cameraList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cameraSpinner.adapter = adapter
    }
}