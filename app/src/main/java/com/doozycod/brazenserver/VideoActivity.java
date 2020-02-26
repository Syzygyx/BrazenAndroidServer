package com.doozycod.brazenserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.doozycod.brazenserver.dialog.Dialog;
import com.doozycod.brazenserver.util.JWTUtils;
import com.doozycod.brazenwatch.BuildConfig;
import com.doozycod.brazenwatch.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.WriterException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.twilio.video.AudioCodec;
import com.twilio.video.CameraCapturer;
import com.twilio.video.CameraCapturer.CameraSource;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.doozycod.brazenserver.MyFirebaseMessagingService.setTokenInterface;


public class VideoActivity extends AppCompatActivity implements OnTokenReceive, OnRoomDecoded, LocationListener {
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "VideoActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    RemoteParticipant remoteParticipant;

    /*
     * Audio and video tracks can be created with names. This feature is useful for categorizing
     * tracks of participants. For example, if one participant publishes a video track with
     * ScreenCapturer and CameraCapturer with the names "screen" and "camera" respectively then
     * other participants can use RemoteVideoTrack#getName to determine which video track is
     * produced from the other participant's screen or camera.
     */
    private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
    private static final String LOCAL_VIDEO_TRACK_NAME = "camera";
    public static final String SERVER_TOKEN_URL = "https://straw-walrus-2048.twil.io/chat_token";
    /*
     * You must provide a Twilio Access Token to connect to the Video service
     */
//    private static final String TWILIO_ACCESS_TOKEN = BuildConfig.TWILIO_ACCESS_TOKEN;
//    private static final String ACCESS_TOKEN_SERVER = BuildConfig.TWILIO_ACCESS_TOKEN_SERVER;

    /*
     * Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */
    private String accessToken;
    String lastLat = "";
    String lastLong = "";
    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room room;
    private LocalParticipant localParticipant;

    /*
     * AudioCodec and VideoCodec represent the preferred codec for encoding and decoding audio and
     * video.
     */
    private AudioCodec audioCodec;
    private VideoCodec videoCodec;
    static String DEFAULT_CHANNEL_NAME = "general";

    /*
     * Encoding parameters represent the sender side bandwidth constraints.
     */
    private EncodingParameters encodingParameters;

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private VideoView primaryVideoView;
    private VideoView thumbnailVideoView;

    /*
     * Android shared preferences used for settings
     */
    private SharedPreferences preferences;
    boolean isNetworkEnabled = false;
    Location location;

    /*
     * Android application UI elements
     */

    private LocalDataTrack localDataTrack;
    private TextView videoStatusTextView;
    private CameraCapturerCompat cameraCapturerCompat;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private FloatingActionButton connectActionFab;
    private FloatingActionButton switchCameraActionFab;
    private FloatingActionButton localVideoActionFab;
    private FloatingActionButton muteActionFab;
    private ProgressBar reconnectingProgressBar;
    private AlertDialog connectDialog;
    private AudioManager audioManager;
    private String remoteParticipantIdentity;
    private MenuItem turnSpeakerOnMenuItem;
    private MenuItem turnSpeakerOffMenuItem;

    private int previousAudioMode;
    private boolean previousMicrophoneMute;
    private VideoRenderer localVideoView;
    private boolean disconnectedFromOnDestroy;
    private boolean isSpeakerPhoneEnabled = true;
    private boolean enableAutomaticSubscription;
    String token;
    QRGEncoder qrgEncoder;
    String roomId = null;
    Bitmap bitmap;
    android.app.Dialog dialog;
    android.app.Dialog statsDialog;
    private TelephonyManager telephonyManager;
    Timer timer;
    private final int interval = 1000 * 60; // 60 Seconds
    int LTESingalStrength = 0;
    private String android_id;
    private String batLevel, batteryTemperature, wifiSignalLevel, LTESignal = "";
    TextView tv_bat_lvl, tv_bat_temp, tv_wifi_signal, tv_net_signal, versioncode, setversionCode, signalStrengthTxt, signalStrengthTxtqr;
    protected LocationManager locationManager;
    String ssid;
    String networkSSID = "Doozycod";
    WifiManager wifiManager;
    List<ScanResult> getWifiSSIDs;
    WifiInfo wifiInfo;
    LinearLayout connection_state;

    //    AccessTokenFetcher accessTokenFetcher;
//    private ChatClientManager clientManager;
    // Update this identity for each individual user, for instance after they login
    public String generatePushToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e("TOKEN", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        token = task.getResult().getToken();
                        roomId = android_id + "token=" + token;
                        showQR();

                    }
                });
        return token;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        primaryVideoView = findViewById(R.id.primary_video_view);
        thumbnailVideoView = findViewById(R.id.thumbnail_video_view);
        videoStatusTextView = findViewById(R.id.video_status_textview);
        reconnectingProgressBar = findViewById(R.id.reconnecting_progress_bar);
        dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        statsDialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        signalStrengthTxtqr = dialog.findViewById(R.id.lteSignals);
        tv_net_signal = statsDialog.findViewById(R.id.networksignalsocket);

        connectActionFab = findViewById(R.id.connect_action_fab);
        switchCameraActionFab = findViewById(R.id.switch_camera_action_fab);
        localVideoActionFab = findViewById(R.id.local_video_action_fab);
        muteActionFab = findViewById(R.id.mute_action_fab);

//        clientManager = TwilioChatApplication.get().getChatClientManager();
        // Create the local data track
        localDataTrack = LocalDataTrack.create(this);
//      device Id
        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

