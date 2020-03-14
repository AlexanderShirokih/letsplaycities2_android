
# Keep LPS client library, because it uses reflection by Gson
-keepattributes Exceptions,LineNumberTable,Signature,Annotation,SourceFile,EnclosingMethod,InnerClasses
-keep public class ru.quandastudio.lpsclient.** { *; }
-keepclassmembers class ru.quandastudio.lpsclient.** { *; }

# Gson specific classes
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keepclassmembers enum * { *; }
-keep public class com.google.gson.**
-keep public class com.google.gson.** {public private protected *;}
-dontobfuscate

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }