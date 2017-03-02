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

*   **var promise = checkForNotifications()**

    This method is used to verify push notifications on buffer on the native code from the javascript interface.
    
*   **var promise = removeNotifications()**

    This method is used clear the last push notifications on buffer on the native code from the javascript interface.

    *   promise - resolved after iOS/Android native finishes processing.
*   **var promise = connect(config)**

    This method is used to establish the ORTC connection.

    *   config - is a JSONObject with the config to connect. Ex: {'appkey':'YOUR_APPLICATION_KEY', 'token':'myToken', 'metadata':'myMetadata', 'url':'https://ortc-developers.realtime.co/server/ssl/2.1/','projectId':'YOUR_GOOGLE_PROJECT_NUMBER'}. ProjectId only necessary on Android Push Notifications.
    *   promise - resolve is call when connection is established.
*  **var promise = disconnect()**

    This method is used to disconnect the ORTC connection.
     *   promise - resolved after connection is disconnected.
     
*	**var promise = getIsConnected()**
	
	Gets ortc client connection state.
	
	Resolves promise with parameter state 0 if not connected and 1 connected.	
*  **var promise = subscribe(channel)**

    Subscribe a channel. Note: In order to receive the push notifications on the channel you just subscribed you have to add an event listener with the name "push-notification" to your html like:

        document.addEventListener("push-notification", 
        	function(notification)
        	{
        		window.plugins.OrtcPushPlugin.log("Push-Received  channel: " + notification.channel + " payload: " + notification.payload);
        		var payload = document.getElementById('payload');
        		payload.innerHTML = JSON.stringify( notification.payload );
        		payload.value = JSON.stringify( notification.payload );
        	}, false);

    **The object notification passed as argument** on the listener function is an JSONObject with the fields **channel**, **payload** and **tapped**, where channel is the name of the channel, payload is the content you sent (like a JSONObject or a String) and tapped is 0 or 1 indicating if the user tapped the notification.

        	{
        		channel: "mychannel",
        		payload: {"sound":"default","badge":"1","name":"Joe","age" :"48"},
        		tapped: 1
        	}

    **NOTE: Please in your payload try to avoid '\n' since you can have some issues.**

    *   is a JSONObject with the channel to subscribe. Ex: {'channel':'mychannel'}
    *  promise - resolved when channel is subscribed.
*  **var promise = unsubscribe(channel)**

    This method is used to unsubscribe a channel previously subscribed.

    *   channel - is a JSONObject with the channel name to unsubscribe. Ex: {'channel':'mychannel'}
    *   promise - resolved when channel is unsubscribed.
*  **var promise = setApplicationIconBadgeNumber(badge)**

    This method is used to set the application badge number on iOS. Not implemented on Android.

    *   badge - the number to appear on the bage.
    *   promise - resolved after iOS/Android native code finishes.
*  **send(config)**

    This method is used to send a message to a channel.

    *   config - is a JSONObject with the channel to send the message. Ex: {'channel':'mychannel','message':'mymessage'}.
*  **var promise = cancelAllLocalNotifications()**

    This method is used to clear notifications from notification center.

    *  promise - resolved after iOS/Android native code finishes.
*  **log(logString)**

    This is a handy method to log data into XCODE/AndroidStudio console from the javascript code.

    *   logString - is the string to be logged into the console.
*  **onException event**

    To get the exceptions from the underlying native ortc client you must create the event listener with the name "onException" on your html code like:

        document.addEventListener("onException", function(exception){
        		window.plugins.OrtcPushPlugin.log("onException: " + exception.description);
            }, false);
            
### Setting notification icons for the Android platform

*	To configure the notification large icon you must have an image on your app resources folder `resources/android/drawable/` named `large_notification_icon`

*	To configure the notification small icon (for the notifications bar) you must have an image on your app resources folder `resources/android/drawable/` named `small_notification_icon`. 

* IMPORANT: The small icon image must be created following the [Android design guidelines](https://material.google.com/style/icons.html#icons-system-icons), otherwise it will be rendered as a white square or won't be rendered at all in the notifications bar. 

*	To configure notification background color you must create a file named `colors.xml` in `resources/android/values/`. In this file you must set a color resource named `notification_color`.
	
#####Example: 

	<resources>
	    <color name="notification_color">#ff0000</color>
	</resources> 	 

### Set notifications display mode

Only available for android. [Check android documentation](https://developer.android.com/guide/topics/ui/notifiers/notifications.html#Heads-up)

**var promise = enableHeadsUpNotifications()** Use this method to set the notification display type to Heads-up. This method persists the set value, to disable the heads-up notifications you must call `disableHeadsUpNotifications`.

**var promise = disableHeadsUpNotifications()** Use this method to set the default notifications display (only the small icon is shown in the notification bar) and disable Heads-up.

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

                       OrtcPushPlugin.connect({'appkey':'YOUR_APPLICATION_KEY', 'token':'myToken', 'metadata':'myMetadata', 'url':'https://ortc-developers.realtime.co/server/ssl/2.1/','projectId':'YOUR_GOOGLE_PROJECT_NUMBER'})
                       .then(function (){
                                        OrtcPushPlugin.log("Connected: ");
                                        var channel = document.getElementById('channel');
                                        OrtcPushPlugin.log("Trying to subscribe: " + channel.value);
                                        OrtcPushPlugin.subscribe({'channel':channel.value})
                                        .then(function (){
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