//      for network Exception
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
//        register broadcast receiver for battery lvl & temp
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(this.mBatInfoTemp, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        checkPlayServices();
        // Listener for the signal strength.
        final PhoneStateListener mListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength sStrength) {
//                signalStrength = sStrength;
                getLTEsignalStrength();
            }
        };

        // Register the listener for the telephony manager
        telephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//        if (generatePushToken() == null) {
        generatePushToken();

//        accessTokenFetcher = new AccessTokenFetcher(this);
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

//        get location manager
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, VideoActivity.this);
                /* ... */
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
//            Log.e("FCM Token", "Token " + token);
//        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, VideoActivity.this);

        setTokenInterface(this);
        JWTUtils.setJWTUTils(this);
        displayLocationSettingsRequest(this);
        /*
         * Get shared preferences to read settings
         */
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(isSpeakerPhoneEnabled);

        /*
         * Check camera and microphone permissions. Needed in Android M.
         */
        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        } else {
            createAudioAndVideoTracks();
            setAccessToken();
        }

    /*    accessTokenFetcher.fetch(new TaskCompletionListener<String, String>() {
            @Override
            public void onSuccess(String token) {
                Log.e(TAG, "onSuccess: "+token );
//                createAccessManager(token);
//                buildClient(token, listener);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: "+message );
//                if (listener != null) {
//                    listener.onError(message);
//                }
            }
        });*/
        /*
         * Set the initial state of the UI
         */
        intializeUI();

//        fetch Location
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
                    Log.e(TAG, "onCreate: Lat long gps" + lastLat + "\n\t " + lastLong);

                }
            }
        }
        Log.e(TAG, "onCreate: Lat long " + lastLat + "\n\t " + lastLong);

//        retrieveAccessTokenfromServer();

    }

//    private void retrieveAccessTokenfromServer() {
//        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        String tokenURL = SERVER_TOKEN_URL + "?device=" + deviceId + "&identity=" + deviceId;
//        Ion.with(this)
//                .load(tokenURL)
//                .asJsonObject()
//                .setCallback(new FutureCallback<JsonObject>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonObject result) {
//                        if (e == null) {
//                            String accessToken = result.get("token").getAsString();
//
//                            Log.e(TAG, "Retrieved access token from server: " + accessToken);
//
////                            setTitle(mIdentity);
//
//                            ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
//                            ChatClient.Properties props = builder.createProperties();
//                            ChatClient.create(VideoActivity.this, accessToken, props, mChatClientCallback);
//
//                        } else {
//                            Log.e(TAG, e.getMessage(), e);
//                            Toast.makeText(VideoActivity.this,
//                                    R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                    }
//                });
//    }

    //    private void loadChannels() {
