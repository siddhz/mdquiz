package g.qmq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Result extends Activity {
	private Thread xmlThd = new Thread(new Runnable() {
		@Override
		public void run() {

			switch (mode) {
			case 0:
				timeMode();
				if (xmlWrite("sb_time.xml", xmlCreater(sortList(
						ReadXML("sb_time.xml"), "valuef", true)))) {
					handler.sendEmptyMessage(1);
				} else {
					handler.sendEmptyMessage(0);
				}
				break;
			case 1:
				memoryMode();
				if (xmlWrite("sb_memory.xml", xmlCreater(sortList(
						ReadXML("sb_memory.xml"), "valuef", false)))) {
					handler.sendEmptyMessage(1);
				} else {
					handler.sendEmptyMessage(0);
				}
				break;
			case 2:
				sdMode();
				if (xmlWrite("sb_sd.xml", xmlCreater(sortList(
						ReadXML("sb_sd.xml"), "valuef", false)))) {
					handler.sendEmptyMessage(1);
				} else {
					handler.sendEmptyMessage(0);
				}
				break;
			}

		}
	});
	private Handler handler = new Handler() {
		// @SuppressWarnings("")
		@Override
		public void handleMessage(Message msg) {
			// pd.dismiss();
			switch (msg.what) {
			case 0:
				pbWait.setVisibility(View.GONE);
				tvWait
						.setText("Save Failed. Unable to write XML. Out of memory?");
				btnAct1.setVisibility(View.VISIBLE);
				btnAct1.setClickable(true);
				break;
			case 1:
				pbWait.setVisibility(View.GONE);
				tvWait.setVisibility(View.GONE);
				btnAct1.setVisibility(View.VISIBLE);
				btnAct2.setVisibility(View.VISIBLE);
				btnAct1.setClickable(true);
				btnAct2.setClickable(true);

				// startAnim(200);
				break;

			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.
		setContentView(R.layout.endresult);

		tvWait = (TextView) findViewById(R.id.end_wait);
		pbWait = (ProgressBar) findViewById(R.id.end_pbwait);
		btnAct1 = (Button) findViewById(R.id.btnBackToMain);
		btnAct2 = (Button) findViewById(R.id.btnToSBoard);

		// Get data
		Bundle bunde = this.getIntent().getExtras();
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		playerName = prefs.getString("playerName", "PlayerRock");
		mode = bunde.getInt("mode");
		switch (mode) {
		case 0: //Time Mode
			acc_r = bunde.getInt("acc_r");
			acc_w = bunde.getInt("acc_w");
			time = bunde.getDouble("time");
			gLength = bunde.getInt("gLength");
			// Final Accuracy
			value1 = acc_r * 100 / (acc_r + acc_w);
			// Average time
			value2 = time / acc_r;
			// Highest correct stack.
			value3 = Double.valueOf(bunde.getInt("hStack"));
			// fTime
			valuef = (2 * time - (time * value1 / 100)) * 50 / gLength;
			break;
		case 1: //Memory Mode
			acc_r = bunde.getInt("acc_r");
			acc_w = bunde.getInt("acc_w");
			time = bunde.getDouble("time");
			gLength = bunde.getInt("gLength");
			// Final Accuracy
			value1 = acc_r * 100 / (acc_r + acc_w);
			// Average time
			value2 = time / acc_r;
			// Highest correct stack.
			value3 = Double.valueOf(bunde.getInt("hStack"));
			// fTime
			valuef = acc_r * 10 + value3;
			break;
		case 2: //SD Mode
			value1 = bunde.getDouble("time");
			valuef = bunde.getInt("qNum");
		}

		// Formating data.
		java.text.DecimalFormat df = new DecimalFormat("#.##");
		value1 = Double.valueOf(df.format(value1));
		value2 = Double.valueOf(df.format(value2));
		value3 = Double.valueOf(df.format(value3));
		valuef = Double.valueOf(df.format(valuef));

		showResult();
		tvWait.setText("Saving result...");
		// Start prosing.
		xmlThd.start();
	}

	private void timeMode() {
		names[0] = "Time";
		names[1] = "Accuracy";
		names[2] = "Average Time";
		names[3] = "Highest Stack";
		HashMap<String, String> hm0 = new HashMap<String, String>();
		hm0.put("label", "Time");
		hm0.put("value", time + "s");
		data.add(hm0);

		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("label", "Total Accuracy");
		hm.put("value", value1 + "%");
		data.add(hm);

		HashMap<String, String> hm1 = new HashMap<String, String>();
		hm1.put("label", "Average Time");
		hm1.put("value", value2 + "s/Q");
		data.add(hm1);

		HashMap<String, String> hm2 = new HashMap<String, String>();
		hm2.put("label", "Highest Correct Stack");
		hm2.put("value", String.valueOf(Math.round(value3)));
		data.add(hm2);

		HashMap<String, String> hm3 = new HashMap<String, String>();
		hm3.put("label", "Final Scalared Time");
		hm3.put("value", df2.format(valuef) + "s");
		data.add(hm3);
	}

	private void memoryMode() {
		names[0] = "Point";
		names[1] = "Accuracy";
		names[2] = "Average Time";
		names[3] = "Highest Stack";
		HashMap<String, String> hm0 = new HashMap<String, String>();
		hm0.put("label", "Time");
		hm0.put("value", time + "s");
		data.add(hm0);

		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("label", "Total Accuracy");
		hm.put("value", value1 + "%");
		data.add(hm);

		HashMap<String, String> hm1 = new HashMap<String, String>();
		hm1.put("label", "Average Time");
		hm1.put("value", value2 + "s/Q");
		data.add(hm1);

		HashMap<String, String> hm2 = new HashMap<String, String>();
		hm2.put("label", "Highest Correct Stack");
		hm2.put("value", String.valueOf(Math.round(value3)));
		data.add(hm2);

		HashMap<String, String> hm3 = new HashMap<String, String>();
		hm3.put("label", "Total Points");
		hm3.put("value", valuef + "p");
		data.add(hm3);

	}

	private void sdMode() {
		names[0] = "Number Of Questions";
		names[1] = "Time";
		names[2] = "-";
		names[3] = "-";
		HashMap<String, String> hm0 = new HashMap<String, String>();
		hm0.put("label", "Time");
		hm0.put("value", value1 + "s");
		data.add(hm0);
		
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("label", "-");
		hm.put("value", "-");
		data.add(hm);

		HashMap<String, String> hm1 = new HashMap<String, String>();
		hm1.put("label", "-");
		hm1.put("value", "-");
		data.add(hm1);

		HashMap<String, String> hm2 = new HashMap<String, String>();
		hm2.put("label", "-");
		hm2.put("value", "-");
		data.add(hm2);

		HashMap<String, String> hm3 = new HashMap<String, String>();
		hm3.put("label", "Number Of Questions");
		hm3.put("value", df2.format(valuef) + "Q");
		data.add(hm3);
	}
	
	private void showResult() {
		SimpleAdapter sAdp = new SimpleAdapter(this, data,// 数据来源
				R.layout.lv_end,// 每一个user xml 相当ListView的一个组件
				new String[] { "label", "value" },
				// 分别对应view 的id
				new int[] { R.id.result_label, R.id.result_value });
		// 获取listview
		((ListView) findViewById(R.id.lv_end)).setAdapter(sAdp);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			startActivity(new Intent(this, MusicQuiz.class));
		return true;
	}

	private ArrayList<HashMap<String, String>> sortList(
			ArrayList<HashMap<String, String>> source, String sortBy,
			Boolean inc) {
		if (source.size() <= 1)
			return source; // One or Zero element list sorted and return.
		ArrayList<HashMap<String, String>> sortList = new ArrayList<HashMap<String, String>>();
		int temp = 0;
		for (int i = 0; source.size() > 0; i++) {
			for (int j = 0; j < source.size(); j++) {
				if (j == 0) // Set first element to minimum.
					temp = 0;
				if (inc) {
					// Increase order.
					if (Double.valueOf(source.get(j).get(sortBy)) < Double
							.valueOf(source.get(temp).get(sortBy)))
						temp = j;
				} else {
					// Decrease order.
					if (Double.valueOf(source.get(j).get(sortBy)) > Double
							.valueOf(source.get(temp).get(sortBy)))
						temp = j;
				}
			}
			sortList.add(source.get(temp));
			source.remove(temp);

		}
		return sortList;
	}

	private String xmlCreater(ArrayList<HashMap<String, String>> source) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			// <?xml version=”1.0″ encoding=”UTF-8″ standalone=”yes”?>
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "source");
			for (int i = 0; i < 10 && i < source.size(); i++) {

				serializer.startTag("", "data");
				serializer.attribute("", "UID", source.get(i).get("UID"));

				// <player>
				serializer.startTag("", "player");
				serializer.text(source.get(i).get("player"));
				serializer.endTag("", "player");

				// <date>
				serializer.startTag("", "date");
				serializer.text(source.get(i).get("date"));
				serializer.endTag("", "date");

				serializer.startTag("", "value1");
				serializer.attribute("", "name", names[1]);
				serializer.text(source.get(i).get("value1"));
				serializer.endTag("", "value1");

				serializer.startTag("", "value2");
				serializer.attribute("", "name", names[2]);
				serializer.text(source.get(i).get("value2"));
				serializer.endTag("", "value2");

				serializer.startTag("", "value3");
				serializer.attribute("", "name", names[3]);
				serializer.text(source.get(i).get("value3"));
				serializer.endTag("", "value3");

				serializer.startTag("", "valuef");
				serializer.attribute("", "name", names[0]);
				serializer.text(source.get(i).get("valuef"));
				serializer.endTag("", "valuef");
				// </date>
				serializer.endTag("", "data");
			}

			serializer.endTag("", "source");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			return null;
		}
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

				NodeList v1List = root.getElementsByTagName("value1");
				Node v1Node = v1List.item(i);
				hm.put("value1", v1Node.getFirstChild().getNodeValue());

				NodeList v2List = root.getElementsByTagName("value2");
				Node v2Node = v2List.item(i);
				hm.put("value2", v2Node.getFirstChild().getNodeValue());

				NodeList v3List = root.getElementsByTagName("value3");
				Node v3Node = v3List.item(i);
				hm.put("value3", v3Node.getFirstChild().getNodeValue());

				NodeList vfList = root.getElementsByTagName("valuef");
				Node vfNode = vfList.item(i);
				hm.put("valuef", vfNode.getFirstChild().getNodeValue());

				NodeList playerList = root.getElementsByTagName("player");
				Node playerNode = playerList.item(i);
				hm.put("player", playerNode.getFirstChild().getNodeValue());

				NodeList dateList = root.getElementsByTagName("date");
				Node dateNode = dateList.item(i);
				hm.put("date", dateNode.getFirstChild().getNodeValue());

				Node nd = nodeList.item(i);
				hm.put("UID", nd.getAttributes().getNamedItem("UID")
						.getNodeValue());
				// get largest UID.
				if (i == 0)
					UID = Integer.valueOf(nd.getAttributes()
							.getNamedItem("UID").getNodeValue());
				else if (Integer.valueOf(nd.getAttributes().getNamedItem("UID")
						.getNodeValue()) > UID) {
					UID = Integer.valueOf(nd.getAttributes()
							.getNamedItem("UID").getNodeValue());
				}
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
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("player", playerName);
		hm.put("date", Integer.valueOf(dt.getYear()) + 1900 + "-"
				+ (Integer.valueOf(dt.getMonth()) + 1) + "-"
				+ Integer.valueOf(dt.getDate()) + " " + dt.getHours() + ":"
				+ dt.getMinutes() + ":" + dt.getSeconds());
		hm.put("UID", String.valueOf(++UID));
		hm.put("value1", String.valueOf(value1));
		hm.put("value2", String.valueOf(value2));
		hm.put("value3", String.valueOf(value3));
		hm.put("valuef", String.valueOf(valuef));
		dataList.add(hm);
		return dataList;
	}

	private final boolean xmlWrite(String path, String source) {
		try {
			OutputStream writer = openFileOutput(path, MODE_PRIVATE);
			OutputStreamWriter xmlWriter = new OutputStreamWriter(writer);
			xmlWriter.write(source);
			xmlWriter.close();
			writer.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public void backToMain(View target) {
		startActivity(new Intent(this, MusicQuiz.class));
		this.finish();
	}

	public void toSBoard(View target) {
		Intent i = new Intent(this, sBoard.class);
		i.putExtra("UID", UID);
		i.putExtra("mode", mode);
		startActivity(i);
		this.finish();

	}
	
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	private SharedPreferences prefs = null;
	private String playerName = null;
	private double valuef, value1, value2, value3;
	private double time;
	private String[] names = new String[4];
	private int UID, mode, acc_r, acc_w, gLength;
	private DecimalFormat df2 = new DecimalFormat("###.##");
	private Date dt = new Date();
	private ProgressDialog mDialog;
	private TextView tvWait;
	private ProgressBar pbWait;
	private Button btnAct1;
	private Button btnAct2;
}

