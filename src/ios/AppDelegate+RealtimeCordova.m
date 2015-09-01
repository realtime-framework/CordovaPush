//
//  AppDelegate+RealtimeCordova.m
//  CordovaPush
//
//  Created by Joao Caixinha on 06/07/15.
//
//

#import "AppDelegate+RealtimeCordova.h"
#import <objc/runtime.h>

static char launchNotificationKey;


@implementation AppDelegate (RealtimeCordova)
@dynamic pushInfo;




+ (void)load
{
    Method original, change;
    
    original = class_getInstanceMethod(self, @selector(init));
    change = class_getInstanceMethod(self, @selector(change_init));
    method_exchangeImplementations(original, change);
}

- (AppDelegate *)change_init
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(registForNotifications)
                                                 name:@"UIApplicationDidFinishLaunchingNotification" object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(processException:)
                                                 name:@"onException"
                                               object:nil];    
    return [self change_init];
}



- (BOOL)registForNotifications
{
    
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 80000
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    } else {
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes: UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge];
    }
#else
    [application registerForRemoteNotificationTypes: UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge];
#endif
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handlePushNotifications)
                                                 name:@"checkForNotifications"
                                               object:nil];
    
    return YES;
}



- (void)application:(UIApplication *)application
didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSString* newToken = [deviceToken description];
    newToken = [newToken stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"<>"]];
    newToken = [newToken stringByReplacingOccurrencesOfString:@" " withString:@""];
    NSLog(@"\n\n - didRegisterForRemoteNotificationsWithDeviceToken:\n%@\n", deviceToken);
    
    [OrtcClient performSelector:@selector(setDEVICE_TOKEN:) withObject:[[NSString alloc] initWithString:newToken]];
}


- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    [self application:application didReceiveRemoteNotification:userInfo];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    
    UIApplicationState appState = UIApplicationStateActive;
    if ([application respondsToSelector:@selector(applicationState)]) {
        appState = application.applicationState;
    }
    
    if (appState == UIApplicationStateActive) {
        [self processPush:userInfo];
    } else {
        //save it for later
        self.pushInfo = userInfo;
    }
}

- (void)processException:(NSNotification *)Notification
{
    NSDictionary *userInfo = Notification.userInfo;
    NSString* error = [NSString stringWithFormat:@"window.plugins.OrtcPushPlugin.onException('%@');", [userInfo objectForKey:@"exception"]];
    [self.viewController.webView stringByEvaluatingJavaScriptFromString:error];
}

- (void)processPush:(NSDictionary *)userInfo
{
    NSMutableDictionary *pushInfo = [[NSMutableDictionary alloc] init];
    
    if ([[[userInfo objectForKey:@"aps" ] objectForKey:@"alert"] isKindOfClass:[NSString class]]) {
        [self handleStd:pushInfo from:userInfo];
    }else
    {
        [self handleCustom:pushInfo from:userInfo];
    }
    
    
    NSString * jsCallBack;
    NSString *channel = [userInfo objectForKey:@"C"];
    
    if ([[pushInfo objectForKey:@"payload"] isKindOfClass:[NSString class]]) {
        
        jsCallBack = [NSString stringWithFormat:@"window.plugins.OrtcPushPlugin.receiveRemoteNotification('%@','%@');",channel, [pushInfo objectForKey:@"payload"]];
        [self.viewController.webView stringByEvaluatingJavaScriptFromString:jsCallBack];
        
    }else{
        
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[pushInfo objectForKey:@"payload"]
                                                           options:(NSJSONWritingOptions)    (NSJSONWritingPrettyPrinted)
                                                             error:&error];
        
        NSString *jsonstring = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        
        jsCallBack = [NSString stringWithFormat:@"window.plugins.OrtcPushPlugin.receiveRemoteNotification('%@',%@);",channel, jsonstring];
        
        [self.viewController.webView stringByEvaluatingJavaScriptFromString:jsCallBack];
    }
}



- (void)handleStd:(NSMutableDictionary*)pushInfo from:(NSDictionary*)userInfo
{
    NSString* msg = [userInfo objectForKey:@"M"];
    int num = 0;
    NSUInteger      len = [msg length];
    unichar         buffer[len+1];
    [msg getCharacters: buffer range: NSMakeRange(0, len)];
    
    NSString *finalM;
    for (int i=0; i<len; i++) {
        if (buffer[i] == '_') {
            num++;
            if (num == 2 && len > i + 1) {
                finalM = [msg substringFromIndex:i+1];
            }
        }
    }
    
    NSError *error = nil;
    NSData *jsonData = [finalM dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
    
    if (json != nil) {
        [pushInfo setObject:json forKey:@"payload"];
    }else
    {
        [pushInfo setObject:finalM forKey:@"payload"];
    }
}


- (void)handleCustom:(NSMutableDictionary*)pushInfo from:(NSDictionary*)userInfo
{
    NSMutableDictionary *payload = [[NSMutableDictionary alloc] init];
    for (NSString* key  in [[userInfo objectForKey:@"aps"] allKeys]) {
        if (![key isEqualToString:@"sound"] && ![key isEqualToString:@"badge"] && ![key isEqualToString:@"alert"]) {
            [payload setObject:[[userInfo objectForKey:@"aps"] objectForKey:key] forKey:key];
        }
    }
    [pushInfo setObject:payload forKey:@"payload"];
}





- (void)handlePushNotifications
{
    if (self.pushInfo != nil) {
        [self processPush:self.pushInfo];
    }
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    [self handlePushNotifications];
    
}


- (void)application:(UIApplication *)application
didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    
}




- (NSDictionary *)pushInfo
{
    return objc_getAssociatedObject(self, &launchNotificationKey);
}

- (void)setPushInfo:(NSDictionary *)aDictionary
{
    objc_setAssociatedObject(self, &launchNotificationKey, aDictionary, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}




@end
