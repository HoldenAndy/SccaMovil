# Add project specific ProGuard rules here.

# Keep data classes used for serialization (Kotlinx Serialization)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.proyecto.scca.**$$serializer { *; }
-keepclassmembers class com.proyecto.scca.** {
    *** Companion;
}
-keepclasseswithmembers class com.proyecto.scca.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Vico
-keep class com.patrykandpatrick.vico.** { *; }

# Kotlinx DateTime
-keep class kotlinx.datetime.** { *; }

# Domain models (must not be obfuscated)
-keep class com.proyecto.scca.domain.model.** { *; }
-keep class com.proyecto.scca.data.remote.dto.** { *; }
