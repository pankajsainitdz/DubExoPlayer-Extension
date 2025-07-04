# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Keep App Inventor classes
-keep class com.google.appinventor.** { *; }
-dontwarn com.google.appinventor.**

# Keep extension classes
-keep class com.dubexoplayer.extension.** { *; }
-keepclassmembers class com.dubexoplayer.extension.** { *; }

# Keep SimpleFunction, SimpleEvent, SimpleProperty annotations
-keepattributes *Annotation*
-keep class com.google.appinventor.components.annotations.** { *; }

# Keep methods with App Inventor annotations
-keepclassmembers class * {
    @com.google.appinventor.components.annotations.SimpleFunction *;
    @com.google.appinventor.components.annotations.SimpleEvent *;
    @com.google.appinventor.components.annotations.SimpleProperty *;
}

# Keep serialization related classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ExoPlayer specific rules
-keep class com.google.android.exoplayer2.ext.** { *; }
-keep class com.google.android.exoplayer2.upstream.** { *; }
-keep class com.google.android.exoplayer2.source.** { *; }
-keep class com.google.android.exoplayer2.trackselection.** { *; }
-keep class com.google.android.exoplayer2.ui.** { *; }

# OkHttp and networking
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Suppress warnings
-dontwarn java.lang.invoke.*
-dontwarn org.codehaus.mojo.animal_sniffer.*
