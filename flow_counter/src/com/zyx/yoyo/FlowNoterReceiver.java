package com.zyx.yoyo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zyx.apkflow.DataUpdater;
import com.zyx.apkflow.InstallApk;

public class FlowNoterReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		DataUpdater du = new DataUpdater(context);
		InstallApk ia = new InstallApk(context);
		String action = intent.getAction();
		if(Intent.ACTION_DATE_CHANGED.equals(action)){//ACTION_TIME_CHANGED
			System.out.println("-------日期改变啦-------");
			du.handleDataWhenDateChanged(ia.getApk(2));
		}else if(Intent.ACTION_SHUTDOWN.equals(action) || Intent.ACTION_REBOOT.equals(action)){
			System.out.println("-------重启啦-------");
			du.handleDataWhenShutDown(ia.getApk(2));
		}
		du.closeDatabase();
	}

}
