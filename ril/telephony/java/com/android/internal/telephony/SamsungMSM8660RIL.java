/*
 * Copyright (C) 2012-2015 The CyanogenMod Project
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
import android.media.AudioManager;
import android.os.AsyncResult;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SignalStrength;

import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Qualcomm RIL for the Samsung MSM8660 family.
 * {@hide}
 */
public class SamsungMSM8660RIL extends SamsungQualcommRIL {

    public SamsungMSM8660RIL(Context context, int networkMode, int cdmaSubscription) {
        super(context, networkMode, cdmaSubscription);
    }

    @Override
    protected void
    processUnsolicited(Parcel p) {
        int dataPosition = p.dataPosition();
        int origResponse = p.readInt();
        int newResponse = origResponse;
        switch (origResponse) {
            case 1036:
                newResponse = RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED;
                break;
            case 1039:
                newResponse = RIL_UNSOL_ON_SS;
                break;
            case 1040:
                newResponse = RIL_UNSOL_STK_CC_ALPHA_NOTIFY;
                break;
            case 1041:
                newResponse = RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED;
                break;
            case 1037: // RIL_UNSOL_TETHERED_MODE_STATE_CHANGED
            case 1038: // RIL_UNSOL_DATA_NETWORK_STATE_CHANGED
            case 1042: // RIL_UNSOL_QOS_STATE_CHANGED_IND
                riljLog("SamsungMSM8660RIL: ignoring unsolicited response " +
                        origResponse);
                return;
        }
        if (newResponse != origResponse) {
            riljLog("SamsungMSM8660RIL: remap unsolicited response from " +
                    origResponse + " to " + newResponse);
            p.setDataPosition(dataPosition);
            p.writeInt(newResponse);
        }
        p.setDataPosition(dataPosition);
        super.processUnsolicited(p);
    }

    @Override
    protected RILRequest
    processSolicited (Parcel p) {
        int serial, error;
        boolean found = false;
        int dataPosition = p.dataPosition(); // save off position within the Parcel
        serial = p.readInt();
        error = p.readInt();
        RILRequest rr = null;
        /* Pre-process the reply before popping it */
        synchronized (mRequestList) {
            RILRequest tr = mRequestList.get(serial);
            if (tr != null && tr.mSerial == serial) {
                if (error == 0 || p.dataAvail() > 0) {
                    try {switch (tr.mRequest) {
                            /* Get those we're interested in */
                        case RIL_REQUEST_DATA_REGISTRATION_STATE:
                        case RIL_REQUEST_OPERATOR:
                            rr = tr;
                            break;
                    }} catch (Throwable thr) {
                        // Exceptions here usually mean invalid RIL responses
                        if (tr.mResult != null) {
                            AsyncResult.forMessage(tr.mResult, null, thr);
                            tr.mResult.sendToTarget();
                        }
                        return tr;
                    }
                }
            }
        }
        if (rr == null) {
            /* Nothing we care about, go up */
            p.setDataPosition(dataPosition);
            // Forward responses that we are not overriding to the super class
            return super.processSolicited(p);
        }
        rr = findAndRemoveRequestFromList(serial);
        if (rr == null) {
            return rr;
        }
        Object ret = null;
        if (error == 0 || p.dataAvail() > 0) {
            switch (rr.mRequest) {
                case RIL_REQUEST_DATA_REGISTRATION_STATE: ret = responseDataRegistrationState(p); break;
                case RIL_REQUEST_OPERATOR: ret =  operatorCheck(p); break;
                default:
                    throw new RuntimeException("Unrecognized solicited response: " + rr.mRequest);
            }
            //break;
        }
        if (RILJ_LOGD) riljLog(rr.serialString() + "< " + requestToString(rr.mRequest)
                               + " " + retToString(rr.mRequest, ret));
        if (rr.mResult != null) {
            AsyncResult.forMessage(rr.mResult, ret, null);
            rr.mResult.sendToTarget();
        }
        return rr;
    }

    private Object
    responseDataRegistrationState(Parcel p) {
      String response[] = (String[])responseStrings(p); // all data from parcell get popped

      /* DANGER WILL ROBINSON
       * In some cases from Vodaphone we are receiving a RAT of 102
       * while in tunnels of the metro. Lets Assume that if we
       * receive 102 we actually want a RAT of 2 for EDGE service */
         if (response.length > 4 &&
             response[0].equals("1") &&
             response[3].equals("102")) {
             response[3] = "2";
         }
      return response;
    }

    // CDMA FIXES, this fixes  bogus values in nv/sim on d2/jf/t0 cdma family or bogus information from sim card
    private Object
    operatorCheck(Parcel p) {
        String response[] = (String[])responseStrings(p);
        for(int i=0; i<2; i++){
            if (response[i]!= null){
                response[i] = Operators.operatorReplace(response[i]);
            }
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getCellInfoList(Message result) {
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
    public void setCellInfoListRate(int rateInMillis, Message response) {
        riljLog("setCellInfoListRate: not supported");
        if (response != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(response, null, ex);
            response.sendToTarget();
        }
    }

    @Override
    public void setInitialAttachApn(String apn, String protocol, int authType, String username,
            String password, Message result) {
        riljLog("setInitialAttachApn: not supported");
        if (result != null) {
            CommandException ex = new CommandException(
                CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
}
