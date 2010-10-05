package g.qmq;

import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class libBuilder extends Activity {
	private Thread xmlThd = new Thread(new Runnable() {
		@Override
		public void run() {
			songListXML();
		}

	});
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // Fail
				tv1.setText(R.string.lib_problem);
				Toast.makeText(libBuilder.this, errMsg, Toast.LENGTH_LONG)
						.show();
				tv2.setText(errMsg);
				break;
			case 1:
				// String strID3 = (id3tag)?"On":"Off";
				tv1.setText(R.string.lib_success);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("music_lib", "Total: " + total + "  "
						+ "Date: " + date);
				editor.commit();
				Toast.makeText(libBuilder.this, R.string.lib_success,
						Toast.LENGTH_LONG).show();
				finish();
				break;
			case 2:
				tv1.setText("Building Library");
				tv2.setText(now + "/" + total);
				break;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.lib_builder);

		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);

		xmlThd.start();
	}

	public void songListXML() {
		String xmlstr, path = "songlist.xml";
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		// id3tag = prefs.getBoolean("id3tag", false);
		comFun cFun = new comFun();
		cFun.getAllMusicFiles(prefs.getString("music_dir", "/sdcard/"),
				prefs.getBoolean("music_searchSubFolder", true), data);

		/*
		 * Create XML content as string.
		 */
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			// <?xml version=¡±1.0¡å encoding=¡±UTF-8¡å standalone=¡±yes¡±?>
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "list");

			SimpleDateFormat sDateFormat = new SimpleDateFormat(
					"MM-dd-yyyy hh:mm:ss");
			date = sDateFormat.format(new java.util.Date());
			total = data.size();

			serializer.attribute("", "total", String.valueOf(total));
			serializer.attribute("", "date", date);
			// serializer.attribute("", "id3", String.valueOf(id3tag));
			serializer.attribute("", "id3", "true");
			for (int i = 0; i < total; i++) {
				serializer.startTag("", "song");
				serializer.attribute("", "name",
						(String) data.get(i).get("mFile_name"));
				dir = (String) data.get(i).get("mFile_path");

				// if (id3tag) {
				AudioFile af = AudioFileIO.read(new File(dir));
				String title = af.getTag().getTitle().toString();
				String artist = af.getTag().getArtist().toString();
				serializer.attribute("", "id3_title",
						title.substring(1, title.length() - 1));
				serializer.attribute("", "id3_artist",
						artist.substring(1, artist.length() - 1));
				// }

				serializer.text(dir);
				serializer.endTag("", "song");
				now = i + 1;
				handler.sendEmptyMessage(2);
			}
			serializer.endTag("", "list");
			serializer.endDocument();
			xmlstr = writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("XML_CREATE", "Fail to create xml.");
			handler.sendEmptyMessage(0);
			return;
		}

		/*
		 * Wring XML string to file.
		 */
		try {
			OutputStream writeOS = openFileOutput(path, MODE_PRIVATE);
			OutputStreamWriter xmlWriter = new OutputStreamWriter(writeOS);
			xmlWriter.write(xmlstr);
			xmlWriter.close();
			writer.close();
		} catch (FileNotFoundException e) {
			Log.e("XML_WRITE", "Fail to write xml file not found.");
			handler.sendEmptyMessage(0);
			return;
		} catch (IOException e) {
			Log.e("XML_WRITE", "Unable to write xml.");
			handler.sendEmptyMessage(0);
			return;
		}
		handler.sendEmptyMessage(1);
		return;
	}

	public void onPause() {
		super.onPause();
		// TODO Move string to xml
		xmlThd.stop();
		Toast.makeText(libBuilder.this, "Canceled", Toast.LENGTH_LONG).show();
		libBuilder.this.finish();
	}

	SharedPreferences prefs = null;
	private TextView tv1, tv2;
	private String errMsg, date, dir;
	private int total, now;
	// private boolean id3tag;
}
