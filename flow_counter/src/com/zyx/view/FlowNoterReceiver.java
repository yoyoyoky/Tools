package com.zyx.view;

import java.util.Calendar;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.zyx.apkflow.DataUpdater;
import com.zyx.apkflow.InstallApk;

public class FlowNoterReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		DataUpdater du = new DataUpdater(context);
		InstallApk ia = new InstallApk(context);
		String action = intent.getAction();
		if(Intent.ACTION_DATE_CHANGED.equals(action)){//Intent.ACTION_TIME_CHANGED
			System.out.println("-------日期改变啦-------");
			Toast.makeText(context, "日期改变啦", Toast.LENGTH_SHORT).show();
			du.handleData(ia.getApk(2), getLastDay(new Date()));
		}else if(Intent.ACTION_SHUTDOWN.equals(action) || Intent.ACTION_REBOOT.equals(action)){
			System.out.println("-------重启啦-------");
			du.handleData(ia.getApk(2), new Date());
		}
		du.closeDatabase();
	}
	
	public static Date getLastDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar.getTime();
	}

}
