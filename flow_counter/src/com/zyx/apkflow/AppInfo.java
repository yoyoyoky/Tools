package com.zyx.apkflow;

import android.graphics.drawable.Drawable;

public class AppInfo {
	
	public String appName = "";//应用名
	public String packageName = "";//包名
	public String versionName = "";//应用版本名
	public int versionCode = 0;//应用版本号
	public int type = 0;//0为系统应用，1为第三方应用
	public long flow = 0;//开机后所消耗的流量
	public Drawable appIcon = null;//应用图标
	public boolean toHide = true;//应用是否可隐藏

}
