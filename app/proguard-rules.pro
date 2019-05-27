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

#混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#把混淆类中的方法名也混淆了
-useuniqueclassmembernames

-dontskipnonpubliclibraryclassmembers

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

#将文件来源重命名为“SourceFile”字符串
-renamesourcefileattribute SourceFile
#保持泛型,保留行号
-keepattributes Signature,Expections,InnerClasses,Deprecated,SourceFile,LineNumberTable
#保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#保持所有实现 Parcelable 接口的类成员
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

#litepal
-keep class org.litepal.** {
    *;
}

-keep class * extends org.litepal.crud.LitePalSupport{
    *;
}

#Fragment不需要在AndroidManifest.xml中注册，需要额外保护下
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep class top.geek_studio.chenlongcould.geeklibrary.** { *;}
-keep class androidx.appcompat.widget.** { *;}

# 保持测试相关的代码
#-dontnote junit.framework.**
#-dontnote junit.runner.**
#-dontwarn android.albumLoader.**
#-dontwarn android.support.albumLoader.**
-dontwarn org.junit.**
-dontwarn com.simplecityapps.**
-dontwarn com.sothree.slidinguppanel.**
-dontwarn com.squareup.haha.**
-dontwarn top.geek_studio.chenlongcould.geeklibrary.**

-dontwarn java.nio.file.*


#------------------okhttp
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

-keep class com.squareup.okhttp.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class okio.**{*;}
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

#------------------okhttp
