package com.zyx.apkflow;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;

public class FlowInfo {

	Context mContext;
	PackageManager pm;
	ActivityManager am;
	InstallApk ia;
	List<RunningAppProcessInfo> mRunningProcess;
	ArrayList<AppInfo> ai;
	DecimalFormat df;

	public FlowInfo(Context mContext) {
		// TODO Auto-generated constructor stub
		this.mContext = mContext;
		pm = mContext.getPackageManager();
		am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		ia = new InstallApk(mContext);
	}
	
	/**获取指定包名的apk的流量使用情况，包括接收和发送数据，返回long[],单位为kb*/
	public long[] getFlow(String packageName) {
		ApplicationInfo ai = null;
		try {
			ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long inTraffic = 0;
		long outTraffic = 0;
		if (ai != null) {
			inTraffic = TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getUidRxBytes(ai.uid) / 1024;
			outTraffic = TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getUidTxBytes(ai.uid) / 1024;
		}

		return new long[] { inTraffic, outTraffic };
	}

	/**获取指定包名的apk的流量消耗总量，单位为kb*/
	public long getTotalFlow(String packageName) {
		long[] flow = getFlow(packageName);
		return flow[0] + flow[1];
	}

	/**获取指定包名的apk的流量使用情况，返回String类型*/
	public String getFlowStr(String packageName) {
		long[] flow = getFlow(packageName);
		if (flow[0] == 0 && flow[1] == 0)
			return "此应用未消耗流量！";
		else {
			long[] in = kb2mb(flow[0]);
			long[] out = kb2mb(flow[1]);
			return "已接收：" + (in[0] == 0 ? "" : in[0] + "M") + in[1] + "K\n已发送：" + (out[0] == 0 ? "" : out[0] + "M") + out[1] + "K";
		}
	}

	/**获取指定类型（0为系统应用，1为第三方应用）流量消耗总量*/
	public long getTotalFlow(int type) {
		ai = ia.getApk(type);
		long totalFlow = 0;
		for (AppInfo a : ai) {
			totalFlow += getTotalFlow(a.packageName);
		}
		return totalFlow;
	}

	/**获取指定包名的apk在指定类型（0为系统应用，1为第三方应用）流量消耗比例*/
	public String getFlowPer(String packageName, int type) {
		df = new DecimalFormat("0.00%");
		long totalFlow = getTotalFlow(type);
		long currentFlow = getTotalFlow(packageName);
		return df.format(currentFlow / totalFlow);
	}

	/**获取总接受+发送的字节数*/
	public long getTotalBytes() {
		return getTotalRxBytes() + getTotalTxBytes();
	}

	/**获取总的接受字节数，包含Mobile和WiFi等*/
	public long getTotalRxBytes() {
		return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
	}

	/**总的发送字节数，包含Mobile和WiFi等*/
	public long getTotalTxBytes() {
		return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalTxBytes() / 1024);
	}

	/**获取通过Mobile连接收到的字节总数，不包含WiFi*/
	public long getMobileRxBytes() {
		return TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getMobileRxBytes() / 1024);
	}

	public static long[] kb2mb(long kb) {
		long mb = kb / 1024;
		long rkb = kb - 1024 * mb;
		return new long[] { mb, rkb };
	}

}
