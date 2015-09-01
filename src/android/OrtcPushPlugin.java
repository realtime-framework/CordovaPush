package co.realtime.plugins.android.cordovapush;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ibt.ortc.api.Ortc;
import ibt.ortc.extensibility.OnConnected;
import ibt.ortc.extensibility.OnDisconnected;
import ibt.ortc.extensibility.OnException;
import ibt.ortc.extensibility.OnMessage;
import ibt.ortc.extensibility.OnReconnected;
import ibt.ortc.extensibility.OnReconnecting;
import ibt.ortc.extensibility.OnSubscribed;
import ibt.ortc.extensibility.OnUnsubscribed;
import ibt.ortc.extensibility.OrtcClient;
import ibt.ortc.extensibility.OrtcFactory;

public class OrtcPushPlugin extends CordovaPlugin {

    public static final String TAG = "ORTCPushPlugin";
    public static final String ACTION_CHECK_NOTIFICATIONS = "checkForNotifications";
    public static final String ACTION_SET_ICON = "setApplicationIconBadgeNumber";
    public static final String ACTION_LOG = "log";
    public static final String ACTION_CONNECT = "connect";
    public static final String ACTION_DISCONNECT = "disconnect";
    public static final String ACTION_SUBSCRIBE = "subscribe";
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    public static final String ACTION_CANCEL_NOTIFICATIONS = "cancelAllLocalNotifications";
    public static final String ACTION_SEND_MESSAGE = "send";
    private OrtcClient client;
    private CallbackContext callback = null;
    private static CordovaWebView gWebView;
    private static Bundle gCachedExtras = null;
    private static boolean gForeground = false;
    private static CordovaInterface sCordova;
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        sCordova = cordova;
        gForeground = true;
        try {
            Ortc ortc = new Ortc();

            OrtcFactory factory;

            factory = ortc.loadOrtcFactory("IbtRealtimeSJ");

            client = factory.createClient();

            client.setHeartbeatActive(true);


            client.onConnected = new OnConnected() {
                @Override
                public void run(OrtcClient ortcClient) {
                    callback.success();
                }
            };


            client.onDisconnected = new OnDisconnected() {
                @Override
                public void run(OrtcClient ortcClient) {
                    Log.i(TAG,"Disconnected" );
                }
            };

            client.onUnsubscribed = new OnUnsubscribed() {
                @Override
                public void run(OrtcClient ortcClient, String channel) {
                    Log.i(TAG,"Unsubscribed from:" + channel);
                    callback.success();
                }
            };

            client.onSubscribed = new OnSubscribed() {
                @Override
                public void run(OrtcClient ortcClient, String s) {
                    callback.success();
                }
            };


            client.onReconnected = new OnReconnected(){
                @Override
                public void run(OrtcClient sender) {
                    Log.i(TAG, "Reconnected");
                }
            };
            client.onReconnecting = new OnReconnecting(){
                @Override
                public void run(OrtcClient sender) {
                    Log.i(TAG, "Reconnecting");
                }
            };

            client.onException = new OnException() {
                @Override
                public void run(OrtcClient ortcClient, Exception e) {
                    onException(e.toString());
                }
            };

        } catch (Exception e) {
            Log.i(TAG,e.toString());
        }

    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        gForeground = false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        gForeground = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gForeground = false;
        gWebView = null;
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        callback = callbackContext;
        gWebView = this.webView;

