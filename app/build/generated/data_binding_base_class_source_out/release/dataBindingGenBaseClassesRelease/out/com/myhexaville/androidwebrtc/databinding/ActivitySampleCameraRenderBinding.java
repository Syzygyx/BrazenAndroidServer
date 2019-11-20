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

public abstract class ActivitySampleCameraRenderBinding extends ViewDataBinding {
  @NonNull
  public final SurfaceViewRenderer surfaceView;

  @NonNull
  public final Toolbar toolbar;

  protected ActivitySampleCameraRenderBinding(DataBindingComponent _bindingComponent, View _root,
      int _localFieldCount, SurfaceViewRenderer surfaceView, Toolbar toolbar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.surfaceView = surfaceView;
    this.toolbar = toolbar;
  }

  @NonNull
  public static ActivitySampleCameraRenderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivitySampleCameraRenderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivitySampleCameraRenderBinding>inflate(inflater, com.myhexaville.androidwebrtc.R.layout.activity_sample_camera_render, root, attachToRoot, component);
  }

  @NonNull
  public static ActivitySampleCameraRenderBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivitySampleCameraRenderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivitySampleCameraRenderBinding>inflate(inflater, com.myhexaville.androidwebrtc.R.layout.activity_sample_camera_render, null, false, component);
  }

  public static ActivitySampleCameraRenderBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  public static ActivitySampleCameraRenderBinding bind(@NonNull View view,
      @Nullable DataBindingComponent component) {
    return (ActivitySampleCameraRenderBinding)bind(component, view, com.myhexaville.androidwebrtc.R.layout.activity_sample_camera_render);
  }
}
