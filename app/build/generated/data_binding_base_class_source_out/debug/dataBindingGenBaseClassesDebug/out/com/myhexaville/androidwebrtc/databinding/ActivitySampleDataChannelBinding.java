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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class ActivitySampleDataChannelBinding extends ViewDataBinding {
  @NonNull
  public final ImageView image;

  @NonNull
  public final TextView remoteText;

  @NonNull
  public final Button sendButton;

  @NonNull
  public final EditText textInput;

  @NonNull
  public final Toolbar toolbar;

  protected ActivitySampleDataChannelBinding(DataBindingComponent _bindingComponent, View _root,
      int _localFieldCount, ImageView image, TextView remoteText, Button sendButton,
      EditText textInput, Toolbar toolbar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.image = image;
    this.remoteText = remoteText;
    this.sendButton = sendButton;
    this.textInput = textInput;
    this.toolbar = toolbar;
  }

  @NonNull
  public static ActivitySampleDataChannelBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivitySampleDataChannelBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivitySampleDataChannelBinding>inflate(inflater, com.myhexaville.androidwebrtc.R.layout.activity_sample_data_channel, root, attachToRoot, component);
  }

  @NonNull
  public static ActivitySampleDataChannelBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivitySampleDataChannelBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivitySampleDataChannelBinding>inflate(inflater, com.myhexaville.androidwebrtc.R.layout.activity_sample_data_channel, null, false, component);
  }

  public static ActivitySampleDataChannelBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  public static ActivitySampleDataChannelBinding bind(@NonNull View view,
      @Nullable DataBindingComponent component) {
    return (ActivitySampleDataChannelBinding)bind(component, view, com.myhexaville.androidwebrtc.R.layout.activity_sample_data_channel);
  }
}
