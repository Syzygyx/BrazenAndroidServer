package com.doozycod.brazenserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import static com.android.volley.VolleyLog.TAG;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        listener = (NetworkListener)context;
        int status = NetworkUtil.getConnectivityStatusString(context);
        if(status == 0) {
            status= 0;
        }
        listener.updateNetworkStatus(status);
    }
}