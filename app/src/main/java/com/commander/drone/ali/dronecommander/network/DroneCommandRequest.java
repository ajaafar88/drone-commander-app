package com.commander.drone.ali.dronecommander.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.commander.drone.ali.dronecommander.data.Constants;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ali on 12/11/2016.
 * https://developer.android.com/training/volley/request-custom.html
 * Create custom Request class to add header to the HTTP requests
 */

public class DroneCommandRequest extends JsonRequest<String> {
    private Response.Listener<String> mResponseListener;

    public DroneCommandRequest(int method, String url, JSONObject jsonRequest,
                               Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        super(method, url,jsonRequest != null? jsonRequest.toString() : null , responseListener, errorListener);
        mResponseListener = responseListener;
    }

    public DroneCommandRequest(int method, String url,
                               Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        this( method, url, null, responseListener, errorListener);
        mResponseListener = responseListener;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(json,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mResponseListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> mHeader = new HashMap<String, String>();

        mHeader.put("Content-Type","application/json; charset=utf-8");
        mHeader.put(Constants.COMMANDER_EMAIL_HEADER_KEY,Constants.COMMANDER_EMAIL_HEADER_VALUE);

        return mHeader;
    }

}
