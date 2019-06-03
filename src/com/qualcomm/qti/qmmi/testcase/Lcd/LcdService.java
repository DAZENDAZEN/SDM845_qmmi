/*
 * Copyright (c) 2017 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.qualcomm.qti.qmmi.testcase.Lcd;

import com.qualcomm.qti.qmmi.bean.TestCase;
import com.qualcomm.qti.qmmi.framework.BaseService;
import com.qualcomm.qti.qmmi.utils.LogUtils;
import com.qualcomm.qti.qmmi.testcase.Light.ShellUtils;

public class LcdService extends BaseService {

    @Override
    public void register() {

    }

    @Override
    public int stop(TestCase testCase) {
/*
		String[] commands1	= new String[]{"rm -f /sdcard/DCIM/Camera/*"};
		ShellUtils.CommandResult tofrm = ShellUtils.execCommand(commands1, false);
		LogUtils.logi("tof RM raw");
*/
		return 0;
    }
    @Override
    public int run(TestCase testCase) {
        LogUtils.logi( "lcdService run");
        return 0;
    }

}
