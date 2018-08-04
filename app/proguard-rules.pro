# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#====WebView + js====
-keepclassmembers class com.why.project.mybridgewebviewdemo.customwebview.jsbridgewebview.MyBridgeWebView {
   public *;
}
-keepclassmembers class com.why.project.mybridgewebviewdemo.customwebview.jsbridgewebview.MyBridgeWebChromeClient {
   public *;
}
-keepclassmembers class com.why.project.mybridgewebviewdemo.customwebview.jsbridgewebview.MyBridgeWebViewClient {
   public *;
}
# keep 使用 webview 的类
-keepclassmembers class com.why.project.mybridgewebviewdemo.MainActivity {
  public *;
}
-keepattributes *Annotation*
#解决：android sdk api >= 17 时需要加@JavascriptInterface”所出现的问题。
-keepattributes *JavascriptInterface*