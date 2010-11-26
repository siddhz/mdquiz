package g.qmq;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class sBoardView extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.s_board);

		// Grasp views ready to use.
		waitRela = (RelativeLayout) findViewById(R.id.rela_wait);

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
			resultT = xmlReader("TimeMode.xml");
			handler.sendEmptyMessage(1);
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
				ListView lv = ((ListView) findViewById(R.id.lv));
				eAdapter eAdap = new eAdapter(sBoardView.this);
				eAdap.addData(resultT);
				lv.setAdapter(eAdap);
				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> av, View v, int i,
							long l) {
						ArrayList<String[]> showList = resultT.get(i);
						int length = showList.size();
						String[] detail = new String[3 * (length - 1)];
						int k = 0;
						for (int x = 1, y = length; x < y; x++) {
							detail[k++] = showList.get(x)[1];
							detail[k++] = showList.get(x)[2];
							detail[k++] = showList.get(x)[3];
						}
						startActivity(new Intent(sBoardView.this,
								showDetail.class).putExtra("detail", detail)
								.putExtra("head", showList.get(0)));
					}
				});
				// Hide loading view.
				waitRela.setVisibility(View.GONE);
				break;
			}
		}
	};
	/*
	 * Handler ***END***
	 */

	private RelativeLayout waitRela;
	private ArrayList<ArrayList<String[]>> resultT = new ArrayList<ArrayList<String[]>>();
}
