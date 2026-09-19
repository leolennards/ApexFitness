# Firestore maps documents to these classes by reflection, so R8 must not rename or strip them
-keep class com.example.apexfitness.data.** { *; }
-keepclassmembers class com.example.apexfitness.data.** { *; }

# Keep readable stack traces in release crash reports
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile
