//package com.doozycod.brazenwatch;
//
//
//import android.content.Context;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.Volley;
//
//public class TokenRequest {
//    private static TokenRequest mInstance;
//
//    private RequestQueue mRequestQueue;
////    private Context context;
//    private TokenRequest() {
//        mRequestQueue = Volley.newRequestQueue(TwilioChatApplication.get().getApplicationContext());
//
//    }
//
//    public static synchronized TokenRequest getInstance() {
//        if (mInstance == null) {
//            mInstance = new TokenRequest();
//        }
//        return mInstance;
//    }
//
//    public <T> void addToRequestQueue(Request<T> req) {
//        mRequestQueue.add(req);
//    }
//}