//        DEFAULT_CHANNEL_NAME = android_id;
//        mChatClient.getChannels().getChannel(DEFAULT_CHANNEL_NAME, new CallbackListener<Channel>() {
//            @Override
//            public void onSuccess(Channel channel) {
//                if (channel != null) {
//                    Log.e(TAG, "Joining Channel: Load " + DEFAULT_CHANNEL_NAME);
//                    Log.e(TAG, "Joining Channel: Load getStatus" + channel.getStatus());
//
//                    if (channel.getStatus() == Channel.ChannelStatus.JOINED) {
//                        // already in the channel, load the messages
//                    } else {
//                        // join the channel
//                        joinChannel(channel);
//                    }
//                } else {
//                    Log.e(TAG, "Creating Channel: " + DEFAULT_CHANNEL_NAME);
//
//                   /* mChatClient.getChannels().channelBuilder().withFriendlyName(DEFAULT_CHANNEL_NAME)
//                            .withType(Channel.ChannelType.PUBLIC)
//                            .build(new CallbackListener<Channel>() {
//                                @Override
//                                public void onSuccess(Channel channel) {
//                                    if (channel != null) {
//                                        Log.e(TAG, "Success creating channel");
//                                    }
//                                }
//
//                                @Override
//                                public void onError(ErrorInfo errorInfo) {
//                                    Log.e(TAG, "Error creating channel: " + errorInfo.getMessage());
//                                }
//                            });*/
//                    mChatClient.getChannels().createChannel(DEFAULT_CHANNEL_NAME,
//                            Channel.ChannelType.PUBLIC, new CallbackListener<Channel>() {
//                                @Override
//                                public void onSuccess(Channel channel) {
//                                    if (channel != null) {
//                                        Log.e(TAG, "Joining Channel: else" + DEFAULT_CHANNEL_NAME);
//                                        joinChannel(channel);
//                                    }
//                                }
//
//                                @Override
//                                public void onError(ErrorInfo errorInfo) {
//                                    Log.e(TAG, "Error creating channel: " + errorInfo.getMessage());
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onError(ErrorInfo errorInfo) {
//                Log.e(TAG, "Error retrieving channel: " + errorInfo.getMessage());
//                mChatClient.getChannels().createChannel(android_id, Channel.ChannelType.PUBLIC, new CallbackListener<Channel>() {
//                    @Override
//                    public void onSuccess(Channel channel) {
//                        if (channel != null) {
////                            joinChannel(channel);
//                        }
//                        Log.e(TAG, "onSuccess: " + channel.getUniqueName());
//                    }
//
//                    @Override
//                    public void onError(ErrorInfo errorInfo) {
//                        Log.e(TAG, "Error creating channel: " + errorInfo.getMessage());
//                    }
//                });
//            }
//
//        });
//
//    }
    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
//            Log.e(TAG, "isInternetAvailable: " + ipAddr.getHostName());
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {


            return false;
        }
    }

    public void sendMessage() {

        getLTEsignalStrength();
//        textField.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject.put("username", Username);
            jsonObject.put("latitute", lastLat);
            jsonObject.put("longitute", lastLong);
            jsonObject.put("batteryLevel", batLevel);
            jsonObject.put("batteryTemp", batteryTemperature);
            if (LTESingalStrength != 0) {
                jsonObject.put("networkSignal", LTESingalStrength + " dBm");
                Log.e(TAG, "sendMessage: (" + LTESingalStrength + ")");


            } else {
                jsonObject.put("networkSignal", "No Signal");
                Log.e(TAG, "sendMessage:1 (" + LTESingalStrength + ")");

            }

//            jsonObject.put("networkSignal", LTESingalStrength + " dBm");
            jsonObject.put("wifiSignal", wifiSignalLevel);
            jsonObject.put("device_id", android_id);

            if (isConnected(VideoActivity.this)) {
                jsonObject.put("chargingStatus", "1");
            } else {
                jsonObject.put("chargingStatus", "0");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (localDataTrack != null) {
            localDataTrack.send(jsonObject.toString());

        } else {
            Log.e(TAG, "Ignoring touch event because data track is release");
        }
//        Message.Options messageOptions = Message.options().withBody(jsonObject.toString());
//        mGeneralChannel.getMessages().sendMessage(messageOptions, new CallbackListener<Message>() {
//            @Override
//            public void onSuccess(Message message) {
//                VideoActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // need to modify user interface elements on the UI thread
//                        Log.e(TAG, "run: cleared " + message.getMessages().getLastConsumedMessageIndex());
//
//                    }
//                });
//            }
//        });
    }

//    private void joinChannel(final Channel channel) {
//        Log.d(TAG, "Joining Channel: join() " + channel.getFriendlyName());
//
//        channel.join(new StatusListener() {
//            @Override
//            public void onSuccess() {
//                mGeneralChannel = channel;
//                Log.e(TAG, "Joined default channel");
////                mGeneralChannel.addListener(mDefaultChannelListener);
////                sendMessage();
//
//                timer = new Timer();
//                timer.scheduleAtFixedRate(new TimerTask() {
//
//                    @Override
//                    public void run() {
//
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                // Stuff that updates the UI
//
////                                tv_bat_lvl.setText(batLevel);
////                                tv_bat_temp.setText(batteryTemperature);
////                                tv_wifi_signal.setText(wifiSignalLevel);
////                                if (LTESignal == null) {
////                                    LTESignal = "No Signal";
////                                    tv_net_signal.setText(LTESignal);
////                                } else {
////                                    tv_net_signal.setText(LTESignal);
////                                }
//                                sendMessage();
//                            }
//                        });
//                    }
//
//                }, 0, interval);
//
//            }
//
//            @Override
//            public void onError(ErrorInfo errorInfo) {
//                Log.e(TAG, "Error joining channel: onError() " + errorInfo.getMessage());
////                Log.e(TAG, "onError: "+channel.getMembers(). );
//                Log.e(TAG, "onError: Get Channel Members " + channel.getUniqueName());
//
//                if (errorInfo.getMessage().contains("Member already exists")) {
//                    leaveChannel(channel);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            joinChannel(channel);
//
//                        }
//                    });
//                }
//            }
//        });
//    }
//
//    private void leaveChannel(final Channel channel) {
////        Log.e(TAG, "Leaving Channel: " + channel.g());
//
//        channel.leave(new StatusListener() {
//            @Override
//            public void onSuccess() {
//                mGeneralChannel = channel;
//                Log.e(TAG, "Leaving default channel");
////
//            }
//
//            @Override
//            public void onError(ErrorInfo errorInfo) {
//                Log.e(TAG, "Error Leaving channel: " + errorInfo.getMessage());
//            }
//        });
//    }
//
//    private CallbackListener<ChatClient> mChatClientCallback =
//            new CallbackListener<ChatClient>() {
//                @Override
//                public void onSuccess(ChatClient chatClient) {
//                    mChatClient = chatClient;
//                    loadChannels();
//                    Log.d(TAG, "Success creating Twilio Chat Client");
//                }
//
//                @Override
//                public void onError(ErrorInfo errorInfo) {
//                    Log.e(TAG, "Error creating Twilio Chat Client: " + errorInfo.getMessage());
//                }
//            };

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
                            status.startResolutionForResult(VideoActivity.this, REQUEST_CHECK_SETTINGS);
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

    //    network callback when Internet/network is available
    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
//            isConnected = true;
//            startActivity(new Intent(VideoActivity.this, AppRTCMainActivity.class));
//            finish();
            Log.e("TAG_INTERNET", "INTERNET CONNECTED");
        }

        @Override
        public void onLost(Network network) {
//            isConnected = false;
            showConnectionError();
            Log.e("TAG_INTERNET", "INTERNET LOST");
        }
    };

    //    show Connection error when Internet is not available
    public void showConnectionError() {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.showerror_conenction);
        RelativeLayout show_error = dialog.findViewById(R.id.show_error);
        signalStrengthTxt = dialog.findViewById(R.id.signals);
        dialog.show();


        /*if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }*/
