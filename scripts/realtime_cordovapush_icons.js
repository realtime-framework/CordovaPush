var filestocopy = [{ 
	"resources/android/drawable/small_notification_icon.png": "platforms/android/res/drawable-ldpi/small_notification_icon.png", 
	"resources/android/drawable/large_notification_icon.png": "platforms/android/res/drawable-ldpi/large_notification_icon.png",
	"resources/android/values/colors.xml": "platforms/android/res/values/colors.xml" 
}];

var fs = require('fs'); 
var path = require('path');

filestocopy.forEach(function(obj) { 
	Object.keys(obj).forEach(function(key) { 
		var val = obj[key]; 
		var srcfile = key; 
		var destfile = val; 
		console.log("copying "+srcfile+" to "+destfile); 
		var destdir = path.dirname(destfile); 
		if (fs.existsSync(srcfile) && fs.existsSync(destdir)) { 
			fs.createReadStream(srcfile).pipe( 
			fs.createWriteStream(destfile)); 
		} 
	}); 
});