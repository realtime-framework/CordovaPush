package co.realtime.plugins.android.cordovapush;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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
    public static final String ACTION_REMOVE_NOTIFICATIONS = "removeNotifications";
    public static final String ACTION_SET_ICON = "setApplicationIconBadgeNumber";
    public static final String ACTION_LOG = "log";
    public static final String ACTION_CONNECT = "connect";
    public static final String ACTION_DISCONNECT = "disconnect";
    public static final String ACTION_SUBSCRIBE = "subscribe";
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    public static final String ACTION_CANCEL_NOTIFICATIONS = "cancelAllLocalNotifications";
    public static final String ACTION_SEND_MESSAGE = "send";
    private static final String ACTION_ENABLE_HEADS_UP_NOTIFICATIONS = "enableHeadsUpNotifications";
    private static final String ACTION_DISABLE_HEADS_UP_NOTIFICATIONS = "disableHeadsUpNotifications";
    private static final String ACTION_GET_CONNECTION_STATE = "getIsConnected";
    private OrtcClient client;
    private static CordovaWebView gWebView;
    private static Bundle gCachedExtras = null;
    private static boolean gForeground = false;
    private static CordovaInterface sCordova;
    private static HashMap commands = new HashMap();
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
                    CallbackContext call = (CallbackContext)commands.get(ACTION_CONNECT);
                    if (call != null)
                        call.success();
                }
            };


            client.onDisconnected = new OnDisconnected() {
                @Override
                public void run(OrtcClient ortcClient) {
                    CallbackContext call = (CallbackContext)commands.get(ACTION_DISCONNECT);
                    if (call != null)
                        call.success();
                }
            };

            client.onUnsubscribed = new OnUnsubscribed() {
                @Override
                public void run(OrtcClient ortcClient, String channel) {
                    CallbackContext call = (CallbackContext)commands.get(ACTION_UNSUBSCRIBE);
                    if (call != null)
                        call.success();
                }
            };

            client.onSubscribed = new OnSubscribed() {
                @Override
                public void run(OrtcClient ortcClient, String s) {
                    CallbackContext call = (CallbackContext)commands.get(ACTION_SUBSCRIBE);
                    if (call != null)
                        call.success();
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

        gWebView = this.webView;
        final JSONArray argss = args;
        try {
            if (ACTION_LOG.equals(action)) {
                Log.i(TAG,args.get(0).toString());
                callbackContext.success();
                return true;
            }
            else if(ACTION_GET_CONNECTION_STATE.equals(action)){
                int isConnected = client.getIsConnected()? 1: 0;
                callbackContext.success(isConnected);
                return true;
            }
            else if(ACTION_CONNECT.equals(action)){
                commands.put(ACTION_CONNECT, callbackContext);
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject arg_object = null;
                        try {
                            arg_object = argss.getJSONObject(0);
                            client.setClusterUrl(arg_object.getString("url"));
                            client.setConnectionMetadata(arg_object.getString("metadata"));
                            client.setGoogleProjectId(arg_object.getString("projectId"));
                            client.setApplicationContext(cordova.getActivity().getApplicationContext());
                            client.connect(arg_object.getString("appkey"),arg_object.getString("token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
            else if(ACTION_DISCONNECT.equals(action)){
                commands.put(ACTION_DISCONNECT, callbackContext);
                client.disconnect();
                return true;
            }
            else if(ACTION_SUBSCRIBE.equals(action)) {
                commands.put(ACTION_SUBSCRIBE, callbackContext);
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject arg_object = null;
                        try {
                            arg_object = argss.getJSONObject(0);
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
            else if(ACTION_UNSUBSCRIBE.equals(action)){
                commands.put(ACTION_UNSUBSCRIBE, callbackContext);
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject arg_object = null;
                        try {
                            arg_object = argss.getJSONObject(0);
                            String channel = arg_object.getString("channel");
                            client.unsubscribe(channel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
            else if(ACTION_CANCEL_NOTIFICATIONS.equals(action)){
                commands.put(ACTION_CANCEL_NOTIFICATIONS, callbackContext);
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
                    sendExtras(gCachedExtras, 1);
                }
                callbackContext.success();
                return true;
            }
            else if(ACTION_REMOVE_NOTIFICATIONS.equals(action)){
                Log.v(TAG, "removing cached extras");
                gCachedExtras = null;
                callbackContext.success();
                return true;
            }
            else if(ACTION_SET_ICON.equals(action)){
                Log.i(TAG,"Set icon badge number not implemented in Android");
                callbackContext.success();
                return true;
            }
            else if(ACTION_ENABLE_HEADS_UP_NOTIFICATIONS.equals(action)){
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        Context context = cordova.getActivity().getApplicationContext();
                        GcmReceiver.setAppPriority(context, 1);
                    }
                });
                return true;
            }
            else if(ACTION_DISABLE_HEADS_UP_NOTIFICATIONS.equals(action)){
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        Context context = cordova.getActivity().getApplicationContext();
                        GcmReceiver.setAppPriority(context, 0);
                    }
                });
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


    public static void sendJavascript(JSONObject json, int tapped) {
        try {
            String send = "";
            String channel = json.getString("channel");
            json.remove("channel");
            try {
                new JSONObject(json.getString("payload"));
                send = String.format("window.plugins.OrtcPushPlugin.receiveRemoteNotification('%s',%s, %d);",channel,json.getString("payload"), tapped);
            } catch (JSONException ex) {
                send = String.format("window.plugins.OrtcPushPlugin.receiveRemoteNotification('%s','%s', %d);",channel,json.getString("payload"), tapped);
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

    public static void sendExtras(Bundle extras, int tapped)
    {
        if (extras != null) {
            if (gWebView != null) {
                sendJavascript(convertBundleToJson(extras), tapped);
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
