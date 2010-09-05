package g.qmq;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import com.admob.android.ads.AdManager;


public class MusicQuiz extends Activity implements OnClickListener,
		OnTouchListener {
	private SharedPreferences prefs = null;
	private View btnNew, btnSet, btnBoard, btnExit, tv;
	private boolean showAnim;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.

		/*
		 * Country and Language List.
		 * http://developer.android.com/reference/java/util/Locale.html
		 */
		String languageToLoad = "en";
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;		
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.main);
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		showAnim = prefs.getBoolean("anim", true);
		if (!prefs.getBoolean("iniData", false))
			iniSystem();

		// Ad Testing
		AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });

		// Set OnClick Listener.
		btnNew = findViewById(R.id.btnNew);
		btnNew.setOnClickListener(this);
		btnNew.setOnTouchListener(this);

		btnSet = findViewById(R.id.btnSettings);
		btnSet.setOnClickListener(this);
		btnSet.setOnTouchListener(this);

		btnBoard = findViewById(R.id.btnBoard);
		btnBoard.setOnClickListener(this);
		btnBoard.setOnTouchListener(this);

		btnExit = findViewById(R.id.btnExit);
		btnExit.setOnClickListener(this);
		btnExit.setOnTouchListener(this);

		tv = findViewById(R.id.side_note);
		tv.setVisibility(View.INVISIBLE);
		// Start-up animations
		if(showAnim) animCtrl(false);
	}

	private void animCtrl(boolean out) {
		// animToolBox sAnim = new animToolBox(0,1,0,1);
		// sAnim.setScaleMode(Animation.RELATIVE_TO_SELF);
		// sAnim.setDelate(2000);
		// sAnim.setTime(2000);
		// sAnim.setS(0.5f, 0.5f);
		// sAnim.animScale(tv);

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
		alertDialogBuilder.setTitle("Welcome to MDroid");
		alertDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						prefs = getSharedPreferences("g.qmq_preferences", 0);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("iniData", true);
						editor.commit();
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
			startActivity(new Intent(this, Prefs.class));
			overridePendingTransition(R.anim.hold,R.anim.fade);
			break;
		case R.id.btnBoard:
			startActivity(new Intent(this, sBoard.class).putExtra("UID", "-1")
					.putExtra("mode", "0"));
			overridePendingTransition(R.anim.hold,R.anim.fade);
			break;
		case R.id.btnExit:
			this.finish();
			break;
		}
	}

	private void openNewQuizDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.mode_title).setItems(
				R.array.quizMode, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						startAct(i);
					}
				}).setCancelable(true).show();
	}

	// Game entry
	protected void startAct(int i) {
		switch (i) {
		case 0:
			startActivity(new Intent(this, ModeTime.class));
			overridePendingTransition(R.anim.zoom_enter, R.anim.hold);
			break;
		case 1:
			startActivity(new Intent(this, ModeMem.class));
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		case 2:
			startActivity(new Intent(this, ModeSD.class));
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		case 3:
			startActivity(new Intent(this, ModeOL.class));
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		case 4:
			startActivity(new Intent(this, GamePlay.class));
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		}
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
		case R.id.menu_view:
			final String url = (String) this.getBaseContext().getResources()
					.getText(R.string.vListUrl);
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			return true;
		}
		return false;
	}

	// Click effect
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			v.setBackgroundResource(R.drawable.btn_main_hrev_c); // 按下的图片对应pressed
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			v.setBackgroundResource(R.drawable.btn_main_hrev); // 常态下的图片对应normal
		}
		return false;
	}
}