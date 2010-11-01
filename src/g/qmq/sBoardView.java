package g.qmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class sBoardView extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.s_board);

		pb = (ProgressBar) findViewById(R.id.sb_pb);
		tv = (TextView) findViewById(R.id.sb_wait);
		tv.setText(this.getResources().getString(R.string.fetching_xml));

		mode = this.getIntent().getExtras().getInt("mode");
		switch (mode) {
		case 0:
			if (listCreater(ReadXML("sb_time.xml"))) {
				// Successfully created list.
				tv.setVisibility(View.GONE);
			} else {
				// Failed to find XML.
				tv.setText("XML not found or empty. Play some game first.");
			}
			break;
		case 1:
			if (listCreater(ReadXML("sb_memory.xml"))) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setText("XML not found or empty. Play some game first.");
			}
			pb.setVisibility(View.GONE);
			break;
		case 2:
			if (listCreater(ReadXML("sb_sd.xml"))) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setText("XML not found or empty. Play some game first.");
			}
			pb.setVisibility(View.GONE);
			break;
		case 9:
			pb.setVisibility(View.VISIBLE);
			tv.setText(this.getResources().getString(R.string.fetching_web));
			thd_1 = new Thread(new Runnable() {
				@Override
				public void run() {
					teleInfo = (TelephonyManager) sBoardView.this
							.getSystemService(Context.TELEPHONY_SERVICE);
					webToolBox webFetcher = new webToolBox();
					// Add Post content
					String[] postCont = new String[] { "imei",
							teleInfo.getDeviceId() };
					String url = "http://comeplay8.com/mDroid/mlist.php";
					if (webFetcher.webCon(url, postCont)) {
						String strResult = webFetcher.strResult;
						if (strResult.contains("[F]")) {
							errMsg = "Error: " + strResult.split("#")[1];
							handler.sendEmptyMessage(2);
						} else if (strResult.contains("[S]")) {
							// 获取排名内容
							String data = strResult.split("#")[1];
							Log.d("DATA", data);
							// 获取当前排名
							myRank = Integer.valueOf(strResult.split("#")[2]);
							Log.d("MYRANK", myRank + "");
							for (int i = 0, tSize = data.split(";").length; i < tSize; i++) {
								HashMap<String, String> hm = new HashMap<String, String>();
								hm.put("name", data.split(";")[i].split(":")[0]);
								hm.put("score",
										data.split(";")[i].split(":")[1]);
								hm.put("country",
										data.split(";")[i].split(":")[2]);
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
			break;
		}
		// Kill progressbar.(S|F)
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				EfficientAdapter eAdp = new EfficientAdapter(sBoardView.this);
				((ListView) findViewById(R.id.lv)).setAdapter(eAdp);
				if (myRank <= 100 & myRank > 0) {
					((ListView) findViewById(R.id.lv)).setSelection(myRank - 1);
				}
				tv.setVisibility(View.GONE);
				pb.setVisibility(View.GONE);
				break;
			case 2:
				tv.setText(errMsg);
				break;
			}
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			finish();
		return true;
	}

	private ArrayList<HashMap<String, String>> ReadXML(String FileName) {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// xml file 放到 assets目录中的
			doc = docBuilder.parse(this.openFileInput(FileName));
			// root element
			Element root = doc.getDocumentElement();
			// Do something here
			// get a NodeList by tagname
			NodeList nodeList = root.getElementsByTagName("data");
			for (int i = 0; i < nodeList.getLength(); i++) {
				HashMap<String, String> hm = new HashMap<String, String>();
				Node nd = nodeList.item(i);
				hm.put("UID", nd.getAttributes().getNamedItem("UID")
						.getNodeValue());
				if (Integer.valueOf(nd.getAttributes().getNamedItem("UID")
						.getNodeValue()) == UID) {
					select = i;
					hm.put("rank", String.valueOf(i + 1) + "<");
				} else {
					hm.put("rank", String.valueOf(i + 1));
				}
				NodeList v1List = root.getElementsByTagName("value1");
				Node v1Node = v1List.item(i);
				hm.put("value1", v1Node.getAttributes().getNamedItem("name")
						.getNodeValue()
						+ ": " + v1Node.getFirstChild().getNodeValue());

				NodeList v2List = root.getElementsByTagName("value2");
				Node v2Node = v2List.item(i);
				hm.put("value2", v2Node.getAttributes().getNamedItem("name")
						.getNodeValue()
						+ ": " + v2Node.getFirstChild().getNodeValue());

				NodeList v3List = root.getElementsByTagName("value3");
				Node v3Node = v3List.item(i);
				hm.put("value3", v3Node.getAttributes().getNamedItem("name")
						.getNodeValue()
						+ ": " + v3Node.getFirstChild().getNodeValue());

				NodeList vfList = root.getElementsByTagName("valuef");
				Node vfNode = vfList.item(i);
				hm.put("valuef", vfNode.getFirstChild().getNodeValue());

				NodeList playerList = root.getElementsByTagName("player");
				Node playerNode = playerList.item(i);
				hm.put("player", playerNode.getFirstChild().getNodeValue());

				NodeList dateList = root.getElementsByTagName("date");
				Node dateNode = dateList.item(i);
				hm.put("date", "Date: "
						+ dateNode.getFirstChild().getNodeValue());

				dataList.add(hm);

			}
			// re = getAttributes().getNamedItem("number").getNodeValue();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}

		return dataList;
	}

	private boolean listCreater(ArrayList<HashMap<String, String>> data) {
		if (data.isEmpty())
			return false;
		// SimpleAdapter sAdp = new SimpleAdapter(this, data,// 数据来源
		// R.layout.lv_sboard,// 每一个user xml 相当ListView的一个组件
		// new String[] { "rank", "valuef", "player", "date", "value1" },
		// // 分别对应view 的id
		// new int[] { R.id.list_rank, R.id.list_valuef, R.id.list_player,
		// R.id.list_date, R.id.list_value1 });
		// // 获取listview
		// ((ListView) findViewById(R.id.lv)).setAdapter(sAdp);
		// ((ListView) findViewById(R.id.lv)).setSelection(select - 2);
		// ((ListView) findViewById(R.id.lv))
		// .setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1,
		// int arg2, long arg3) {
		// Log.d("ListView", arg2 + "Clicked");
		// }
		//
		// });
		ListView lv = ((ListView) findViewById(R.id.lv));
		EfficientAdapter eAdap = new EfficientAdapter(sBoardView.this);
		eAdap.addData(data);
		lv.setAdapter(eAdap);
		if (myRank > 0) {
			lv.setSelection(myRank - 1);
		}
		tv.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
		return true;
	}

	private ArrayList<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();

	private class EfficientAdapter extends BaseAdapter {
		// API LIST14
		private LayoutInflater mInflater;

		public EfficientAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return DATA.size();
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
				convertView = mInflater.inflate(R.layout.list_score, null);

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.tv_rank = (TextView) convertView
						.findViewById(R.id.ls_rank);
				holder.tv_time = (TextView) convertView
						.findViewById(R.id.ls_time);
				holder.tv_date = (TextView) convertView
						.findViewById(R.id.ls_date);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.tv_rank.setText(String.valueOf(position + 1)
					+ suffix(position + 1));
			if (position == myRank - 1) {
				holder.tv_rank.setBackgroundColor(Color.BLUE);
			} else {
				holder.tv_rank.setBackgroundColor(Color.TRANSPARENT);
			}
			switch (position) {
			case 0:
				holder.tv_rank.setTextColor(Color.RED);
				holder.tv_rank.setTextSize(20f);

				break;
			case 1:
				holder.tv_rank.setTextColor(Color.BLUE);
				holder.tv_rank.setTextSize(19f);
				break;
			case 2:
				holder.tv_rank.setTextColor(Color.GREEN);
				holder.tv_rank.setTextSize(18f);
				break;
			default:
				holder.tv_rank.setTextColor(Color.WHITE);
				holder.tv_rank.setTextSize(16f);
				break;
			}
			holder.tv_time.setText(DATA.get(position).get("time"));
			holder.tv_date.setText(DATA.get(position).get("date"));

			return convertView;
		}

		class ViewHolder {
			TextView tv_rank, tv_time, tv_date;
		}

		private String suffix(int num) {
			// Handle special case 11,12,13
			if (num == 11 || num == 12 || num == 13)
				return "th";
			switch (num % 10) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
			}
		}

		private void addData(ArrayList<HashMap<String, String>> data) {
			DATA.addAll(data);
		}

		private ArrayList<HashMap<String, String>> DATA = new ArrayList<HashMap<String, String>>();
	}

	private TelephonyManager teleInfo;
	private int UID = -1, myRank = -1, select = 0, mode;
	private Thread thd_1;
	private String errMsg;
	private TextView tv;
	private ProgressBar pb;
}
