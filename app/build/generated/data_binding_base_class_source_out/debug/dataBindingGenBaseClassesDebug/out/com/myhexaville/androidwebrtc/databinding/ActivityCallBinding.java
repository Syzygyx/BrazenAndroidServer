package com.myhexaville.androidwebrtc.databinding;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.myhexaville.androidwebrtc.app_rtc_sample.view.PercentFrameLayout;
import java.lang.Deprecated;
import java.lang.Object;
import org.webrtc.SurfaceViewRenderer;

public abstract class ActivityCallBinding extends ViewDataBinding {
  @NonNull
  public final ImageButton buttonCallDisconnect;

  @NonNull
  public final ImageButton buttonCallSwitchCamera;

  @NonNull
  public final ImageButton buttonCallToggleMic;

  @NonNull
  public final LinearLayout buttonsCallContainer;

  @NonNull
  public final FrameLayout callFragmentContainer;

  @NonNull
  public final SeekBar captureFormatSliderCall;

  @NonNull
  public final TextView captureFormatTextCall;

  @NonNull
  public final TextView contactNameCall;

  @NonNull
  public final PercentFrameLayout localVideoLayout;

  @NonNull
  public final SurfaceViewRenderer localVideoView;

  @NonNull
  public final PercentFrameLayout remoteVideoLayout;

  @NonNull
  public final SurfaceViewRenderer remoteVideoView;

  protected ActivityCallBinding(Object _bindingComponent, View _root, int _localFieldCount,
      ImageButton buttonCallDisconnect, ImageButton buttonCallSwitchCamera,
      ImageButton buttonCallToggleMic, LinearLayout buttonsCallContainer,
      FrameLayout callFragmentContainer, SeekBar captureFormatSliderCall,
      TextView captureFormatTextCall, TextView contactNameCall, PercentFrameLayout localVideoLayout,
      SurfaceViewRenderer localVideoView, PercentFrameLayout remoteVideoLayout,
      SurfaceViewRenderer remoteVideoView) {
    super(_bindingComponent, _root, _localFieldCount);
    this.buttonCallDisconnect = buttonCallDisconnect;
    this.buttonCallSwitchCamera = buttonCallSwitchCamera;
    this.buttonCallToggleMic = buttonCallToggleMic;
    this.buttonsCallContainer = buttonsCallContainer;
    this.callFragmentContainer = callFragmentContainer;
    this.captureFormatSliderCall = captureFormatSliderCall;
    this.captureFormatTextCall = captureFormatTextCall;
    this.contactNameCall = contactNameCall;
    this.localVideoLayout = localVideoLayout;
    this.localVideoView = localVideoView;
    this.remoteVideoLayout = remoteVideoLayout;
    this.remoteVideoView = remoteVideoView;
  }

  @NonNull
  public static ActivityCallBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_call, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityCallBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityCallBinding>inflateInternal(inflater, com.myhexaville.androidwebrtc.R.layout.activity_call, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityCallBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_call, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityCallBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityCallBinding>inflateInternal(inflater, com.myhexaville.androidwebrtc.R.layout.activity_call, null, false, component);
  }

  public static ActivityCallBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static ActivityCallBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityCallBinding)bind(component, view, com.myhexaville.androidwebrtc.R.layout.activity_call);
  }
}
