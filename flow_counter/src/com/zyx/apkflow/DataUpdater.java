package com.zyx.apkflow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zyx.utils.DataHelper;

@SuppressLint("SimpleDateFormat")
public class DataUpdater {

	private static SQLiteDatabase db;
	private static DataHelper helper;
	private static String FLOW_TABLE = "flow_table";
	private static FlowInfo mFlowInfo;
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public DataUpdater(Context context) {

		mFlowInfo = new FlowInfo(context);
		
		// ����DataHelper����
		helper = new DataHelper(context);

		//��һ�ε���ʱ�������DataHelper�е�onCreate����
		db = helper.getReadableDatabase();
		
	}

	/**�����ݿ��������*/
	public void insertData(ContentValues cv) {
		db.insert(FLOW_TABLE, null, cv);
	}

	/**ɾ�����ݿ�����*/
	public void deleteData() {
		String whereClause = "receive=? and send=?";
		String[] whereArgs = new String[] { "0", "0" };
		db.delete(FLOW_TABLE, whereClause, whereArgs);
	}

	/** �����������Ĺ�������app�б����һ��ֵΪ����app��������������ֵ */
	public List<AppInfo> getFlow(String startDate, String endDate, List<AppInfo> apps) {
		List<AppInfo> flows = new ArrayList<AppInfo>();// �������Ĺ�������Ӧ���б�
		AppInfo total = new AppInfo();
		total.flow = 0;// ����Ӧ������������
		for (AppInfo a : apps) {
			a.flow = queryData(startDate, endDate, a.packageName);
			total.flow += a.flow;
			if (flows.size() == 0) {// ���б�Ϊ��ʱ��ֱ�Ӳ���
				flows.add(a);
			} else {// ������룬��a���ĵ��������ڻ����i���ĵ���������a�嵽iǰ��
				for (int i = 0; i < flows.size(); i++) {
					if (a.flow >= flows.get(i).flow) {
						flows.add(i, a);
						break;
					}
					if (i == flows.size() - 1) {// ���Ҳ�����aС��ֵʱ��ֱ�Ӳ��������
						flows.add(a);
						break;
					}
				}
			}
		}
		flows.add(total);
		return flows;
	}

	/**��ѯָ��ʱ��ε�apk�����������*/
	public long queryData(String startDate, String endDate, String packageName) {
		String[] columns = new String[] { "receive", "send" };
		String selection = "package=? and date >=? and date<=?";
		String[] selectionArgs = new String[] { packageName, startDate, endDate };
		Cursor cursor = db.query(FLOW_TABLE, columns, selection, selectionArgs, null, null, null);

		long total = 0;
		while (cursor.moveToNext()) {

			long receive = cursor.getLong(cursor.getColumnIndex("receive"));
			long send = cursor.getLong(cursor.getColumnIndex("send"));

			total += receive + send;

		}
		if (endDate.compareTo(df.format(new Date())) >= 0) {
			long[] flow = mFlowInfo.getFlow(packageName);
			total += flow[0] + flow[1];
		}
		return total;
	}

	/**����ָ����������ÿ��������������*/
	public List<Map<String, Object>> queryData(String packageName) {
		String[] columns = new String[] { "date", "receive", "send" };
		String selection = "package=?";
		String[] selectionArgs = new String[] { packageName };
		Cursor cursor = db.query(FLOW_TABLE, columns, selection, selectionArgs, null, null, null);

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		long total = 0;
		boolean includeToday = false;
		
		while (cursor.moveToNext()) {
			String date = cursor.getString(cursor.getColumnIndex("date"));
			long receive = cursor.getLong(cursor.getColumnIndex("receive"));
			long send = cursor.getLong(cursor.getColumnIndex("send"));
			if (date.compareTo(df.format(new Date())) == 0) {//�������ݿ������н��������
				long[] flow = mFlowInfo.getFlow(packageName);
				receive += flow[0];
				send += flow[1];
				includeToday = true;
			}
			if (receive >= 0 && send >= 0 && !(receive==0 && send ==0)) {//ͬʱΪ0����������ڷ����仯����δ���������ֻ������ݿ��е�����Ϊ�����������ϱ��ο����������ģ�ǡ�õ���Ϊ0

				total += receive + send;

				map = new HashMap<String, Object>();
				map.put("date", date);
				map.put("receive", receive);
				map.put("send", send);

				data.add(map);
			}
		}
		if(!includeToday){
			long[] flow = mFlowInfo.getFlow(packageName);
			long receive = flow[0];
			long send = flow[1];
			
			if(receive > 0 || send > 0){//Ԥ������ ĳһ���м�¼��Ϊ0�����
				
				total += receive + send;
				
				map = new HashMap<String, Object>();
				map.put("date", df.format(new Date()));
				map.put("receive", receive);
				map.put("send", send);
				
				data.add(map);
			}
		}
		
		// ���һ��ֵΪ������
		map = new HashMap<String, Object>();
		map.put("total", total);
		data.add(map);

		return data;
	}

