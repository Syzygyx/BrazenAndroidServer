/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.zxing.WriterException;
import com.myhexaville.androidwebrtc.BuildConfig;
import com.myhexaville.androidwebrtc.R;
import com.myhexaville.androidwebrtc.app_rtc.Interface.OnCallEvents;
import com.myhexaville.androidwebrtc.app_rtc.Utils.SharedPreferencesMethod;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.AppRTCAudioManager;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.AppRTCClient;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.AppRTCClient.RoomConnectionParameters;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.AppRTCClient.SignalingParameters;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.PeerConnectionClient;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.PeerConnectionClient.PeerConnectionParameters;
import com.myhexaville.androidwebrtc.app_rtc.webrtc.WebSocketRTCClient;
import com.myhexaville.androidwebrtc.databinding.ActivityCallBinding;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.CAPTURE_PERMISSION_REQUEST_CODE;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.EXTRA_ROOMID;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_HEIGHT_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_HEIGHT_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_WIDTH_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_WIDTH_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_X_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_X_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_Y_CONNECTED;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.LOCAL_Y_CONNECTING;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.REMOTE_HEIGHT;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.REMOTE_WIDTH;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.REMOTE_X;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.REMOTE_Y;
import static com.myhexaville.androidwebrtc.app_rtc.Utils.Constant.STAT_CALLBACK_PERIOD;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT;


public class CallActivity extends AppCompatActivity
        implements AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents, OnCallEvents, LocationListener
        /*, Connectable*/
{
    int LTESingalStrength = 0;
    private static final String LOG_TAG = "CallActivity";
    String lastLat = "0.0";
    String lastLong = "0.0";
    String RandomString;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    // to check if we are connected to Network
    boolean isConnected = true;
    boolean onCallDiscoonect = false;
    int EVENT_FLAG = 0;
    boolean isUpdateRunning = true;
//    MerlinsBeard merlin;
    private int FLAG = 0;
    LinearLayout bgColorStats;
    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;
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
    private boolean iceConnected;
    private boolean isError;
    Dialog statsDialog;
    private long callStartedTimeMs;
    private boolean micEnabled = false;
    protected LocationManager locationManager;
    private ActivityCallBinding binding;
    Dialog dialog;
    String roomId = null;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    private String android_id, Username, batLevel, batteryTemperature, wifiSignalLevel, LTESignal;
    private Boolean hasConnection = false;
    private Socket mSocket;
    Location location;
    boolean isNetworkEnabled = false;
    private SignalStrength signalStrength;
    private TelephonyManager telephonyManager;
    public final static String LTE_TAG = "LTE_Tag";
    private final int interval = 1000 * 60; // 60 Seconds
    Timer timer = new Timer();
    String tempName = "brazan";
    int curVersion, vcode, vclient = 0;
    String app_link, temp_room = "";
    File file;
    TextView tv_bat_lvl, tv_bat_temp, tv_wifi_signal, tv_net_signal, versioncode, setversionCode, signalStrengthTxt;
    SharedPreferencesMethod sharedPreferenceMethod;
    boolean isConnectionError = false;
    String ssid;
    String networkSSID = "Doozycod";
    WifiManager wifiManager;
    List<ScanResult> getWifiSSIDs;
    WifiInfo wifiInfo;
    JSONObject onCallJSON;
    boolean isCallConnected = false;
    /*public final int REQUEST_ENABLE_BT = 101;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;*/
//    ConnectedThread mConnectedThread;

    {
        try {
            mSocket = IO.socket("https://intense-bayou-55879.herokuapp.com/");
        } catch (URISyntaxException e) {
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
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
//        stats Dialog initialize
        statsDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

//        get android ID for random room
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

//        initialize shared preferences
        sharedPreferenceMethod = new SharedPreferencesMethod(this);

//        Socket method call and initialization
        socketIO();

        // get IMEI Number
        getIMEI();
        Log.e(TAG, "onCreate: "+getIMEI());

//        get version code of app
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            curVersion = pInfo.versionCode;
            vcode = pInfo.versionCode;
            final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
//                Toast.makeText(CallActivity.this, "\nLongitute" +location.getLatitude() +"\n Latitude"+ location.getLongitude(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Location: \n Latitude" + location.getLatitude() + "\nLongitute" + location.getLongitude());
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
//          Wifi check and Connect using socket emit
        wifiCheck();
        // Get Intent parameters.
        Intent intent = getIntent();


        if (intent.hasExtra("false")) {
            roomId = sharedPreferenceMethod.getpermanentRoomId();
        }
            /*if (intent.getStringExtra(EXTRA_ROOMID).equals("clientsocket")) {
                roomId = tempName + android_id + sharedPreferenceMethod.getNewRoomError();
                sharedPreferenceMethod.permanentRoomId(tempName + android_id);
            }*/
        if (intent.hasExtra(EXTRA_ROOMID) && intent.getStringExtra(EXTRA_ROOMID).equals("socket")) {
            roomId = tempName + android_id + sharedPreferenceMethod.getNewRoomError();
        } else {
            roomId = tempName + android_id + sharedPreferenceMethod.getUser();
            sharedPreferenceMethod.permanentRoomId(tempName + android_id);
        }

//        permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_PHONE_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        200);
            }
        }

//        add remote video view
        remoteRenderers.add(binding.remoteVideoView);

//        get location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);

        // Create video renderers.
        rootEglBase = EglBase.create();
        binding.localVideoView.init(rootEglBase.getEglBaseContext(), null);
        binding.remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        binding.localVideoView.setZOrderMediaOverlay(true);
        binding.localVideoView.setEnableHardwareScaler(true);
        binding.remoteVideoView.setEnableHardwareScaler(true);

        if (roomId == null || roomId.length() == 0) {
            Log.e(LOG_TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

//        register broadcast receiver for battery lvl & temp
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(this.mBatInfoTemp, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // If capturing format is not specified for screencapture, use screen resolution.
        peerConnectionParameters = PeerConnectionParameters.createDefault();

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        appRtcClient = new WebSocketRTCClient(this);
        // Create connection parameters.
        roomConnectionParameters = new RoomConnectionParameters("https://appr.tc", roomId, false);

//      listener for button
        setupListeners();

        Log.e(TAG, "onCreate: IMEI NUMBER : "+getIMEI());
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);

        if (intent.hasExtra("call_event")) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("call", "callResume");
                jsonObject.put("networkName", ssid);
                jsonObject.put("type", "server");
//                jsonObject.put("manual", "no");
                Log.e(TAG, "call_event: " + mSocket.emit("call_connect", jsonObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        call manager when user connected to room
        startCall();


//        show QR code for room connection
        if (sharedPreferenceMethod.getScanned()) {
            showStats();
        } else {
            showQR();
        }

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
//        merlin = MerlinsBeard.from(this);

//        Turn Location dialog
        displayLocationSettingsRequest(this);
        /*// Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);*/


//        pairDevice();

    }

    public String getIMEI() {

        //andGoToYourNextStep
        return getDeviceIMEI(CallActivity.this);
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceIMEI(Activity activity) {

        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            else
                deviceUniqueIdentifier = tm.getDeviceId();
            if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length())
                deviceUniqueIdentifier = "0";
        }
        return deviceUniqueIdentifier;
    }


    //    update server app
    void updateDialogShow() {
//        Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
//        dialog.setContentView(R.layout.update_dialog);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        Button not_now = dialog.findViewById(R.id.not_now);
//        Button update_now = dialog.findViewById(R.id.update_now);
//        dialog.show();
//        not_now.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                showStats();
//            }
//        });
//        update_now.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//        if (!isUpdateRunning) {
        updateServerApk();
//            isUpdateRunning = true;
//        }
//                dialog.dismiss();
//                showStats();
//            }
//        });
    }

    //    get wifi list to connect server to same network
    void wifiCheck() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

//        wifi info and wifi SSID
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            ssid = wifiInfo.getSSID();
        }

        if (mWifi.isConnected()) {
            if (getWifiSSIDs != null) {
                for (int i = 0; i < getWifiSSIDs.size(); i++) {
                    if (getWifiSSIDs.get(i).SSID.equals(networkSSID)) {
                        Log.e(TAG, "wifiCheck: BSSID " + getWifiSSIDs.get(i).BSSID);
                    } else {
                        if (!sharedPreferenceMethod.getWifiSSID().equals("")) {
                            connectToWifi(sharedPreferenceMethod.getWifiSSID(), sharedPreferenceMethod.getWifiPassword());
                        }
                    }
                }
            } else {
                registerReceiver(mWifiScanReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            }

        } else {
            registerReceiver(mWifiScanReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }

    }

    //    connect to wifi if available using network ssid and network password
    void connectToWifi(String networkSSID, String networkPass) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }
//        remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    //    wifi names list receiver
    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                getWifiSSIDs = mScanResults;
                for (int i = 0; i < mScanResults.size(); i++) {
//                    Log.e(" Network ID", "wifi in range: " + getWifiSSIDs.get(i).SSID);
                }
            }
        }
    };

    //    update using apk
    public void updateServerApk() {
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = "serverupdate.apk";
        destination = destination + fileName;
        final Uri uri = Uri.parse("file://" + destination);
        //Delete update file if exists
        file = new File(destination);
        if (file.exists()) {
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();
        }
        //get url of app on server
        String url = app_link;

        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Update");
        request.setTitle("Brazen Monitor");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        String finalDestination = destination;
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

//                InstallAPK(finalDestination);
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri newuri = FileProvider.getUriForFile(CallActivity.this, getPackageName() + ".fileprovider", file);
                install.setDataAndType(newuri, manager.getMimeTypeForDownloadedFile(downloadId));
                startActivity(install);
                Toast.makeText(getApplicationContext(), "Downloading update..", Toast.LENGTH_LONG).show();
                disconnect();
                unregisterReceiver(this);
                finish();
            }
        };

        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }


    //    wifi signal level
    private void getWifiSignal() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Log.d(TAG, "getWifiSignal: " + level + "");
        if (level <= 5 && level >= 4) {
            //Best signal
            wifiSignalLevel = "Very Good";

        } else if (level < 4 && level >= 3) {
            //Good signal
            wifiSignalLevel = "Good";
        } else if (level < 3 && level >= 2) {
            //Low signal
            wifiSignalLevel = "Low";

        } else if (level < 2 && level >= 1) {
            //Very weak signal
            wifiSignalLevel = "Very Low";
        } else {
            // no signals
            wifiSignalLevel = "No Wifi Signal";
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(CallActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    //    showing stats on call connect
    void showStats() {

        statsDialog.setContentView(R.layout.stats_dialog);
        statsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tv_bat_lvl = statsDialog.findViewById(R.id.batlvlsocket);
        bgColorStats = statsDialog.findViewById(R.id.background);
        tv_bat_temp = statsDialog.findViewById(R.id.batTempsocket);
        tv_net_signal = statsDialog.findViewById(R.id.networksignalsocket);
        versioncode = statsDialog.findViewById(R.id.versioncode);
        tv_wifi_signal = statsDialog.findViewById(R.id.wifisignalsocket);
        statsDialog.show();
        versioncode.setText("v" + BuildConfig.VERSION_CODE);
        tv_bat_lvl.setText(batLevel);
        tv_bat_temp.setText(batteryTemperature);
        tv_wifi_signal.setText(wifiSignalLevel);
        tv_net_signal.setText(LTESignal);
//        bgColorStats.setBackgroundColor(getResources().getColor(R.color.bgColor));

//        tv_bat_temp.setTextColor(getResources().getColor(R.color.blackFontColor));
//        tv_bat_lvl.setTextColor(getResources().getColor(R.color.blackFontColor));
//        tv_net_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
//        tv_net_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
//        tv_wifi_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
//        if (onCallDiscoonect) {
//            bgColorStats.setBackgroundColor(getResources().getColor(R.color.bgColor));
//        }
//        timer.schedule(hourlyTask, 0l, 1000 * 60);
        statsDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Prevent dialog close on back press button
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Stuff that updates the UI

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

                    }
                });
            }

        }, 0, interval);
    }

    private void setupListeners() {
        binding.buttonCallDisconnect.
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCallHangUp();
                    }
                });

        binding.buttonCallSwitchCamera.setOnClickListener(view -> onCameraSwitch());

        binding.buttonCallToggleMic.setOnClickListener(view -> {
            boolean enabled = onToggleMic();
            binding.buttonCallToggleMic.setAlpha(enabled ? 1.0f : 0.3f);
        });
    }

    //   if room id avialable connect server and start call
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            return;
        }
        startCall();
    }

    //    socket for data share
    private void socketIO() {
        Username = sharedPreferenceMethod.getpermanentRoomId();
        if (hasConnection) {

        } else {
            mSocket.connect();
            mSocket.on("connect user", onNewUser);
            mSocket.on("jsondata", onNewMessage);
            mSocket.on("new_apk", onNewUpdate);
            mSocket.on("new_room", onNewRoom);
            mSocket.on("myWifi", onMyWifi);
            mSocket.on("new_room_client", onNewRoomClient);
            mSocket.on("call_connect", onCallConnect);

            JSONObject userId = new JSONObject();
            try {
                userId.put("connected", Username);
                userId.put("userType", "Server");
                mSocket.emit("connect user", userId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //    for update room id
    void sendData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", Username);
            RandomString = getRandomString(4);
            jsonObject.put("socket_room", RandomString);
            sharedPreferenceMethod.spInsert(RandomString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("Socket Emit ROOM", "sendMessage: " + mSocket.emit("new_room", jsonObject));
    }

    //random string for room id
    private static String getRandomString(final int sizeOfRandomString) {
        final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    //    send stats to client app
    public void sendMessage(String lati, String longi, String batteryTemp, String batterylevel, String networksignal, String wifiSignalLvl) {
        Log.e(TAG, "sendMessage: LTE" + LTESignal);
        if (lati.equals("") && longi.equals("")) {
            lati = "0.0";
            longi = "0.0";
        }
        if (LTESignal == null) {
            LTESignal = "No Signal";
        }
//        String message = textField.getText().toString().trim();
        if (TextUtils.isEmpty(lati)) {
            Log.e("sendMessage2", "sendMessage:2 " + lati);
            return;
        }
//        textField.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject.put("username", Username);
            jsonObject.put("latitute", lati);
            jsonObject.put("longitute", longi);
            jsonObject.put("batteryTemp", batteryTemp);
            jsonObject.put("batteryLevel", batterylevel);
            jsonObject.put("networkSignal", networksignal);
            jsonObject.put("wifiSignal", wifiSignalLvl);
            jsonObject.put("device_id", tempName + android_id);
            jsonObject.put("permanent_room", sharedPreferenceMethod.getpermanentRoomId());
            jsonObject.put("device_imei", getIMEI());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("jsondata", "Data to send: " + jsonObject + "\n" + sharedPreferenceMethod.getpermanentRoomId());

        Log.e("Socket Emit", "sendMessage: 1 " + mSocket.emit("jsondata", jsonObject));
    }

    //    onMessage Emitter for message
    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("Receive", "run: " + args.length);
                    JSONObject data = (JSONObject) args[0];
                    Log.e(TAG, "run: on new Message" + data);

                }
            });
        }
    };

    //    onNewMessage Emitter for new Room
    Emitter.Listener onNewRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                        String Room = data.getString("socket_room");
                        sharedPreferenceMethod.spNewRoomError(Room);
                        Log.e("NEW SOCKET", " SERVER ROOM " + username + "  socket :  " + Room);
                        if (Room.equals(sharedPreferenceMethod.getpermanentRoomId())) {
                            Log.e(TAG, "run: Server emit");
                        } else {
//                            if(EVENT_FLAG==0){
//                                EVENT_FLAG=1;
                            Intent intent = new Intent(CallActivity.this, CallActivity.class);
                            intent.putExtra(EXTRA_ROOMID, "socket");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    //  onCallConnect emitter
    Emitter.Listener onCallConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onCallJSON = (JSONObject) args[0];
                    String username;
                    Log.e(TAG, "call JSON: " + onCallJSON);
                    try {
                        if (onCallJSON.has("scanned") && onCallJSON.getString("scanned").equals("false")) {
                            sharedPreferenceMethod.insertScanned(false);
                        }
                        if (onCallJSON.has("scanned") && onCallJSON.getString("scanned").equals("true")) {
                            sharedPreferenceMethod.insertScanned(true);

                        }
                        if (onCallJSON.has("manual")) {
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        } else {
                            if (onCallJSON.has("color") && onCallJSON.getString("color").equals("red")) {
                                if (statsDialog.isShowing()) {
                                    bgColorStats.setBackgroundColor(getResources().getColor(R.color.bgColor));
                                    tv_bat_temp.setTextColor(getResources().getColor(R.color.whiteFontColor));
                                    tv_bat_lvl.setTextColor(getResources().getColor(R.color.whiteFontColor));
                                    tv_net_signal.setTextColor(getResources().getColor(R.color.whiteFontColor));
                                    tv_net_signal.setTextColor(getResources().getColor(R.color.whiteFontColor));
                                    tv_wifi_signal.setTextColor(getResources().getColor(R.color.whiteFontColor));
                                }
                            }
                            if (onCallJSON.has("color") && onCallJSON.getString("color").equals("white")) {
                                if (statsDialog.isShowing()) {
                                    bgColorStats.setBackgroundColor(getResources().getColor(R.color.bgColorWhite));
                                    tv_bat_temp.setTextColor(getResources().getColor(R.color.blackFontColor));
                                    tv_bat_lvl.setTextColor(getResources().getColor(R.color.blackFontColor));
                                    tv_net_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
                                    tv_net_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
                                    tv_wifi_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
                                }
//                                finish();

                            }


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //    onNewRoomClient Emitter for new Room
    Emitter.Listener onNewRoomClient = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                        String Room = data.getString("socket_room");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //    onNewUpdate Emitter for update server app
    Emitter.Listener onNewUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String versioncode;
                    String id;
                    try {
                        username = data.getString("username");
                        versioncode = data.getString("versioncode");
                        app_link = data.getString("app_link");
                        temp_room = data.getString("new_room");
                        vclient = Integer.parseInt(versioncode);
                        sharedPreferenceMethod.spInsert(temp_room);

                        if (data.has("scanned") && data.getString("scanned").equals("user")) {
                            sharedPreferenceMethod.insertScanned(true);
                        }
                        Log.e("MessagefromClient", "run: OnUpdate " + data);
                        if (curVersion < vclient) {
//                            if (!isUpdateRunning) {
//                            updateDialogShow();

//                            updateServerApk();

//                                isUpdateRunning = true;
//                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            });
        }
    };

    //    onNewUser Emitter for new user
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
                    String username = args[0].toString();
                    try {
                        JSONObject object = new JSONObject(username);
//                        username = object.getString("connected");
                        if (object.has("scanned") && object.getString("scanned").equals("false")) {
                            sharedPreferenceMethod.insertScanned(false);
                        }
                        Log.e("UserName", "run: " + object);
                        if (locationManager != null) {
                            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                            if (isNetworkEnabled) {
                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (location != null) {
//                                Toast.makeText(CallActivity.this, location.getLatitude() + location.getLongitude() + "", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
//                                Toast.makeText(CallActivity.this, location.getLatitude() + location.getLongitude() + "", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //    onMyWifi Emitter for connect to wifi if found!
    Emitter.Listener onMyWifi = new Emitter.Listener() {
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

                    String username = args[0].toString();
                    try {
                        JSONObject object = new JSONObject(username);
                        Log.e("myWifi", "run: " + object);
                        networkSSID = object.getString("name");
                        sharedPreferenceMethod.wifiSSIDandPass(object.getString("name"), object.getString("password"));
                        connectToWifi(sharedPreferenceMethod.getWifiSSID(), sharedPreferenceMethod.getWifiPassword());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //    camera 2 api for video streaming
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
        onCallHangUp();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("call", "callPaused");
            jsonObject.put("networkName", ssid);
            jsonObject.put("type", "server");

//                jsonObject.put("manual", "no");
            Log.e(TAG, "onPause: " + mSocket.emit("call_connect", jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
        // Don't stop the video when using screen capture to allow user to show other apps to the remote
        // end.
//        if (peerConnectionClient != null) {
//            peerConnectionClient.stopVideoSource();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FLAG == 0) {
            FLAG = 1;
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("call", "callResume");
                jsonObject.put("networkName", ssid);
                jsonObject.put("type", "server");
//                jsonObject.put("manual", "no");
//                Log.e(TAG, "onResume: " + mSocket.emit("call_connect", jsonObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        socketIO();
//        checkConnectivity();
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
            /*String contactName = args.getString(EXTRA_ROOMID);
           binding.contactNameCall.setText(contactName);*/
        }

        binding.captureFormatTextCall.setVisibility(View.GONE);
        binding.captureFormatSliderCall.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        disconnect();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (statsDialog.isShowing()) {
            statsDialog.dismiss();

        }
        this.unregisterReceiver(mWifiScanReceiver);
//        if (statsDialog.isShowing()) {
//            statsDialog.dismiss();
//        }
        activityRunning = false;
        rootEglBase.release();
        super.onDestroy();
        unregisterReceiver(mBatInfoTemp);
        unregisterReceiver(mBatInfoReceiver);

        if (isFinishing()) {
            Log.e("Destroying", "onDestroy: ");
            Username = "";
        } else {
            Log.i("Destroying", "onDestroy: is rotating.....");
        }
    }

    //    Receive for Battery Info
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batLevel = level + "%";
            if (level < 20) {
                Toast.makeText(CallActivity.this, "Battery low, charge device to continue.", Toast.LENGTH_SHORT).show();
            }
            getWifiSignal();
        }
    };

    //    Receive Temperature
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
            @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    // cast to CellInfoLte and call all the CellInfoLte methods you need
                    CellInfoLte ci = (CellInfoLte) cellInfo;
                    Log.e("", "LTE signal strength:  " + ci.getCellSignalStrength().getDbm());
                    LTESingalStrength = ci.getCellSignalStrength().getDbm();
                    signalStrengthTxt.setText("LTE Signal : " + LTESingalStrength + "dBm");
//                    Log.e("signallsss ", "LTE signal  " + ci.getCellSignalStrength().getDbm());
                    /*if (!merlin.isConnected()) {
                        finish();
                        startActivity(new Intent(CallActivity.this, AppRTCMainActivity.class));
                    }*/
                }
            }
            if (LTESingalStrength <= 0 && LTESingalStrength >= -50) {
                LTESignal = "Very Good";
            } else if (LTESingalStrength < -50 && LTESingalStrength >= -70) {
                LTESignal = "Good";
            } else if (LTESingalStrength < -70 && LTESingalStrength >= -80) {
                LTESignal = "Average";
            } else if (LTESingalStrength < -80 && LTESingalStrength >= -90) {
                LTESignal = "Low";
            } else if (LTESingalStrength < -90 && LTESingalStrength >= -110) {
                LTESignal = "Very Low";
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
//        Back up code
        /*if (isFinishing()) {

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
        }*/

        disconnect();
    }

    //    show Connection error when Internet is not available
    public void showConnectionError() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.showerror_conenction);
        RelativeLayout show_error = dialog.findViewById(R.id.show_error);
        signalStrengthTxt = dialog.findViewById(R.id.signals);
        dialog.show();
        show_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

                startActivity(new Intent(CallActivity.this, AppRTCMainActivity.class));
            }
        });
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

    //    network callback when Internet/network is available
    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            startActivity(new Intent(CallActivity.this, AppRTCMainActivity.class));
            finish();
            Log.e("TAG_INTERNET", "INTERNET CONNECTED");
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            showConnectionError();
            Log.e("TAG_INTERNET", "INTERNET LOST");
        }
    };

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(true);
        }
        return true;
    }

    //    update video view for Video call
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

        Log.v("room==>", roomId);
        onToggleMic();

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(false);
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(LOG_TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(LOG_TAG, "Call is connected in closed or error state");
            return;
        }
