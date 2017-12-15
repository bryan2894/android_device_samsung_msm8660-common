/*
 * Copyright (C) 2012-2015 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package com.android.internal.telephony;

import static com.android.internal.telephony.RILConstants.*;

import android.content.Context;
import android.hardware.radio.V1_0.Dial;
import android.hardware.radio.V1_0.IRadio;
import android.hardware.radio.V1_0.UusInfo;
import android.media.AudioManager;
import android.os.AsyncResult;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;

import com.android.internal.telephony.dataconnection.DataProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Qualcomm RIL for the Samsung MSM8660 family.
 * {@hide}
 */
public class SamsungMSM8660RIL extends RIL implements CommandsInterface {

    private static final int RIL_REQUEST_DIAL_EMERGENCY = 10016;
    private boolean setPreferredNetworkTypeSeen = false;

    public SamsungMSM8660RIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public SamsungMSM8660RIL(Context context, int preferredNetworkType,
            int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    @Override
    protected RadioResponse createRadioResponse() {
        return new SamsungMSM8660RadioResponse(this);
    }

    @Override
    protected RadioIndication createRadioIndication() {
        return new SamsungMSM8660RadioIndication(this);
    }

    @Override
    public void acceptCall(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(RIL_REQUEST_ANSWER, result,
                    mRILDefaultWorkSource);

            if (RILJ_LOGD) {
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }

            try {
                radioProxy.acceptCall(rr.mSerial);
                mMetrics.writeRilAnswer(mPhoneId, rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "acceptCall", e);
            }
        }
    }

    @Override
    public void dial(String address, int clirMode, Message result) {
        dial(address, clirMode, null, result);
    }

    @Override
    public void dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        if (PhoneNumberUtils.isEmergencyNumber(address)) {
            dialEmergencyCall(address, clirMode, uusInfo, result);
            return;
        }
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(RIL_REQUEST_DIAL, result,
                    mRILDefaultWorkSource);

            Dial dialInfo = new Dial();
            dialInfo.address = convertNullToEmptyString(address);
            dialInfo.clir = clirMode;
            if (uusInfo != null) {
                UusInfo info = new UusInfo();
                info.uusType = uusInfo.getType();
                info.uusDcs = uusInfo.getDcs();
                info.uusData = new String(uusInfo.getUserData());
                dialInfo.uusInfo.add(info);
            }

            if (RILJ_LOGD) {
                // Do not log function arg for privacy
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }

            try {
                radioProxy.dial(rr.mSerial, dialInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "dial", e);
            }
        }
    }

    private void
    dialEmergencyCall(String address, int clirMode, UUSInfo uusInfo, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(RIL_REQUEST_DIAL_EMERGENCY, result,
                    mRILDefaultWorkSource);

            Dial dialInfo = new Dial();
            dialInfo.address = convertNullToEmptyString(address);
            dialInfo.clir = clirMode;
            if (uusInfo != null) {
                UusInfo info = new UusInfo();
                info.uusType = uusInfo.getType();
                info.uusDcs = uusInfo.getDcs();
                info.uusData = new String(uusInfo.getUserData());
                dialInfo.uusInfo.add(info);
            }

            if (RILJ_LOGD) {
                // Do not log function arg for privacy
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            }

            try {
                radioProxy.dial(rr.mSerial, dialInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "dial", e);
            }
        }
    }

    @Override
    public void getImsRegistrationState(Message result) {
        if(mRilVersion >= 8)
            super.getImsRegistrationState(result);
        else {
            if (result != null) {
                CommandException ex = new CommandException(
                    CommandException.Error.REQUEST_NOT_SUPPORTED);
                AsyncResult.forMessage(result, null, ex);
                result.sendToTarget();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getCellInfoList(Message result, WorkSource workSource) {
        riljLog("getCellInfoList: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellInfoListRate(int rateInMillis, Message result, WorkSource workSource) {
        riljLog("setCellInfoListRate: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }

    // This call causes ril to crash the socket, stopping further communication
    @Override
    public void
    getHardwareConfig (Message result) {
        riljLog("Ignoring call to 'getHardwareConfig'");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataAllowed(boolean allowed, Message result) {
        riljLog("setDataAllowed: not supported");

        if (result != null) {
            CommandException e = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, e);
            result.sendToTarget();
        }
    }

    @Override
    public void getRadioCapability(Message response) {
        riljLog("getRadioCapability: returning static radio capability");
        if (response != null) {
            Object ret = makeStaticRadioCapability();
            AsyncResult.forMessage(response, ret, null);
            response.sendToTarget();
        }
    }

    @Override
    public void setPreferredNetworkType(int networkType , Message response) {
        riljLog("setPreferredNetworkType: " + networkType);

        if (!setPreferredNetworkTypeSeen) {
            riljLog("Need to reboot modem!");
            setRadioPower(false, null);
            setPreferredNetworkTypeSeen = true;
        }

        super.setPreferredNetworkType(networkType, response);
    }

    @Override
    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
        riljLog("startLceService: not supported");
        if (result != null) {
            CommandException e = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, e);
            result.sendToTarget();
        }
    }

    @Override
    public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, Message result) {
        riljLog("setInitialAttachApn: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }

    @Override
    public void getModemActivityInfo(Message result) {
        riljLog("getModemActivityInfo: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }

    @Override
    public void iccOpenLogicalChannel(String aid, int p2, Message result) {
        riljLog("iccOpenLogicalChannel: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                    CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
        return;
    }

    @Override
    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction,
                                              int p1, int p2, int p3, String data,
                                              Message result) {
        riljLog("iccTransmitApduLogicalChannel: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                    CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
        return;
    }

    @Override
    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2,
                                            int p3, String data, Message result) {
        riljLog("iccTransmitApduBasicChannel: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                    CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
        return;
    }
}
