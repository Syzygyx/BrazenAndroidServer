//package com.doozycod.brazenwatch;
//
//import android.app.Application;
//
//
//public class TwilioChatApplication extends Application {
//  private static TwilioChatApplication instance;
//  private ChatClientManager basicClient;
//
//  public static TwilioChatApplication get() {
//    return instance;
//  }
//
//  public ChatClientManager getChatClientManager() {
//    return this.basicClient;
//  }
//
//  @Override
//  public void onCreate() {
//    super.onCreate();
//
//    TwilioChatApplication.instance = this;
//    basicClient = new ChatClientManager(getApplicationContext());
//  }
//}
