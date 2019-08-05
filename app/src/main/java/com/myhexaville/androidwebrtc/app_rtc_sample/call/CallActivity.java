/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.myhexaville.androidwebrtc.app_rtc_sample.call;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.zxing.WriterException;
import com.myhexaville.androidwebrtc.R;
import com.myhexaville.androidwebrtc.databinding.ActivityCallBinding;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.AppRTCAudioManager;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.AppRTCClient;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.AppRTCClient.RoomConnectionParameters;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.AppRTCClient.SignalingParameters;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.PeerConnectionClient;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.PeerConnectionClient.PeerConnectionParameters;
import com.myhexaville.androidwebrtc.app_rtc_sample.web_rtc.WebSocketRTCClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.CAPTURE_PERMISSION_REQUEST_CODE;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.EXTRA_ROOMID;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_HEIGHT_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_HEIGHT_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_WIDTH_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_WIDTH_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_X_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_X_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_Y_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.LOCAL_Y_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.REMOTE_HEIGHT;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.REMOTE_WIDTH;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.REMOTE_X;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.REMOTE_Y;
import static com.myhexaville.androidwebrtc.app_rtc_sample.util.Constants.STAT_CALLBACK_PERIOD;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT;


public class CallActivity extends AppCompatActivity
        implements AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents, OnCallEvents, LocationListener {

    private static final String LOG_TAG = "CallActivity";
    String lastLat = "";
    String lastLong = "";
    private PeerConnectionClient peerConnectionClient;
    private AppRTCClient appRtcClient;
    private SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager;
    private EglBase rootEglBase;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();
    private boolean activityRunning;
    private static final String TAG = "SampleDataChannelAct";
    private RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionParameters peerConnectionParameters;
    private PeerConnectionFactory factory;
    private boolean iceConnected;
    private boolean isError;
    Dialog statsDialog;
    private long callStartedTimeMs;
    private boolean micEnabled = false;
    protected LocationManager locationManager;
    private PeerConnection localPeerConnection, remotePeerConnection;
    private DataChannel localDataChannel;
    private ActivityCallBinding binding;
    Dialog dialog;
    String roomId = null;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    private String android_id, Username, batLevel, batteryTemperature, wifiSignalLevel, LTESignal;
    private Boolean hasConnection = false;
    private Socket mSocket;
    Location location;
    private SignalStrength signalStrength;
    private TelephonyManager telephonyManager;
    private final static String LTE_TAG = "LTE_Tag";
    private final static String LTE_SIGNAL_STRENGTH = "getLteSignalStrength";
    private final int interval = 1000 * 60; // 60 Seconds
    Timer timer = new Timer();
    TextView tv_bat_lvl, tv_bat_temp, tv_wifi_signal, tv_net_signal;

    {
        try {
            mSocket = IO.socket("https://intense-bayou-55879.herokuapp.com/");
        } catch (URISyntaxException e) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call);
        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // Get Intent parameters.
        Intent intent = getIntent();
        roomId = intent.getStringExtra(EXTRA_ROOMID);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        200);
            }
        }

//        initializePeerConnectionFactory();
        remoteRenderers.add(binding.remoteVideoView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);

