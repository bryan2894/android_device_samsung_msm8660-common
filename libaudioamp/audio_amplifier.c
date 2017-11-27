/*
 * Copyright (C) 2017, The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "libaudioamp"
#define LOG_NDEBUG 0

#include <stdint.h>
#include <stdlib.h>
#include <sys/types.h>

#include <sys/ioctl.h>
#include <fcntl.h>
#include <pthread.h>

#include <cutils/log.h>

#include <system/audio.h>
#include "audio_amplifier.h"

#include <msm8660/platform.h>

#include <linux/a2220.h>

#define A2220_DEVICE "/dev/audience_a2220"

#define UNUSED __attribute__((unused))

typedef struct a2220_dev {
    audio_mode_t current_mode;
    int fd;
    int mode;
    pthread_mutex_t lock;
} a2220_device_t;

static a2220_device_t *a2220_dev = NULL;

int a2220_init(void)
{
    int rc = -1;

    a2220_dev->fd = open(A2220_DEVICE, O_RDWR);
    a2220_dev->mode = A2220_PATH_SUSPEND;

    if (!a2220_dev->fd) {
        ALOGE("%s: unable to open a2220 device!", __func__);
        close(a2220_dev->fd);
        return rc;
    } else {
        ALOGV("%s: device opened, fd=%d", __func__, a2220_dev->fd);
        pthread_mutex_init(&a2220_dev->lock, NULL);
    }

    return 0;
}

int a2220_set_mode(int mode)
{
    int rc = -1;

    if (a2220_dev->mode != mode) {
        pthread_mutex_lock(&a2220_dev->lock);

        rc = ioctl(a2220_dev->fd, A2220_SET_CONFIG, mode);
        if (rc < 0) {
            ALOGE("%s: ioctl failed, errno=%d", __func__, errno);
        } else {
            a2220_dev->mode = mode;
            ALOGV("%s: Audience A2220 mode is set to %d.", __func__, mode);
        }
        pthread_mutex_unlock(&a2220_dev->lock);
    }
    return rc;
}

int set_mode(audio_mode_t mode)
{
    int ret = 0;

    a2220_dev->current_mode = mode;

    return ret;
}

int enable_input_devices(uint32_t devices, bool enable)
{
    int mode = A2220_PATH_SUSPEND;

    if (a2220_dev->current_mode == AUDIO_MODE_IN_CALL || a2220_dev->current_mode == AUDIO_MODE_IN_COMMUNICATION) {
        /* Enable noise suppression for input */
        switch (devices) {
            case SND_DEVICE_IN_VOIP_HANDSET_MIC:
                mode = A2220_PATH_INCALL_RECEIVER_NSON;
                break;
            case SND_DEVICE_IN_SPEAKER_DMIC:
            case SND_DEVICE_IN_SPEAKER_DMIC_AEC:
            case SND_DEVICE_IN_SPEAKER_DMIC_NS:
            case SND_DEVICE_IN_SPEAKER_DMIC_AEC_NS:
            case SND_DEVICE_IN_VOIP_SPEAKER_MIC:
                if (enable)
                    mode = A2220_PATH_INCALL_SPEAKER;
            case SND_DEVICE_IN_VOIP_HEADSET_MIC:
                if (enable)
                    mode = A2220_PATH_INCALL_HEADSET;
                break;
            default:
                if (enable)
                    mode = A2220_PATH_SUSPEND;
                break;
        }
    }

    a2220_set_mode(mode);

    return 0;
}

int amplifier_close(void)
{
    if (a2220_dev->fd >= 0)
        close(a2220_dev->fd);

    pthread_mutex_destroy(&a2220_dev->lock);

    free(a2220_dev);

    return 0;
}

int amplifier_open(void)
{
    if (a2220_dev) {
        ALOGE("%s:%d: Unable to open second instance of A2220 amplifier\n",
                __func__, __LINE__);
        return -EBUSY;
    }

    a2220_dev = calloc(1, sizeof(a2220_device_t));
    if (!a2220_dev) {
        ALOGE("%s:%d: Unable to allocate memory for amplifier device\n",
                __func__, __LINE__);
        return -ENOMEM;
    }

    a2220_dev->current_mode = AUDIO_MODE_NORMAL;

    a2220_init();

    return 0;
}

int set_input_devices(uint32_t devices)
{
    return 0;
}

int set_output_devices(uint32_t devices)
{
    return 0;
}

int enable_output_devices(uint32_t devices, bool enable)
{
    return 0;
}

int output_stream_start(struct audio_stream_out *stream, bool offload)
{
    return 0;
}

int input_stream_start(struct audio_stream_in *stream)
{
    return 0;
}

int output_stream_standby(struct audio_stream_out *stream)
{
    return 0;
}

int input_stream_standby(struct audio_stream_in *stream)
{
    return 0;
}

int set_parameters(struct str_parms *parms)
{
    return 0;
}
