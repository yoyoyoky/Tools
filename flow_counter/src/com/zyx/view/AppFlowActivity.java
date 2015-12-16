package com.zyx.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zyx.apkflow.AppInfo;
import com.zyx.apkflow.DataUpdater;
import com.zyx.apkflow.FlowInfo;
import com.zyx.apkflow.InstallApk;
import com.zyx.yoyo.R;

public class AppFlowActivity extends Activity {

	ImageView app_icon;
	TextView app_name;
	TextView total_flows;
	TextView used_days;
	TextView no_flow;
	ListView flowListView;

	Context mContext;

	DataUpdater du;
	InstallApk ia;

	List<Map<String, Object>> mData;
	List<String> packagesList;

	String PACKAGENAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_flow);
		
		PACKAGENAME = getIntent().getStringExtra("packageName");

		mContext = getApplication();

		du = new DataUpdater(mContext);
		ia = new InstallApk(mContext);

		initView();
	}

	private void initView() {
		app_icon = (ImageView) findViewById(R.id.app_icon);
		app_name = (TextView) findViewById(R.id.app_name);
		total_flows = (TextView) findViewById(R.id.total_flows);
		used_days = (TextView) findViewById(R.id.used_days);
		no_flow = (TextView) findViewById(R.id.no_flow);
		flowListView = (ListView) findViewById(R.id.everyday_flow);

		setAdapter();
	}

	private void setAdapter() {
		// TODO Auto-generated method stub
		mData = getData();
		flowListView.setAdapter(new MyAdapter(mContext));

	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.flow_item, null);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
				holder.flow = (TextView) convertView.findViewById(R.id.flow);
				holder.percent = (TextView) convertView.findViewById(R.id.percent);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.date.setText((CharSequence) mData.get(position).get("date"));
			holder.progress.setProgress((Integer) mData.get(position).get("progress"));
			holder.flow.setText((CharSequence) mData.get(position).get("flow"));
			holder.percent.setText((CharSequence) mData.get(position).get("percent"));
			return convertView;
		}
	}

	public final class ViewHolder {
		public TextView date;
		public ProgressBar progress;
		public TextView flow;
		public TextView percent;
	}

	@SuppressLint("UseValueOf")
	private List<Map<String, Object>> getData() {

		List<Map<String, Object>> data = du.queryData(PACKAGENAME);

		int days = data.size() - 1;
		if(days==0){
			no_flow.setVisibility(View.VISIBLE);
		}else{
			no_flow.setVisibility(View.GONE);
		}

		long total = (Long) data.get(days).get("total");
		long[] mkb = FlowInfo.kb2mb(total);

		AppInfo a = ia.getApkInfo(PACKAGENAME);
		app_icon.setBackground(a.appIcon);
		app_name.setText(a.appName);
		used_days.setText(Html.fromHtml("已使用：<font color=\"#FF6A6A\">" + days + "</font> 天"));
		total_flows.setText(Html.fromHtml("共：<font color=\"#FFA500\">" + (mkb[0]==0?"":mkb[0] + "</font> M <font color=\"#FFA500\">") + mkb[1] + "</font> K"));

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		for (int i = 0; i < days; i++) {

			long r = (Long) data.get(i).get("receive");
			long s = (Long) data.get(i).get("send");
			long[] flow = FlowInfo.kb2mb(r + s);
			int p = new Double((r + s) * 100.0 / total * 1.0).intValue();
			DecimalFormat df = new DecimalFormat("0.00%");

			map = new HashMap<String, Object>();
			map.put("date", data.get(i).get("date"));
			map.put("progress", p == 0 ? 1 : p);
			map.put("flow", (flow[0]==0?"":flow[0] + " M ") + flow[1] + " K");
			map.put("percent", df.format((r + s) * 1.0 / total * 1.0));
			list.add(map);
		}
		return list;
	}

}
