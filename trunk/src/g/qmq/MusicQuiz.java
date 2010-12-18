package g.qmq;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;

import com.admob.android.ads.AdManager;

public class MusicQuiz extends Activity implements OnClickListener,
		OnTouchListener {
	private SharedPreferences prefs = null;
	private Button btnNew, btnSet, btnBoard, btnExit;
	private View tv;
	private boolean showAnim;
	private boolean forceEN;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.	
		
		/*
		 * Country and Language List.
		 * http://developer.android.com/reference/java/util/Locale.html
		 */
		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/oldengl.ttf");
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		forceEN = prefs.getBoolean("force_en", false);
		if (forceEN) {
			String languageToLoad = "en";
			Locale locale = new Locale(languageToLoad);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config,
					getBaseContext().getResources().getDisplayMetrics());
		}
		setContentView(R.layout.main);

		showAnim = prefs.getBoolean("anim", true);
		if (!prefs.getBoolean("iniData", false))
			iniSystem();

		// Ad Testing
		AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });

		// Set OnClick Listener.
		btnNew = (Button) findViewById(R.id.btnNew);
		btnNew.setOnClickListener(this);
		btnNew.setOnTouchListener(this);
		btnNew.setTypeface(tf);

		btnSet = (Button) findViewById(R.id.btnSettings);
		btnSet.setOnClickListener(this);
		btnSet.setOnTouchListener(this);
		btnSet.setTypeface(tf);

		btnBoard = (Button) findViewById(R.id.btnBoard);
		btnBoard.setOnClickListener(this);
		btnBoard.setOnTouchListener(this);
		btnBoard.setTypeface(tf);

		btnExit = (Button) findViewById(R.id.btnExit);
		btnExit.setOnClickListener(this);
		btnExit.setOnTouchListener(this);
		btnExit.setTypeface(tf);

		tv = findViewById(R.id.side_note);
		tv.setVisibility(View.INVISIBLE);
		// Start-up animations
		if (showAnim)
			animCtrl(false);
	}

	private void animCtrl(boolean out) {
		animToolBox anim = new animToolBox('l', out);
		anim.setTime(1500);
		long delate = 0;
		anim.animMove(btnNew);
		anim.setDelate(delate += 150);
		anim.animMove(btnSet);
		anim.setDelate(delate += 150);
		anim.animMove(btnBoard);
		anim.setDelate(delate += 150);
		anim.animMove(btnExit);
	}

	private void iniSystem() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setIcon(R.drawable.icon_common);
		alertDialogBuilder.setMessage(R.string.agreement);
		alertDialogBuilder.setTitle("Welcome to MDroid"); //TODO MOVE TO XML
		alertDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						prefs = getSharedPreferences("g.qmq_preferences", 0);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("iniData", true);
						editor.commit();
						iniFolderSelect();
					}
				});
		alertDialogBuilder.setCancelable(false);
		final AlertDialog startDialog = alertDialogBuilder.create();
		startDialog.show();
	}

	
	private void iniFolderSelect(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setIcon(R.drawable.icon_common);		
		alertDialogBuilder.setTitle("Welcome to MDroid"); //TODO MOVE TO XML
		alertDialogBuilder.setMessage("To get started, you need to select your music folder."); //TODO MOVE TO XML
		alertDialogBuilder.setPositiveButton("Continue",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						startActivity(new Intent(MusicQuiz.this, DirBrowser.class));
					}
				});
		alertDialogBuilder.setCancelable(false);
		final AlertDialog startDialog = alertDialogBuilder.create();
		startDialog.show();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnNew:
			openNewQuizDialog();
			break;
		case R.id.btnSettings:
			overridePendingTransition(R.anim.hold, R.anim.fade);
			startActivity(new Intent(this, Prefs.class));
			break;
		case R.id.btnBoard:
			overridePendingTransition(R.anim.hold, R.anim.fade);
			startActivity(new Intent(this, sBoard.class).putExtra("UID", -1)
					.putExtra("mode", 0));
			break;
		case R.id.btnExit:
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		}
	}

	private void openNewQuizDialog() {
//		new AlertDialog.Builder(this)
//				.setTitle(R.string.mode_title)
//				.setItems(R.array.quizMode,
//						new DialogInterface.OnClickListener() {
//							public void onClick(
//									DialogInterface dialoginterface, int i) {
//								startAct(i);
//							}
		// }).setCancelable(true).show();
		overridePendingTransition(R.anim.zoom_enter, R.anim.fade);
		startActivity(new Intent(this, ModeSelection.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
			// More items go here (if any) ...
		case R.id.about:
			startActivity(new Intent(this, About.class));
			return true;
		}
		return false;
	}

	// Click effect
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			v.setBackgroundResource(R.drawable.btn_main_hrev_c);
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			v.setBackgroundResource(R.drawable.btn_main_hrev);
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
		// android.os.Process.killProcess(android.os.Process.myPid());
	}
}