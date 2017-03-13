(function(cordova) {
 
  function OrtcPushPlugin() {}

  OrtcPushPlugin.prototype.checkForNotifications = function() {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "checkForNotifications", []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.removeNotifications = function() {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "removeNotifications", []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.connect = function(config) {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "connect", config ? [config] : []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.getIsConnected = function() {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(res){resolve(res);}, function(){reject();}, "OrtcPushPlugin", "getIsConnected", []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.enableHeadsUpNotifications = function() {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "enableHeadsUpNotifications", []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.disableHeadsUpNotifications = function() {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "disableHeadsUpNotifications", []);
    });
    return promise;
  };
   
  OrtcPushPlugin.prototype.disconnect = function() {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "disconnect", []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.subscribe = function(config) {
   var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "subscribe", config ? [config] : []);
   });
   return promise;
  };


  OrtcPushPlugin.prototype.unsubscribe = function(config) {
    var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "unsubscribe", config ? [config] : []);
    });
    return promise;
  };

  OrtcPushPlugin.prototype.setApplicationIconBadgeNumber = function(badge) {
   var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "setApplicationIconBadgeNumber", [badge]);
   });
   return promise;
  };

  OrtcPushPlugin.prototype.send = function(config) {
    cordova.exec(null, null, "OrtcPushPlugin", "send", config ? [config] : []);
  };

  // Call this to clear all notifications from the notification center
  OrtcPushPlugin.prototype.cancelAllLocalNotifications = function() {
   var promise = new Promise(function(resolve, reject) {
        cordova.exec(function(){resolve();}, function(){reject();}, "OrtcPushPlugin", "cancelAllLocalNotifications", []);
   });
   return promise;
  };

  OrtcPushPlugin.prototype.log = function(log) {
    cordova.exec(null, null, "OrtcPushPlugin", "log", log ? [log] : []);
  };

  OrtcPushPlugin.prototype.receiveRemoteNotification = function(channel, payload, tapped) {
    var ev = document.createEvent('HTMLEvents');
    ev.channel = channel;
    ev.payload = payload;
    ev.tapped = tapped;
    ev.initEvent('push-notification', true, true, arguments);
    document.dispatchEvent(ev);
  };

  OrtcPushPlugin.prototype.onException = function(error){
    var ev = document.createEvent('HTMLEvents');
    ev.description = error;
    ev.initEvent('onException', true, true, arguments);
    document.dispatchEvent(ev);
  };

  cordova.addConstructor(function() {
                           if(!window.plugins) window.plugins = {};
                           window.plugins.OrtcPushPlugin = new OrtcPushPlugin();
                        });

})(window.cordova || window.Cordova || window.PhoneGap);


// call when device is ready
document.addEventListener("deviceready", function () {
                         if(window.plugins && window.plugins.OrtcPushPlugin){
                         var OrtcPushPlugin = window.plugins.OrtcPushPlugin;
                         OrtcPushPlugin.checkForNotifications();

                         }
                         });


// call when app resumes
document.addEventListener("resume", function () {
                         if(window.plugins && window.plugins.OrtcPushPlugin){
                         var OrtcPushPlugin = window.plugins.OrtcPushPlugin;
                         OrtcPushPlugin.checkForNotifications();
                         }
                         });


