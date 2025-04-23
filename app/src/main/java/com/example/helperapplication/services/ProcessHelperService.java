package com.example.helperapplication.services;

public class ProcessHelperService {
    static {
        System.loadLibrary("process_helper_lib");
    }

    public native String getProcessesStats();
}
