/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.myhexaville.androidwebrtc.app_rtc.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.myhexaville.androidwebrtc.R;
import com.myhexaville.androidwebrtc.app_rtc.Utils.SharedPreferencesMethod;
import com.novoda.merlin.Connectable;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import java.util.List;
import java.util.Random;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.EXTRA_ROOMID;


/**
 * Handles the initial setup where the user selects which room to join.
 */

public class AppRTCMainActivity extends AppCompatActivity {
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    final int min = 10000;
    final int max = 99999;
    int random;
    TextView signalStrengthTxt;
    String roomID;
    SharedPreferencesMethod sharedPreferenceMethod;

    private TelephonyManager telephonyManager;
    private SignalStrength signalStrength;
    // to check if we are connected to Network
    boolean isConnected = true;
    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;
    private int LTESingalStrength = 0;
    Merlin merlin;
    MerlinsBeard merlinsBeard;

    @SuppressLint("HardwareIds")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferenceMethod = new SharedPreferencesMethod(this);
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);
        merlinsBeard = MerlinsBeard.from(this);
        random = new Random().nextInt((max - min) + 1) + min;
        roomID = "brezan" + Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        sharedPreferenceMethod.permanentRoomId(roomID);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Listener for the signal strength.
        final PhoneStateListener mListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength sStrength) {
                signalStrength = sStrength;
//                getLTEsignalStrength();
            }
        };

        // Register the listener for the telephony manager
        telephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
       /* try {
            Runtime.getRuntime().exec("dpm set-device-owner com.myhexaville.androidwebrtc.app_rtc.Services/.AdminReceiver");
        } catch (Exception e) {
            Log.e("signalStrength", "device owner not set");
            Log.e("signalStrength", e.toString());
            e.printStackTrace();
        }*/
        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
//                connect();
                Log.e("Merlin", "onConnect: merlin connected!");
                // Do something you has internet!
            }
        });

    }

    private void getLTEsignalStrength() {
        try {

            @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    // cast to CellInfoLte and call all the CellInfoLte methods you need
                    CellInfoLte ci = (CellInfoLte) cellInfo;
                    LTESingalStrength = ci.getCellSignalStrength().getDbm();
                    signalStrengthTxt.setText("LTE Signal : " + LTESingalStrength + "dBm");
                    Log.e("signallsss ", "LTE signal  " + ci.getCellSignalStrength().getDbm());

                }
            }

        } catch (Exception e) {
            Log.e("LTE TAG", "Exception: " + e.toString());
        }
    }

    // check connectivity in onPause()
    @Override
    protected void onPause() {
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
//        stop merlin for checking connectivity
        merlin.unbind();
        super.onPause();
    }

    //  check device is connected to internet or not
    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
//            on network available  try to connect to server
            connect();
            Log.e("INTERNET", "INTERNET CONNECTED");
        }

//        on connection lost
        @Override
        public void onLost(Network network) {
            isConnected = false;
//            show connection error dialog
            showConnectionError();
            Log.e("ATAG", "INTERNET LOST");
        }
    };

    // Method to check network connectivity in Main Activity
    private void checkConnectivity() {
        // here we are getting the connectivity service from connectivity manager
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
         /*
         Getting network Info
         give Network Access Permission in Manifest
         */
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

         /*
         isConnected is a boolean variable
         here we check if network is connected or is getting connected
         */
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {

            /*
            SHOW ANY ACTION YOU WANT TO SHOW
            WHEN WE ARE NOT CONNECTED TO INTERNET/NETWORK
            */
            Log.e("ATAG", "NO NETWORK!");
            Toast.makeText(this, "Connection lost! Trying to connect!", Toast.LENGTH_SHORT).show();

// if Network is not connected we will register a network callback to  monitor network
            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(), connectivityCallback);
            monitoringConnectivity = true;
            showConnectionError();
        } else {
            connect();
        }
    }

    //    onConnection lost
    public void showConnectionError() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.showerror_conenction);
        RelativeLayout show_error = dialog.findViewById(R.id.show_error);
        signalStrengthTxt = dialog.findViewById(R.id.signals);
        dialog.show();
        getLTEsignalStrength();
        show_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(AppRTCMainActivity.this, AppRTCMainActivity.class));
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @AfterPermissionGranted(RC_CALL)
    private void connect() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
//            connectToRoom(sharedPreferenceMethod.getpermanentRoomId());
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra(EXTRA_ROOMID, sharedPreferenceMethod.getpermanentRoomId());
            startActivityForResult(intent, CONNECTION_REQUEST);
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    //    power manager for awake screen always when video is streaming
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
//        check connectivity if app is alive in foreground
        if (isScreenOn) {
            checkConnectivity();
        }

        super.onResume();
        merlin.bind();
    }


}
