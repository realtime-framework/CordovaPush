# Cordova Push Notifications for iOS/Android (using Realtime Messaging)

## Description

This Cordova plugin should be used with the iOS/Android platform together with the Realtime Messaging library (ORTC) for Push Notifications support.

### Important

Push Notifications only work in real devices for the iOS platform (not on simulator).

* * *

## Step by step configuration iOS/Android

1.  **Add the plugin using the Cordova CLI**

        cordova plugin add cordovapush

2.   **Generate the APNS certificates for push notification on the Apple Developer Center**

    [Follow the iOS Push Notifications tutorial](http://messaging-public.realtime.co/documentation/starting-guide/mobilePushAPNS.html)
3.   **Generate the Android API keys for push notification on the Google Cloud Messaging**

    [Follow the Android Push Notifications tutorial](http://messaging-public.realtime.co/documentation/starting-guide/mobilePushGCM.html)

4.   **Build and run the app in an iOS/Android device**

    NOTE: iOS - The Push Notifications won't work on the simulators, only on actual devices.  
     NOTE: Android - The Push Notifications will work on the emulators only if is a Google APIs emulator.

* * *

## API reference

### Plugin

* * *

*   **checkForNotifications(callback())**

    This method is used to verify push notifications on buffer on the native code from the javascript interface.

    *   callback() - is triggered after iOS/Android native finishes processing.
*   **connect(config, successCallback())**

    This method is used to establish the ORTC connection.

    *   config - is a JSONObject with the config to connect. Ex: {'appkey':'YOUR_APPLICATION_KEY', 'token':'myToken', 'metadata':'myMetadata', 'url':'https://ortc-developers.realtime.co/server/ssl/2.1/','projectId':'YOUR_GOOGLE_PROJECT_NUMBER'}. ProjectId only necessary on Android Push Notifications.
    *   successCallback() - this function is call when connection is established.
*  **disconnect(callback())**

    This method is used to disconnect the ORTC connection.

    *   callback() - is triggered after connection is disconnected.
*  **subscribe(channel, callback())**

    Subscribe a channel. Note: In order to receive the push notifications on the channel you just subscribed you have to add an event listener with the name "push-notification" to your html like:

        document.addEventListener("push-notification", 
        	function(notification)
        	{
        		window.plugins.OrtcPushPlugin.log("Push-Received  channel: " + notification.channel + " payload: " + notification.payload);
        		var payload = document.getElementById('payload');
        		payload.innerHTML = JSON.stringify( notification.payload );
        		payload.value = JSON.stringify( notification.payload );
        	}, false);

    **The object notification passed as argument** on the callback function is an JSONObject with the fields **channel** and **payload**, where channel is the name of the channel and payload is the content you sent. (like a JSONObject or a String).

        Ex: {"channel":"mychannel","payload":"{"sound":"default","badge":"1","name":"Joe","age" :"48}"}

    **NOTE: Please in your payload try to avoid '\n' since you can have some issues.**

    *   is a JSONObject with the channel to subscribe. Ex: {'channel':'mychannel'}
    *   callback() - is triggered after channel is subscribed.
*  **unsubscribe(channel,callback())**

    This method is used to unsubscribe a channel previously subscribed.

    *   channel - is a JSONObject with the channel name to unsubscribe. Ex: {'channel':'mychannel'}
    *   callback() - is triggered after channel is unsubscribed.
*  **setApplicationIconBadgeNumber(badge,callback())**

    This method is used to set the application badge number on iOS. Not implemented on Android.

    *   badge - the number to appear on the bage.
    *   callback() - is triggered after iOS/Android native code finishes.
*  **send(config)**

    This method is used to send a message to a channel.

    *   config - is a JSONObject with the channel to send the message. Ex: {'channel':'mychannel','message':'mymessage'}.
*  **cancelAllLocalNotifications(callback())**

    This method is used to clear notifications from notification center.

    *   callback() - is triggered after iOS/Android native code finishes.
*  **log(logString)**

    This is a handy method to log data into XCODE/AndroidStudio console from the javascript code.

    *   logString - is the string to be logged into the console.
*  **onException event**

    To get the exceptions from the underlying native ortc client you must create the event listener with the name "onException" on your html code like:

        document.addEventListener("onException", function(exception){
        		window.plugins.OrtcPushPlugin.log("onException: " + exception.description);
            }, false);

## Usage example

* * *

Add to your HTML page HEAD section:

    <script type="text/javascript">
         document.addEventListener("deviceready",
                  function () {
                  	if(window.plugins && window.plugins.OrtcPushPlugin){
                    		var OrtcPushPlugin = window.plugins.OrtcPushPlugin;
                    		OrtcPushPlugin.checkForNotifications();
                     }
                   }, false);
    </script>

Add to your app:

    //Establish connection with ORTC server and subscribe the channel entered in the input text box on the HTML interface.

    function subscribe()
               {
                   if(window.plugins && window.plugins.OrtcPushPlugin){
                       var OrtcPushPlugin = window.plugins.OrtcPushPlugin;
                       OrtcPushPlugin.log("Connecting");

                       OrtcPushPlugin.connect({'appkey':'YOUR_APPLICATION_KEY', 'token':'myToken', 'metadata':'myMetadata', 'url':'https://ortc-developers.realtime.co/server/ssl/2.1/','projectId':'YOUR_GOOGLE_PROJECT_NUMBER'}, function (){
                                        OrtcPushPlugin.log("Connected: ");
                                        var channel = document.getElementById('channel');
                                        OrtcPushPlugin.log("Trying to subscribe: " + channel.value);
                                        OrtcPushPlugin.subscribe({'channel':channel.value}, function (){
                                                                   var subcribed = document.getElementById('subscribed');
                                                                   subcribed.innerHTML = "subscribed: " + channel.value;
                                                                   OrtcPushPlugin.log("subscribed: " + channel.value);
                                                                   //OrtcPushPlugin.disconnect();
                                                                   });
                                          });
                   }
               };

    //Catch the push-notification event when a new notification is received (or tapped by the user)
    //Shows the extra property of push notification payload (can be customized using the Realtime Custom Push REST endpoint)

    document.addEventListener("push-notification", 
    	function(notification)
    	{
    		window.plugins.OrtcPushPlugin.log("Push-Received  channel: " + notification.channel + " payload: " + notification.payload);
    		var payload = document.getElementById('payload');
    		payload.innerHTML = "payload: " + notification.payload.name;
    		payload.value = "payload: " + notification.payload.name;
    	}, false);

## Testing the custom push notifications delivery

To test your Push Notifications you need to go through the setup process (see the iOS Push Notifications Starting Guide) and use the Realtime REST API to send a custom push with the following POST to https://ortc-mobilepush.realtime.co/mp/publish

    {
       "applicationKey": "[INSERT YOUR APP KEY]",
       "privateKey": "[INSERT YOUR APP PRIVATE KEY]",
       "channel" : "[INSERT CHANNEL TO SEND PUSH]",
       "message" : "[INSERT ALERT TEXT]",
        "payload" : "{
         \"sound\" : \"default\",
         \"badge\" : \"1\",
         \"name\" : \"Joe\",
         \"age\" : \"48\"
        }"
    }

Have fun pushing!