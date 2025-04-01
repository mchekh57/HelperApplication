package com.example.helperapplication.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import java.util.TreeMap;

public class SettingHelperService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static TreeMap<String, String> getSystemSettings(Context context){
        return getSettings(context, Settings.System.CONTENT_URI);
    }
    public static TreeMap<String, String> getGlobalSettings(Context context){
        return getSettings(context, Settings.Global.CONTENT_URI);
    }
    public static TreeMap<String, String> getSecureSettings(Context context){
        return getSettings(context, Settings.Secure.CONTENT_URI);
    }
    public static TreeMap<String, String> getSettings(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        return resultConverter(cursor);
    }
    private static TreeMap<String, String> resultConverter(Cursor cursor){
        TreeMap<String, String> settingsMap = new TreeMap<>();
        if(cursor == null) {
            settingsMap.put("Error:", "No settings found.");
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Settings.NameValueTable.NAME));
            String value = cursor.getString(cursor.getColumnIndexOrThrow(Settings.NameValueTable.VALUE));
            settingsMap.put(name,value);
        }
        cursor.close();
        return settingsMap;
    }
}
