package com.myhexaville.androidwebrtc.app_rtc_sample.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by ooo on 6/22/2018.
 */

public class SharedPreferenceMethod {
    Context context;

    public SharedPreferenceMethod(Context context) {
        this.context = context;
    }

    public void spInsert(String newroom) {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editior = sp.edit();
        sp_editior.putString("newroom", newroom);
        sp_editior.commit();
    }

    public String getUser() {
        SharedPreferences sp = context.getSharedPreferences("BrazenServer", Context.MODE_PRIVATE);
        return sp.getString("newroom", "");

    }


}
