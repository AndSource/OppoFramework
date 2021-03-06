package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.config.V1_0.SimSlotStatus;
import android.hardware.radio.config.V1_1.ModemsConfig;
import android.hardware.radio.config.V1_2.IRadioConfigResponse;
import android.telephony.ModemInfo;
import android.telephony.PhoneCapability;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.IccSlotStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RadioConfigResponse extends IRadioConfigResponse.Stub {
    private static final String TAG = "RadioConfigResponse";
    private final RadioConfig mRadioConfig;

    public RadioConfigResponse(RadioConfig radioConfig) {
        this.mRadioConfig = radioConfig;
    }

    @Override // android.hardware.radio.config.V1_0.IRadioConfigResponse
    public void getSimSlotsStatusResponse(RadioResponseInfo responseInfo, ArrayList<SimSlotStatus> slotStatus) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr != null) {
            ArrayList<IccSlotStatus> ret = RadioConfig.convertHalSlotStatus(slotStatus);
            if (responseInfo.error == 0) {
                RadioResponse.sendMessageResponse(rr.mResult, ret);
                StringBuilder sb = new StringBuilder();
                sb.append(rr.serialString());
                sb.append("< ");
                RadioConfig radioConfig = this.mRadioConfig;
                sb.append(RadioConfig.requestToString(rr.mRequest));
                sb.append(" ");
                sb.append(ret.toString());
                Rlog.d(TAG, sb.toString());
                return;
            }
            rr.onError(responseInfo.error, ret);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
            return;
        }
        Rlog.e(TAG, "getSimSlotsStatusResponse: Error " + responseInfo.toString());
    }

    @Override // android.hardware.radio.config.V1_2.IRadioConfigResponse
    public void getSimSlotsStatusResponse_1_2(RadioResponseInfo responseInfo, ArrayList<android.hardware.radio.config.V1_2.SimSlotStatus> slotStatus) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr != null) {
            ArrayList<IccSlotStatus> ret = RadioConfig.convertHalSlotStatus_1_2(slotStatus);
            if (responseInfo.error == 0) {
                RadioResponse.sendMessageResponse(rr.mResult, ret);
                StringBuilder sb = new StringBuilder();
                sb.append(rr.serialString());
                sb.append("< ");
                RadioConfig radioConfig = this.mRadioConfig;
                sb.append(RadioConfig.requestToString(rr.mRequest));
                sb.append(" ");
                sb.append(ret.toString());
                Rlog.d(TAG, sb.toString());
                return;
            }
            rr.onError(responseInfo.error, ret);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
            return;
        }
        Rlog.e(TAG, "getSimSlotsStatusResponse_1_2: Error " + responseInfo.toString());
    }

    @Override // android.hardware.radio.config.V1_0.IRadioConfigResponse
    public void setSimSlotsMappingResponse(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr == null) {
            Rlog.e(TAG, "setSimSlotsMappingResponse: Error " + responseInfo.toString());
        } else if (responseInfo.error == 0) {
            RadioResponse.sendMessageResponse(rr.mResult, null);
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("< ");
            RadioConfig radioConfig = this.mRadioConfig;
            sb.append(RadioConfig.requestToString(rr.mRequest));
            Rlog.d(TAG, sb.toString());
        } else {
            rr.onError(responseInfo.error, null);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
        }
    }

    private PhoneCapability convertHalPhoneCapability(android.hardware.radio.config.V1_1.PhoneCapability phoneCapability) {
        int maxActiveData = phoneCapability.maxActiveData;
        boolean validationBeforeSwitchSupported = phoneCapability.isInternetLingeringSupported;
        List<ModemInfo> logicalModemList = new ArrayList<>();
        Iterator<android.hardware.radio.config.V1_1.ModemInfo> it = phoneCapability.logicalModemList.iterator();
        while (it.hasNext()) {
            logicalModemList.add(new ModemInfo(it.next().modemId));
        }
        return new PhoneCapability(0, maxActiveData, 0, logicalModemList, validationBeforeSwitchSupported);
    }

    @Override // android.hardware.radio.config.V1_1.IRadioConfigResponse
    public void getPhoneCapabilityResponse(RadioResponseInfo responseInfo, android.hardware.radio.config.V1_1.PhoneCapability phoneCapability) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr != null) {
            PhoneCapability ret = convertHalPhoneCapability(phoneCapability);
            if (responseInfo.error == 0) {
                RadioResponse.sendMessageResponse(rr.mResult, ret);
                StringBuilder sb = new StringBuilder();
                sb.append(rr.serialString());
                sb.append("< ");
                RadioConfig radioConfig = this.mRadioConfig;
                sb.append(RadioConfig.requestToString(rr.mRequest));
                sb.append(" ");
                sb.append(ret.toString());
                Rlog.d(TAG, sb.toString());
                return;
            }
            rr.onError(responseInfo.error, ret);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
            return;
        }
        Rlog.e(TAG, "getPhoneCapabilityResponse: Error " + responseInfo.toString());
    }

    @Override // android.hardware.radio.config.V1_1.IRadioConfigResponse
    public void setPreferredDataModemResponse(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr == null) {
            Rlog.e(TAG, "setPreferredDataModemResponse: Error " + responseInfo.toString());
        } else if (responseInfo.error == 0) {
            RadioResponse.sendMessageResponse(rr.mResult, null);
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("< ");
            RadioConfig radioConfig = this.mRadioConfig;
            sb.append(RadioConfig.requestToString(rr.mRequest));
            Rlog.d(TAG, sb.toString());
        } else {
            rr.onError(responseInfo.error, null);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
        }
    }

    @Override // android.hardware.radio.config.V1_1.IRadioConfigResponse
    public void setModemsConfigResponse(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr == null) {
            Rlog.e(TAG, "setModemsConfigResponse: Error " + responseInfo.toString());
        } else if (responseInfo.error == 0) {
            RadioResponse.sendMessageResponse(rr.mResult, Integer.valueOf(rr.mRequest));
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("< ");
            RadioConfig radioConfig = this.mRadioConfig;
            sb.append(RadioConfig.requestToString(rr.mRequest));
            Rlog.d(TAG, sb.toString());
        } else {
            rr.onError(responseInfo.error, null);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
        }
    }

    @Override // android.hardware.radio.config.V1_1.IRadioConfigResponse
    public void getModemsConfigResponse(RadioResponseInfo responseInfo, ModemsConfig modemsConfig) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr == null) {
            Rlog.e(TAG, "getModemsConfigResponse: Error " + responseInfo.toString());
        } else if (responseInfo.error == 0) {
            RadioResponse.sendMessageResponse(rr.mResult, modemsConfig);
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("< ");
            RadioConfig radioConfig = this.mRadioConfig;
            sb.append(RadioConfig.requestToString(rr.mRequest));
            Rlog.d(TAG, sb.toString());
        } else {
            rr.onError(responseInfo.error, modemsConfig);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
        }
    }
}
