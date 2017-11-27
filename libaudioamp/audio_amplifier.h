/*
 * Copyright (C) 2013, The CyanogenMod Project
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

#include <stdint.h>
#include <sys/cdefs.h>
#include <sys/types.h>

#include <hardware/audio.h>
#include <hardware/hardware.h>

#include <system/audio.h>

struct str_parms;

#if defined(__cplusplus)
extern "C" {
#endif
    int amplifier_open(void);

    int amplifier_close(void);

    /**
     * Notify amplifier device of current input devices
     *
     * This function should handle only input devices.
     */
    int set_input_devices(uint32_t devices);

    /**
     * Notify amplifier device of current output devices
     *
     * This function should handle only output devices.
     */
    int set_output_devices(uint32_t devices);

    /**
     * Notify amplifier device of output device enable/disable
     *
     * This function should handle only output devices.
     */
    int enable_output_devices(uint32_t devices, bool enable);

    /**
     * Notify amplifier device of input device enable/disable
     *
     * This function should handle only input devices.
     */
    int enable_input_devices(uint32_t devices, bool enable);

    /**
     * Notify amplifier device about current audio mode
     */
    int set_mode(audio_mode_t mode);

    /**
     * Notify amplifier device that an output stream has started
     */
    int output_stream_start(struct audio_stream_out *stream, bool offload);

    /**
     * Notify amplifier device that an input stream has started
     */
    int input_stream_start(struct audio_stream_in *stream);

    /**
     * Notify amplifier device that an output stream has stopped
     */
    int output_stream_standby(struct audio_stream_out *stream);

    /**
     * Notify amplifier device that an input stream has stopped
     */
    int input_stream_standby(struct audio_stream_in *stream);

    /**
     * set/get output audio device parameters.
     */
    int set_parameters(struct str_parms *parms);
#if defined(__cplusplus)
}
#endif