//      stats from watch(Brazen server app)
        showStats();
        if(statsDialog.isShowing()){
            bgColorStats.setBackgroundColor(Color.WHITE);
            tv_bat_temp.setTextColor(getResources().getColor(R.color.blackFontColor));
        tv_bat_lvl.setTextColor(getResources().getColor(R.color.blackFontColor));
        tv_net_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
        tv_net_signal.setTextColor(getResources().getColor(R.color.blackFontColor));
        tv_wifi_signal.setTextColor(getResources().getColor(R.color.blackFontColor));

        }
        // Update video view.
        updateVideoView();

        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);

        if (locationManager != null) {
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    lastLat = location.getLatitude() + "";
                    lastLong = location.getLongitude() + "";
                }
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    lastLat = location.getLatitude() + "";
                    lastLong = location.getLongitude() + "";
                }
            }
        }
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
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
        if (isConnectionError) {
            Intent intent = new Intent(CallActivity.this, CallActivity.class);
            intent.putExtra("false", "false");
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            isConnectionError = false;
        } else {
//            Intent intent = new Intent(CallActivity.this, CallActivity.class);
//            intent.putExtra("call1", "call");
//            intent.putExtra(EXTRA_ROOMID, "disconnected");
//            startActivity(intent);
            /*if (onCallJSON != null) {
                if (onCallJSON.has("manual")) {
                    if (statsDialog.isShowing()) {
                        statsDialog.dismiss();
                        finish();

                    }
                } else {*/
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

/*
                }
            }*/
//            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (!activityRunning) {
            Log.e(LOG_TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
//            send data to client app and send room id
            sendData();

        }
    }


    private void reportError(final String description) {
        runOnUiThread(() -> {
            if (!isError) {
                isError = true;
                if (isAPNEnabled(this)) {

                } else {
                    startActivity(new Intent(CallActivity.this, AppRTCMainActivity.class));
                }
                disconnectWithErrorMessage(description);
            }
        });
    }

    private static boolean isAPNEnabled(Context paramContext) {
        try {
            NetworkInfo networkInfo = ((ConnectivityManager) paramContext.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            return networkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
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
        /*final long delta = System.currentTimeMillis() - callStartedTimeMs;*/

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
        /*final long delta = System.currentTimeMillis() - callStartedTimeMs;*/
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
        /*final long delta = System.currentTimeMillis() - callStartedTimeMs;*/
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
        /*final long delta = System.currentTimeMillis() - callStartedTimeMs;*/
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

        JSONObject userId = new JSONObject();
        try {
            userId.put("username", Username + " DisConnected");
            mSocket.emit("connect user", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*mSocket.disconnect();
        mSocket.off("connect user", onNewUser);
        mSocket.off("jsondata", onNewMessage);
        mSocket.off("new_apk", onNewUpdate);
        mSocket.off("new_room", onNewRoom);
        mSocket.off("new_room_client", onNewRoomClient);*/

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


    @SuppressLint("SetTextI18n")
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
                dialog.setContentView(R.layout.custom_record_timer);
                signalStrengthTxt = dialog.findViewById(R.id.lteSignals);
                setversionCode = dialog.findViewById(R.id.vercode);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                ImageView QR_img = dialog.findViewById(R.id.QR_img);
                QR_img.setImageBitmap(bitmap);
                dialog.show();

                setversionCode.setText("v" + BuildConfig.VERSION_CODE);

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
        /*String loc = location.getLatitude() + " " + location.getLongitude();*/
        lastLat = location.getLatitude() + "";
        lastLong = location.getLongitude() + "";

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            /*if (location != null) {
                sendMessage(location.getLatitude() + "", location.getLongitude() + "", batteryTemperature, batLevel, LTESignal, wifiSignalLevel);
            }*/
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /*@Override
    public void onConnect() {
        finish();
        startActivity(new Intent(CallActivity.this, AppRTCMainActivity.class));
    }*/
}
