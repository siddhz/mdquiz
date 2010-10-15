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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Result extends Activity implements OnClickListener,
		OnTouchListener {
	private final static int MAX_ENTRY = 100; // Max entry stored in XML.
	private final static long animTime = 1500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.result);

		// int staffLength = 4;

		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/oldengl.ttf");

		tv[0] = (TextView) findViewById(R.id.result_title);
		tv[1] = (TextView) findViewById(R.id.ToMain);
		tv[2] = (TextView) findViewById(R.id.ToBoard);

		tv[1].setOnClickListener(this);
		tv[2].setOnClickListener(this);
		for (int i = 0, j = tv.length; i < j; i++) {
			tv[i].setTypeface(tf);
		}

		// TextView tvr[] = new TextView[staffLength];
		// String str[] = new String[] { "Java Code (NEW)", "100%", "Time:",
		// "150S" };
		//
		// tvr = staffView(tvr, str, "fonts/oldengl.ttf", 26f, Color.BLACK);
		//
		// RelativeLayout[] rl = new RelativeLayout[staffLength / 2];
		// rl = layoutFactory(rl, tvr);
		//
		// RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
		// RelativeLayout.LayoutParams.FILL_PARENT, 50);
		//
		// LinearLayout mWorld = (LinearLayout) findViewById(R.id.world);
		//
		// for (int i = 0, j = rl.length; i < j; i++) {
		// mWorld.addView(rl[i], param);
		// }

		prefs = getSharedPreferences("g.qmq_preferences", 0);
		playerName = prefs.getString("playerName", "PlayerRock");
		try {
			Bundle bundle = this.getIntent().getExtras();
			resultData = bundle.getStringArray("resultData");
			mode = bundle.getChar("MODE");

			int dLength = resultData.length / 3 * 2;
			TextView resultTV[] = new TextView[dLength];
			String formatData[] = new String[dLength];
			for (int i = 0, k = 0, j = resultData.length; i < j; i++) {
				if ((i + 1) % 3 != 0) {
					if ((i + 2) % 3 == 0) {
						formatData[k] = resultData[i] + resultData[i + 1];
					} else {
						formatData[k] = resultData[i];
					}
					k++;
				}
			}
			resultTV = staffView(resultTV, formatData, "fonts/oldengl.ttf",
					30f, Color.BLACK);

			RelativeLayout[] rl = new RelativeLayout[dLength / 2];
			rl = layoutFactory(rl, resultTV);

			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, 60);

			LinearLayout mWorld = (LinearLayout) findViewById(R.id.resultBox);

			for (int i = 0, j = rl.length; i < j; i++) {
				mWorld.addView(rl[i], param);
			}

			animToolBox ab = new animToolBox(480f, 0, 0, 0);
			long delate = 0;
			long last = animTime;
			ab.setTime(last);
			for (int i = 0, j = rl.length; i < j; i++) {
				ab.animMove(rl[i]);
				ab.setDelate(delate += last / 3);
			}
//			animToolBox ab_2 = new animToolBox(0, 0, 800, 0);
//			ab_2.setTime(last);
//			ab_2.setDelate(delate);
//			LinearLayout endBox = (LinearLayout) findViewById(R.id.endBox);
//			ab_2.animMove(endBox);
			moveBox am = new moveBox(0,0,800f,0);
			Animation anim = null;
			am.start(tv[0], anim);

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
		Td_saveResult.start();

	}

	private Thread Td_saveResult = new Thread(new Runnable() {
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

	// private void anim(View v, long delate, long offset, boolean fill,
	// PointF sP, PointF eP) {
	// Animation a = null;
	// a = new TranslateAnimation(sP.x, eP.x, sP.y, eP.y);
	// a.setInterpolator(new AccelerateDecelerateInterpolator());
	// a.setDuration(2000);
	// a.setFillAfter(true);
	// a.setStartOffset(0);
	// a.setAnimationListener(new AnimationListener() {
	//
	// @Override
	// public void onAnimationEnd(Animation animation) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animation animation) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onAnimationStart(Animation animation) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// });
	// v.startAnimation(a);
	// }

	/**
	 * Pre: TextView and attributes. Pos: TextView with edited attributes.
	 * 
	 * @param tv
	 *            TextView array.
	 * @param text
	 *            String array.
	 * @param ttf
	 *            text font
	 * @param size
	 *            font size
	 * @param color
	 *            font color.
	 * @return TextView with edited attributes.
	 */
	private TextView[] staffView(TextView[] tv, String[] text, String ttf,
			float size, int color) {
		if (tv.length != text.length)
			return null;
		for (int i = 0, j = tv.length; i < j; i++) {
			tv[i] = new TextView(this);
			Typeface tf = Typeface.createFromAsset(getAssets(), ttf);
			tv[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			tv[i].setTypeface(tf);
			tv[i].setTextColor(color);
			tv[i].setText(text[i]);
		}
		return tv;
	}

	private RelativeLayout[] layoutFactory(RelativeLayout[] rl, TextView[] tv) {
		int j = rl.length;
		if (j + j != tv.length)
			return null;
		for (int i = 0; i < j; i++) {
			// Add left TV
			rl[i] = new RelativeLayout(this);
			rl[i].setBackgroundDrawable(getResources().getDrawable(
					R.drawable.result_staff));
			rl[i].setPadding(25, 0, 0, 0);
			RelativeLayout.LayoutParams ParamL = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			ParamL.addRule(RelativeLayout.CENTER_VERTICAL);
			ParamL.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			rl[i].addView(tv[i * 2], ParamL);

			// Add right TV
			RelativeLayout.LayoutParams ParamR = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			ParamR.addRule(RelativeLayout.CENTER_VERTICAL);
			ParamR.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rl[i].addView(tv[i * 2 + 1], ParamR);
		}
		return rl;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ToMain) {
			Result.this.finish();
		} else {
			overridePendingTransition(R.anim.hold, R.anim.fade);
			startActivity(new Intent(this, sBoard.class).putExtra("UID", "-1")
					.putExtra("mode", "0"));
			Result.this.finish();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		((TextView) v).setTextColor(Color.GRAY);
		return false;
	}

	private SharedPreferences prefs = null;
	private String playerName, cDate;
	private String[] resultData = null;
	private char mode;
	private ArrayList<ArrayList<String[]>> dataStore = new ArrayList<ArrayList<String[]>>();
	private int mYear, mMonth, mDay, mHour, mMinute, uid;
	private TextView tv[] = new TextView[3];

}
