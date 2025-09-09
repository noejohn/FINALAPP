// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.2" apply false // use IDE-supported AGP
//id ("com.android.application") version "8.6.0" apply false
    id("com.android.library") version "8.5.2" apply false
// Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.3" apply false
//id ("org.jetbrains.kotlin.android") version "1.9.25" apply false
}