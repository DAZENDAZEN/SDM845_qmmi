/*
 * Copyright (c) 2017 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.qualcomm.qti.qmmi.testcase.Sim;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.reflect.Method;

import com.qualcomm.qti.qmmi.R;
import com.qualcomm.qti.qmmi.bean.TestCase;
import com.qualcomm.qti.qmmi.framework.BaseService;
import com.qualcomm.qti.qmmi.utils.LogUtils;

public class SimService extends BaseService {
	static final String TAG = "ZD_qmmi_simservice";
	Context mContext;

    TelephonyManager mTelephonyManager = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.logi( "onStartCommand");
        mTelephonyManager = TelephonyManager.from(getApplicationContext());
	mContext = getApplicationContext();
        if (mTelephonyManager == null) {
            LogUtils.loge( "No mWifiManager service here");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void register() {

    }

    @Override
    public int stop(TestCase testCase) {
        return 0;
    }
    public boolean setMobileNetworkEnable(boolean enable) {

	//sim1
        boolean success0 = false;

        try {
            int subid0 = SubscriptionManager.from(mContext).getActiveSubscriptionInfoForSimSlotIndex(0).getSubscriptionId();
            TelephonyManager telephonyService = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            Method setDataEnabled = telephonyService.getClass().getDeclaredMethod("setDataEnabled", int.class, boolean.class);
            if (null != setDataEnabled) {
                setDataEnabled.invoke(telephonyService, subid0, enable);
                success0 = true;
                Log.e(TAG, "ZD setMobileNetworkEnable success, slotIndex = 0, subid = " + subid0 + ", enable = " + enable);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ZD setMobileNetworkEnable failure, slotIndex = 0");
        }
		
	//sim2
        boolean success1 = false;

        try {
	            int subid1 = SubscriptionManager.from(mContext).getActiveSubscriptionInfoForSimSlotIndex(1).getSubscriptionId();
	            TelephonyManager telephonyService = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	            Method setDataEnabled = telephonyService.getClass().getDeclaredMethod("setDataEnabled", int.class, boolean.class);
	            if (null != setDataEnabled) {
	                setDataEnabled.invoke(telephonyService, subid1, enable);
	                success1 = true;
	                Log.e(TAG, "ZD setMobileNetworkEnable success, slotIndex = 1, subid = " + subid1 + ", enable = " + enable);
	            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ZD setMobileNetworkEnable failure, slotIndex = 1");
        }
        return success0 || success1;
    }

    @Override
    public int run(TestCase testCase) {

        int sub = Integer.valueOf(testCase.getParameter().get("sub"));
        StringBuffer sb = new StringBuffer();
        String iccId = null;
        sb.append(this.getResources().getString(R.string.sim_list)).append("\n");

        try {
            if (mTelephonyManager.isMultiSimEnabled()) {
                int[] subId = SubscriptionManager.getSubId(sub);
                iccId = mTelephonyManager.getSimSerialNumber(subId[0]);
            } else {
                iccId = mTelephonyManager.getSimSerialNumber();
            }
            LogUtils.logi("iccId:" + iccId);
            if (iccId != null && !iccId.equals("")) {

                String simSlot = this.getResources().getString(R.string.sim_slot);

                sb.append(String.format(simSlot, sub+1))
                    .append(": ")
                    .append(this.getResources().getString(R.string.sim_deteched))
                    .append("\n");
                testCase.addTestData("SIM" + sub, "deteched");
		setMobileNetworkEnable(true);
                updateResultForCase(testCase.getName(), TestCase.STATE_PASS);
            }else{

                String simSlot = this.getResources().getString(R.string.sim_slot);

                sb.append(String.format(simSlot, sub+1))
                .append(": ")
                .append(this.getResources().getString(R.string.sim_not_deteched))
                .append("\n");
                testCase.addTestData("SIM" + sub, "not deteched");
                updateResultForCase(testCase.getName(), TestCase.STATE_FAIL);
            }
        }catch (SecurityException e){
            LogUtils.logi( "getSimSerialNumber error:" + e);
        }

        updateView(testCase.getName(), sb.toString());
        LogUtils.logi( "simservice run");
        return 0;
    }

}
