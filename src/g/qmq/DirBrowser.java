package g.qmq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DirBrowser extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.file_layout);
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		currentDir = prefs.getString("music_dir", "/");
		try {
			fill(new File(currentDir).listFiles());
		} catch (Exception e) {
			Toast.makeText(this, "Current directory not found.", // TODO move to XML
					Toast.LENGTH_LONG).show(); 
			currentDir = "/";
			fill(new File(currentDir).listFiles());
		}

		View btnSelectDir = findViewById(R.id.btnSelectDir);
		btnSelectDir.setOnClickListener(this);
	}

	private ArrayList<HashMap<String, Object>> users = new ArrayList<HashMap<String, Object>>();

	private void fill(File[] files) {
//		musicUnderFolder = 0; //Reset counter.
		users.clear();
		TextView dirTextView = (TextView) findViewById(R.id.file_folderName);
		dirTextView.setText(currentDir);
		if (!currentDir.contentEquals("/")) {
			HashMap<String, Object> user = new HashMap<String, Object>();
			user.put("img", R.drawable.foler);
			user.put("folder", "...");
			users.add(user);
		}

		// for (File file : files) {
		// if (file.canRead() && !file.isHidden()) {
		// String[] st = splitString(file.getPath(), "/");
		// String isfolder = "";
		// int icon = R.drawable.foler;
		// if (file.isDirectory()) {
		// isfolder = "/";
		// HashMap<String, Object> user = new HashMap<String, Object>();
		// user.put("img", icon);
		// user.put("folder", st[st.length - 1] + isfolder);
		// users.add(user);
		// }
		// }

		for (File file : files) {
			if (file.canRead() && !file.isHidden()) {
				int icon = R.drawable.foler;
				if (file.isDirectory()) {
					HashMap<String, Object> user = new HashMap<String, Object>();
					user.put("img", icon);
					user.put("folder", file.getName() + "/");
					users.add(user);
				}
			}
//			if (isMusicFile(file.getName())) {
//				musicUnderFolder++;
//			}

		}
//		TextView musicCount = (TextView) findViewById(R.id.file_musicCount);
//		musicCount.setText("Music files under correct folder: "+musicUnderFolder); //TODO MOVE TO XML

		SimpleAdapter saImageItems = new SimpleAdapter(this, users,
				R.layout.file_view, new String[] { "img", "folder" },
				new int[] { R.id.img, R.id.file_name });
		((ListView) findViewById(R.id.users)).setAdapter(saImageItems);
		((ListView) findViewById(R.id.users))
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						String selectDir = currentDir
								+ (String) users.get(arg2).get("folder");
						File file = new File(selectDir);
						if (selectDir.endsWith("/...")) {
							selectDir = file.getParent();
							File file1 = new File(selectDir);
							selectDir = file1.getParent();
							if (!selectDir.endsWith("/"))
								selectDir = selectDir + "/";
						}
						if (file.isDirectory() || selectDir.endsWith("/")) {
							currentDir = selectDir;
							fill(new File(currentDir).listFiles());
							Log.v("curDir", currentDir);
						}
					}

				});
	}

//	private boolean isMusicFile(String name) {
//		if (name.endsWith(".mp3") || name.endsWith(".wmv")
//				|| name.endsWith(".wav"))
//			return true;
//		return false;
//	}

	public String[] splitString(String str, String sdelimiter) {
		String[] array = str.split(sdelimiter);
		return array;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSelectDir:
			prefs = getSharedPreferences("g.qmq_preferences", 0);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("music_dir", currentDir);
			editor.commit();
			Log.d("Prefs", prefs.getString("music_dir", "None"));
			finish();
			startActivity(new Intent(this, libBuilder.class));
			break;
		}
	}
	private String currentDir;
	private SharedPreferences prefs = null;
//	private int musicUnderFolder;
}