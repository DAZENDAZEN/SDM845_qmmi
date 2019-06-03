/*
 * Copyright (c) 2017 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.qualcomm.qti.qmmi.testcase.Light;


import android.content.Intent;
import android.hardware.light.V2_0.Brightness;
import android.hardware.light.V2_0.Flash;
import android.hardware.light.V2_0.ILight;
import android.hardware.light.V2_0.LightState;
import android.hardware.light.V2_0.Type;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.content.pm.PackageManager;

import android.os.Handler;


import com.qualcomm.qti.qmmi.R;
import com.qualcomm.qti.qmmi.bean.TestCase;
import com.qualcomm.qti.qmmi.framework.BaseService;
import com.qualcomm.qti.qmmi.model.HidlManager;
import com.qualcomm.qti.qmmi.utils.LogUtils;
import com.qualcomm.qti.qmmi.testcase.Light.ShellUtils;


import com.qualcomm.qti.qmmi.framework.BaseActivity;



public class LightService extends BaseService {
	public static int count=0;
    private ILight iLight = null;
    private HidlManager hidlManager = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.logi("onStartCommand");
        try {
            iLight = ILight.getService();
        } catch (Exception e) {
            LogUtils.loge(e.toString());
        } catch (Throwable t) {
            //catch NoClassDefFoundError if HIDL not exist
            LogUtils.loge("NoClassDefFoundError occur!");
            t.printStackTrace();
        }

        hidlManager = HidlManager.getInstance();
        if (hidlManager == null) {
            LogUtils.loge("No hidl manager found");
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void register() {

    }

    @Override
    public int stop(TestCase testCase) {
        int type = Integer.valueOf(testCase.getParameter().get("type"));
		LogUtils.loge("Light stop");
		LogUtils.logi("Light stop type" + Type.toString(type));
		//fan off
		String[] cmd = new String[]{"echo 0 > sys/g2_control/fan0_enable","echo 0 > sys/g2_control/fan1_enable"};
		ShellUtils.execCommand(cmd, false);
		//just for FP
		if(count <= 3){
			count++;
		}else{ count = 0; }
		LogUtils.loge("LightService service stop count:" + count);

		try {
            if (type != Type.BACKLIGHT)
                setLight(testCase, false); //false
        } catch (Throwable t) {
            //catch NoClassDefFoundError if HIDL not exist
            LogUtils.logi( "No HAL interface, NoClassDefFoundError occur!");
        }

        //disable charger before test light "rgb"
        if (type == Type.NOTIFICATIONS) {
            if (hidlManager != null) {
                hidlManager.chargerEnable(true);
            }
        }
        return 0;
    }

    @Override
    public int run(TestCase testCase) {

        int type = Integer.valueOf(testCase.getParameter().get("type"));
		LogUtils.logi("Light run type" + Type.toString(type));
        //disable charger before test light "rgb"
        if (type == Type.NOTIFICATIONS) {
            if (hidlManager != null) {
                hidlManager.chargerEnable(false);
            }
        }
		LogUtils.loge("Light run");
        setLight(testCase, true); //true
        if (iLight != null) {
			if(type == Type.BACKLIGHT){
            updateView(testCase.getName(), this.getResources().getString(R.string.finger_test));
			}else {
			updateView(testCase.getName(), this.getResources().getString(R.string.light_on));
			}
        } else {
            updateView(testCase.getName(), this.getResources().getString(R.string.light_hidl_miss));
        }
        return 0;
    }



    private void setLight(TestCase testCase, boolean on) {
		
        int type = Integer.valueOf(testCase.getParameter().get("type"));
        String color = testCase.getParameter().get("color");
        try {
            LogUtils.logi("LightService service run for:" + Type.toString(type));
			LogUtils.loge("LightService service run for:" + color);
            LightState state = new LightState();
            state.flashMode = Flash.NONE;
            state.flashOnMs = 0;
            state.flashOffMs = 0;
            state.brightnessMode = Brightness.USER;

            if (on) {
                if (type == Type.BUTTONS) {   //Button light test
                //  state.color = 0xFF020202;
                } else if (type == Type.NOTIFICATIONS) {  // LED light test
              				  LogUtils.logi( "NOTIFICATIONS in!");
               		     if ("led".equalsIgnoreCase(color)){
               		        // state.color = 0xFF0000;

							
							new Handler().postDelayed(new Runnable() {
							    @Override
							    public void run() {
							    	LogUtils.logi( "RED in!");
               		     			ShellUtils.execCommand(new String[]{"echo 8 > /sys/class/i2c-adapter/i2c-0/0-0030/led_pattern"}, false);
              		       		 	LogUtils.logi( "RED out!");
							    }
							},500);
							//open fan
							String[] cmd = new String[]{"echo 100 > sys/g2_control/fan0_duty","echo 100 > sys/g2_control/fan0_period","echo 100 > sys/g2_control/fan1_duty","echo 100 > sys/g2_control/fan1_period","echo 1 > sys/g2_control/fan0_enable","echo 1 > sys/g2_control/fan1_enable"};
							ShellUtils.execCommand(cmd, false);
							
                  		  }
                    else if ("blue".equalsIgnoreCase(color))
                        //state.color = 0x0000FF;
                        ShellUtils.execCommand(new String[]{"echo 4 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
                    else if ("green".equalsIgnoreCase(color))
                        //state.color = 0x00FF00;
                        ShellUtils.execCommand(new String[]{"echo 6 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
					else if ("white".equalsIgnoreCase(color))
                        //state.color = 0x00FF00;
                        ShellUtils.execCommand(new String[]{"echo 8 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
                    else
                        LogUtils.loge(" LIGHT: Unknow LED color");
	
                } else if (type == Type.BACKLIGHT) {
				/************************finger print test********************************/
					if(count==0){
						LogUtils.logi( "finger test!");
						PackageManager packageManager = this.getPackageManager();
						String packageName = "com.goodix.gftest";
						Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
						
						if (launchIntentForPackage != null) {
							this.startActivity(launchIntentForPackage);
						}
						count++;
					}
					LogUtils.loge("LightService service run count:" + count);
                }

            } else {
            	
              //  state.color = 0x00000000;
			/*
			String[] commands1 = new String[]{"cat /sys/class/power_supply/battery/status"};
			ShellUtils.CommandResult batStatus = ShellUtils.execCommand(commands1, false);
			String[] commands2 = new String[]{"cat /sys/class/power_supply/battery/capacity"};
			ShellUtils.CommandResult batLevel = ShellUtils.execCommand(commands2, false);
		
		if (batStatus.result == 0 && batLevel.result == 0 && "Charging".equals(batStatus.successMsg)) {
			if("100".equals(batLevel.successMsg)){
			 ShellUtils.execCommand(new String[]{"echo 8 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			}else if(Integer.parseInt(batLevel.successMsg)<=99  && Integer.parseInt(batLevel.successMsg) >= 75)
		     {
			 ShellUtils.execCommand(new String[]{"echo 7 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			}else if(Integer.parseInt(batLevel.successMsg) < 75 && Integer.parseInt(batLevel.successMsg) >= 50)
		    {
			 ShellUtils.execCommand(new String[]{"echo 5 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			 }else if(Integer.parseInt(batLevel.successMsg) < 50 && Integer.parseInt(batLevel.successMsg) >= 25)
			 {
			 ShellUtils.execCommand(new String[]{"echo 3 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			 }else if(Integer.parseInt(batLevel.successMsg) < 25)
			 {
			 ShellUtils.execCommand(new String[]{"echo 1 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			 }

		}
		else if(batStatus.result == 0 && batLevel.result == 0){
			 if(Integer.parseInt(batLevel.successMsg) <= 99 && Integer.parseInt(batLevel.successMsg) >= 75){
			  ShellUtils.execCommand(new String[]{"echo 8 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
		     }else if(Integer.parseInt(batLevel.successMsg) < 75 && Integer.parseInt(batLevel.successMsg) >= 50)
			  {
			  ShellUtils.execCommand(new String[]{"echo 6 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			  }else if(Integer.parseInt(batLevel.successMsg) < 50 && Integer.parseInt(batLevel.successMsg) >= 25)
			  {
			  ShellUtils.execCommand(new String[]{"echo 4 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			  }
			  else if(Integer.parseInt(batLevel.successMsg) < 25 && Integer.parseInt(batLevel.successMsg) >= 5)
			  {
			  ShellUtils.execCommand(new String[]{"echo 2 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			  }else if(Integer.parseInt(batLevel.successMsg) < 5)
			  {
			  ShellUtils.execCommand(new String[]{"echo 0 > /sys/class/i2c-adapter/i2c-2/2-0030/led_pattern"}, false);
			  }
	    }*/



			  
            }


            if (iLight != null) {
                LogUtils.logi("LightService set light on for:");
                iLight.setLight(type, state);
            }
        } catch (Exception e) {
            LogUtils.loge("Exception in light service" + e.toString());
        } catch (Throwable t) {
            //catch NoClassDefFoundError if HIDL not exist
            LogUtils.loge("NoClassDefFoundError occur!");
        }
    }
}
