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
	
	/**��ȡָ��������apk������ʹ��������������պͷ������ݣ�����long[],��λΪkb*/
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

	/**��ȡָ��������apk������������������λΪkb*/
	public long getTotalFlow(String packageName) {
		long[] flow = getFlow(packageName);
		return flow[0] + flow[1];
	}

	/**��ȡָ��������apk������ʹ�����������String����*/
	public String getFlowStr(String packageName) {
		long[] flow = getFlow(packageName);
		if (flow[0] == 0 && flow[1] == 0)
			return "��Ӧ��δ����������";
		else {
			long[] in = kb2mb(flow[0]);
			long[] out = kb2mb(flow[1]);
			return "�ѽ��գ�" + (in[0] == 0 ? "" : in[0] + "M") + in[1] + "K\n�ѷ��ͣ�" + (out[0] == 0 ? "" : out[0] + "M") + out[1] + "K";
		}
	}

	/**��ȡָ�����ͣ�0ΪϵͳӦ�ã�1Ϊ������Ӧ�ã�������������*/
	public long getTotalFlow(int type) {
		ai = ia.getApk(type);
		long totalFlow = 0;
		for (AppInfo a : ai) {
			totalFlow += getTotalFlow(a.packageName);
		}
		return totalFlow;
	}

	/**��ȡָ��������apk��ָ�����ͣ�0ΪϵͳӦ�ã�1Ϊ������Ӧ�ã��������ı���*/
	public String getFlowPer(String packageName, int type) {
		df = new DecimalFormat("0.00%");
		long totalFlow = getTotalFlow(type);
		long currentFlow = getTotalFlow(packageName);
		return df.format(currentFlow / totalFlow);
	}

	/**��ȡ�ܽ���+���͵��ֽ���*/
	public long getTotalBytes() {
		return getTotalRxBytes() + getTotalTxBytes();
	}

	/**��ȡ�ܵĽ����ֽ���������Mobile��WiFi��*/
	public long getTotalRxBytes() {
		return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
	}

	/**�ܵķ����ֽ���������Mobile��WiFi��*/
	public long getTotalTxBytes() {
		return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalTxBytes() / 1024);
	}

	/**��ȡͨ��Mobile�����յ����ֽ�������������WiFi*/
	public long getMobileRxBytes() {
		return TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getMobileRxBytes() / 1024);
	}

	public static long[] kb2mb(long kb) {
		long mb = kb / 1024;
		long rkb = kb - 1024 * mb;
		return new long[] { mb, rkb };
	}

}
