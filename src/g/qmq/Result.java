package g.qmq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;

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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Window;

public class Result extends Activity {
	private final static int MAX_ENTRY = 100; // Max entry stored in XML.

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.result);

		prefs = getSharedPreferences("g.qmq_preferences", 0);
		playerName = prefs.getString("playerName", "PlayerRock");
		try {
			Bundle bundle = this.getIntent().getExtras();
			resultData = bundle.getStringArray("resultData");
			mode = bundle.getChar("MODE");
		} catch (Exception e) {
			e.printStackTrace();
			AlertDialog.Builder errDialog = new AlertDialog.Builder(this);
			errDialog.setIcon(R.drawable.icon_problem);
			errDialog.setTitle("Result cannot be saved.");
			errDialog.setMessage("Error");
			errDialog.setPositiveButton("Close",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Result.this.finish();
						}
					});
		}
		initThread.start();

	}

	private Thread initThread = new Thread(new Runnable() {
		@Override
		public void run() {
			int msg = 1;
			String xmlName = null;
			switch (mode) {
			case 'T':
				xmlName = "TimeMode.xml";
			}
			// Read existing data.
			dataStore = ReadXML(xmlName);

			final Calendar c = Calendar.getInstance();
			mYear = c.get(Calendar.YEAR); // 获取当前年份
			mMonth = c.get(Calendar.MONTH);// 获取当前月份
			mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
			mHour = c.get(Calendar.HOUR_OF_DAY);// 获取当前的小时数
			mMinute = c.get(Calendar.MINUTE);// 获取当前的分钟数
			cDate = mYear + "-" + mMonth + "-" + mDay + " " + mHour + ":"
					+ mMinute;

			// Add current if not empty.
			try {
				/*
				 * Add current to the data.
				 */
				ArrayList<String[]> current_data = new ArrayList<String[]>();
				String[] newStr = new String[] { String.valueOf(uid),
						playerName, cDate };
				current_data.add(newStr);
				int i = 0, j = resultData.length;
				while (i < j) {
					String[] temp = new String[] { resultData[i],
							resultData[++i], resultData[++i] };
					i++;
					current_data.add(temp);
				}
				dataStore.add(current_data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Sort data.
			dataStore = sortList(dataStore, 1, true);

			// Create and write XML file.
			String xmlStr = makeXML(dataStore);
			if (xmlStr != null) {
				writeXML(xmlName, xmlStr);
			} else {
				Log.v("XML_EMPTY", "Nothing to write into xml.");
			}
			if (ReadXML(xmlName) == null) {
				msg = 0;
				return;
			}
			handler.sendEmptyMessage(msg);
		}
	});

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				break;
			case 1:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private ArrayList<ArrayList<String[]>> ReadXML(String FileName) {
		ArrayList<ArrayList<String[]>> temp_data = new ArrayList<ArrayList<String[]>>();
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
					String[] temp = new String[3];
					temp[0] = rootNode.getAttributes().getNamedItem("uid")
							.getNodeValue();
					int cUid = Integer.valueOf(temp[0]);
					uid = (cUid >= uid) ? cUid + 1 : uid;
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
							fieldList.add(temp);
						}
					}
					temp_data.add(fieldList);
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
		return temp_data;
	}

	private String makeXML(ArrayList<ArrayList<String[]>> source) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			// <?xml version=”1.0″ encoding=”UTF-8″ standalone=”yes”?>
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "source");
			serializer.attribute("", "lastUpdate", cDate);
			for (int i = 0; i < MAX_ENTRY && i < source.size(); i++) {

				serializer.startTag("", "data");
				serializer.attribute("", "uid", source.get(i).get(0)[0]);
				serializer.attribute("", "player", source.get(i).get(0)[1]);
				serializer.attribute("", "date", source.get(i).get(0)[2]);

				for (int j = 1, k = source.get(i).size(); j < k; j++) {
					serializer.startTag("", "fields");
					serializer.attribute("", "name", source.get(i).get(j)[0]);
					serializer.attribute("", "unit", source.get(i).get(j)[2]);
					serializer.text(source.get(i).get(j)[1]);
					serializer.endTag("", "fields");
				}

				serializer.endTag("", "data");
			}

			serializer.endTag("", "source");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			return null;
		}
	}

	private boolean writeXML(String name, String source) {
		try {
			OutputStream writer = openFileOutput(name, MODE_PRIVATE);
			OutputStreamWriter xmlWriter = new OutputStreamWriter(writer);
			xmlWriter.write(source);
			xmlWriter.close();
			writer.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private ArrayList<ArrayList<String[]>> sortList(
			ArrayList<ArrayList<String[]>> source, int sortBy, Boolean inc) {
		if (source == null)
			return null;
		if (source.size() <= 1)
			return source;
		ArrayList<ArrayList<String[]>> sortedList = new ArrayList<ArrayList<String[]>>();
		while (!source.isEmpty()) {
			int temp = 0;
			for (int i = 0; i < source.size(); i++) {
				if (inc) {
					if (Double.valueOf(source.get(i).get(sortBy)[1]) < Double
							.valueOf(source.get(temp).get(sortBy)[1])) {
						temp = i;
					}
				} else {
					if (Double.valueOf(source.get(i).get(sortBy)[1]) > Double
							.valueOf(source.get(temp).get(sortBy)[1])) {
						temp = i;
					}
				}
			}
			sortedList.add(source.get(temp));
			source.remove(temp);
		}
		return sortedList;
	}

	private SharedPreferences prefs = null;
	private String playerName, cDate;
	private String[] resultData = null;
	private char mode;
	private ArrayList<ArrayList<String[]>> dataStore = new ArrayList<ArrayList<String[]>>();
	private int mYear, mMonth, mDay, mHour, mMinute, uid;
}