//        show_error.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//
//                startActivity(new Intent(CallActivity.this, AppRTCMainActivity.class));
//            }
//        });
    }

    void showStats() {
        statsDialog.setContentView(R.layout.stats_dialog);
        statsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tv_bat_lvl = statsDialog.findViewById(R.id.batlvlsocket);
//        bgColorStats = statsDialog.findViewById(R.id.background);
        tv_bat_temp = statsDialog.findViewById(R.id.batTempsocket);
        tv_net_signal = statsDialog.findViewById(R.id.networksignalsocket);
        versioncode = statsDialog.findViewById(R.id.versioncode);
        connection_state = statsDialog.findViewById(R.id.connection_state);
        tv_wifi_signal = statsDialog.findViewById(R.id.wifisignalsocket);
        statsDialog.show();
        versioncode.setText("v" + BuildConfig.VERSION_CODE);
        tv_bat_lvl.setText(batLevel);
        tv_bat_temp.setText(batteryTemperature);
        tv_wifi_signal.setText(wifiSignalLevel);
        if (LTESingalStrength != 0) {
            tv_net_signal.setText(LTESingalStrength + " dBm");

        } else {
            tv_net_signal.setText("No Signal");
        }
        statsDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Prevent dialog close on back press button
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        tv_bat_lvl.setText(batLevel);
                        tv_bat_temp.setText(batteryTemperature);
                        tv_wifi_signal.setText(wifiSignalLevel);
//                        if (LTESignal == null) {
//                            LTESignal = "No Signal";
//                            tv_net_signal.setText(LTESignal);
//                        } else {
//                            tv_net_signal.setText(LTESignal);
//                        }
                    }
                });
            }

        }, 0, interval);
    }

    public boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
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
                        /*if (!sharedPreferenceMethod.getWifiSSID().equals("")) {
                            connectToWifi(sharedPreferenceMethod.getWifiSSID(), sharedPreferenceMethod.getWifiPassword());
                        }*/
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
    //    Receive for Battery Info
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batLevel = level + "%";
            if (level < 20) {
                Toast.makeText(VideoActivity.this, "Battery low, charge device to continue.", Toast.LENGTH_SHORT).show();
            }
            getWifiSignal();
        }
    };

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

    //    Receive Temperature
    private BroadcastReceiver mBatInfoTemp = new BroadcastReceiver() {
        float temp = 0;

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            temp = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
            batteryTemperature = temp + " C";
            getLTEsignalStrength();
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
                    Log.d("LTE TAG", "LTE signal strength: " + ci.getCellSignalStrength().getDbm());
                    LTESingalStrength = ci.getCellSignalStrength().getDbm();
                    if (LTESingalStrength != 0) {
                        signalStrengthTxtqr.setText(LTESingalStrength + " dBm");
                        signalStrengthTxt.setText(LTESingalStrength + " dBm");
                        tv_net_signal.setText(LTESingalStrength + " dBm");
                    } else {
                        signalStrengthTxtqr.setText("No Signal");
                        signalStrengthTxt.setText("No Signal");
                        tv_net_signal.setText("No Signal");
                    }

                }
            }

        } catch (Exception e) {
            Log.e("LTE_TAG", "Exception: " + e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_video_activity, menu);
        turnSpeakerOnMenuItem = menu.findItem(R.id.menu_turn_speaker_on);
        turnSpeakerOffMenuItem = menu.findItem(R.id.menu_turn_speaker_off);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_turn_speaker_on:
            case R.id.menu_turn_speaker_off:
                boolean expectedSpeakerPhoneState = !audioManager.isSpeakerphoneOn();

                audioManager.setSpeakerphoneOn(expectedSpeakerPhoneState);
                turnSpeakerOffMenuItem.setVisible(expectedSpeakerPhoneState);
                turnSpeakerOnMenuItem.setVisible(!expectedSpeakerPhoneState);
                isSpeakerPhoneEnabled = expectedSpeakerPhoneState;

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            boolean cameraAndMicPermissionGranted = true;

            for (int grantResult : grantResults) {
                cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks();
                setAccessToken();
            } else {
                Toast.makeText(this,
                        R.string.permissions_needed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        if (isInternetAvailable()) {
            Log.e(TAG, "onResume: Internet connected");
        } else {
            Log.e(TAG, "onResume: Internet not connected");
        }
        /*
         * Update preferred audio and video codec in case changed in settings
         */
        audioCodec = getAudioCodecPreference(SettingsActivity.PREF_AUDIO_CODEC,
                SettingsActivity.PREF_AUDIO_CODEC_DEFAULT);
        videoCodec = getVideoCodecPreference(SettingsActivity.PREF_VIDEO_CODEC,
                SettingsActivity.PREF_VIDEO_CODEC_DEFAULT);
        enableAutomaticSubscription = getAutomaticSubscriptionPreference(SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT);
        /*
         * Get latest encoding parameters
         */
        final EncodingParameters newEncodingParameters = getEncodingParameters();

        /*
         * If the local video track was released when the app was put in the background, recreate.
         */
        if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            localVideoTrack = LocalVideoTrack.create(this,
                    true,
                    cameraCapturerCompat.getVideoCapturer(),
                    LOCAL_VIDEO_TRACK_NAME);
            localVideoTrack.addRenderer(localVideoView);

            /*
             * If connected to a Room then share the local video track.
             */
            if (localParticipant != null) {
                localParticipant.publishTrack(localVideoTrack);

                /*
                 * Update encoding parameters if they have changed.
                 */
                if (!newEncodingParameters.equals(encodingParameters)) {
                    localParticipant.setEncodingParameters(newEncodingParameters);
                }
            }
        }

        /*
         * Update encoding parameters
         */
        encodingParameters = newEncodingParameters;

        /*
         * Route audio through cached value.
         */
        audioManager.setSpeakerphoneOn(isSpeakerPhoneEnabled);

        /*
         * Update reconnecting UI
         */
        if (room != null) {
            reconnectingProgressBar.setVisibility((room.getState() != Room.State.RECONNECTING) ?
                    View.GONE :
                    View.VISIBLE);
            videoStatusTextView.setText("Connected to " + room.getName());
        }
    }

    @Override
    protected void onPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null) {
            /*
             * If this local video track is being shared in a Room, unpublish from room before
             * releasing the video track. Participants will be notified that the track has been
             * unpublished.
             */
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localVideoTrack);
            }

            localVideoTrack.release();
            localVideoTrack = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }
