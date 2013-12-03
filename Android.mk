  LOCAL_PATH := $(call my-dir)
  include $(CLEAR_VARS)
  
  LOCAL_PROGUARD_ENABLED := disabled

  LOCAL_MODULE_TAGS := optional

  LOCAL_STATIC_JAVA_LIBRARIES := android-common android-support-v13 acra
  # Build all java files in the java subdirectory
  LOCAL_SRC_FILES := $(call all-java-files-under, src)
   
  # Name of the APK to build
  LOCAL_PACKAGE_NAME := YAOS-Updater
  
  # Tell it to build an APK
  include $(BUILD_PACKAGE)

  include $(CLEAR_VARS)
  LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := acra:libs/acra.jar
  include $(BUILD_MULTI_PREBUILT)