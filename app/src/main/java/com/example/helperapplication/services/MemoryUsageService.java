package com.example.helperapplication.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MemoryUsageService extends Service {
    public static String getMemoryInfo(Context context) {
        StringBuilder memInfo = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                memInfo.append(line).append("\n");
            }
        } catch (IOException e) {
            memInfo.append("Could not read /proc/meminfo: ").append(e.getMessage());
            memInfo.append(getMemoryInfoAlternative(context));
        }
        return memInfo.toString();
    }
    private static String getMemoryInfoAlternative(Context context) {
        StringBuilder memoryInfo = new StringBuilder();
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfoObj = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfoObj);

            memoryInfo.append("MemTotal: ").append(memoryInfoObj.totalMem / 1024).append(" kB\n")
                    .append("MemAvailable: ").append(memoryInfoObj.availMem / 1024).append(" kB\n")
                    .append("MemoryLow: ").append(memoryInfoObj.lowMemory ? "Yes" : "No").append("\n");
        } catch (Exception e) {
            memoryInfo.append("Failed to get memory information: ").append(e.getMessage());
        }
        return memoryInfo.toString();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}