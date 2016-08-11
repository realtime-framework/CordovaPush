#!/usr/bin/env node

// Copy native resources
var rootdir = process.argv[2];
var exec = require('child_process').exec;

// Native resources to copy
var androidNativePath = 'resources/android/';

// Android platform resource path
var androidResPath = 'platforms/android/res/';

function copyAndroidResources() {
  exec('cp -Rf ' + androidNativePath + '* ' + androidResPath);
  process.stdout.write('Copied android native resources');
}

if (rootdir) {
  copyAndroidResources();
}