package g.qmq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DirBrowser extends Activity implements OnClickListener {
	private String currentDir = "/sdcard/";
	private SharedPreferences prefs = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.file_layout);
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		currentDir = prefs.getString("music_dir", "/sdcard/");
		fill(new File(currentDir).listFiles());
		
		View btnSelectDir = findViewById(R.id.btnSelectDir);
		btnSelectDir.setOnClickListener(this);
	}

	private ArrayList<HashMap<String, Object>> users = new ArrayList<HashMap<String, Object>>();

	private void fill(File[] files) {
		users.clear();
		TextView dirTextView = (TextView) findViewById(R.id.file_folderName);
		dirTextView.setText(currentDir);
		if(!currentDir.contentEquals("/")){
			HashMap<String, Object> user = new HashMap<String, Object>();
			user.put("img", R.drawable.foler);
			user.put("folder", "...");
			users.add(user);
		}
		for (File file : files) {
			String[] st = splitString(file.getPath(),"/");
			String isfolder = "";
			int icon = R.drawable.foler;
			if(file.isDirectory()){
				isfolder = "/";
				HashMap<String, Object> user = new HashMap<String, Object>();
				user.put("img", icon);
				user.put("folder", st[st.length-1]+isfolder);
				users.add(user);	
			}
//			if(isMusicFile(file.getPath())){
//				icon = R.drawable.music_file;
//			}else if(isfolder == ""){
//				icon = R.drawable.other_file;
//			}

		}

		SimpleAdapter saImageItems = new SimpleAdapter(this, users,// 数据来源
				R.layout.file_view,// 每一个user xml 相当ListView的一个组件
				new String[] {"img", "folder"},
				// 分别对应view 的id
				new int[] { R.id.img , R.id.file_name});
		// 获取listview
		((ListView) findViewById(R.id.users)).setAdapter(saImageItems);
		((ListView) findViewById(R.id.users))
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						String selectDir = currentDir + (String) users.get(arg2).get("folder");
						File file = new File(selectDir);
						if(selectDir.endsWith("/...")){
							selectDir = file.getParent();
							File file1 = new File(selectDir);
							selectDir = file1.getParent();
							if(!selectDir.endsWith("/")) selectDir = selectDir + "/";
						}
						if(file.isDirectory() || selectDir.endsWith("/")){
							currentDir = selectDir ;
							fill(new File(currentDir).listFiles());
							Log.d("curDir", currentDir);
						}
					}

				});
	}
	
//	private boolean isMusicFile(String path) {
//		if(path.endsWith(".mp3") || path.endsWith(".wmv") || path.endsWith(".wav"))
//			return true;
//		return false;
//	}
	
	public String[] splitString(String str, String sdelimiter) {
		String[] array = str.split(sdelimiter);
		return array;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnSelectDir:
			prefs = getSharedPreferences("g.qmq_preferences", 0);
			SharedPreferences.Editor editor = prefs.edit();	
			editor.putString("music_dir", currentDir);
			editor.commit();
			Log.d("Prefs", prefs.getString("music_dir", "None"));
			finish();
			break;
		}
		
	}
}