//        leaveChannel(mGeneralChannel);
        unregisterReceiver(mBatInfoTemp);
        unregisterReceiver(mBatInfoReceiver);
        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }

        super.onDestroy();
    }


    @SuppressLint("SetTextI18n")
    void showQR() {
        if (roomId.length() > 0) {
            Log.e("Show QRCODE", "\nRoom ID: " + roomId);
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
                dialog.setContentView(R.layout.show_qr_dialog);
                signalStrengthTxtqr = dialog.findViewById(R.id.lteSignals);
//                setversionCode = dialog.findViewById(R.id.vercode);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                ImageView QR_img = dialog.findViewById(R.id.QR_img);
                QR_img.setImageBitmap(bitmap);
                dialog.show();
                getLTEsignalStrength();
                if (LTESingalStrength != 0) {
                    signalStrengthTxtqr.setText(LTESingalStrength + " dBm");
                } else {
                    signalStrengthTxtqr.setText("No Signal");

                }
//                setversionCode.setText("v" + BuildConfig.VERSION_CODE);

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

    private boolean checkPermissionForCameraAndMicrophone() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    private void createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(this, true, LOCAL_AUDIO_TRACK_NAME);

        // Share your camera
        cameraCapturerCompat = new CameraCapturerCompat(this, getAvailableCameraSource());
        localVideoTrack = LocalVideoTrack.create(this,
                true,
                cameraCapturerCompat.getVideoCapturer(),
                LOCAL_VIDEO_TRACK_NAME);
        primaryVideoView.setMirror(true);
        localVideoTrack.addRenderer(primaryVideoView);
        localVideoView = primaryVideoView;
    }

    private CameraSource getAvailableCameraSource() {
        return (CameraCapturer.isSourceAvailable(CameraSource.FRONT_CAMERA)) ?
                (CameraSource.FRONT_CAMERA) :
                (CameraSource.BACK_CAMERA);
    }

    private void setAccessToken() {
//        if (!BuildConfig.USE_TOKEN_SERVER) {
//            /*
//             * OPTION 1 - Generate an access token from the getting started portal
//             * https://www.twilio.com/console/video/dev-tools/testing-tools and add
//             * the variable TWILIO_ACCESS_TOKEN setting it equal to the access token
//             * string in your local.properties file.
//             */
        this.accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTS2IxZmRlNTc0ZjI4NmE3YzcyMGUwYjQ3NDQ4NGEyYmMyLTE1ODE0MTU0OTYiLCJpc3MiOiJTS2IxZmRlNTc0ZjI4NmE3YzcyMGUwYjQ3NDQ4NGEyYmMyIiwic3ViIjoiQUNmZDA4ZjU2YTgzMTA1OWIwMmYyMTVjMzIwZjk2YTEzYSIsImV4cCI6MTU4MTQxOTA5NiwiZ3JhbnRzIjp7ImlkZW50aXR5IjoiYnJhemVuIiwidmlkZW8iOnsicm9vbSI6IkgxIn19fQ.mRuhXpyIqVO3baNAaohuyp10rm1rgpldDvSHdrWnoUA";
//        } else {
        /*
         * OPTION 2 - Retrieve an access token from your own web app.
         * Add the variable ACCESS_TOKEN_SERVER assigning it to the url of your
         * token server and the variable USE_TOKEN_SERVER=true to your
         * local.properties file.
         */
//            retrieveAccessTokenfromServer();
//        }
    }

    private void connectToRoom(String roomName, String accessToken) {
        configureAudio(true);
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                .roomName(roomName).dataTracks(Collections.singletonList(localDataTrack));

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (localAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(Collections.singletonList(localAudioTrack));
        }

        /*
         * Add local video track to connect options to share with participants.
         */
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
        }

        /*
         * Set the preferred audio and video codec for media.
         */
        connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
        connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));

        /*
         * Set the sender side encoding parameters.
         */
        connectOptionsBuilder.encodingParameters(encodingParameters);

        /*
         * Toggles automatic track subscription. If set to false, the LocalParticipant will receive
         * notifications of track publish events, but will not automatically subscribe to them. If
         * set to true, the LocalParticipant will automatically subscribe to tracks as they are
         * published. If unset, the default is true. Note: This feature is only available for Group
         * Rooms. Toggling the flag in a P2P room does not modify subscription behavior.
         */
        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription);

        room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
        setDisconnectAction();
    }

    /*
     * The initial state when there is no active room.
     */
    private void intializeUI() {
        connectActionFab.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.ic_video_call_white_24dp));
        connectActionFab.show();
       /* connectActionFab.setOnClickListener(


        );*/
        switchCameraActionFab.show();
        switchCameraActionFab.setOnClickListener(switchCameraClickListener());
        localVideoActionFab.show();
        localVideoActionFab.setOnClickListener(localVideoClickListener());
        muteActionFab.show();
        muteActionFab.setOnClickListener(muteClickListener());
    }

    /*
     * Get the preferred audio codec from shared preferences
     */
    private AudioCodec getAudioCodecPreference(String key, String defaultValue) {
        final String audioCodecName = preferences.getString(key, defaultValue);

        switch (audioCodecName) {
            case IsacCodec.NAME:
                return new IsacCodec();
            case OpusCodec.NAME:
                return new OpusCodec();
            case PcmaCodec.NAME:
                return new PcmaCodec();
            case PcmuCodec.NAME:
                return new PcmuCodec();
            case G722Codec.NAME:
                return new G722Codec();
            default:
                return new OpusCodec();
        }
    }

    /*
     * Get the preferred video codec from shared preferences
     */
    private VideoCodec getVideoCodecPreference(String key, String defaultValue) {
        final String videoCodecName = preferences.getString(key, defaultValue);

        switch (videoCodecName) {
            case Vp8Codec.NAME:
                boolean simulcast = preferences.getBoolean(SettingsActivity.PREF_VP8_SIMULCAST,
                        SettingsActivity.PREF_VP8_SIMULCAST_DEFAULT);
                return new Vp8Codec(simulcast);
            case H264Codec.NAME:
                return new H264Codec();
            case Vp9Codec.NAME:
                return new Vp9Codec();
            default:
                return new Vp8Codec();
        }
    }

    private boolean getAutomaticSubscriptionPreference(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private EncodingParameters getEncodingParameters() {
        final int maxAudioBitrate = Integer.parseInt(
                preferences.getString(SettingsActivity.PREF_SENDER_MAX_AUDIO_BITRATE,
                        SettingsActivity.PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT));
        final int maxVideoBitrate = Integer.parseInt(
                preferences.getString(SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE,
                        SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT));

        return new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
    }

    /*
     * The actions performed during disconnect.
     */
    private void setDisconnectAction() {
        connectActionFab.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.ic_call_end_white_24px));
        connectActionFab.show();
        connectActionFab.setOnClickListener(disconnectClickListener());
    }

    /*
     * Creates an connect UI dialog
     */
    private void showConnectDialog() {
        EditText roomEditText = new EditText(this);
        connectDialog = Dialog.createConnectDialog(roomEditText,
                connectClickListener(roomEditText),
                cancelConnectDialogClickListener(),
                this);
        connectDialog.show();
    }

    /*
     * Called when remote participant joins the room
     */
    @SuppressLint("SetTextI18n")
    private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            Snackbar.make(connectActionFab,
                    "Multiple participants are not currently support in this UI",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        remoteParticipantIdentity = remoteParticipant.getIdentity();
        videoStatusTextView.setText("RemoteParticipant " + remoteParticipantIdentity + " joined");

        /*
         * Add remote participant renderer
         */
        if (remoteParticipant.getRemoteVideoTracks().size() > 0) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);

            /*
             * Only render video tracks that are subscribed to
             */
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                addRemoteParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }

        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(remoteParticipantListener());
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private void addRemoteParticipantVideo(VideoTrack videoTrack) {
        moveLocalVideoToThumbnailView();
        primaryVideoView.setMirror(false);
        videoTrack.addRenderer(primaryVideoView);
    }

    private void moveLocalVideoToThumbnailView() {
        if (thumbnailVideoView.getVisibility() == View.GONE) {
//            thumbnailVideoView.setVisibility(View.VISIBLE);
            localVideoTrack.removeRenderer(primaryVideoView);
            localVideoTrack.addRenderer(thumbnailVideoView);
            localVideoView = thumbnailVideoView;
            thumbnailVideoView.setMirror(cameraCapturerCompat.getCameraSource() ==
                    CameraSource.FRONT_CAMERA);
        }
    }

    /*
     * Called when remote participant leaves the room
     */
    @SuppressLint("SetTextI18n")
    private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
        videoStatusTextView.setText("RemoteParticipant " + remoteParticipant.getIdentity() +
                " left.");
        remoteParticipantIdentity = "";
        if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
            return;
        }

        /*
         * Remove remote participant renderer
         */
        if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);

            /*
             * Remove video only if subscribed to participant track
             */
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }
        moveLocalVideoToPrimaryView();
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeRenderer(primaryVideoView);
    }

    private void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
