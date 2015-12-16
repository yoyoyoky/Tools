package com.zyx.apkflow;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class InstallApk {

	private PackageManager pm;

	private ArrayList<AppInfo> allList = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> systemList = new ArrayList<AppInfo>();
	private ArrayList<AppInfo> thirdList = new ArrayList<AppInfo>();

	private List<PackageInfo> packages = new ArrayList<PackageInfo>();

	public InstallApk(Context context) {
		// TODO Auto-generated constructor stub

		this.pm = context.getPackageManager();

		//因为有些apk可能被删掉 但是数据还在 所以用GET_UNINSTALLED_PACKAGES 这个flag，而不用0
		this.packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		
		getApk();
	}

	public ArrayList<AppInfo> getApk(int type) {
		switch (type) {
		case 0:
			return systemList;

		case 1:
			return thirdList;

		default:
			return allList;
		}
	}

	private void getApk() {
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfo ai = new AppInfo();
			ai.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
			ai.packageName = packageInfo.packageName;
			ai.versionName = packageInfo.versionName;
			ai.versionCode = packageInfo.versionCode;
			ai.appIcon = packageInfo.applicationInfo.loadIcon(pm);
			ai.toHide = packageInfo.applicationInfo.enabled;
			if (ai.packageName.equals("com.zyx.yoyo"))
				continue;
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				ai.type = 1;
				thirdList.add(ai);
			} else {
				ai.type = 0;
				systemList.add(ai);
			}
			allList.add(ai);
		}
	}
	
	public AppInfo getApkInfo(String packageName){
		AppInfo apk = null;
		for(AppInfo a: allList){
			if(a.packageName.equals(packageName)){
				apk = a;
				break;
			}
		}
		return apk;
	}

}
