<h1>Cordova Push Notifications for iOS/Android</h1>
<h2> (using Realtime Messaging)</h2>


<h2>Description</h2>

This Cordova plugin should be used with the iOS/Android platform together with the Realtime Messaging library (ORTC) for Push Notifications support.  

<h3>Important</h3>
Push Notifications only work in real devices for the iOS platform (not on simulator).

<hr/>
<h3>Contents</h3>

<ul>
	<li><a ref='pc'>Project configuration</a></li>
	<li><a ref='Mi'>Manual instalation</a></li>
	<li><a ref='api'>API reference</a></li>
	<li><a ref='ue'>Usage examples</a></li>
	<li><a ref='te'>Testing the custom push notifications delivery</a></li>
</ul> 
<hr/>

<h2 id='pc'>Project configuration</h2>
<h3 id='pd'>Plugin dependencies</h3>
<ul>
	<li>iOS Realtime Messaging (ORTC) SDK: <a href='http://messaging-public.realtime.co/api/download/ios/2.1.0/ApiiOS.zip'>download</a></li>
	<li>Android Realtime Messaging (ORTC) SDK: <a href='http://messaging-public.realtime.co/api/download/android/2.1.0/ortc-android.zip'>download</a></li>
	<li>SocketRocket library: <a href='https://github.com/square/SocketRocket'>GitHub repo</a></li>
</ul>

<h3 id='pd'>Step by step iOS/Android</h3>
<ol>

	<li><h5>Add the plugin using the Cordova CLI</h5>
	<pre><code>cordova plugin add co.realtime.plugins.cordovapush</code></pre>
	</li>
	<p></p>

			<li><h5>Generate the APNS certificates for push notification on the Apple Developer Center</h5>
			<a href='http://messaging-public.realtime.co/documentation/starting-guide/mobilePushAPNS.html'>Follow the iOS Push Notifications tutorial</a>
			</li>
			<li><h5>Generate the Android API keys for push notification on the Google Cloud Messaging</h5>
			<a href='http://messaging-public.realtime.co/documentation/starting-guide/mobilePushGCM.html'>Follow the Android Push Notifications tutorial</a>
			</li>
			<p></p>

			<li><h5>Build and run the app in an iOS/Android device</h5>
			NOTE: iOS - The Push Notifications won't work on the simulators, only on actual devices.<br/>
			NOTE: Android - The Push Notifications will work on the emulators only if is a Google APIs emulator.
			</li>
			
		</ul>
	</li>
	<ul>
</ol>



<hr/>
<h2 id='api'>API reference</h2>
<h3>Plugin</h3>
<hr/>
<ul>
	<li><h4>checkForNotifications(callback())</h4><p>This method is used to verify push notifications on buffer on the native code from the javascript interface.
	<ul>
		<li>callback() - is triggered after iOS/Android native finishes processing.</li>
	</ul>
	</p></li>
	<li><h4>connect(config, successCallback())</h4><p>This method is used to establish the ORTC connection.
	<ul>
		<li>config - is a JSONObject with the config to connect. Ex: {'appkey':'YOUR_APPLICATION_KEY', 'token':'myToken', 'metadata':'myMetadata', 'url':'https://ortc-developers.realtime.co/server/ssl/2.1/','projectId':'YOUR_GOOGLE_PROJECT_NUMBER'}. ProjectId only necessary on Android Push Notifications.</li>
		<li>successCallback() - this function is call when connection is established.</li>
	</ul>
	</p></li>
	<li><h4>disconnect(callback())</h4><p>This method is used to disconnect the ORTC connection.</p>
		<ul>
			<li>callback() - is triggered after connection is disconnected.</li>
		</ul>
	</li>
	<li><h4>subscribe(channel, callback())</h4><p>Subscribe a channel. Note: In order to receive the push notifications on the channel you just subscribed you have to add an event listener with the name "push-notification" to your html like:  <pre><code>document.addEventListener("push-notification", 
	function(notification)
	{
		window.plugins.OrtcPushPlugin.log("Push-Received  channel: " + notification.channel + " payload: " + notification.payload);
		var payload = document.getElementById('payload');
		payload.innerHTML = JSON.stringify( notification.payload );
		payload.value = JSON.stringify( notification.payload );
	}, false);</code></pre>
	   <p><b>The object notification passed as argument</b> on the callback function is an JSONObject with the fields <b>channel</b> and <b>payload</b>, where channel is the name of the channel and payload is the content you sent. (like a JSONObject or a String). <p><pre><code>Ex: {"channel":"mychannel","payload":"{"sound":"default","badge":"1","name":"Joe","age" :"48}"}</code></pre></p>
	   <p><b>NOTE: Please in your payload try to avoid '\n' since you can have some issues.</b></p>
		<ul>
			<li>is a JSONObject with the channel to subscribe. Ex: {'channel':'mychannel'}</li>
			<li>callback() - is triggered after channel is subscribed.</li>
		</ul>
	</li>
	<li><h4>unsubscribe(channel,callback())</h4><p>This method is used to unsubscribe a channel previously subscribed.</p>
		<ul>
			<li>channel - is a JSONObject with the channel name to unsubscribe. Ex: {'channel':'mychannel'}</li>
			<li>callback() - is triggered after channel is unsubscribed.</li>
		</ul>
	</li>
	<li><h4>setApplicationIconBadgeNumber(badge,callback())</h4><p>This method is used to set the application badge number on iOS. Not implemented on Android.</p>
		<ul>
			<li>badge - the number to appear on the bage.</li>
			<li>callback() - is triggered after iOS/Android native code finishes.</li>
		</ul>
	</li>
	<li><h4>send(config)</h4><p>This method is used to send a message to a channel.</p>
		<ul>
			<li>config - is a JSONObject with the channel to send the message. Ex: {'channel':'mychannel','message':'mymessage'}.</li>
		</ul>
	</li>
	<li><h4>cancelAllLocalNotifications(callback())</h4><p>This method is used to clear notifications from notification center.</p>
		<ul>
			<li>callback() - is triggered after iOS/Android native code finishes.</li>
		</ul>
	</li>
	<li><h4>log(logString)</h4><p>This is a handy method to log data into XCODE/AndroidStudio console from the javascript code.</p>
		<ul>
			<li>logString - is the string to be logged into the console.</li>
		</ul>
	</li>
	<li><h4>onException event</h4>
	<p>To get the exceptions from the underlying native ortc client you must create the event listener with the name "onException" on your html code like:</p>
	<pre><code>document.addEventListener("onException", function(exception){
		window.plugins.OrtcPushPlugin.log("onException: " + exception.description);
    }, false);</code></pre>
	</li>
</ul> 
<h2 id='ue'>Usage example</h2>
<hr/>

Add to your HTML page HEAD section:
<pre><code>
&lt;script type="text/javascript">
     document.addEventListener("deviceready",
              function () {
              	if(window.plugins && window.plugins.OrtcPushPlugin){
                		var OrtcPushPlugin = window.plugins.OrtcPushPlugin;
                		OrtcPushPlugin.checkForNotifications();
                 }
               }, false);
&lt;/script>
</code></pre>

Add to your app:
<pre><code>

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
        
</code></pre>

<h2 id='te'>Testing the custom push notifications delivery</h2>

<p>To test your Push Notifications you need to go through the setup process (see the iOS Push Notifications Starting Guide) and use the Realtime REST API to send a custom push with the following POST to https://ortc-mobilepush.realtime.co/mp/publish</p>

<pre><code>{
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
</code></pre>

Have fun pushing!
