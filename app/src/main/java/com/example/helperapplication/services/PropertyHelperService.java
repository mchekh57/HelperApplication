package com.example.helperapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeMap;

public class PropertyHelperService extends Service {
    static Process process = null;
    static BufferedReader reader = null;
    public static  TreeMap<String, String> getProperties() {
        TreeMap<String, String> propertyMap = new TreeMap<>();
        try {
            process = Runtime.getRuntime().exec("getprop");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("]: \\[");
                if (parts.length == 2) {
                    String key = parts[0].replace("[", "").trim();
                    String value = parts[1].replace("]", "").trim();
                    propertyMap.put(key, value);
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e("PropertyHelperService", "Error fetching properties", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return propertyMap;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}