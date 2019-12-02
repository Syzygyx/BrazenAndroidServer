package com.myhexaville.androidwebrtc.app_rtc.Utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesMethod {
    private Context context;

    public SharedPreferencesMethod(Context context) {
        this.context = context;
    }

    public void spInsert(String newroom) {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        sp_editior.putString("newroom", newroom);
        sp_editior.apply();
    }

    public void spNewRoomError(String newroom) {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        sp_editior.putString("newroom_err", newroom);
        sp_editior.apply();
    }

    public void wifiSSIDandPass(String ssid, String wifi_password) {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        sp_editior.putString("ssid", ssid);
        sp_editior.putString("wifi_password", wifi_password);
        sp_editior.apply();
    }

    public boolean getScanned() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        if (sp.contains("is_manual_dis")) {

//            return true;
        }
        return sp.getBoolean("is_manual_dis",false);

        /*else {
            return sp.getBoolean("is_manual_dis", false);

        }*/

    }

    public void insertScanned(boolean key) {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        sp_editior.putBoolean("is_manual_dis", key);
        sp_editior.apply();
    }

    public String getWifiSSID() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        return sp.getString("ssid", "");
    }

    public String getWifiPassword() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        return sp.getString("wifi_password", "");
    }

    public String getNewRoomError() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        return sp.getString("newroom_err", "");
    }

    public String getUser() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        return sp.getString("newroom", "");
    }

    public String getpermanentRoomId() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServerRoom", Context.MODE_PRIVATE);
        return sp.getString("permanent_room", "");
    }

    public void permanentRoomId(String roomId) {
        SharedPreferences sp = context.getSharedPreferences("BrazenServerRoom", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        sp_editior.putString("permanent_room", roomId);
        sp_editior.apply();
    }

}
