//package com.doozycod.brazenwatch;
//
//import android.content.Context;
//import android.provider.Settings;
//import android.util.Log;
//
//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.android.volley.VolleyLog.TAG;
//import static VideoActivity.SERVER_TOKEN_URL;
//
//public class AccessTokenFetcher {
//
//    private Context context;
//    String identity;
//    public AccessTokenFetcher(Context context) {
//        this.context = context;
//    }
//    public AccessTokenFetcher(Context context, String identity) {
//        this.context = context;
//        this.identity = identity;
//    }
//
//    public void fetch(final TaskCompletionListener<String, String> listener) {
//        JSONObject obj = new JSONObject(getTokenRequestParams(context));
//        String requestUrl =SERVER_TOKEN_URL;
//
//        JsonObjectRequest jsonObjReq =
//                new JsonObjectRequest(Request.Method.POST, requestUrl, obj, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        String token = null;
//                        try {
//                            token = response.getString("token");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
////                            listener.onError(e);
//                            listener.onError("Failed to parse token JSON response");
//                        }
//                        listener.onSuccess(token);
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, "onErrorResponse: " + error);
//                        listener.onError(error+" Failed to fetch token");
//                    }
//                });
//        jsonObjReq.setShouldCache(false);
//        TokenRequest.getInstance().addToRequestQueue(jsonObjReq);
////        context.add
//    }
//
//    private Map<String, String> getTokenRequestParams(Context context) {
//        String androidId =
//                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//        Map<String, String> params = new HashMap<>();
//        params.put("deviceId", androidId);
//        params.put("identity", "random");
//        return params;
//    }
//
///*    private String getStringResource(int id) {
//        Resources resources = TwilioChatApplication.get().getResources();
//        return resources.getString(id);
//    }*/
//
//}