	/**��ȡ���ݿ����м�¼�����а���*/
	public List<String> getPackages() {

		List<String> packagesList = new ArrayList<String>();
		Set<String> packagesSet = new HashSet<String>();
		Cursor cursor = db.query(FLOW_TABLE, new String[] { "package" }, null, null, null, null, null);
		while (cursor.moveToNext())
			packagesSet.add(cursor.getString(cursor.getColumnIndex("package")));

		for (String str : packagesSet)
			packagesList.add(str);

		return packagesList;
	}

	/**���ݰ��������ڣ���ѯ���ݿ����ݣ��������ڣ��򷵻�null*/
	public long[] queryData(String packageName, String date) {

		String[] columns = new String[] { "receive", "send" };
		String selection = "package=? and date=?";
		String[] selectionArgs = new String[] { packageName, date };
		Cursor cursor = db.query(FLOW_TABLE, columns, selection, selectionArgs, null, null, null);

		boolean flag = cursor.moveToLast();
		if (flag) {
			long r = cursor.getLong(cursor.getColumnIndex("receive"));
			long s = cursor.getLong(cursor.getColumnIndex("send"));
			System.out.println("query------->" + packageName + "���գ�" + r + " " + "���ͣ�" + s);
			return new long[] { r, s };
		} else {
			return null;
		}
	}
	
	/**���ݰ��������ڣ��������ݿ�����*/
	public void updateData(String packageName, ContentValues cv, String date) {
		String whereClause = "package=? and date=?";
		String[] whereArgs = new String[] { packageName, date };
		db.update(FLOW_TABLE, cv, whereClause, whereArgs);
	}
	
	/**�����������ݿ�����*/
	public void handleData(List<AppInfo> apps,Date date){
		String strDate = df.format(date);
		for (AppInfo a : apps) {
			handleData(a.packageName, strDate);
		}
	}

	/**�����ݿ����Ѵ���ָ�����������ڵ����ݣ���������ݿ����ݣ����򽫸����ݲ��룻ͬʱ����ʱ��ı�ʱ����ʹ�õ����ڻ���*/
	public void handleData(String packageName, String date) {

		long[] newFlow = mFlowInfo.getFlow(packageName);

		if (newFlow[0] != 0 || newFlow[1] != 0) {

			long[] oldFlow = queryData(packageName, date);

			boolean flag = oldFlow == null;

			ContentValues cv = new ContentValues();

			if (flag) {//���ݿ�����������
				cv.put("package", packageName);
				cv.put("date", date);// ������������
				cv.put("receive", newFlow[0]);
				cv.put("send", newFlow[1]);
				insertData(cv);
			} else {
				cv.put("receive", oldFlow[0] + newFlow[0]);
				cv.put("send", oldFlow[1] + newFlow[1]);
				updateData(packageName, cv, date);
			}

			deleteData();// ɾ�����õĿ�����

			String currentDate = df.format(new Date());
			if (!date.equals(currentDate)) {// ʱ��ı䣬��δ�ػ����´θ�������ʱ�����ȥ���ο���ʱǰһ�����ķѵ�����
				cv.put("package", packageName);
				cv.put("date", currentDate);// ������������
				cv.put("receive", -newFlow[0]);
				cv.put("send", -newFlow[1]);
				insertData(cv);
			}
		}
	}
	
	/**�ж����ݿ����Ƿ����*/
	public boolean tabIsExist(String tabName) {
		boolean result = false;
		if (tabName == null) {
			return false;
		}
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "'";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	/**�ر����ݿ�*/
	public void closeDatabase() {
		db.close();
	}

}
