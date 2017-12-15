/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Context;
import android.hardware.radio.V1_0.ActivityStatsInfo;
import android.hardware.radio.V1_0.AppStatus;
import android.hardware.radio.V1_0.CardStatus;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.IRadioResponse;
import android.hardware.radio.V1_0.LastCallFailCauseInfo;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.LceStatusInfo;
import android.hardware.radio.V1_0.NeighboringCell;
import android.hardware.radio.V1_0.RadioError;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SendSmsResult;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import android.service.carrier.CarrierIdentifier;
import android.telephony.CellInfo;
import android.telephony.ModemActivityInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SamsungMSM8660RadioResponse extends RadioResponse {

    public SamsungMSM8660RadioResponse(RIL ril) {
        super(ril);
    }

    @Override
    protected void responseIccCardStatus(RadioResponseInfo responseInfo, CardStatus cardStatus) {
        RILRequest rr = mRil.processResponse(responseInfo);

        if (rr != null) {
            IccCardStatus iccCardStatus = new IccCardStatus();
            iccCardStatus.setCardState(cardStatus.cardState);
            iccCardStatus.setUniversalPinState(cardStatus.universalPinState);
            iccCardStatus.mGsmUmtsSubscriptionAppIndex = cardStatus.gsmUmtsSubscriptionAppIndex;
            iccCardStatus.mCdmaSubscriptionAppIndex = cardStatus.cdmaSubscriptionAppIndex;
            iccCardStatus.mImsSubscriptionAppIndex = cardStatus.imsSubscriptionAppIndex;
            int numApplications = cardStatus.applications.size();

            // limit to maximum allowed applications
            if (numApplications
                    > com.android.internal.telephony.uicc.IccCardStatus.CARD_MAX_APPS) {
                numApplications =
                        com.android.internal.telephony.uicc.IccCardStatus.CARD_MAX_APPS;
            }
            iccCardStatus.mApplications = new IccCardApplicationStatus[numApplications];
            for (int i = 0; i < numApplications; i++) {
                AppStatus rilAppStatus = cardStatus.applications.get(i);
                IccCardApplicationStatus appStatus = new IccCardApplicationStatus();
                appStatus.app_type       = appStatus.AppTypeFromRILInt(rilAppStatus.appType);
                appStatus.app_state      = appStatus.AppStateFromRILInt(rilAppStatus.appState);
                appStatus.perso_substate = appStatus.PersoSubstateFromRILInt(
                        rilAppStatus.persoSubstate);
                appStatus.aid            = rilAppStatus.aidPtr;
                appStatus.app_label      = rilAppStatus.appLabelPtr;
                appStatus.pin1_replaced  = rilAppStatus.pin1Replaced;
                appStatus.pin1           = appStatus.PinStateFromRILInt(rilAppStatus.pin1);
                appStatus.pin2           = appStatus.PinStateFromRILInt(rilAppStatus.pin2);
                iccCardStatus.mApplications[i] = appStatus;
            }
            mRil.riljLog("responseIccCardStatus: from HIDL: " + iccCardStatus);
            if (responseInfo.error == RadioError.NONE) {
                sendMessageResponse(rr.mResult, iccCardStatus);
            }
            mRil.processResponseDone(rr, responseInfo, iccCardStatus);
        }
    }

    @Override
    protected void responseCurrentCalls(RadioResponseInfo responseInfo,
                                      ArrayList<android.hardware.radio.V1_0.Call> calls) {
        RILRequest rr = mRil.processResponse(responseInfo);

        if (rr != null) {
            int num = calls.size();
            ArrayList<DriverCall> dcCalls = new ArrayList<DriverCall>(num);
            DriverCall dc;

            for (int i = 0; i < num; i++) {
                dc = new DriverCall();
                // TODO: change name of function stateFromCLCC() in DriverCall.java to name
                // clarifying what is CLCC
                dc.state = DriverCall.stateFromCLCC((int) (calls.get(i).state));
                dc.index = calls.get(i).index;
                dc.TOA = calls.get(i).toa;
                dc.isMpty = calls.get(i).isMpty;
                dc.isMT = calls.get(i).isMT;
                dc.als = calls.get(i).als;
                dc.isVoice = calls.get(i).isVoice;
                dc.isVoicePrivacy = calls.get(i).isVoicePrivacy;
                dc.number = calls.get(i).number;
                dc.numberPresentation =
                        DriverCall.presentationFromCLIP(
                                (int) (calls.get(i).numberPresentation));
                dc.name = calls.get(i).name;
                dc.namePresentation =
                        DriverCall.presentationFromCLIP((int) (calls.get(i).namePresentation));
                if (calls.get(i).uusInfo.size() == 1) {
                    dc.uusInfo = new UUSInfo();
                    dc.uusInfo.setType(calls.get(i).uusInfo.get(0).uusType);
                    dc.uusInfo.setDcs(calls.get(i).uusInfo.get(0).uusDcs);
                    if (calls.get(i).uusInfo.get(0).uusData != null) {
                        byte[] userData = calls.get(i).uusInfo.get(0).uusData.getBytes();
                        dc.uusInfo.setUserData(userData);
                    } else {
                        mRil.riljLog("responseCurrentCalls: uusInfo data is null");
                    }

                    mRil.riljLogv(String.format("Incoming UUS : type=%d, dcs=%d, length=%d",
                            dc.uusInfo.getType(), dc.uusInfo.getDcs(),
                            dc.uusInfo.getUserData().length));
                    mRil.riljLogv("Incoming UUS : data (hex): "
                            + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
                } else {
                    mRil.riljLogv("Incoming UUS : NOT present!");
                }

                // Make sure there's a leading + on addresses with a TOA of 145
                dc.number = PhoneNumberUtils.stringFromStringAndTOA(dc.number, dc.TOA);

                dcCalls.add(dc);

                if (dc.isVoicePrivacy) {
                    mRil.mVoicePrivacyOnRegistrants.notifyRegistrants();
                    mRil.riljLog("InCall VoicePrivacy is enabled");
                } else {
                    mRil.mVoicePrivacyOffRegistrants.notifyRegistrants();
                    mRil.riljLog("InCall VoicePrivacy is disabled");
                }
            }

            Collections.sort(dcCalls);

            if ((num == 0) && mRil.mTestingEmergencyCall.getAndSet(false)) {
                if (mRil.mEmergencyCallbackModeRegistrant != null) {
                    mRil.riljLog("responseCurrentCalls: call ended, testing emergency call,"
                            + " notify ECM Registrants");
                    mRil.mEmergencyCallbackModeRegistrant.notifyRegistrant();
                }
            }

            if (responseInfo.error == RadioError.NONE) {
                sendMessageResponse(rr.mResult, dcCalls);
            }
            mRil.processResponseDone(rr, responseInfo, dcCalls);
        }
    }
}
