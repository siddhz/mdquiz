package g.qmq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;

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
import android.content.SharedPreferences;
import android.os.Bundle;
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
			Bundle bunde = this.getIntent().getExtras();
			resultData = bunde.getStringArray("resultData");
		} catch (Exception e) {
			e.printStackTrace();
		}

		Random rd = new Random();

		ReadXML("test.xml", data);
		
//		for (int i = 0; i < 10; i++) {
			ArrayList<String[]> newDL = new ArrayList<String[]>();
			String[] newStr = new String[] { "125", "ROCKNAME",
					"2010-10-21:23:11:23" };
			newDL.add(newStr);
			newStr = new String[] { "Time", rd.nextDouble() + "", "s" };
			newDL.add(newStr);
			data.add(newDL);
//		}
		sortList(data, 1, true);
		String xmlStr = makeXML(data);
		writeXML("test.xml", xmlStr);
		ReadXML("test.xml", data);

	}

	private boolean ReadXML(String FileName, ArrayList<ArrayList<String[]>> data) {
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
					data.add(fieldList);
				}
			}
		} catch (ParserConfigurationException e) {
			return false;
		} catch (SAXException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (Exception e) {
			return false;
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
		return true;
	}

	private String makeXML(ArrayList<ArrayList<String[]>> source) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			// <?xml version=”1.0″ encoding=”UTF-8″ standalone=”yes”?>
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "source");
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
		}
		return true;
	}

	private void sortList(ArrayList<ArrayList<String[]>> source, int sortBy,
			Boolean inc) {
		if (source.size() <= 1)
			return; // One or Zero element list sorted and return.
		ArrayList<ArrayList<String[]>> tempList = new ArrayList<ArrayList<String[]>>();
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
			tempList.add(source.get(temp));
			source.remove(temp);
		}
		source.addAll(tempList);
	}

	private SharedPreferences prefs = null;
	private String playerName;
	private String[] resultData;
	private char mode;
	private ArrayList<ArrayList<String[]>> data = new ArrayList<ArrayList<String[]>>();
}
