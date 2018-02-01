/* //device/libs/telephony/ril_unsol_commands.h
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
    {RIL_OEM_UNSOL_RESPONSE_BASE, NULL, WAKE_PARTIAL}, // RIL_OEM_UNSOL_RESPONSE_BASE should be defined as 11000 in ril.h
    {11001, NULL, WAKE_PARTIAL}, // 11001
    {11002, NULL, WAKE_PARTIAL}, // 11002
    {RIL_UNSOL_STK_CALL_CONTROL_RESULT, NULL, WAKE_PARTIAL}, // 11003
    {11004, NULL, WAKE_PARTIAL}, // 11004
    {11005, NULL, WAKE_PARTIAL}, // 11005
    {11006, NULL, WAKE_PARTIAL}, // 11006
    {11007, NULL, WAKE_PARTIAL}, // 11007
    {11008, NULL, WAKE_PARTIAL}, // 11008
    {11009, NULL, WAKE_PARTIAL}, // 11009
    {RIL_UNSOL_AM, NULL, WAKE_PARTIAL}, // 11010
    {11011, NULL, WAKE_PARTIAL}, // 11011
    {11012, NULL, WAKE_PARTIAL}, // 11012
    {11013, NULL, WAKE_PARTIAL}, // 11013
    {11014, NULL, WAKE_PARTIAL}, // 11014
    {11015, NULL, WAKE_PARTIAL}, // 11015
    {11016, NULL, WAKE_PARTIAL}, // 11016
    {11017, NULL, WAKE_PARTIAL}, // 11017
    {11018, NULL, WAKE_PARTIAL}, // 11018
    {RIL_UNSOL_SIM_LOCK_INFO, NULL, WAKE_PARTIAL}, // 11019
    {RIL_UNSOL_UART, NULL, WAKE_PARTIAL}, // 11020
    {RIL_UNSOL_SIM_PB_READY, NULL, WAKE_PARTIAL}, // 11021
    {11022, NULL, WAKE_PARTIAL}, // 11022
    {11023, NULL, WAKE_PARTIAL}, // 11023
    {11024, NULL, WAKE_PARTIAL}, // 11024
    {11025, NULL, WAKE_PARTIAL}, // 11025
    {RIL_UNSOL_FACTORY_AM, NULL, WAKE_PARTIAL}, // 11026
