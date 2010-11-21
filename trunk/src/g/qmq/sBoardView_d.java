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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class sBoardView_d extends Activity {

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
			if (listCreater(ReadXML("TimeMode.xml"))) {
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

			// Start thread 1.
			thd_1.start();
			break;
		}
		// Kill progress bar.
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				EfficientAdapter eAdp = new EfficientAdapter(sBoardView_d.this);
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

	private ArrayList<ArrayList<String[]>> ReadXML(String FileName) {
		ArrayList<ArrayList<String[]>> key_data = new ArrayList<ArrayList<String[]>>();
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

			NodeList rootList = root.getElementsByTagName("data");

			for (int i = 0, j = rootList.getLength(); i < j; i++) {
				Node rootNode = rootList.item(i);
				NodeList nodeList = rootNode.getChildNodes();
				if (nodeList != null) {
					ArrayList<String[]> fieldList = new ArrayList<String[]>();
					ArrayList<String[]> keyList = new ArrayList<String[]>();
					String[] temp = new String[3];
					temp[0] = rootNode.getAttributes().getNamedItem("uid")
							.getNodeValue();
					temp[1] = rootNode.getAttributes().getNamedItem("player")
							.getNodeValue();
					temp[2] = rootNode.getAttributes().getNamedItem("date")
							.getNodeValue();
					fieldList.add(temp);
					for (int k = 0, l = nodeList.getLength(); k < l; k++) {
						Node node = nodeList.item(k);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							temp = new String[3];
							temp[0] = node.getAttributes().getNamedItem("name")
									.getNodeValue();
							temp[1] = node.getFirstChild().getNodeValue();
							temp[2] = node.getAttributes().getNamedItem("unit")
									.getNodeValue();
							if (node.getAttributes().getNamedItem("key")
									.getNodeValue() == "1") {
								String[] key_temp = new String[] {
										node.getFirstChild().getNodeValue(),
										rootNode.getAttributes()
												.getNamedItem("player")
												.getNodeValue(),
										rootNode.getAttributes()
												.getNamedItem("date")
												.getNodeValue(), };
								keyList.add(key_temp);
							}
							fieldList.add(temp);
						}
					}
					whole_data.add(fieldList);
					key_data.add(keyList);
				}
			}
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (Exception e) {
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
		return key_data;
	}

	private boolean listCreater(ArrayList<ArrayList<String[]>> data) {
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
		EfficientAdapter eAdap = new EfficientAdapter(sBoardView_d.this);
		eAdap.addData(data);
		lv.setAdapter(eAdap);
		if (myRank > 0) {
			lv.setSelection(myRank - 1);
		}
		tv.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
		return true;
	}

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
						.findViewById(R.id.ls_score);
				holder.tv_date = (TextView) convertView
						.findViewById(R.id.ls_player);

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
			holder.tv_time.setText(DATA.get(position).get(1)[1]);
			holder.tv_date.setText(DATA.get(position).get(2)[1]);

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

		private void addData(ArrayList<ArrayList<String[]>> data) {
			DATA.addAll(data);
		}

		private ArrayList<ArrayList<String[]>> DATA = new ArrayList<ArrayList<String[]>>();
	}

	private ArrayList<ArrayList<String[]>> whole_data = new ArrayList<ArrayList<String[]>>();
	private TelephonyManager teleInfo;
	private int UID = -1, myRank = -1, select = 0, mode;
	private Thread thd_1;
	private String errMsg;
	private TextView tv;
	private ProgressBar pb;
}
