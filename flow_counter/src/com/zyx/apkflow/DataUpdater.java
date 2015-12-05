package com.zyx.apkflow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.util.Log;

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
		// 创建DataHelper对象
		helper = new DataHelper(context);

		db = helper.getReadableDatabase();

		createTable();

	}

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

	private void createTable() {
		if (!tabIsExist(FLOW_TABLE)) {
			// 创建表SQL语句
			String stu_table = "create table " + FLOW_TABLE + "(_id integer primary key autoincrement,date text,package text,receive long,send log)";

			// 执行SQL语句
			db.execSQL(stu_table);
		}
	}

	public void insertData(ContentValues cv) {
		db.insert(FLOW_TABLE, null, cv);
	}

	public void deleteData() {
		String whereClause = "receive=? and send=?";
		String[] whereArgs = new String[] { "0", "0" };
		db.delete(FLOW_TABLE, whereClause, whereArgs);
	}

	/** 返回所有消耗过流量的app列表，最后一个值为所有app所消耗流量的总值 */
	public List<AppInfo> getFlow(String startDate, String endDate, List<AppInfo> apps) {
		List<AppInfo> flows = new ArrayList<AppInfo>();// 所有消耗过流量的应用列表
		AppInfo total = new AppInfo();
		total.flow = 0;// 所有应用总消耗流量
		for (AppInfo a : apps) {
			a.flow = queryData(startDate, endDate, a.packageName);
			total.flow += a.flow;
			if (flows.size() == 0) {// 当列表为空时，直接插入
				flows.add(a);
			} else {// 倒序插入，当a消耗的流量大于或等于i消耗的流量，将a插到i前面
				for (int i = 0; i < flows.size(); i++) {
					if (a.flow >= flows.get(i).flow) {
						flows.add(i, a);
						break;
					}
					if (i == flows.size() - 1) {// 当找不到比a小的值时，直接插在最后面
						flows.add(a);
						break;
					}
				}
			}
		}
		flows.add(total);
		return flows;
	}

	/**查询指定时间段的apk流量消耗情况*/
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

	/**返回指定包名至今每天的数据消耗情况*/
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
			if (date.compareTo(df.format(new Date())) == 0) {//假如数据库中已有今天的数据
				long[] flow = mFlowInfo.getFlow(packageName);
				receive += flow[0];
				send += flow[1];
				includeToday = true;
			}
			if (receive >= 0 && send >= 0 && !(receive==0 && send ==0)) {//同时为0的情况：日期发生变化，但未重新启动手机，数据库中的数据为负数，而加上本次开机流量消耗，恰好抵消为0

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
			
			if(receive > 0 || send > 0){//预防出现 某一天有记录且为0的情况
				
				total += receive + send;
				
				map = new HashMap<String, Object>();
				map.put("date", df.format(new Date()));
				map.put("receive", receive);
				map.put("send", send);
				
				data.add(map);
			}
		}
		
		// 最后一个值为总流量
		map = new HashMap<String, Object>();
		map.put("total", total);
		data.add(map);

		return data;
	}

	public List<String> getPackages() {

		List<String> packagesList = new ArrayList<String>();
		Set<String> packagesSet = new HashSet<String>();
		Cursor cursor = db.query(FLOW_TABLE, new String[] { "package" }, null, null, null, null, null);
		while (cursor.moveToNext())
			packagesSet.add(cursor.getString(cursor.getColumnIndex("package")));
		Log.e("===========", "========" + packagesSet.size());

		for (String str : packagesSet)
			packagesList.add(str);

		return packagesList;
	}

	public long[] queryData(String packageName, String date) {

		String[] columns = new String[] { "receive", "send" };
		String selection = "package=? and date=?";
		String[] selectionArgs = new String[] { packageName, date };
		Cursor cursor = db.query(FLOW_TABLE, columns, selection, selectionArgs, null, null, null);

		boolean flag = cursor.moveToLast();
		if (flag) {
			long r = cursor.getLong(cursor.getColumnIndex("receive"));
			long s = cursor.getLong(cursor.getColumnIndex("send"));
			System.out.println("query------->" + packageName + "接收：" + r + " " + "发送：" + s);
			return new long[] { r, s };
		} else {
			return null;
		}
	}

	public void updateData(String packageName, ContentValues cv, String date) {
		String whereClause = "package=? and date=?";
		String[] whereArgs = new String[] { packageName, date };
		db.update(FLOW_TABLE, cv, whereClause, whereArgs);
	}

	public void handleData(String packageName, String date) {

		long[] newFlow = mFlowInfo.getFlow(packageName);

		if (newFlow[0] != 0 || newFlow[1] != 0) {

			long[] oldFlow = queryData(packageName, date);

			boolean flag = oldFlow == null;

			ContentValues cv = new ContentValues();

			if (flag) {
				cv.put("package", packageName);
				cv.put("date", date);// 流量消耗日期
				cv.put("receive", newFlow[0]);
				cv.put("send", newFlow[1]);
				insertData(cv);
			} else {
				cv.put("receive", oldFlow[0] + newFlow[0]);
				cv.put("send", oldFlow[1] + newFlow[1]);
				updateData(packageName, cv, date);
			}

			deleteData();// 删除无用的空数据

			String currentDate = df.format(new Date());
			if (!date.equals(currentDate)) {// 时间改变，且未关机，下次更新数据时，需减去本次开机时前一天所耗费的流量
				cv.put("package", packageName);
				cv.put("date", currentDate);// 流量消耗日期
				cv.put("receive", -newFlow[0]);
				cv.put("send", -newFlow[1]);
				insertData(cv);
			}
		}
	}

	public void handleDataWhenDateChanged(List<AppInfo> apps) {
		String date = df.format(getLastDay(new Date()));
		for (AppInfo a : apps) {
			handleData(a.packageName, date);
		}
	}

	public void handleDataWhenShutDown(List<AppInfo> apps) {
		String date = df.format(new Date());
		for (AppInfo a : apps) {
			handleData(a.packageName, date);
		}
	}

	public void closeDatabase() {
		db.close();
	}

	public static Date getLastDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar.getTime();
	}

}
