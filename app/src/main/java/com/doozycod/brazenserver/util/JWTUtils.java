package com.doozycod.brazenserver.util;

import android.util.Base64;
import android.util.Log;

import com.doozycod.brazenserver.OnRoomDecoded;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JWTUtils {
    public static OnRoomDecoded onRoomDecoded;


    public static void setJWTUTils(OnRoomDecoded roomId) {

        onRoomDecoded = roomId;
    }

    public static void decoded(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.e("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.e("JWT_DECODED", "Body: " + getJson(split[1]));
            JSONObject jsonObject = new JSONObject(getJson(split[1])).getJSONObject("grants").getJSONObject("video");
            String room = jsonObject.getString("room");
            Log.e("JWT_DECODED room", "room: " + room);
            onRoomDecoded.getRoomId(room,JWTEncoded);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            //Error
        }
    }
    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
