package g.qmq;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class endResult_OL extends Activity implements OnClickListener {

	private final static String url = "http://comeplay8.com/mDroid/r.php";
	private Thread thd_1;
	private TelephonyManager teleInfo;
	private TextView tv1, wait, rankTitle;
	private String errMsg;
	private SharedPreferences prefs;
	private ArrayList<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();
	private int myRank = -1;
	private RelativeLayout rl;
	private Button btn1, btn2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.endresult_ol);

		btn1 = (Button) findViewById(R.id.ol_btn1);
		btn1.setOnClickListener(this);
		btn2 = (Button) findViewById(R.id.ol_btn2);
		btn2.setOnClickListener(this);

		// Get phone info
		teleInfo = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		thd_1 = new Thread(new Runnable() {
			@Override
			public void run() {
				Bundle bunde = endResult_OL.this.getIntent().getExtras();
				double time = bunde.getDouble("time");
				int qNum = bunde.getInt("qNum");
				prefs = getSharedPreferences("g.qmq_preferences", 0);
				String name = new String("UTF-8");
				name = prefs.getString("playerName", "PlayerRock");
				webToolBox webFetcher = new webToolBox();
				String[] postCont = new String[] { "name", name, "score",
						String.valueOf(qNum * 10 - time), "time",
						String.valueOf(time), "qNum", String.valueOf(qNum),
						"imei", teleInfo.getDeviceId() };
				if (webFetcher.webCon(url, postCont)) {
					String strResult = webFetcher.strResult;
					if (strResult.contains("[F]")) {
						errMsg = "Error: " + strResult.split("#")[1];
						handler.sendEmptyMessage(2);
					} else if (strResult.contains("[S]")) {
						// 获取排名内容
						String data = strResult.split("#")[1];
						// 获取当前排名
						myRank = Integer.valueOf(strResult.split("#")[2]);
						for (int i = 0, tSize = data.split(";").length; i < tSize; i++) {
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("name", data.split(";")[i].split(":")[0]);
							hm.put("score", data.split(";")[i].split(":")[1]);
							hm.put("country", data.split(";")[i].split(":")[2]);
							listData.add(hm);
						}
						handler.sendEmptyMessage(1);
					}
				} else {
					errMsg = webFetcher.errMsg;
					handler.sendEmptyMessage(2);
				}
			}
		});

		// Start thread 1.
		thd_1.start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ProgressBar pb = (ProgressBar) findViewById(R.id.ol_pb);
			tv1 = (TextView) findViewById(R.id.ol_tv1);
			wait = (TextView) findViewById(R.id.ol_wait);
			rankTitle = (TextView) findViewById(R.id.ol_rankTitle);
			rl = (RelativeLayout) findViewById(R.id.ol_btnLayout);
			switch (msg.what) {
			case 0:
				break;
			case 1:// Success
				String toastMsg,
				toastMsg2 = null;
				listCreater(listData);
				pb.setVisibility(View.GONE);
				wait.setVisibility(View.GONE);
				rl.setVisibility(View.VISIBLE);
				// tv1.setVisibility(View.VISIBLE);
				rankTitle.setVisibility(View.VISIBLE);
				if (myRank <= 100 & myRank > 0) {
					toastMsg = "Congratulations! You are now top 100th.";
					toastMsg2 = "Your global ranking is " + suffix(myRank);
				} else {
					toastMsg = "Sorry, you have not make into top 100th";
					toastMsg2 = "Keep trying don't give up!";
				}

				Toast.makeText(g.qmq.endResult_OL.this, toastMsg,
						Toast.LENGTH_LONG).show();
				Toast.makeText(g.qmq.endResult_OL.this, toastMsg2,
						Toast.LENGTH_LONG).show();
				break;
			case 2:// Fail
				pb.setVisibility(View.GONE);
				wait.setVisibility(View.GONE);
				tv1.setVisibility(View.VISIBLE);
				tv1.setText(errMsg);
				break;
			}

		}
	};

	// public boolean webFetcher(double time, int qNum, String name) {
	// double score = qNum * 10 - time;
	//
	// String uriAPI = "http://comeplay8.com/mDroid/r.php";
	//
	// /* 建立HTTPost对象 */
	// HttpPost httpRequest = new HttpPost(uriAPI);
	//
	// /* NameValuePair实现请求参数的封装 */
	// List<NameValuePair> params = new ArrayList<NameValuePair>();
	// params.add(new BasicNameValuePair("name", name));
	// params.add(new BasicNameValuePair("score", String.valueOf(score)));
	// params.add(new BasicNameValuePair("time", String.valueOf(time)));
	// params.add(new BasicNameValuePair("qNum", String.valueOf(qNum)));
	// params.add(new BasicNameValuePair("imei", teleInfo.getDeviceId()));
	// params.add(new BasicNameValuePair("sCode",
	// "9EB2F1619156C86DD91C5652516F175E"));
	// try {
	// /* 添加请求参数到请求对象 */
	// httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	// /* 发送请求并等待响应 */
	// HttpResponse httpResponse = new DefaultHttpClient()
	// .execute(httpRequest);
	// /* 若状态码为200 ok */
	// if (httpResponse.getStatusLine().getStatusCode() == 200) {
	// /* 读返回数据(整个Post后的页面源码.) */
	// String strResult = EntityUtils.toString(httpResponse
	// .getEntity(), "utf_8");
	// if (strResult.contains("[F]")) {
	// errMsg = strResult.split("#")[1];
	// return false;
	// } else if (strResult.contains("[S]")) {
	// // 获取排名内容
	// String data = strResult.split("#")[1];
	// // 获取当前排名
	// myRank = Integer.valueOf(strResult.split("#")[2]);
	// for (int i = 0; i < data.split(";").length; i++) {
	// HashMap<String, String> hm = new HashMap<String, String>();
	// hm.put("name", data.split(";")[i].split(":")[0]);
	// hm.put("score", data.split(";")[i].split(":")[1]);
	// hm.put("country", data.split(";")[i].split(":")[2]);
	// listData.add(hm);
	// }
	// return true;
	// }
	//
	// } else {
	// errMsg = "Error Response: "
	// + httpResponse.getStatusLine().toString();
	// return false;
	// }
	// } catch (ClientProtocolException e) {
	// errMsg = e.getMessage().toString();
	// e.printStackTrace();
	// } catch (IOException e) {
	// errMsg = e.getMessage().toString();
	// e.printStackTrace();
	// } catch (Exception e) {
	// errMsg = e.getMessage().toString();
	// e.printStackTrace();
	// }
	// return false;
	// }

	private void listCreater(ArrayList<HashMap<String, String>> data) {
		EfficientAdapter eAdp = new EfficientAdapter(this);
		((ListView) findViewById(R.id.ol_lv)).setAdapter(eAdp);
		if (myRank <= 100) {
			((ListView) findViewById(R.id.ol_lv)).setSelection(myRank - 1);
		}
	}

	private class EfficientAdapter extends BaseAdapter {
		// API LIST14
		private LayoutInflater mInflater;

		public EfficientAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return listData.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.lv_ol, null);

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.tv0 = (TextView) convertView
						.findViewById(R.id.ol_value0);
				holder.tv1 = (TextView) convertView
						.findViewById(R.id.ol_value1);
				holder.tv2 = (TextView) convertView
						.findViewById(R.id.ol_value2);
				holder.tv3 = (TextView) convertView
						.findViewById(R.id.ol_value3);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.tv0.setText(String.valueOf(suffix(position + 1)));
			if (position == myRank - 1) {
				holder.tv0.setBackgroundColor(Color.BLUE);
			} else {
				holder.tv0.setBackgroundColor(Color.TRANSPARENT);
			}
			switch (position) {
			case 0:
				holder.tv0.setTextColor(Color.RED);
				holder.tv0.setTextSize(20f);
				break;
			case 1:
				holder.tv0.setTextColor(Color.BLUE);
				holder.tv0.setTextSize(19f);
				break;
			case 2:
				holder.tv0.setTextColor(Color.GREEN);
				holder.tv0.setTextSize(18f);
				break;
			default:
				holder.tv0.setTextColor(Color.WHITE);
				holder.tv0.setTextSize(16f);
				break;
			}
			holder.tv1.setText(listData.get(position).get("name"));
			holder.tv2.setText(listData.get(position).get("score"));
			holder.tv3.setText(listData.get(position).get("country"));

			return convertView;
		}

		class ViewHolder {
			TextView tv0, tv1, tv2, tv3;
		}

	}

	public String suffix(int num) {
		// Handle special case 11,12,13
		if (num == 11 || num == 12 || num == 13)
			return num + "th";
		switch (num % 10) {
		case 1:
			return num + "st";
		case 2:
			return num + "nd";
		case 3:
			return num + "rd";
		default:
			return num + "th";
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ol_btn1:
			finish();
			break;
		case R.id.ol_btn2:
			final String url = (String) this.getBaseContext().getResources()
					.getText(R.string.vListUrl);
			openUrl(url);
			break;
		}
	}

	public void openUrl(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}
