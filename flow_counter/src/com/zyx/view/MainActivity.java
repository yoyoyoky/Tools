package com.zyx.view;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zyx.apkflow.AppInfo;
import com.zyx.apkflow.DataUpdater;
import com.zyx.apkflow.FlowInfo;
import com.zyx.apkflow.InstallApk;
import com.zyx.utils.RootChecker;
import com.zyx.utils.ShellUtils;
import com.zyx.yoyo.R;

public class MainActivity extends Activity {

	private Context context;
	private ListView appList;
	private Button bSystem, bThirdPart, bAll;
	private LinearLayout dateLayout;
	private TextView startDate, endDate, no_flow;
	private ClearEditText mClearEditText;

	private List<Map<String, Object>> mData;
	private List<AppInfo> infoList;
	private InstallApk ia;
	private FlowInfo mFlowInfo;

	private MyAdapter adapter;

	private final int VERSION_PAGE = 0;
	private final int FLOW_PAGE = 1;
	private final int TOP_TEN = 2;
	private static int CURRENT_PAGE = 0;

	private final int SYSTEM_TYPE = 0;
	private final int THIRD_PART_TYPE = 1;
	private final int ALL_TYPE = 2;
	private static int CURRENT_TYPE = 0;
	private static int NUMS = 0;

	private final int MSG_SEARCH = 3;

	private static long TOTAL_FLOW = 0;

	private static int COLOR_BLUE = Color.rgb(92, 172, 238);
	private static int COLOR_ORANGE = Color.rgb(255, 165, 0);

