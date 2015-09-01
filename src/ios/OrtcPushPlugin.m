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
    
    if (!_ortc) {
        // Instantiate Messaging Client
        _ortc = [OrtcClient ortcClientWithConfig:self];
        
        // Set connection properties
        [_ortc setConnectionMetadata:metadata];
        [_ortc setClusterUrl:serverUrl];
    }
    
    // Connect
    [_ortc connect:appKey authenticationToken:token];
}

- (void)disconnect:(CDVInvokedUrlCommand*)command
{
    [self trowException:@"ORTC not connected" forCommand:command code:^(CDVInvokedUrlCommand *cmd) {
        [_connectCommand setObject:cmd forKey:@"disconnect"];
        [_ortc disconnect];
    }];
    
    
}

- (void)trowException:(NSString*)exception forCommand:(CDVInvokedUrlCommand*)command code:(void (^)(CDVInvokedUrlCommand*))code{
    if (!_ortc) {
        [[NSNotificationCenter defaultCenter] postNotificationName:@"onException" object:nil userInfo:@{@"exception":exception}];
        return;
    }
    code(command);
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
    [[NSNotificationCenter defaultCenter] postNotificationName:@"onException" object:nil userInfo:@{@"exception":error.localizedDescription}];
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
    [self trowException:@"ORTC not connected" forCommand:command code:^(CDVInvokedUrlCommand *cmd) {
        NSString* channel = [[cmd.arguments objectAtIndex:0] objectForKey:@"channel"];
        [_connectCommand setObject:cmd forKey:[NSString stringWithFormat:@"sub:%@",channel]];
        
        [_ortc subscribeWithNotifications:channel subscribeOnReconnected:YES onMessage:^(OrtcClient* ortc, NSString* channel, NSString* message) {
            
            //NSLog(@"%@", message);
        }];
    }];
}

- (void)unsubscribe:(CDVInvokedUrlCommand*)command
{
    [self trowException:@"ORTC not connected" forCommand:command code:^(CDVInvokedUrlCommand *cmd) {
        NSString* channel = [[cmd.arguments objectAtIndex:0] objectForKey:@"channel"];
        [_connectCommand setObject:cmd forKey:[NSString stringWithFormat:@"unsub:%@",channel]];
        [_ortc unsubscribe:channel];
    }];
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
 [self trowException:@"ORTC not connected" forCommand:command code:^(CDVInvokedUrlCommand *cmd) {
    NSDictionary* args = [cmd.arguments objectAtIndex:0];
    
    NSString* channel = [args objectForKey:@"channel"];
    NSString* channelMsg = [args objectForKey:@"message"];
    
     [_ortc send:channel message:channelMsg];
 }];
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















