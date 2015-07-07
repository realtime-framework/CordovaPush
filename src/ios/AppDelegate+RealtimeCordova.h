//
//  AppDelegate+RealtimeCordova.h
//  CordovaPush
//
//  Created by Joao Caixinha on 06/07/15.
//
//

#import "AppDelegate.h"
#import "OrtcClient.h"

@interface AppDelegate (RealtimeCordova)

@property(retain, nonatomic)CDVViewController *viewController;
@property(retain, nonatomic)NSDictionary *pushInfo;

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions;
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo;

@end
