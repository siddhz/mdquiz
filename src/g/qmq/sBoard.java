package g.qmq;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class sBoard extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.s_board);

		try {
			UID = this.getIntent().getExtras().getInt("UID");
			MODE = this.getIntent().getExtras().getInt("mode");
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("UID | MODE", UID + " | " + MODE);
		}
		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/oldengl.ttf");

		titleTV[0] = (TextView) findViewById(R.id.sboard_title_1);
		titleTV[0].setTypeface(tf);
		titleTV[0].setOnClickListener(this);
		// Grasp views ready to use.
		waitRela = (RelativeLayout) findViewById(R.id.rela_wait);

//		TextView[] listTVs = new TextView[4];
//		listTVs[0] = (TextView) findViewById(R.id.list_rank);
//		listTVs[1] = (TextView) findViewById(R.id.list_player);
//		listTVs[2] = (TextView) findViewById(R.id.list_rank);
//		listTVs[3] = (TextView) findViewById(R.id.list_date);
//		for (int i = 0, j = listTVs.length; i < j; i++) {
//			listTVs[i].setTypeface(tf);
//		}

		// Show progress bar.
		waitRela.setVisibility(View.VISIBLE);
		initThd.start();
	}

	/**
	 * Reads XML and output result in format:
	 * [Array]Entry::[Array]Field::[String[]]key(0|1),Name,Value,Unit
	 * 
	 * @param name
	 *            Name of the XML file.
	 */
	private ArrayList<ArrayList<String[]>> xmlReader(String fileName) {
		ArrayList<ArrayList<String[]>> result = new ArrayList<ArrayList<String[]>>();
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// Load XML file from asset folder
			doc = docBuilder.parse(this.openFileInput(fileName));
			// set root element
			Element root = doc.getDocumentElement();
			NodeList rootList = root.getElementsByTagName("data");
			// Read data in loops.
			for (int i = 0, j = rootList.getLength(); i < j; i++) {
				Node rootNode = rootList.item(i);
				NodeList nodeList = rootNode.getChildNodes();
				if (nodeList != null) {
					ArrayList<String[]> fieldList = new ArrayList<String[]>();
					// ArrayList<String[]> keyList = new ArrayList<String[]>();
					// First field is value of uid, player and date.
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
							temp = new String[4];
							// If it is a key field.
							temp[0] = node.getAttributes().getNamedItem("key")
									.getNodeValue();
							temp[1] = node.getAttributes().getNamedItem("name")
									.getNodeValue();
							temp[2] = node.getFirstChild().getNodeValue();
							temp[3] = node.getAttributes().getNamedItem("unit")
									.getNodeValue();
							fieldList.add(temp);
						}
					}
					result.add(fieldList);
				}
			}
		} catch (SAXException e) {
			Log.e("XML_SAX_ERROR", "Failed to read xml, xml struct problem.");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			Log.e("XML_IO_ERROR", "XML file not found or unreadable");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
		return result;
	}

	/*
	 * Thread ***START***
	 */
	private Thread initThd = new Thread(new Runnable() {
		@Override
		public void run() {
			if ((resultT = xmlReader("TimeMode.xml")) != null) {
				handler.sendEmptyMessage(1);
			} else {
				TextView tv_failed = (TextView) findViewById(R.id.sb_fail);
				tv_failed.setVisibility(View.VISIBLE);
				waitRela.setVisibility(View.GONE);
			}
		}
	});
	/*
	 * Thread ***END***
	 */

	/*
	 * Handler ***START***
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: // Success
				try {
					// New ListView
					ListView lv = ((ListView) findViewById(R.id.lv));
					// Inflater layout.
					eAdapter eAdap = new eAdapter(sBoard.this);
					// Adds data to adapter
					eAdap.addData(resultT);
					// Set adapter to the ListView just created.
					lv.setAdapter(eAdap);
					lv.setCacheColorHint(0x00000000);
					lv.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> av, View v,
								int i, long l) {
							ArrayList<String[]> showList = resultT.get(i);
							int length = showList.size();
							String[] detail = new String[3 * (length - 1)];
							int k = 0;
							for (int x = 1, y = length; x < y; x++) {
								detail[k++] = showList.get(x)[1];
								detail[k++] = showList.get(x)[2];
								detail[k++] = showList.get(x)[3];
							}
							startActivity(new Intent(sBoard.this,
									showDetail.class)
									.putExtra("detail", detail).putExtra(
											"head", showList.get(0)));
						}
					});
				} catch (Exception e) {
					TextView tv_failed = (TextView) findViewById(R.id.sb_fail);
					tv_failed.setVisibility(View.VISIBLE);
				}
				// Hide loading view.
				waitRela.setVisibility(View.GONE);
				break;
			}
		}
	};

	/*
	 * Handler ***END***
	 */
	@Override
	public void onClick(View v) {
		int selected = 0;
		switch (v.getId()) {
		case R.id.sboard_title_1:
			selected = 0;
			break;
		}
		for (int i = 0, l = titleTV.length; i < l; i++) {
			if (i == selected) {
				titleTV[i].setBackgroundResource(R.drawable.bar_mid_on);
			} else {
				titleTV[i].setBackgroundResource(R.drawable.bar_mid_off);
			}
		}
	}

	private RelativeLayout waitRela;
	private ArrayList<ArrayList<String[]>> resultT = new ArrayList<ArrayList<String[]>>();
	private int UID = -1, MODE = 0;
	private TextView[] titleTV = new TextView[1];

	// tabHost = (TabHost) findViewById(android.R.id.tabhost);
	// tabHost.setBackgroundColor(Color.argb(150, 22, 70, 150));
	// tabHost.setBackgroundResource(R.drawable.main_bg);
	//
	// TabSpec timeTabSpec = tabHost.newTabSpec("timeTab");
	// TabSpec otherTabSpec = tabHost.newTabSpec("otherTab");
	//
	// timeTabSpec.setIndicator(getResources().getString(R.string.tMode_name),
	// getResources().getDrawable(R.drawable.tab_time));
	// timeTabSpec.setContent(new Intent(this, sBoardView.class).putExtra(
	// "mode", 0).putExtra("UID", UID));
	//
	// otherTabSpec.setIndicator("Other").setContent(
	// new Intent(this, sBoardView.class).putExtra("mode", 0)
	// .putExtra("UID", UID));
	//
	// tabHost.addTab(timeTabSpec);
	// tabHost.addTab(otherTabSpec);
	//
	// tabHost.setCurrentTab(MODE);
	// tabHost.setOnTabChangedListener(new OnTabChangeListener() {
	// @Override
	// public void onTabChanged(String tabId) {
	// int cTabId = tabHost.getCurrentTab();
	// changeTab(cTabId);
	// UID = -1;
	// }
	// });
	// }
	//
	// private void changeTab(int tabId) {
	// LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
	// TabWidget tw = (TabWidget) ll.getChildAt(0);
	// // 以上两行代码，找到TabWidget
	//
	// int tabCount = tw.getChildCount();
	//
	// for (int i = 0; i < tabCount; i++) {
	// RelativeLayout rl = (RelativeLayout) tw.getChildAt(i);
	// TextView tv = (TextView) rl.getChildAt(1);
	// // 以上两行代码，找到要修改属性的TextView
	//			
	//			
	// String tabLabel = "Bye!";
	// int color = Color.TRANSPARENT;
	// rl.setBackgroundColor(color);
	// if (i == tabId) { // 如果是选定tab，则修改之
	// tabLabel = "Hello!";
	// color = Color.RED;
	// }
	//
	// tv.setText(tabLabel);
	// tv.setTextColor(color);
	// // 以上两行代码，修改TextView的属性
	//
	// }
	// }
	// private TabHost tabHost;

}