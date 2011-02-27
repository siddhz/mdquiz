package g.qmq;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.admob.android.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GamePlay_Flash extends Activity implements OnClickListener {

	/* ***********************
	 * General constants
	 */
	private final static int MAX_ERROR = 10;// Number of errors can occur before
	// stop.
	private final static int GLENGTH = 10;// Game Length
	private final int SOUND_RIGHT = 1;
	private final int SOUND_WRONG = 2;
	private final int GAME_LENGTH = 10;
	private final int MAX_MISS = 10;
	private final char MODE_CODE = 'F';

	/*
	 * General constants**********************
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.
		setContentView(R.layout.mode_flash);

		/* ***************
		 * Loading presences.
		 */
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		subFolder = prefs.getBoolean("music_searchSubFolder", true);
		repeat = prefs.getBoolean("repeat", false);
		id3tag = prefs.getBoolean("id3tag", false);
		isAnim = prefs.getBoolean("anim", true);
		/*
		 * Loading presences.**************
		 */
		// Get screen sizes
		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screenWidth = displayMetrics.widthPixels;
		screenHeight = displayMetrics.heightPixels;

		btn = (Button) findViewById(R.id.btn_answer1);
		btn.setOnClickListener(this);

		sp = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		spMap = new HashMap<Integer, Integer>();
		spMap.put(SOUND_RIGHT, sp.load(this, R.raw.sound_right, 1));
		spMap.put(SOUND_WRONG, sp.load(this, R.raw.sound_wrong, 1));

		initThread.start();
		addView = (AdView) findViewById(R.id.gameplay_ad);
	}

	@Override
	public void onClick(View v) {
		AudioManager mgr = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = (float) (streamVolumeCurrent / streamVolumeMax * 1.5);
		String tMsg = "!";

		if (currentIndex == rightIndex) {
			timeSwitch = false;
			sp.play(spMap.get(SOUND_RIGHT), volume, volume, 1, 0, 1f);
			tMsg = "Correct!"; // TODO move to XML
			hit++;
			stack++;
			if (stack > hStack)
				hStack = stack;
			if (stack >= 2)
				tMsg = "Correct!" + " x" + stack;
			handler.sendEmptyMessage(0);
			// moveAnim('o');
			// if (questionNum + 1 >= gLength) {
			// endGame(true, "Timed Quiz Complete!");
			// }
		} else {
			sp.play(spMap.get(SOUND_WRONG), volume, volume, 1, 0, 1f);
			miss++;
			stack = 0;
			Animation timeAnim = AnimationUtils.loadAnimation(this,
					R.anim.time_add);
			timeAnim.setFillAfter(true);
			TextView tTV = (TextView) findViewById(R.id.time_pluse);
			tTV.setVisibility(View.VISIBLE);
			tTV.startAnimation(timeAnim);
			tMsg = "Wrong!"; // TODO move to XML
			animToolBox sAnim = new animToolBox(animToolBox.ZOOM_IN_CENTER);
			sAnim.animScale(btn);
			if (miss > MAX_MISS) {
				endGame(true, "GAME OVER");
				return;
			}
		}
		Toast.makeText(this, tMsg, 0).show();
	}

	/**
	 * Randomly pick number of questions from the pool
	 * 
	 * @param qPool
	 *            Pool of questions to pick from.
	 * @param qNum
	 *            Number of questions desired.
	 * @param repeat
	 *            False if repeat is NOT allowed.
	 * @param played
	 *            Exclude all indexes of qPool's boolean is true when repeat is
	 *            NOT allowed.
	 * @return ArrayList with HashMap contains name("name") and path("path").
	 */
	private ArrayList<HashMap<String, String>> questionMaker(
			final ArrayList<HashMap<String, String>> qPool, final int qNum,
			final boolean repeat, ArrayList<Integer> played) {
		int poolSize = qPool.size();
		// Pre-Checks
		if ((poolSize <= played.size() && !repeat) || poolSize < qNum) {
			return null;
		}
		ArrayList<HashMap<String, String>> qSet = new ArrayList<HashMap<String, String>>();
		ArrayList<Integer> candidatePool = new ArrayList<Integer>();
		// Random number generator
		Random rng = new Random();
		// //Try Picking the first candidate.
		// do{
		// //Pick a candidate from the pool using RNG.
		// candidate = rng.nextInt(poolSize);
		// }while(repeat && isRepeat(candidate, played));
		// //Put it at random spot
		for (int i = 0; i < qNum; i++) {
			int tempCan;
			do {
				tempCan = rng.nextInt(poolSize);
			} while ((repeat || isRepeat(tempCan, played))
					|| isRepeat(tempCan, candidatePool));
			candidatePool.add(tempCan);
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("name", qPool.get(tempCan).get("mFile_name"));
			hm.put("path", qPool.get(tempCan).get("mFile_path"));
			qSet.add(hm);
		}
		return qSet;
	}

	/**
	 * Function check if the value in already in.
	 * 
	 * @param v
	 *            value which use to compare.
	 * @param played
	 *            the list of all values.
	 * @return ture if a repeated value is found, false otherwise.
	 */
	private boolean isRepeat(int v, ArrayList<Integer> played) {
		if (played.size() <= 0) {
			return false;
		}
		for (int i = 0, l = played.size(); i < l; i++) {
			if (played.get(i) == v) {
				return true;
			}
		}
		return false;
	}

	/**
	 * At ready stage, file is checked to make sure question can be made.
	 */
	private void readyStage() {
		comFun cFun = new comFun();
		AlertDialog.Builder dialog = null;
		Resources res = this.getResources();
		// Pre-game check
		if (mFiles.size() > GAME_LENGTH * 4) {
			dialog = cFun
					.alertMaker(this, res.getString(R.string.tMode_startTitle),
							res.getString(R.string.tMode_startMsg),
							R.drawable.icon_common);
			dialog.setPositiveButton(R.string.btn_continue,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// Initialize
							questionNum = 0;
							played.clear();
							timeSwitch = false;
							timer = new Timer(true);
							timer.schedule(task, 1000, 100);

							handler.sendEmptyMessage(0);
						}

					});
		} else {
			dialog = cFun.alertMaker(this, "Warning!", res
					.getString(R.string.warn_notEnoughSound),
					R.drawable.icon_question);
			dialog.setPositiveButton(R.string.btn_continue,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							repeat = true;

							// Initialize
							questionNum = 0;
							played.clear();
							timeSwitch = false;
							timer = new Timer(true);
							timer.schedule(task, 1000, 100);

							handler.sendEmptyMessage(0);
						}

					});
			dialog.setNegativeButton(R.string.btn_quit,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							GamePlay_Flash.this.finish();
						}

					});
		}
		dialog.show();
	}

	protected void missAnswer() {
		AudioManager mgr = (AudioManager) GamePlay_Flash.this
				.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = (float) (streamVolumeCurrent / streamVolumeMax * 1.5);
		String tMsg = "!";
		sp.play(spMap.get(SOUND_WRONG), volume, volume, 1, 0, 1f);
		miss++;
		stack = 0;
		Animation timeAnim = AnimationUtils.loadAnimation(GamePlay_Flash.this,
				R.anim.time_add);
		timeAnim.setFillAfter(true);
		TextView tTV = (TextView) findViewById(R.id.time_pluse);
		tTV.setVisibility(View.VISIBLE);
		tTV.startAnimation(timeAnim);
		tMsg = "Missed"; // TODO move to XML
		animToolBox sAnim = new animToolBox(animToolBox.ZOOM_IN_CENTER);
		sAnim.animScale(btn);
		if (miss > MAX_MISS) {
			endGame(true, "GAME OVER");
			return;
		}
		Toast.makeText(GamePlay_Flash.this, tMsg, 0).show();
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
							Intent i = new Intent(GamePlay_Flash.this,
									Result.class);
							Bundle b = new Bundle();
							// First place must be mode code;
							String[] resultData = new String[] { "0",
									"Total Questions",
									String.valueOf(questionNum), "Q", "0",
									"Incorrect Answers", String.valueOf(hit),
									"Q", "0", "Accuracy", String.valueOf(acc),
									"%", "0", "Total Penalty",
									String.valueOf(2), "Sec", "1",
									"Total Time",
									String.valueOf(timePass / 10), "Sec" };
							b.putStringArray("resultData", resultData);
							b.putChar("MODE", MODE_CODE);
							// bundle.putDouble("time", timePass / 10.0);
							// // bundle.putInt("acc_r", acc_r);
							// // bundle.putInt("acc_w", acc_w);
							// // bundle.putInt("hStack", hStack);
							// bundle.putInt("mode", mode);
							// bundle.putInt("gLength", gLength);
							i.putExtras(b);
							startActivity(i);
							// Close current activity.
							GamePlay_Flash.this.finish();
						} else {
							startActivity(new Intent(GamePlay_Flash.this,
									MusicQuiz.class));
							GamePlay_Flash.this.finish();
						}
					}
				});
		final AlertDialog startDialog = alertDialogBuilder.create();
		startDialog.show();
	}

	private Animation moveAnim(final char direction, float fromX, float toX,
			float fromY, float toY, long dur) {
		if (mp.isPlaying() && direction == 'o')
			mp.stop();

		Animation anim = null;
		anim = new TranslateAnimation(fromX, toX, fromY, toY);
		anim.setDuration(dur);
		anim.setFillAfter(true);
		if (direction == 'o') {
			anim.setInterpolator(new AccelerateInterpolator());
		} else if (direction == 'i') {
			anim.setInterpolator(new AccelerateInterpolator());
		}
		return anim;
	}

	private AnimationListener animInListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation a) {
			Animation anim = moveAnim('o', 0, 0, 0, screenHeight, 1500);
			anim.setAnimationListener(animOutListener);
			btn.startAnimation(anim);
		}

		@Override
		public void onAnimationRepeat(Animation a) {
		}

		@Override
		public void onAnimationStart(Animation a) {
			timeSwitch = true;
			mp.start();
		}

	};

	private AnimationListener animOutListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation a) {
			timeSwitch = false;
			mp.stop();
			if (rightIndex == currentIndex) {// Missed Right Answer
				missAnswer();
			}
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onAnimationRepeat(Animation a) {
		}

		@Override
		public void onAnimationStart(Animation a) {
		}

	};

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
				inputS = GamePlay_Flash.this.openFileInput("songlist.xml");
				if (!cFun.getAllMusicFiles(inputS, subFolder, mFiles, id3tag)) {
					handler.sendEmptyMessage(9);
					return;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				handler.sendEmptyMessage(9);
				return;
			}

			// Initialize played list.
			played = new ArrayList<Integer>();
			handler.sendEmptyMessage(8);
		}
	});

	// Timer
	TimerTask task = new TimerTask() {
		public void run() {
			if (timeSwitch) {
				timePass++;
				TextView timerV = (TextView) findViewById(R.id.status_time);
				timerV.setText(((double) timePass / 10) + "s");
			}
		}
	};
	/*
	 * Thread Block *************END
	 */

	/*
	 * Handler Block *************
	 */
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // Looping thought set
				if (currentIndex >= 4 || !isStart) {// Missed all answers
					isStart = true;
					cSet = questionMaker(mFiles, 4, repeat, played);
					Random random = new Random();
					rightIndex = random.nextInt(cSet.size());
					currentIndex = -1;
				}
				currentIndex++;
				Animation anim = moveAnim('i', 0, 0, -screenHeight, 0, 1500);
				anim.setAnimationListener(animInListener);
				btn.startAnimation(anim);
				break;
			case 8:
				iniProgressBar.setVisibility(View.GONE);
				iniTV.setVisibility(View.GONE);
				readyStage();
				break;
			case 9: // Failed to load files
				// TODO
				break;
			case 99: // Timer Actions
				if (timeSwitch) {
					timePass++;
					TextView timerV = (TextView) findViewById(R.id.status_time);
					timerV.setText(((double) timePass / 10) + "s");
				}
				break;
			}

			super.handleMessage(msg);
		}
	};

	/*
	 * Handler Block ************* END
	 */

	private int screenWidth = 320, screenHeight = 480, timePass = 0,
			questionNum = 0, errorCount = 0, hit = 0, miss = 0;
	private boolean subFolder, repeat, id3tag, timeSwitch, isAnim, isStart = false;
	private SharedPreferences prefs = null;
	private ProgressBar iniProgressBar;
	private TextView iniTV;
	private Button btn;
	private ArrayList<HashMap<String, String>> mFiles = new ArrayList<HashMap<String, String>>();
	private ArrayList<Integer> played = null;
	private MediaPlayer mp = new MediaPlayer();
	private Random rng = new Random();
	private Timer timer;
	private char mode;
	private TextView tv_acc;
	private double acc;
	private ArrayList<HashMap<String, String>> cSet = new ArrayList<HashMap<String, String>>();
	private int currentIndex, rightIndex;

	private HashMap<Integer, Integer> spMap;
	private SoundPool sp;

	private AdView addView;

	/* Time fields */
	private int stack, hStack;
}
