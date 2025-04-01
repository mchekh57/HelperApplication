package com.example.helperapplication.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.NonNull;
import java.util.Collections;

public class CameraStreamService extends Service {
    private static final String TAG = "CameraStreamService";
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CameraDevice.StateCallback stateCallback;
    private CameraManager cameraManager;
    private Surface previewSurface;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public void startCameraStream(String cameraId, SurfaceTexture texture) {
        try {
            texture.setDefaultBufferSize(1280, 720);
            previewSurface = new Surface(texture);
            stateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    try {
                        camera.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                captureSession = session;
                                try {
                                    CaptureRequest.Builder captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    captureRequest.addTarget(previewSurface);
                                    session.setRepeatingRequest(captureRequest.build(), null, null);
                                } catch (CameraAccessException e) {
                                    Log.e(TAG, "Capture session error", e);
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Log.e(TAG, "Capture session configure failed");
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Capture access error", e);
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Capture error" + error);
                    camera.close();
                }
            };
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (Exception e){
            Log.e(TAG, "Capture access error", e);
        }
    }
    public void stopCameraStream(){
        if (captureSession != null){
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public String[] getCameraIdList() {
        try {
            return cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return new String[]{};
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCameraStream();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class LocalBinder extends Binder {
        public CameraStreamService getService(){
            return CameraStreamService.this;
        }
    }
}