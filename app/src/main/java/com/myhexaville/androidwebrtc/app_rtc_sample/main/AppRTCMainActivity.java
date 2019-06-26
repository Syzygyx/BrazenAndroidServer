/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.myhexaville.androidwebrtc.app_rtc_sample.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.myhexaville.androidwebrtc.R;
import com.myhexaville.androidwebrtc.app_rtc_sample.call.CallActivity;
import com.myhexaville.androidwebrtc.databinding.ActivityMainBinding;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission_group.CAMERA;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.EXTRA_ROOMID;

/**
 * Handles the initial setup where the user selects which room to join.
 */
public class AppRTCMainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AppRTCMainActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    final int min = 10000;
    final int max = 999999;
    int random;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        random = new Random().nextInt((max - min) + 1) + min;

        connect();

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @AfterPermissionGranted(RC_CALL)
    private void connect() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {

            connectToRoom(random + "");
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }


    private void connectToRoom(String roomId) {
        Log.e("Random :-", roomId);

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        connect();
        super.onResume();
    }
}
