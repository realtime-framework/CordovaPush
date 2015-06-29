//
//  OrtcPlugin.m
//  babblr
//
//  Created by Joao Caixinha on 12/09/14.
//
//

#import "OrtcPushPlugin.h"

@implementation OrtcPushPlugin

- (void)connect:(CDVInvokedUrlCommand*)command
{
    _connectCommand = [[NSMutableDictionary alloc] init];
    [_connectCommand setObject:command forKey:@"connect"];
    NSDictionary* args = [command.arguments objectAtIndex:0];
    
    NSString* appKey = [args objectForKey:@"appkey"];
    NSString* token = [args objectForKey:@"token"];
    NSString* metadata = [args objectForKey:@"metadata"];
    NSString* serverUrl = [args objectForKey:@"url"];
    
    // Instantiate Messaging Client
    _ortc = [OrtcClient ortcClientWithConfig:self];
    
    // Set connection properties
    [_ortc setConnectionMetadata:metadata];
    [_ortc setClusterUrl:serverUrl];
    
    // Connect
    [_ortc connect:appKey authenticationToken:token];
}

- (void)disconnect:(CDVInvokedUrlCommand*)command
{
    [_connectCommand setObject:command forKey:@"disconnect"];
    [_ortc disconnect];
}

- (void)onConnected:(OrtcClient *)ortc
{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[[_connectCommand objectForKey:@"connect"] callbackId]];
}

- (void)onDisconnected:(OrtcClient *)ortc
{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[[_connectCommand objectForKey:@"disconnect"] callbackId]];
}

- (void)onException:(OrtcClient *)ortc error:(NSError *)error
{
    NSLog(@"EXCEPTION %@", error.description);
}

- (void)onReconnected:(OrtcClient *)ortc
{
}

- (void)onReconnecting:(OrtcClient *)ortc
{
}

- (void)onSubscribed:(OrtcClient *)ortc channel:(NSString *)channel
{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[[_connectCommand objectForKey:[NSString stringWithFormat:@"sub:%@",channel]] callbackId]];
}

- (void)onUnsubscribed:(OrtcClient *)ortc channel:(NSString *)channel
{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[[_connectCommand objectForKey:[NSString stringWithFormat:@"unsub:%@",channel]] callbackId]];
}


- (void)subscribe:(CDVInvokedUrlCommand*)command
{
    NSString* channel = [[command.arguments objectAtIndex:0] objectForKey:@"channel"];
    [_connectCommand setObject:command forKey:[NSString stringWithFormat:@"sub:%@",channel]];
    
    [_ortc subscribeWithNotifications:channel subscribeOnReconnected:YES onMessage:^(OrtcClient* ortc, NSString* channel, NSString* message) {
        
        NSLog(@"%@", message);
    }
     ];
}

- (void)unsubscribe:(CDVInvokedUrlCommand*)command
{
    NSString* channel = [[command.arguments objectAtIndex:0] objectForKey:@"channel"];
    [_connectCommand setObject:command forKey:[NSString stringWithFormat:@"unsub:%@",channel]];
    [_ortc unsubscribe:channel];
}

- (void)setApplicationIconBadgeNumber:(CDVInvokedUrlCommand*)command{
    
    NSInteger badge = [[command.arguments objectAtIndex:0] integerValue];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badge];
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[command callbackId]];
}

- (void)send:(CDVInvokedUrlCommand*)command
{
    NSDictionary* args = [command.arguments objectAtIndex:0];
    
    NSString* channel = [args objectForKey:@"channel"];
    NSString* channelMsg = [args objectForKey:@"channelMsg"];
    
    [_ortc send:channel message:channelMsg];
}

- (void)cancelAllLocalNotifications:(CDVInvokedUrlCommand*)command{
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 0];
	[[UIApplication sharedApplication] cancelAllLocalNotifications];
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[command callbackId]];
}


- (void)checkForNotifications:(CDVInvokedUrlCommand*)command
{
    [[NSNotificationCenter defaultCenter] postNotificationName:@"checkForNotifications" object:@"OrtcPushPlugin"];
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[command callbackId]];
}

- (void)log:(CDVInvokedUrlCommand*)command
{
    NSLog(@"OrtcPushPlugin: %@",[command.arguments objectAtIndex:0]);
}

@end















