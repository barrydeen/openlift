# Keep Room entities & generated implementation
-keep class dev.openlift.app.data.local.entity.** { *; }
-keep class dev.openlift.app.data.local.ExerciseCatalog** { *; }

# Keep kotlinx.serialization serializers
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class dev.openlift.app.**$$serializer { *; }
-keepclassmembers class dev.openlift.app.** {
    *** Companion;
}
-keepclasseswithmembers class dev.openlift.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**
