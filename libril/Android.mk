# Copyright 2006 The Android Open Source Project

ifneq ($(BOARD_PROVIDES_LIBRIL),true)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_VENDOR_MODULE := true

LOCAL_SRC_FILES:= \
    ril.cpp \
    ril_event.cpp\
    RilSapSocket.cpp \
    ril_service.cpp \
    sap_service.cpp

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libutils \
    libcutils \
    libhardware_legacy \
    librilutils \
    android.hardware.radio@1.0 \
    android.hardware.radio.deprecated@1.0 \
    libhidlbase  \
    libhidltransport \
    libhwbinder

LOCAL_STATIC_LIBRARIES := \
    libprotobuf-c-nano-enable_malloc \

<<<<<<< HEAD:libril/Android.mk
LOCAL_CFLAGS :=

ifeq ($(SIM_COUNT), 2)
    LOCAL_CFLAGS += -DANDROID_MULTI_SIM
=======
LOCAL_CFLAGS += -Wno-unused-parameter

ifeq ($(SIM_COUNT), 2)
    LOCAL_CFLAGS += -DANDROID_MULTI_SIM -DDSDA_RILD1
>>>>>>> afc35718... ril: import Oreo AOSP libril:ril/libril/Android.mk
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_2
endif

LOCAL_C_INCLUDES += external/nanopb-c
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../include
LOCAL_EXPORT_C_INCLUDE_DIRS := $(LOCAL_PATH)/../include

LOCAL_MODULE:= libril
ifeq ($(TARGET_NEEDS_GCC_LIBRIL),true)
    LOCAL_CLANG := false
else
    LOCAL_CLANG := true
    LOCAL_SANITIZE := integer
endif

include $(BUILD_SHARED_LIBRARY)

<<<<<<< HEAD:libril/Android.mk

# For RdoServD which needs a static library
# =========================================
ifneq ($(ANDROID_BIONIC_TRANSITION),)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    ril.cpp

LOCAL_STATIC_LIBRARIES := \
    libutils_static \
    libcutils \
    librilutils_static \
    libprotobuf-c-nano-enable_malloc

LOCAL_MODULE:= libril_static

include $(BUILD_STATIC_LIBRARY)
endif # ANDROID_BIONIC_TRANSITION
=======
>>>>>>> afc35718... ril: import Oreo AOSP libril:ril/libril/Android.mk
endif # BOARD_PROVIDES_LIBRIL