//            thumbnailVideoView.setVisibility(View.GONE);
            if (localVideoTrack != null) {
                localVideoTrack.removeRenderer(thumbnailVideoView);
                localVideoTrack.addRenderer(primaryVideoView);
            }
            localVideoView = primaryVideoView;
            primaryVideoView.setMirror(cameraCapturerCompat.getCameraSource() ==
                    CameraSource.FRONT_CAMERA);
        }
    }

    /*
     * Room events listener
     */
    @SuppressLint("SetTextI18n")
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                localParticipant = room.getLocalParticipant();
                videoStatusTextView.setText("Connected to " + room.getName());
                setTitle(room.getName());
                Log.e(TAG, "onConnected: " + room.getSid());
                for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
//                    remoteParticipant.
                    break;
                }

//                sendMessage();
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage();
                            }
                        });
                    }

                }, 0, interval);
                if (connection_state != null) {
                    connection_state.setVisibility(View.GONE);
                }
                showStats();
//                dialog.dismiss();

            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {
                videoStatusTextView.setText("Reconnecting to " + room.getName());
                reconnectingProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "onReconnecting: " + twilioException.getExplanation());
                Toast.makeText(VideoActivity.this, "Reconnecting...", Toast.LENGTH_LONG).show();
                if (connection_state != null) {
                    connection_state.setVisibility(View.VISIBLE);
                }
                if (isInternetAvailable()) {
                    showStats();
                } else {
                    showQR();
                    statsDialog.dismiss();
                }

                /*showQR();
                if (statsDialog.isShowing()) {
                    statsDialog.dismiss();
                }*/
            }

            @Override
            public void onReconnected(@NonNull Room room) {
                videoStatusTextView.setText("Connected to " + room.getName());
                reconnectingProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "onReconnected: ");
                if (connection_state != null) {

                    connection_state.setVisibility(View.GONE);
                }
                showStats();
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                videoStatusTextView.setText("Failed to connect");
                Log.e(TAG, "onConnectFailure: " + e);
                configureAudio(false);
                intializeUI();
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                Log.e(TAG, "onDisconnected: " + e);

                if (isInternetAvailable()) {

                } else {
                    Toast.makeText(VideoActivity.this, "\tSomething went wrong,\n" +
                            "Check Your Internet Connectivity!", Toast.LENGTH_SHORT).show();
                }
                if (remoteParticipantIdentity.equals("")) {
                    localParticipant = null;
                    videoStatusTextView.setText("Disconnected from " + room.getName());

                   /* if (e == null) {
                    } else {
                        showQR();
                    }*/
                   /* if (e != null) {
                        if (e.getExplanation().contains("Signaling connection timed out")) {
                            showQR();
                        }


                    }*/

                    reconnectingProgressBar.setVisibility(View.GONE);
                    VideoActivity.this.room = null;
                    // Only reinitialize the UI if disconnect was not called from onDestroy()
                    if (!disconnectedFromOnDestroy) {
                        configureAudio(false);
                        intializeUI();
                        moveLocalVideoToPrimaryView();
                    }
                  /*  if (!dialog.isShowing()) {
                        showQR();
                    }*/
                }
            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
                addRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
                removeRemoteParticipant(remoteParticipant);
                Log.e(TAG, "onParticipantDisconnected: " + remoteParticipant.getIdentity());
