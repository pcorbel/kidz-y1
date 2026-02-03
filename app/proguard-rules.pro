# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep all classes in the com.kidz.y1 package
-keep class com.kidz.y1.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep MediaPlayer callbacks
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.load.data.DataFetcher {
 <init>(...);
}
-keep class com.kidz.y1.utils.GlideConfiguration
-keep class com.kidz.y1.utils.Id3ModelLoader
-keep class com.kidz.y1.utils.Id3DataFetcher

# Keep ViewModels and LiveData
-keep class com.kidz.y1.viewmodels.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep repositories
-keep class com.kidz.y1.repositories.** { *; }

# Keep models
-keep class com.kidz.y1.models.** { *; }

# Keep utility classes
-keep class com.kidz.y1.utils.** { *; }

# Keep activities
-keep class com.kidz.y1.activities.** { *; }

# Keep custom views
-keep class com.kidz.y1.views.** { *; }

