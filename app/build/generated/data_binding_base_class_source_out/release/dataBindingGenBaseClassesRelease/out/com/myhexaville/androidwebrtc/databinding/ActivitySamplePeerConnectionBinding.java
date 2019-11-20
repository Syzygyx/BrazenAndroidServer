package com.myhexaville.androidwebrtc.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.webrtc.SurfaceViewRenderer;

public abstract class ActivitySamplePeerConnectionBinding extends ViewDataBinding {
  @NonNull
  public final SurfaceViewRenderer surfaceView;

  @NonNull
  public final SurfaceViewRenderer surfaceView2;

  @NonNull
  public final Toolbar toolbar;

  protected ActivitySamplePeerConnectionBinding(DataBindingComponent _bindingComponent, View _root,
      int _localFieldCount, SurfaceViewRenderer surfaceView, SurfaceViewRenderer surfaceView2,
      Toolbar toolbar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.surfaceView = surfaceView;
    this.surfaceView2 = surfaceView2;
    this.toolbar = toolbar;
  }

  @NonNull
  public static ActivitySamplePeerConnectionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivitySamplePeerConnectionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivitySamplePeerConnectionBinding>inflate(inflater, com.myhexaville.androidwebrtc.R.layout.activity_sample_peer_connection, root, attachToRoot, component);
  }

  @NonNull
  public static ActivitySamplePeerConnectionBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivitySamplePeerConnectionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivitySamplePeerConnectionBinding>inflate(inflater, com.myhexaville.androidwebrtc.R.layout.activity_sample_peer_connection, null, false, component);
  }

  public static ActivitySamplePeerConnectionBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  public static ActivitySamplePeerConnectionBinding bind(@NonNull View view,
      @Nullable DataBindingComponent component) {
    return (ActivitySamplePeerConnectionBinding)bind(component, view, com.myhexaville.androidwebrtc.R.layout.activity_sample_peer_connection);
  }
}
