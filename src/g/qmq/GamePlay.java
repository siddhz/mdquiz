package g.qmq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GamePlay extends Activity implements OnTouchListener,
		OnClickListener {
	private final static int MAX_ERROR = 10; //Number of errors can occur before stop.
	private final static char MODE_CODE_TIME = 'T'; //Timed mode.

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.

		// TODO Some kind of switch form intent to choose XML file for the each
		// mode.

		setContentView(R.layout.time_mode);

		// Loading presences.
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		subFolder = prefs.getBoolean("music_searchSubFolder", true);
		gLength = Integer.parseInt(prefs.getString("prefs_length", "10"));
		repeat = prefs.getBoolean("repeat", false);
		id3tag = prefs.getBoolean("id3tag", false);
		isAnim = prefs.getBoolean("anim", true);

		// Get screen size
		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screenWidth = displayMetrics.widthPixels;
		screenHeight = displayMetrics.heightPixels;

		btn[0] = (Button) findViewById(R.id.btn_answer1);
		btn[1] = (Button) findViewById(R.id.btn_answer2);
		btn[2] = (Button) findViewById(R.id.btn_answer3);
		btn[3] = (Button) findViewById(R.id.btn_answer4);
		// Setup on click.
		btn[0].setOnClickListener(this);
		btn[1].setOnClickListener(this);
		btn[2].setOnClickListener(this);
		btn[3].setOnClickListener(this);

		btn[0].setOnTouchListener(this);
		btn[1].setOnTouchListener(this);
		btn[2].setOnTouchListener(this);
		btn[3].setOnTouchListener(this);

		initThread.start();
	}

	private void readyStage() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		// Pregame check
		if (mFiles.size() - 4 > gLength) {
			dialog.setIcon(R.drawable.icon_common);
			dialog.setTitle("Let's Rock."); // TODO move to XML
			dialog.setMessage(R.string.tMode_startMsg);
			dialog.setPositiveButton(R.string.btn_continue,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							questionNum = 0;
							played = new int[mFiles.size()];

							// Start time counting.
							timeSwitch = false;
							timer = new Timer(true);
							timer.schedule(task, 1000, 100);

							questionGiver();
						}

					});
		} else {
			dialog.setIcon(R.drawable.icon_question);
			dialog.setTitle("Warning!"); // TODO move to XML
			dialog.setMessage(R.string.warn_notEnoughSound);
			dialog.setPositiveButton(R.string.btn_continue,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							questionNum = 0;
							played = new int[mFiles.size()];

							// Start time counting.
							timeSwitch = false;
							timer = new Timer(true);
							timer.schedule(task, 1000, 100);

							questionGiver();
						}

					});
			dialog.setNegativeButton(R.string.btn_quit,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							GamePlay.this.finish();
						}

					});
		}
		dialog.show();
	}

	// Core function.
	private void questionGiver() {
		// Check possibility of giving another question.
		if ((questionNum >= mFiles.size() && !repeat) || mFiles.size() < 1) {
			endGame(false, "No more question can be generate.");
			return;
		}

		int qCan;
		do {
			qCan = rng.nextInt(mFiles.size());
		} while (isRepeat(qCan) && !repeat);
		played[questionNum] = qCan;

		int theOne = qCan, to = mFiles.size();
		int[] c = new int[] { -1, -1, -1 };

		do {
			c[0] = rng.nextInt(to);
		} while (c[0] == theOne);
		do {
			c[1] = rng.nextInt(to);
		} while (c[1] == theOne || c[1] == c[0]);
		do {
			c[2] = rng.nextInt(to);
		} while (c[2] == theOne || c[2] == c[0] || c[2] == c[1]);
		// Assign right answer randomly to a button.
		btnRight = rng.nextInt(3);
		btn[btnRight].setText((String) mFiles.get(theOne).get("mFile_name"));
		// Assign other choice in order.
		int j = 0;
		for (int i = 0, l = btn.length; i < l; i++) {
			if (i != btnRight)
				btn[i].setText((String) mFiles.get(c[j++]).get("mFile_name"));
		}

		// Prepare Music.
		String scr = (String) mFiles.get(qCan).get("mFile_path");
		if (!setMusic(true, scr, -1)) {
			handler.sendEmptyMessage(3);
			return;
		}

		// Update Question number.
		questionNum++;
		TextView tv = (TextView) findViewById(R.id.status_probNum);
		tv.setText(questionNum + "/" + gLength);
		handler.sendEmptyMessage(2);
	}

	private boolean isRepeat(int value) {
		if (played.length < 1)
			return false;
		for (int i = 0, l = played.length; i < l; i++) {
			if (played[i] == value) {
				return true;
			}
		}
		return false;
	}

	private void answer(int i) {
		Log.i("Answer", i + " received.");
		switch (mode) {
		case 0:
			timeModeCore(i);
			break;
		case 1:
			break;
		}
	}

	public boolean setMusic(boolean isLoop, String src, int start) {
		try {
			mp.reset();
			mp.setDataSource(src);
			mp.prepare();
			if (start < 0) {
				int mLength = mp.getDuration();
				if (mLength < 1) {
					start = 0;
				} else {
					Random rng = new Random();
					start = (int) ((mLength / 2) + (Math
							.pow(-1, rng.nextInt(1)) * (mLength / 10)));
				}
			}
			mp.seekTo(start);
			mp.setLooping(isLoop);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			if (mp.isPlaying())
				mp.stop();
			mp.reset();
			return false;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			if (mp.isPlaying())
				mp.stop();
			mp.reset();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			if (mp.isPlaying())
				mp.stop();
			mp.reset();
			return false;
		}
		return true;
	}

	private void moveAnim(final char direction) {
		if (!isAnim) {
			questionGiver();
			return;
		}
		if (mp.isPlaying() && direction == 'o')
			mp.stop();
		AnimationListener animListener = new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation a) {
				if (direction == 'o') {
					questionGiver(); // Next question.
					timeSwitch = false;
				}
				if (direction == 'i') {
					timeSwitch = true;
					mp.start();
				}
			}

			@Override
			public void onAnimationRepeat(Animation a) {
			}

			@Override
			public void onAnimationStart(Animation a) {
			}

		};
		Animation anim = null;
		Animation anim2 = null;
		if (direction == 'o') {
			anim = new TranslateAnimation(0, screenWidth, 0, 0);
			anim2 = new TranslateAnimation(0, -screenWidth, 0, 0);
		} else {
			anim = new TranslateAnimation(screenWidth, 0, 0, 0);
			anim2 = new TranslateAnimation(-screenWidth, 0, 0, 0);
		}
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setDuration(800);
		anim.setFillAfter(true);
		anim2.setInterpolator(new AccelerateDecelerateInterpolator());
		anim2.setDuration(800);
		anim2.setFillAfter(true);
		btn[0].startAnimation(anim);
		btn[1].startAnimation(anim2);
		btn[2].startAnimation(anim);
		anim2.setAnimationListener(animListener);
		btn[3].startAnimation(anim2);

	}

	public void endGame(final boolean isSuccess, String msg) {
		timeSwitch = false;
		if (mp.isPlaying())
			mp.stop();
		String title;
		if (isSuccess) {
			title = "Quiz Complete!";
		} else {
			title = "A Problem Has Occur!"; // TODO Move this string to
			// string.xml
		}
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setIcon(R.drawable.icon_problem);
		alertDialogBuilder.setMessage(msg);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						if (isSuccess) {
							Intent i = new Intent(GamePlay.this,
									Result.class);
							Bundle bundle = new Bundle();
							switch(mode){
							case MODE_CODE_TIME:
								timeBundle(bundle);
								break;
							}
//							bundle.putDouble("time", timePass / 10.0);
//							// bundle.putInt("acc_r", acc_r);
//							// bundle.putInt("acc_w", acc_w);
//							// bundle.putInt("hStack", hStack);
//							bundle.putInt("mode", mode);
//							bundle.putInt("gLength", gLength);
							i.putExtras(bundle);
							startActivity(i);
							// Close current activity.
							GamePlay.this.finish();
						} else {
							startActivity(new Intent(GamePlay.this,
									MusicQuiz.class));
							GamePlay.this.finish();
						}
					}
				});
		final AlertDialog startDialog = alertDialogBuilder.create();
		startDialog.show();
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			v.setBackgroundResource(R.drawable.btnbg_c);
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			v.setBackgroundResource(R.drawable.btnbg);
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_answer1:
			answer(0);
			break;
		case R.id.btn_answer2:
			answer(1);
			break;
		case R.id.btn_answer3:
			answer(2);
			break;
		case R.id.btn_answer4:
			answer(3);
			break;
		}
	}

	public void onPause() {
		super.onPause();
		timeSwitch = false;
		mp.reset();
	}

	/*
	 * Thread Block *************
	 */
	private Thread initThread = new Thread(new Runnable() {
		@Override
		public void run() {
			iniProgressBar = (ProgressBar) findViewById(R.id.time_pb);
			iniTV = (TextView) findViewById(R.id.time_tvWait);
			comFun cFun = new comFun();
			InputStream inputS = null;
			try {
				inputS = GamePlay.this.openFileInput("songlist.xml");
				if (!cFun.getAllMusicFiles(inputS, subFolder, mFiles, id3tag)) {
					handler.sendEmptyMessage(9);
					return;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			// Initialize played list.
			played = new int[mFiles.size()];
			handler.sendEmptyMessage(9);
		}
	});

	// Timer
	TimerTask task = new TimerTask() {
		public void run() {
			handler.sendEmptyMessage(1);
		}
	};
	/*
	 * Thread Block *************
	 */

	/*
	 * Handler Block *************
	 */
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: // Timer Actions
				if (timeSwitch) {
					timePass++;
					TextView timerV = (TextView) findViewById(R.id.status_time);
					timerV.setText(((double) timePass / 10) + "s");
				}
				break;
			case 2: // Move on to next question.
				// Clear up.
				for (int k = 0, l = btn.length; k < l; k++) {
					btn[k].setVisibility(View.VISIBLE);
					btn[k].setClickable(true);
				}
				moveAnim('i');
				break;
			// Error Case
			case 3:// Error on giving question.
				errorCount++;
				if (errorCount >= MAX_ERROR) {
					// TODO move below string to XML
					endGame(false,
							"Music not found try refreash the music library in settings.");
				} else {
					questionGiver();
				}
				break;
			case 8:
				// TODO move below string to XML
				endGame(false, "Fail to fetch music file, "
						+ "please check your music folder and try again.");
				break;

			// Initialize game
			case 9:
				iniProgressBar.setVisibility(View.GONE);
				iniTV.setVisibility(View.GONE);
				readyStage();
				break;
			}
			super.handleMessage(msg);
		}
	};

	/*
	 * Handler Block *************
	 */

	/*
	 * Time Mode Core Function ******************
	 */
	private void timeModeCore(int a) {
		final int tPenalty = 50; // Penalty for guessing wrong (1 = 1/10
		// second)
		String tMsg = "!";

		if (a == btnRight) {
			timeSwitch = false;
			tMsg = "Correct!"; // TODO move to XML
			rCount++;
			stack++;
			if (stack > hStack)
				hStack = stack;
			if (stack >= 2)
				tMsg = "Correct!" + " x" + stack;
			moveAnim('o');
		} else {
			wCount++;
			stack = 0;
			tMsg = "Wrong!"; // TODO move to XML
			animToolBox sAnim = new animToolBox(animToolBox.ZOOM_IN_CENTER);
			sAnim.animScale(btn[a]);
			timePass += tPenalty;
		}
		Toast.makeText(this, tMsg, 0).show();
	}
	
	private void timeBundle(Bundle b){
		String acc = String.valueOf(Math.round(rCount/(rCount+wCount)*100));
		//First place must be mode code;
		String[] resultData = new String[]{
				String.valueOf(MODE_CODE_TIME),
				"Total Time",String.valueOf(timePass),
				"Total Questions",String.valueOf(questionNum),
				"Correct Answers",String.valueOf(rCount),
				"Incorrect Answers",String.valueOf(wCount),
				"Accuracy",acc
				};
		b.putStringArray("resultData", resultData);
	}

	/*
	 * Time Mode Core Function ******************
	 */

	/*
	 * TESTING
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		switch(keyCode){
		case KeyEvent.KEYCODE_0:
			Intent i = new Intent(GamePlay.this,
					Result.class);
			Random rd = new Random();
			Bundle bundle = new Bundle();
			String[] test = new String[]{
					String.valueOf(MODE_CODE_TIME),
					"Time", String.valueOf(rd.nextInt(100)),"S",
					"Total","100", "",
					"Corrects","5", "",
					"Incorrects","5", ""
			};
			bundle.putStringArray("resultData", test);
			i.putExtra("resultData", bundle);
			startActivity(i);
			break;
		}
	    return false;
	}
	/*
	 * TESTING ***
	 */
	
	/* Common fields */
	private int screenWidth = 320, screenHeight = 480, gLength, timePass = 0,
			mode, questionNum = 0, btnRight, errorCount = 0, rCount, wCount;
	private boolean subFolder, repeat, id3tag, timeSwitch, isAnim;
	private SharedPreferences prefs = null;
	private ProgressBar iniProgressBar;
	private TextView iniTV;
	private Button btn[] = new Button[4];
	private ArrayList<HashMap<String, Object>> mFiles = new ArrayList<HashMap<String, Object>>();
	private int played[];
	private MediaPlayer mp = new MediaPlayer();
	private Random rng = new Random();
	private Timer timer;

	/* Time fields */
	private int stack, hStack;
}