//                Toast.makeText(VideoActivity.this, "Reconnecting...", Toast.LENGTH_SHORT).show();

                showQR();
                timer.cancel();
//                leaveChannel(mGeneralChannel);
                statsDialog.dismiss();

                if (room != null) {
                    room.disconnect();
                }

            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }


    RemoteDataTrack.Listener dataTrackListener = new RemoteDataTrack.Listener() {

        @Override
        public void onMessage(@NonNull RemoteDataTrack remoteDataTrack, @NonNull ByteBuffer messageBuffer) {
            Log.e(TAG, "onMessage: byte buffer " + messageBuffer.toString());

        }

        @Override
        public void onMessage(@NonNull RemoteDataTrack remoteDataTrack, @NonNull String message) {
            Log.e(TAG, "onMessage: " + message);
        }


    };

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 111)
                        .show();
            } else {
                Log.e(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    private RemoteParticipant.Listener remoteParticipantListener() {
        return new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackPublished(RemoteParticipant remoteParticipant,
                                              RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(TAG, String.format("onAudioTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(),
                        remoteAudioTrackPublication.getTrackName()));
                videoStatusTextView.setText("onAudioTrackPublished");
            }

            @Override
            public void onAudioTrackUnpublished(RemoteParticipant remoteParticipant,
                                                RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(TAG, String.format("onAudioTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(),
                        remoteAudioTrackPublication.getTrackName()));
                videoStatusTextView.setText("onAudioTrackUnpublished");
            }

            @Override
            public void onDataTrackPublished(RemoteParticipant remoteParticipant,
                                             RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(TAG, String.format("onDataTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(),
                        remoteDataTrackPublication.getTrackName()));
                videoStatusTextView.setText("onDataTrackPublished");
            }

            @Override
            public void onDataTrackUnpublished(RemoteParticipant remoteParticipant,
                                               RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(TAG, String.format("onDataTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(),
                        remoteDataTrackPublication.getTrackName()));
                videoStatusTextView.setText("onDataTrackUnpublished");
            }

            @Override
            public void onVideoTrackPublished(RemoteParticipant remoteParticipant,
                                              RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(TAG, String.format("onVideoTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(),
                        remoteVideoTrackPublication.getTrackName()));
                videoStatusTextView.setText("onVideoTrackPublished");
            }

            @Override
            public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant,
                                                RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(TAG, String.format("onVideoTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(),
                        remoteVideoTrackPublication.getTrackName()));
                videoStatusTextView.setText("onVideoTrackUnpublished");
            }

            @Override
            public void onAudioTrackSubscribed(RemoteParticipant remoteParticipant,
                                               RemoteAudioTrackPublication remoteAudioTrackPublication,
                                               RemoteAudioTrack remoteAudioTrack) {
                Log.i(TAG, String.format("onAudioTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(),
                        remoteAudioTrack.getName()));
                videoStatusTextView.setText("onAudioTrackSubscribed");
            }

            @Override
            public void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                                 RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                 RemoteAudioTrack remoteAudioTrack) {
                Log.i(TAG, String.format("onAudioTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(),
                        remoteAudioTrack.getName()));
                videoStatusTextView.setText("onAudioTrackUnsubscribed");
            }

            @Override
            public void onAudioTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                       RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                       TwilioException twilioException) {
                Log.i(TAG, String.format("onAudioTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
                videoStatusTextView.setText("onAudioTrackSubscriptionFailed");
            }

            @Override
            public void onDataTrackSubscribed(RemoteParticipant remoteParticipant,
                                              RemoteDataTrackPublication remoteDataTrackPublication,
                                              RemoteDataTrack remoteDataTrack) {

                remoteDataTrack.setListener(dataTrackListener);
                sendMessage();

                Log.i(TAG, String.format("onDataTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(),
                        remoteDataTrack.getName()));
                videoStatusTextView.setText("onDataTrackSubscribed");
            }

            @Override
            public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                                RemoteDataTrackPublication remoteDataTrackPublication,
                                                RemoteDataTrack remoteDataTrack) {
                Log.i(TAG, String.format("onDataTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(),
                        remoteDataTrack.getName()));
                videoStatusTextView.setText("onDataTrackUnsubscribed");
            }

            @Override
            public void onDataTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                      RemoteDataTrackPublication remoteDataTrackPublication,
                                                      TwilioException twilioException) {
                Log.i(TAG, String.format("onDataTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
                videoStatusTextView.setText("onDataTrackSubscriptionFailed");
            }

            @Override
            public void onVideoTrackSubscribed(RemoteParticipant remoteParticipant,
                                               RemoteVideoTrackPublication remoteVideoTrackPublication,
                                               RemoteVideoTrack remoteVideoTrack) {
                Log.i(TAG, String.format("onVideoTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack.isEnabled(),
                        remoteVideoTrack.getName()));
                videoStatusTextView.setText("onVideoTrackSubscribed");
                addRemoteParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                                 RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                 RemoteVideoTrack remoteVideoTrack) {
                Log.i(TAG, String.format("onVideoTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack.isEnabled(),
                        remoteVideoTrack.getName()));
                videoStatusTextView.setText("onVideoTrackUnsubscribed");
                removeParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                       RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                       TwilioException twilioException) {
                Log.i(TAG, String.format("onVideoTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
                videoStatusTextView.setText("onVideoTrackSubscriptionFailed");
                Snackbar.make(connectActionFab,
                        String.format("Failed to subscribe to %s video track",
                                remoteParticipant.getIdentity()),
                        Snackbar.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                            RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                             RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                            RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                             RemoteVideoTrackPublication remoteVideoTrackPublication) {
            }
        };
    }

    private DialogInterface.OnClickListener connectClickListener(final EditText roomEditText) {
        return (dialog, which) -> {
            /*
             * Connect to room
             */
        };
    }

    private View.OnClickListener disconnectClickListener() {
        return v -> {
            /*
             * Disconnect from room
             */
            if (room != null) {
                room.disconnect();
            }
            intializeUI();
        };
    }

    private View.OnClickListener connectActionClickListener() {
        return v -> showConnectDialog();
    }

    private DialogInterface.OnClickListener cancelConnectDialogClickListener() {
        return (dialog, which) -> {
            intializeUI();
            connectDialog.dismiss();
        };
    }

    private View.OnClickListener switchCameraClickListener() {
        return v -> {
            if (cameraCapturerCompat != null) {
                CameraSource cameraSource = cameraCapturerCompat.getCameraSource();
                cameraCapturerCompat.switchCamera();
                if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                    thumbnailVideoView.setMirror(cameraSource == CameraSource.BACK_CAMERA);
                } else {
                    primaryVideoView.setMirror(cameraSource == CameraSource.BACK_CAMERA);
                }
            }
        };
    }

    private View.OnClickListener localVideoClickListener() {
        return v -> {
            /*
             * Enable/disable the local video track
             */
            if (localVideoTrack != null) {
                boolean enable = !localVideoTrack.isEnabled();
                localVideoTrack.enable(enable);
                int icon;
                if (enable) {
                    icon = R.drawable.ic_videocam_white_24dp;
                    switchCameraActionFab.show();
                } else {
                    icon = R.drawable.ic_videocam_off_black_24dp;
                    switchCameraActionFab.hide();
                }
                localVideoActionFab.setImageDrawable(
                        ContextCompat.getDrawable(VideoActivity.this, icon));
            }
        };
    }

    private View.OnClickListener muteClickListener() {
        return v -> {
            /*
             * Enable/disable the local audio track. The results of this operation are
             * signaled to other Participants in the same Room. When an audio track is
             * disabled, the audio is muted.
             */
            if (localAudioTrack != null) {
                boolean enable = !localAudioTrack.isEnabled();
                localAudioTrack.enable(enable);
                int icon = enable ?
                        R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_black_24dp;
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(
                        VideoActivity.this, icon));
            }
        };
    }

//    private void retrieveAccessTokenfromServer() {
//        Ion.with(this)
//                .load(String.format("%s?identity=%s", ACCESS_TOKEN_SERVER,
//                        UUID.randomUUID().toString()))
//                .asString()
//                .setCallback((e, token) -> {
//                    if (e == null) {
//                        VideoActivity.this.accessToken = token;
//                    } else {
//                        Toast.makeText(VideoActivity.this,
//                                R.string.error_retrieving_access_token, Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });
//    }

    private void configureAudio(boolean enable) {
        if (enable) {
            previousAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch
            requestAudioFocus();
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            /*
             * Always disable microphone mute during a WebRTC call.
             */
            previousMicrophoneMute = audioManager.isMicrophoneMute();
            audioManager.setMicrophoneMute(false);
        } else {
            audioManager.setMode(previousAudioMode);
            audioManager.abandonAudioFocus(null);
            audioManager.setMicrophoneMute(previousMicrophoneMute);
        }
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            AudioFocusRequest focusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(
                                    i -> {
                                    })
                            .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    @Override
    public void getToken(String token) {
        try {
            JWTUtils.decoded(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        connectToRoom();

    }

    @Override
    public void getRoomId(String roomId, String token) {
        Log.e(TAG, "getRoomId: " + roomId + " \n token =" + token);
        connectToRoom(roomId, token);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLat = location.getLatitude() + "";
        lastLong = location.getLongitude() + "";
        Log.e(TAG, "onLocationChanged: Lat long " + lastLat + "\n\t " + lastLong);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lastLat = location.getLatitude() + "";
            lastLong = location.getLongitude() + "";

            /*if (location != null) {
                sendMessage(location.getLatitude() + "", location.getLongitude() + "", batteryTemperature, batLevel, LTESignal, wifiSignalLevel);
            }*/
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled: Lat long " + lastLat + "\n\t " + lastLong);

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