	private DataUpdater du;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		context = getApplicationContext();
		ia = new InstallApk(context);
		mFlowInfo = new FlowInfo(context);
		du = new DataUpdater(context);
	}

	private void initView() {

		appList = (ListView) findViewById(R.id.applist);
		bSystem = (Button) findViewById(R.id.system);
		bThirdPart = (Button) findViewById(R.id.thirdpart);
		bAll = (Button) findViewById(R.id.allapp);
		dateLayout = (LinearLayout) findViewById(R.id.datepicker);
		startDate = (TextView) findViewById(R.id.startdate);
		endDate = (TextView) findViewById(R.id.enddate);
		no_flow = (TextView) findViewById(R.id.no_flow);
		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

		adapter = new MyAdapter(this);

		sendMessage(0);

		appList.setOnItemClickListener(new OnItemClickListenerImpl());

		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				mData = getData(s.toString());
				sendMessage(3);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.system:
			CURRENT_TYPE = SYSTEM_TYPE;
			break;
		case R.id.thirdpart:
			CURRENT_TYPE = THIRD_PART_TYPE;
			break;
		case R.id.allapp:
			CURRENT_TYPE = ALL_TYPE;
			break;
		default:
			break;
		}
		sendMessage(0);
	}

	private void setTabColor() {
		switch (CURRENT_TYPE) {
		case SYSTEM_TYPE:
			bSystem.setTextColor(Color.BLUE);
			bThirdPart.setTextColor(Color.BLACK);
			bAll.setTextColor(Color.BLACK);
			break;
		case THIRD_PART_TYPE:
			bSystem.setTextColor(Color.BLACK);
			bThirdPart.setTextColor(Color.BLUE);
			bAll.setTextColor(Color.BLACK);
			break;
		case ALL_TYPE:
			bSystem.setTextColor(Color.BLACK);
			bThirdPart.setTextColor(Color.BLACK);
			bAll.setTextColor(Color.BLUE);
			break;

		default:
			break;
		}
		infoList = ia.getApk(CURRENT_TYPE);
		TOTAL_FLOW = mFlowInfo.getTotalFlow(CURRENT_TYPE);
	}

	@SuppressLint("UseValueOf")
	private List<Map<String, Object>> getData(String filter) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		for (int i = 0; i < NUMS; i++) {
			no_flow.setVisibility(View.GONE);
			if (CURRENT_PAGE != TOP_TEN && !infoList.get(i).appName.contains(filter))
				continue;
			if (CURRENT_PAGE == FLOW_PAGE && mFlowInfo.getFlowStr(infoList.get(i).packageName).contains("此应用"))
				continue;
			if (CURRENT_PAGE == TOP_TEN && infoList.get(i).flow == 0) {
				if (i == 0) {
					no_flow.setVisibility(View.VISIBLE);
				}
				break;
			}
			map = new HashMap<String, Object>();
			map.put("img", infoList.get(i).appIcon);
			map.put("title", (CURRENT_PAGE == TOP_TEN?(+(i+1)+". "):"")+infoList.get(i).appName);
			map.put("packName", infoList.get(i).packageName);
			map.put("pro", 50);
			map.put("total", "0");
			map.put("per", "0");
			if (CURRENT_PAGE == VERSION_PAGE) {
				map.put("info", infoList.get(i).versionName);
				if (infoList.get(i).toHide)
					map.put("per", "隐藏");
				else
					map.put("per", "显示");
			} else if (CURRENT_PAGE == FLOW_PAGE) {
				map.put("info", mFlowInfo.getFlowStr(infoList.get(i).packageName).replaceAll("\n", "\t\t"));
				long curFlow = mFlowInfo.getTotalFlow(infoList.get(i).packageName);
				DecimalFormat df = new DecimalFormat("0.00%");
				map.put("per", df.format((curFlow * 1.0) / (TOTAL_FLOW * 1.0)));
			} else if (CURRENT_PAGE == TOP_TEN) {

				long f = infoList.get(i).flow;
				long[] flow = FlowInfo.kb2mb(f);
				int p = new Double(f * 100.0 / infoList.get(infoList.size() - 1).flow * 1.0).intValue();
				map.put("pro", p == 0 ? 1 : p);
				map.put("total", (flow[0] == 0 ? "" : flow[0] + " M ") + flow[1] + " K");
			}
			list.add(map);
		}
		return list;
	}

	private void setAdapter() {
		appList.setAdapter(adapter);
	}

	private class OnItemClickListenerImpl implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			String packageName = (String) mData.get(position).get("packName");
			String flow = mFlowInfo.getFlowStr(packageName);
			int index = 0;
			for (int i = 0; i < infoList.size(); i++) {
				if (infoList.get(i).packageName.equals(packageName)) {
					index = i;
					break;
				}
			}
			if (CURRENT_PAGE == FLOW_PAGE) {

				mData.get(position).put("info", flow.replaceAll("\n", "\t\t"));
				long curFlow = mFlowInfo.getTotalFlow(packageName);
				DecimalFormat df = new DecimalFormat("0.00%");
				mData.get(position).put("per", df.format((curFlow * 1.0) / (TOTAL_FLOW * 1.0)));
				sendMessage(1);

				Builder builder = new Builder(MainActivity.this);
				builder.setTitle((CharSequence) mData.get(position).get("title"));
				builder.setMessage(flow);
				builder.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

				Dialog dialog = builder.create();
				dialog.show();

			} else if (CURRENT_PAGE == VERSION_PAGE) {
				if (RootChecker.isRootSystem()) {
					if (infoList.get(index).toHide) {
						ShellUtils.execCommand("pm disable " + packageName, true);
						infoList.get(index).toHide = false;
						sendMessage(1);
						Toast.makeText(context, "该应用已隐藏,该应用将不会有流量消耗，也不可使用，但会保存已有数据！", Toast.LENGTH_SHORT).show();
					} else {
						ShellUtils.execCommand("pm enable " + packageName, true);
						infoList.get(index).toHide = true;
						sendMessage(1);
						Toast.makeText(context, "该应用已恢复显示，可正常使用！", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context, "对不起，未获得系统权限，无法执行隐藏操作！", Toast.LENGTH_LONG).show();
				}
			} else {
				Intent intent = new Intent(MainActivity.this, AppFlowActivity.class);
				intent.putExtra("packageName", infoList.get(index).packageName);
				startActivity(intent);
			}
		}		
	}

	public final class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView info;
		public TextView per;
		public TextView total;
		public ProgressBar pro;
		public LinearLayout title_layout;
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

				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.per = (TextView) convertView.findViewById(R.id.per);
				holder.total = (TextView) convertView.findViewById(R.id.top_total);
				holder.pro = (ProgressBar) convertView.findViewById(R.id.top_ten_pro);
				holder.title_layout = (LinearLayout) convertView.findViewById(R.id.title_layout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String text="";
			if (CURRENT_PAGE == TOP_TEN) {

				holder.info.setVisibility(View.GONE);
				holder.per.setVisibility(View.GONE);

				holder.total.setVisibility(View.VISIBLE);
				holder.pro.setVisibility(View.VISIBLE);

				holder.pro.setProgress((Integer) mData.get(position).get("pro"));
				holder.total.setText((CharSequence) mData.get(position).get("total"));
				text = "^[0-9]+(.)";
			} else {

				holder.pro.setVisibility(View.GONE);
				holder.total.setVisibility(View.GONE);

				holder.info.setVisibility(View.VISIBLE);
				holder.per.setVisibility(View.VISIBLE);

				holder.info.setText((CharSequence) mData.get(position).get("info"));
				if (mData.get(position).get("per").equals("显示")) {
					holder.per.setTextColor(COLOR_BLUE);
				} else {
					holder.per.setTextColor(COLOR_ORANGE);
				}
				holder.per.setText((CharSequence) mData.get(position).get("per"));
				text = mClearEditText.getText().toString();
			}
			
			holder.img.setBackground((Drawable) mData.get(position).get("img"));
			holder.title.setText((highlight((String) mData.get(position).get("title"), text)));
			return convertView;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_flowrank) {
			CURRENT_PAGE = FLOW_PAGE;
			dateLayout.setVisibility(View.GONE);
			mClearEditText.setVisibility(View.VISIBLE);
		} else if (id == R.id.action_versioninfo) {
			CURRENT_PAGE = VERSION_PAGE;
			dateLayout.setVisibility(View.GONE);
			mClearEditText.setVisibility(View.VISIBLE);
		} else if (id == R.id.action_topten) {
			CURRENT_PAGE = TOP_TEN;
			dateLayout.setVisibility(View.VISIBLE);
			mClearEditText.setVisibility(View.GONE);
		}
		sendMessage(0);
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				setTabColor();
			}
			boolean isTop = CURRENT_PAGE == TOP_TEN;
			if (isTop) {
				if (startDate.getText().toString().equals("开始时间") && endDate.getText().toString().equals("结束时间")) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					infoList = du.getFlow("0", df.format(new Date()), infoList);
				} else {
					infoList = du.getFlow(startDate.getText().toString(), endDate.getText().toString(), infoList);
				}
			}
			// 设置listview中的item数目
			NUMS = isTop ? infoList.size() - 1 : infoList.size();

			if (msg.what != MSG_SEARCH)
				mData = getData(mClearEditText.getText().toString());

			setAdapter();
			super.handleMessage(msg);
		};
	};

	private void sendMessage(int what) {
		Message message = new Message();
		message.what = what;
		myHandler.sendMessage(message);
	}

	public void chooseDate(final View view) {
		Calendar c = Calendar.getInstance();
		Dialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
				if (view.getId() == R.id.startdate) {
					startDate.setText(year + "-" + ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" + (dayOfMonth > 9 ? dayOfMonth : "0" + dayOfMonth));
				} else if (view.getId() == R.id.enddate) {
					endDate.setText(year + "-" + ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" + (dayOfMonth > 9 ? dayOfMonth : "0" + dayOfMonth));
				}
				String start = (String) startDate.getText();
				String end = (String) endDate.getText();
				if (!start.equals("开始时间") && !end.equals("结束时间")) {
					if (Integer.parseInt(start.replace("-", "")) > Integer.parseInt(end.replace("-", ""))) {
						Toast.makeText(context, "时间选择有误，请重新选择", Toast.LENGTH_SHORT).show();
					} else {
						sendMessage(1);
					}
				}
			}
		}, c.get(Calendar.YEAR), // 传入年份
				c.get(Calendar.MONTH), // 传入月份
				c.get(Calendar.DAY_OF_MONTH) // 传入天数
		);
		dialog.show();
	}

	/** 高亮关键字 */
	public static SpannableStringBuilder highlight(String text, String target) {
		SpannableStringBuilder spannable = new SpannableStringBuilder(text);
		CharacterStyle span = null;

		if (!target.equals("")) {
			Pattern p = Pattern.compile(target);
			Matcher m = p.matcher(text);
			while (m.find()) {
				span = new ForegroundColorSpan(COLOR_BLUE);// 需要重复！
				spannable.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		du.closeDatabase();
		super.onDestroy();
	}

}
