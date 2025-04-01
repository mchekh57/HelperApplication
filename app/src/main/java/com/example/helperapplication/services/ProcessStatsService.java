package com.example.helperapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessStatsService extends Service {
    public static String getRunningProcessesStats(){
        try {
            Process process = Runtime.getRuntime().exec("ps -A -o PID,STAT,%CPU,%MEM,CMD");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            return output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching CPU usage";
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