//        initializePeerConnections();

        // Create video renderers.
        rootEglBase = EglBase.create();
        binding.localVideoView.init(rootEglBase.getEglBaseContext(), null);
        binding.remoteVideoView.init(rootEglBase.getEglBaseContext(), null);

        binding.localVideoView.setZOrderMediaOverlay(true);
        binding.localVideoView.setEnableHardwareScaler(true);
        binding.remoteVideoView.setEnableHardwareScaler(true);
        updateVideoView();


        if (roomId == null || roomId.length() == 0) {

            Log.e(LOG_TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(this.mBatInfoTemp, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // If capturing format is not specified for screencapture, use screen resolution.
        peerConnectionParameters = PeerConnectionParameters.createDefault();

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        appRtcClient = new WebSocketRTCClient(this);

        // Create connection parameters.
        roomConnectionParameters = new RoomConnectionParameters("https://appr.tc", roomId, false);

        setupListeners();

        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);

        startCall();
        showQR();


        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Listener for the signal strength.
        final PhoneStateListener mListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength sStrength) {
                signalStrength = sStrength;
                getLTEsignalStrength();
            }
        };

        // Register the listener for the telephony manager
        telephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    private void getWifiSignal() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Log.d(TAG, "getWifiSignal: " + level + "");
        if (level <= 5 && level >= 4) {
            //Best signal
            wifiSignalLevel = "Very Good";
            Log.d(TAG, "getWifiSignal: " + level + "");

        } else if (level < 4 && level >= 3) {
            //Good signal
            wifiSignalLevel = "Good";
            Log.d(TAG, "getWifiSignal: " + level + "");

        } else if (level < 3 && level >= 2) {
            //Low signal
            wifiSignalLevel = "Low";
            Log.d(TAG, "getWifiSignal: " + level + "");

        } else if (level < 2 && level >= 1) {
            //Very weak signal
            wifiSignalLevel = "Very Low";
            Log.d(TAG, "getWifiSignal: " + level + "");
        } else {
            // no signals
            wifiSignalLevel = "No Wifi Signal";
            Log.d(TAG, "getWifiSignal: " + level + "");
        }
    }

    void showStats() {
        statsDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        statsDialog.setContentView(R.layout.stats_dialog);
        statsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tv_bat_lvl = statsDialog.findViewById(R.id.batlvlsocket);
        tv_bat_temp = statsDialog.findViewById(R.id.batTempsocket);
        tv_net_signal = statsDialog.findViewById(R.id.networksignalsocket);
        tv_wifi_signal = statsDialog.findViewById(R.id.wifisignalsocket);
        statsDialog.show();

        tv_bat_lvl.setText(batLevel);
        tv_bat_temp.setText(batteryTemperature);
        tv_wifi_signal.setText(wifiSignalLevel);
        tv_net_signal.setText(LTESignal);

//        timer.schedule(hourlyTask, 0l, 1000 * 60);

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Log.e(TAG, "run: Thread");
                        tv_bat_lvl.setText(batLevel);
                        tv_bat_temp.setText(batteryTemperature);
                        tv_wifi_signal.setText(wifiSignalLevel);
                        if (LTESignal == null) {
                            LTESignal = "No Signal";
                            tv_net_signal.setText(LTESignal);
                        } else {
                            tv_net_signal.setText(LTESignal);

                        }
                        sendMessage(lastLat, lastLong, batteryTemperature, batLevel, LTESignal, wifiSignalLevel);
                        // Stuff that updates the UI

                    }
                });

            }

        }, 0, interval);

    }


    private void initializePeerConnections() {

        localPeerConnection = createPeerConnection(factory, true);
        remotePeerConnection = createPeerConnection(factory, false);
        this.localDataChannel = remotePeerConnection.createDataChannel("sendDataChannel", new DataChannel.Init());

        // localDataChannel = localPeerConnection.createDataChannel("sendDataChannel", new DataChannel.Init());
        localDataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {

            }

            @Override
            public void onStateChange() {
                Log.d(TAG, "onStateChange: " + localDataChannel.state().toString());
                runOnUiThread(() -> {
                    if (localDataChannel.state() == DataChannel.State.OPEN) {
                        // binding.sendButton.setEnabled(true);
                    } else {
                        // binding.sendButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                Log.d(TAG, "onMessage: hlkjio");
            }
        });
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
    }

    private void setupListeners() {
        binding.buttonCallDisconnect.setOnClickListener(view -> onCallHangUp());

        binding.buttonCallSwitchCamera.setOnClickListener(view -> onCameraSwitch());

        binding.buttonCallToggleMic.setOnClickListener(view -> {
            boolean enabled = onToggleMic();
            binding.buttonCallToggleMic.setAlpha(enabled ? 1.0f : 0.3f);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            return;
        }
        startCall();
    }

    private void socketIO() {
        Username = android_id;
        if (hasConnection) {

        } else {
            mSocket.connect();
            mSocket.on("connect user", onNewUser);
            mSocket.on("jsondata", onNewMessage);

            JSONObject userId = new JSONObject();
            try {
                userId.put("connected", Username + " Connected");
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

//    TimerTask hourlyTask = new TimerTask() {
//        @Override
//        public void run() {
//            Log.e(TAG, "sendMessage: " + "TimeTask started! ");
//
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    Log.e(TAG, "run: Thread");
//                    tv_bat_lvl.setText(batLevel);
//                    tv_bat_temp.setText(batteryTemperature);
//                    tv_wifi_signal.setText(wifiSignalLevel);
//                    if (LTESignal == null) {
//                        LTESignal = "No Signal";
//                        tv_net_signal.setText(LTESignal);
//                    } else {
//                        tv_net_signal.setText(LTESignal);
//
//                    }
//                    sendMessage(lastLat, lastLong, batteryTemperature, batLevel, LTESignal, wifiSignalLevel);
//                    // Stuff that updates the UI
//
//                }
//            });
//        }
//    };

    public void sendMessage(String lati, String longi, String batteryTemp, String batterylevel, String networksignal, String wifiSignalLvl) {
        Log.e(TAG, "sendMessage: LTE" + LTESignal);
        if (lati.equals("") && longi.equals("")) {
            lati = "39.124032";
            longi = "-104.880821";
        }
        if (LTESignal == null) {
            LTESignal = "No Signal";
        }
        Log.e("jsondata", "Data to send: " + lati + " \n" + longi + " \n" + batteryTemp + " \n" + batterylevel + " \n" + networksignal + " \n" + wifiSignalLvl);
//        String message = textField.getText().toString().trim();
        if (TextUtils.isEmpty(lati)) {
            Log.e("sendMessage2", "sendMessage:2 " + lati);
            return;
        }
//        textField.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", Username);
            jsonObject.put("latitute", lati);
            jsonObject.put("longitute", longi);
            jsonObject.put("batteryTemp", batteryTemp);
            jsonObject.put("batteryLevel", batterylevel);
            jsonObject.put("networkSignal", networksignal);
            jsonObject.put("wifiSignal", wifiSignalLvl);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("Socket Emit", "sendMessage: 1" + mSocket.emit("jsondata", jsonObject));
    }

    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("new", "run: " + args.length);
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String id;
                    try {
                        username = data.getString("username");
                        message = data.getString("latitute");
                        Log.e("Message", "run: " + username + message);

//                        Toast.makeText(CallActivity.this, message, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };
    Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    int length = args.length;

                    if (length == 0) {
                        return;
                    }
                    //Here i'm getting weird error..................///////run :1 and run: 0
                    Log.e("RUN", "run: " + args.length);
                    String username = args[0].toString();
                    try {
                        JSONObject object = new JSONObject(username);
                        username = object.getString("connected");
                        Log.e("UserName", "run: " + username);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
//                                Toast.makeText(CallActivity.this, location.getLatitude() + location.getLongitude() + "", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onNewUser: " + location.getLatitude() + location.getLongitude());

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private boolean captureToTexture() {
        return true;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(LOG_TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(LOG_TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(LOG_TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(LOG_TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    // Activity interfaces
    @Override
    public void onPause() {
        super.onPause();
        activityRunning = false;
        disconnect();

        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
//        if (peerConnectionClient != null) {
//            peerConnectionClient.stopVideoSource();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String contactName = args.getString(EXTRA_ROOMID);
//            binding.contactNameCall.setText(contactName);

        }


        binding.captureFormatTextCall.setVisibility(View.GONE);
        binding.captureFormatSliderCall.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        disconnect();
        activityRunning = false;
        rootEglBase.release();
        super.onDestroy();
        unregisterReceiver(mBatInfoTemp);
        unregisterReceiver(mBatInfoReceiver);
        if (isFinishing()) {
            Log.i("Destroying", "onDestroy: ");


            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " DisConnected");
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off("jsondata", onNewMessage);
            mSocket.off("connect user", onNewUser);
            Username = "";

        } else {
            Log.i("Destroying", "onDestroy: is rotating.....");
        }
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batLevel = level + "%";
            getWifiSignal();

        }
    };


    private BroadcastReceiver mBatInfoTemp = new BroadcastReceiver() {
        float temp = 0;

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            temp = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;

            batteryTemperature = temp + " C";

        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getLTEsignalStrength() {
        try {

            int LTESingalStrength = 0;

            @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    // cast to CellInfoLte and call all the CellInfoLte methods you need
                    CellInfoLte ci = (CellInfoLte) cellInfo;
//                    Log.e("signallsss ", "LTE signal strength: " + ci.getCellSignalStrength().getDbm());
                    LTESingalStrength = ci.getCellSignalStrength().getDbm();
                }
            }
            if (LTESingalStrength <= 0 && LTESingalStrength >= -50) {
                LTESignal = "Very Good";
                Log.e("OUT ", "LTE signal strength: " + LTESignal);

            } else if (LTESingalStrength < -50 && LTESingalStrength >= -70) {
                LTESignal = "Good";
                Log.e("OUT ", "LTE signal strength: " + LTESignal);

            } else if (LTESingalStrength < -70 && LTESingalStrength >= -80) {
                LTESignal = "Average";
                Log.e("OUT ", "LTE signal strength: " + LTESignal);

            } else if (LTESingalStrength < -80 && LTESingalStrength >= -90) {
                LTESignal = "Low";
                Log.e("OUT ", "LTE signal strength: " + LTESignal);

            } else if (LTESingalStrength < -90 && LTESingalStrength >= -110) {
                LTESignal = "Very Low";
                Log.e("OUT ", "LTE signal strength: " + LTESignal);

            } else {
                LTESignal = "No Signal";
                Log.e("OUT ", "LTE signal strength: " + LTESignal);
            }

        } catch (Exception e) {
            Log.e(LTE_TAG, "Exception: " + e.toString());
        }
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        if (isFinishing()) {

            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " DisConnected");
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off("jsondata", onNewMessage);
            mSocket.off("connect user", onNewUser);
            Username = "";

        } else {
            Log.i("Destroying", "onDestroy: is rotating.....");
        }
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(true);

        }
        return true;
    }

    private void updateVideoView() {
        binding.remoteVideoLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        binding.remoteVideoView.setScalingType(SCALE_ASPECT_FILL);
        binding.remoteVideoView.setMirror(false);

        if (iceConnected) {
            binding.localVideoLayout.setPosition(
                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
            binding.localVideoView.setScalingType(SCALE_ASPECT_FIT);
        } else {
            binding.localVideoLayout.setPosition(
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
            binding.localVideoView.setScalingType(SCALE_ASPECT_FILL);
        }
        binding.localVideoView.setMirror(true);

        binding.localVideoView.requestLayout();
        binding.remoteVideoView.requestLayout();
    }

    private void startCall() {

        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.

        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this);

        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(LOG_TAG, "Starting the audio manager...");
        audioManager.start(this::onAudioManagerDevicesChanged);
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.setMode(AudioManager.ADJUST_MUTE);
        }
        audioManager.setSpeakerphoneOn(false);
    }

    // Should be called from UI thread
    @SuppressLint("MissingPermission")
    private void callConnected() {
//        dialog.dismiss();
        Log.e("room==>", roomId);

        socketIO();
        onToggleMic();
        sendMessage(lastLat, lastLong, batteryTemperature, batLevel, LTESignal, wifiSignalLevel);

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(false);
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(LOG_TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(LOG_TAG, "Call is connected in closed or error state");
            return;
        }

        showStats();
//        dialog.dismiss();

        // Update video view.
        updateVideoView();
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);

        if (locationManager != null) {
            location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                lastLat = location.getLatitude() + "";
                lastLong = location.getLongitude() + "";
            }
        }
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(

            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(LOG_TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        activityRunning = false;
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }

        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        binding.localVideoView.release();
        binding.remoteVideoView.release();
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);

        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (!activityRunning) {
            Log.e(LOG_TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok,
                            (dialog, id) -> {
                                dialog.cancel();
                                disconnect();
                            })
                    .create()
                    .show();
        }
    }


    private void reportError(final String description) {
        runOnUiThread(() -> {
            if (!isError) {
                isError = true;
                disconnectWithErrorMessage(description);
            }
        });
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            Logging.d(LOG_TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(LOG_TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), binding.localVideoView,
                remoteRenderers, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onConnectedToRoom(final SignalingParameters params) {
        runOnUiThread(() -> onConnectedToRoomInternal(params));
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(LOG_TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(LOG_TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(LOG_TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(() -> {
            disconnect();
        });
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory, boolean isLocal) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
//                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: " + isLocal);
                if (isLocal) {
//                    remotePeerConnection.addIceCandidate(iceCandidate);
                } else {
//                    localPeerConnection.addIceCandidate(iceCandidate);
                }
                Log.d(TAG, "onIceCandidate: ");
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: ");
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: is local: " + isLocal + " , state: " + dataChannel.state());

                dataChannel.registerObserver(new DataChannel.Observer() {
                    @Override
                    public void onBufferedAmountChange(long l) {

                    }

                    @Override
                    public void onStateChange() {
                        Log.d(TAG, "onStateChange: remote data channel state: " + dataChannel.state().toString());
                    }

                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        Log.d(TAG, "onMessage: got message");
                        Toast.makeText(CallActivity.this, "Connected", Toast.LENGTH_SHORT).show();
//                        String message = byteBufferToString(buffer.data, Charset.defaultCharset());
//                        Log.d(TAG, "onMessage2: " + message);
//                        runOnUiThread(() -> binding.text.setText(message));
//                        Toast.makeText(CallActivity.this, "" + message, Toast.LENGTH_SHORT).show();
//                        readIncomingMessage(buffer.data);
                    }


                });
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }


    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                if (signalingParameters.initiator) {
                    appRtcClient.sendOfferSdp(sdp);
                } else {
                    appRtcClient.sendAnswerSdp(sdp);
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(LOG_TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidate(candidate);
            }

        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidateRemovals(candidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            iceConnected = true;
            callConnected();

        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(() -> {
            iceConnected = false;
            disconnect();
        });
        showQR();

        statsDialog.dismiss();
        Log.i("Destroying", "onDestroy: ");

        JSONObject userId = new JSONObject();
        try {
            userId.put("username", Username + " DisConnected");
            mSocket.emit("connect user", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.disconnect();
        mSocket.off("jsondata", onNewMessage);
        mSocket.off("connect user", onNewUser);
        Username = "";
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(() -> {
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

    void showQR() {
        if (roomId.length() > 0) {
            Log.e(LOG_TAG, "Room ID: " + roomId);
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    roomId, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.custom_record_timer);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                ImageView QR_img = dialog.findViewById(R.id.QR_img);
                QR_img.setImageBitmap(bitmap);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        // Prevent dialog close on back press button
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
            } catch (WriterException e) {
                Log.e(this + "", e.toString());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String loc = location.getLatitude() + " " + location.getLongitude();
        Log.e(TAG, "onLocationChanged: " + loc);
        lastLat = location.getLatitude() + "";
        lastLong = location.getLongitude() + "";

//        sendMessage(lastLat, lastLong, batteryTemperature, batLevel, LTESignal, wifiSignalLevel);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
//                sendMessage(location.getLatitude() + "", location.getLongitude() + "", batteryTemperature, batLevel, LTESignal, wifiSignalLevel);
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
