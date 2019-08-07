package io.xpush.chat.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.services.XPushService;


public class LoginRequest extends StringRequest {

    private Context baseContext;

    public LoginRequest(Context context, String url, Map<String, String> params, Response.Listener<JSONObject> listener,
                        Response.ErrorListener errorListener) {
        super(url, params, listener, errorListener);

        baseContext = context;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String notiId = pref.getString("REGISTERED_NOTIFICATION_ID", null);
        if( notiId != null ) {
            params.put("N", notiId);
        }
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        JSONObject parsed = null;
        try {
            parsed = new JSONObject( new String(response.data) );

            Log.d(TAG, parsed.toString());

            if ("ok".equalsIgnoreCase(parsed.getString("status"))) {
                JSONObject result = parsed.getJSONObject("result");

                String token = null;
                if( result.has("token")) {
                    token = result.getString("token");
                }
                String server = result.getString("server");
                String serverUrl = result.getString("serverUrl");

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(baseContext);
                SharedPreferences.Editor editor = pref.edit();

                XPushSession xpushSession = new XPushSession();

                xpushSession.setId(getParams().get("U"));
                xpushSession.setPassword(getParams().get("PW"));
                xpushSession.setDeviceId(getParams().get("D"));

                if( result.getJSONObject("user").has("DT") ){
                    JSONObject data = null;
                    try {
                        Object dt = result.getJSONObject("user").get("DT");
                        if( dt instanceof String ){
                            data =  new JSONObject( (String)dt );
                        } else if ( dt instanceof JSONObject ){
                            data = (JSONObject) dt;
                        }

                    } catch( JSONException je ){
                        je.printStackTrace();
                    }

                    if( data.has("I") ){
                        xpushSession.setImage(data.getString("I"));
                    }

                    if( data.has("NM") ) {
                        xpushSession.setName(data.getString("NM"));
                    }
                }

                xpushSession.setToken(token);
                xpushSession.setServerName(server);
                xpushSession.setServerUrl(serverUrl);
                xpushSession.setNotiId(getParams().get("N"));

                editor.putString("XPUSH_SESSION", xpushSession.toJSON().toString());
                editor.commit();

                XPushService.actionStart(baseContext);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            parsed = null;
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}