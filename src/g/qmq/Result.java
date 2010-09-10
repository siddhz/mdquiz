package g.qmq;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class Result extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.result);

		prefs = getSharedPreferences("g.qmq_preferences", 0);
		playerName = prefs.getString("playerName", "PlayerRock");
		Bundle bunde = this.getIntent().getExtras();
		resultData = bunde.getStringArray("resultData");

//		mode = resultData[0].charAt(1);
		mSQLiteDB = this.openOrCreateDatabase("resultDB", MODE_PRIVATE, null);
		try{
			mSQLiteDB.execSQL(CREATE_TIME_TABLE);
		}catch(Exception e){
			Log.v("DB", "Database already exist.");
		}
	}

	private SharedPreferences prefs = null;
	private String playerName;
	private String[] resultData;
	private char mode;
	private SQLiteDatabase mSQLiteDB;
	
	private final static  String CREATE_TIME_TABLE = "CREATE TABLE time (" +
			"_uid INTEGER PRIMARY KEY," +
			"Date DATE," +
			"PlayerName TEXT," +
			"Time INTEGER," +
			"Total INTEGER," +
			"Correct INTEGER," +
			"Incorrect INTEGER)";
}