        try {
            if (ACTION_LOG.equals(action)) {
                Log.i(TAG,args.get(0).toString());
                callbackContext.success();
                return true;
            }
            else if(ACTION_CONNECT.equals(action)){
                JSONObject arg_object = args.getJSONObject(0);
                client.setClusterUrl(arg_object.getString("url"));
                client.setConnectionMetadata(arg_object.getString("metadata"));
                client.setGoogleProjectId(arg_object.getString("projectId"));
                client.setApplicationContext(this.cordova.getActivity().getApplicationContext());
                client.connect(arg_object.getString("appkey"),arg_object.getString("token"));

                return true;
            }
            else if(ACTION_DISCONNECT.equals(action)){
                client.disconnect();
                return true;
            }
            else if(ACTION_SUBSCRIBE.equals(action)){
                JSONObject arg_object = args.getJSONObject(0);
                String channel = arg_object.getString("channel");
                client.subscribeWithNotifications(channel, true, new OnMessage() {
                    @Override
                    public void run(OrtcClient ortcClient, final String channel, final String message) {
                        cordova.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Received message:" + message + " from channel: " + channel);

                            }
                        });

                    }
                });
                return true;
            }
            else if(ACTION_UNSUBSCRIBE.equals(action)){
                JSONObject arg_object = args.getJSONObject(0);
                String channel = arg_object.getString("channel");
                client.unsubscribe(channel);
                return true;
            }
            else if(ACTION_CANCEL_NOTIFICATIONS.equals(action)){
                final NotificationManager notificationManager = (NotificationManager) this.cordova.getActivity().getSystemService(this.cordova.getActivity().NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                callbackContext.success();
                return true;
            }
            else if(ACTION_SEND_MESSAGE.equals(action)){
                JSONObject arg_object = args.getJSONObject(0);
                String channel = arg_object.getString("channel");
                String message = arg_object.getString("message");
                client.send(channel,message);
                return true;
            }
            else if(ACTION_CHECK_NOTIFICATIONS.equals(action)){
                if ( gCachedExtras != null) {
                    Log.v(TAG, "sending cached extras");
                    sendExtras(gCachedExtras);
                    gCachedExtras = null;
                }
                callbackContext.success();
                return true;
            }
            else if(ACTION_SET_ICON.equals(action)){
                Log.i(TAG,"Set icon badge number not implemented in Android");
                callbackContext.success();
                return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    public static boolean isInForeground()
    {
        return gForeground;
    }

    public static boolean isActive()
    {
        return gWebView != null;
    }


    private static void onException(String error){
        final String exception = String.format("window.plugins.OrtcPushPlugin.onException('%s');", error);
        sCordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gWebView.loadUrl("javascript:" + exception);
            }
        });
    }


    public static void sendJavascript(JSONObject json) {
        try {
            String send = "";
            String channel = json.getString("channel");
            json.remove("channel");
            try {
                new JSONObject(json.getString("payload"));
                send = String.format("window.plugins.OrtcPushPlugin.receiveRemoteNotification('%s',%s);",channel,json.getString("payload"));
            } catch (JSONException ex) {
                send = String.format("window.plugins.OrtcPushPlugin.receiveRemoteNotification('%s','%s');",channel,json.getString("payload"));
            }


            Log.i(TAG, "sendJavascript: " + send);

            if (gWebView != null) {
                final String finalSend = send;
                sCordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gWebView.loadUrl("javascript:" + finalSend);
                    }
                });

            }
        } catch (Exception e) {
            Log.e(TAG, "sendJavascript: JSON exception");
        }

    }

    public static void sendExtras(Bundle extras)
    {
        if (extras != null) {
            if (gWebView != null) {
                sendJavascript(convertBundleToJson(extras));
            } else {
                Log.v(TAG, "sendExtras: caching extras to send at a later time.");
                gCachedExtras = extras;
            }
        }
    }

    private static JSONObject convertBundleToJson(Bundle extras)
    {
        try
        {
            JSONObject json = new JSONObject();

            if (extras.containsKey("P")){
                json = new JSONObject();
                json.put("payload",extras.getString("P"));
            }
            else{
                String message = extras.getString("M");
                String newMsg = message.substring(message.indexOf("_", message.indexOf("_") + 1)+1);
                json.put("payload",newMsg);
            }

            if (extras.containsKey("C")){
                json.put("channel", extras.getString("C"));
            }
            //Iterator<String> it = extras.keySet().iterator();
            /*while (it.hasNext())
            {
                String key = it.next();
                Object value = extras.get(key);

                if (key.equals("foreground"))
                {
                    json.put(key, extras.getBoolean("foreground"));
                }
                else if (key.equals("C")){
                    json.put("channel", extras.getString("C"));
                }
                if (key.equals("M")) {
                    String message = extras.getString("M");
                    String newMsg = null;
                    String [] parts = message.split("_");
                    if(parts.length > 1){
                        try {
                            JSONObject jsonObject = new JSONObject(parts[parts.length-1]);
                            newMsg = jsonObject.getString("message");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        newMsg = message;
                    }
                    //json.put("message", newMsg);
                }
                else if (key.equals("P")){
                    json.put("payload",extras.getString("P"));
                    *//*String payloadString = value.toString();
                    if (payloadString.startsWith("{")) {
                        try {
                             JSONObject payloadJsonObject = new JSONObject(payloadString);
                             Iterator payloadIt = payloadJsonObject.keys();
                             while(payloadIt.hasNext()){
                                   String keyPayload = (String) payloadIt.next();
                                   Object valuePaylod = payloadJsonObject.get(keyPayload);
                                   json.put(keyPayload,valuePaylod);
                                }
                             }
                             catch (Exception e) {
                                   json.put(key, value);
                             }
                     }*//*

                }


            }*/

            Log.v(TAG, "extrasToJSON: " + json.toString());

            return json;
        }
        catch( JSONException e)
        {
            Log.e(TAG, "extrasToJSON: JSON exception");
            return null;
        }

    }

}
