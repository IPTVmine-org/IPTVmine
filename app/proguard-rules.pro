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

# Preserve the Channel class and its fields
# Add project-specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see:
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name of the JavaScript interface
# class:
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
# }

# Uncomment this to preserve the line number information for
# debugging stack traces.
# -keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
# -renamesourcefileattribute SourceFile

# Preserve the Channel class and its fields and methods
-keep class com.samyak2403.iptvmine.model.Channel {
    <fields>;
    <methods>;
}

# Keep the CREATOR field necessary for Parcelable implementation
-keepclassmembers class com.samyak2403.iptvmine.model.Channel {
    public static final android.os.Parcelable$Creator *;
}

# Preserve the Parcel constructor
-keepclassmembers class com.samyak2403.iptvmine.model.Channel {
    public Channel(android.os.Parcel);
}

# Preserve the HomeFragment class and its fields and methods
-keep class com.samyak2403.iptvmine.screens.HomeFragment {
    <fields>;
    <methods>;
}

# Preserve the ChannelsProvider class and its fields and methods
-keep class com.samyak2403.iptvmine.provider.ChannelsProvider {
    <fields>;
    <methods>;
}

# Keep ViewModel and LiveData-related classes (used by ViewModelProvider)
-keep class androidx.lifecycle.** { *; }

# Keep the ChannelsAdapter class and its fields and methods (for RecyclerView)
-keep class com.samyak2403.iptvmine.adapter.ChannelsAdapter {
    <fields>;
    <methods>;
}

# Preserve all Fragment subclasses
-keep class * extends androidx.fragment.app.Fragment {
    <fields>;
    <methods>;
}

# Preserve Handler-related classes
-keep class android.os.Handler {
    <fields>;
    <methods>;
}

# Preserve Looper-related classes
-keep class android.os.Looper {
    <fields>;
    <methods>;
}

# Keep LiveData and ViewModel classes and their methods
-keep class androidx.lifecycle.LiveData {
    <methods>;
}

-keep class androidx.lifecycle.MutableLiveData {
    <methods>;
}

-keep class androidx.lifecycle.ViewModel {
    <methods>;
}

# Keep coroutine-related classes and methods
-keep class kotlinx.coroutines.CoroutineScope {
    <methods>;
}

-keep class kotlinx.coroutines.Dispatchers {
    <fields>;
    <methods>;
}

-keep class kotlinx.coroutines.JobSupport {
    <fields>;
    <methods>;
}

# Keep networking-related classes and methods (if necessary)
-keepclassmembers class java.net.HttpURLConnection {
    <methods>;
}

-keep class java.net.URL {
    <methods>;
}

# Keep the ChannelsProvider ViewModel class and its members
-keepclassmembers class com.samyak2403.iptvmine.provider.ChannelsProvider {
    *;
}

# Keep the Channel model class
-keepclassmembers class com.samyak2403.iptvmine.model.Channel {
    *;
}

# Prevent obfuscation of methods used for reflection by Android Lifecycle
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep methods and fields annotated with @SerializedName or similar (if using serialization)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep coroutine-related classes and methods to avoid runtime issues
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep URL and HttpURLConnection usage (common in network calls)
-keep class java.net.URL { *; }
-keep class java.net.HttpURLConnection { *; }

