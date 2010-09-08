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

public class ModeTime extends Activity implements OnClickListener,
		OnTouchListener {
	private final int mode = 0;
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: // Timer Actions
				if (timeOn) {
					if (gamePause) {
						timer.cancel();
						endGame(false, "Game ended for unknow reason.");
						return;
					}
					time++;
					TextView timerV = (TextView) findViewById(R.id.status_time);
					timerV.setText(((double) time / 10) + "s");
				}
				break;
			case 2: // Animations
				// Clear off button vis and click.
				for (int k = 0, l = btn.length; k < l; k++) {
					btn[k].setVisibility(View.VISIBLE);
					btn[k].setClickable(true);
				}
				animOut(false);
				break;
			// Initialize game
			case 3:
				pb.setVisibility(View.GONE);
				wait.setVisibility(View.GONE);
				readyStage();
				break;
			case 4:
				endGame(false, "Fail to fetch music file, "
						+ "please check your music folder and try again.");
				break;
			// Initialize game
			}
			super.handleMessage(msg);
		}
	};
	TimerTask task = new TimerTask() {
		public void run() {
			handler.sendEmptyMessage(1);
		}
	};

	private Thread initThread = new Thread(new Runnable() {
		@Override
		public void run() {
			pb = (ProgressBar) findViewById(R.id.time_pb);
			wait = (TextView) findViewById(R.id.time_tvWait);
			comFun cFun = new comFun();
			InputStream is = null;
			try {
				is = ModeTime.this.openFileInput("songlist.xml");
				if (!cFun.getAllMusicFiles(is, subFolder, mFiles, id3tag)) {
					handler.sendEmptyMessage(4);
					return;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			// Initialize played list.
			played = new int[mFiles.size()];
			handler.sendEmptyMessage(3);
		}
	});

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.
		setContentView(R.layout.mode_time);
		// Load presences.
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		subFolder = prefs.getBoolean("music_searchSubFolder", true);
		gLength = Integer.parseInt(prefs.getString("prefs_length", "10"));
		repeat = prefs.getBoolean("repeat", false);
		id3tag = prefs.getBoolean("id3tag", false);
		
		DisplayMetrics displayMetrics = new DisplayMetrics();  
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);    
        screenWidth = displayMetrics.widthPixels;  
		// Testing
		// gLength = 1;

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
		for (int i = 0, l = btn.length; i < l; i++)
			btn[i].setVisibility(View.INVISIBLE);

		initThread.start();
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
			Log.d("played.length", played.length + "");
			answer(2);
			break;
		case R.id.btn_answer4:
			answer(3);
			break;
		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
			arg0.setBackgroundResource(R.drawable.btnbg_c); // 按下的图片对应pressed
		} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
			arg0.setBackgroundResource(R.drawable.btnbg); // 常态下的图片对应normal
		}
		return false;
	}

	private void answer(int i) {
		Log.v("Answer", i + " received.");
		String toastMsg = "Unknow";
		if (i == btnRight) {
			mp.reset();
			timeOn = false;
			acc_r++;
			rightStack++;
			if (rightStack > hStack)
				hStack = rightStack;
			if (rightStack > 2 && rightStack < 10) {
				toastMsg = "Correct! X " + rightStack;
			} else if (rightStack >= 10) {
				toastMsg = "Correct! X " + rightStack + " AWESOME!!";
			} else {
				toastMsg = "Correct!";
			}
			// If game is up.
			if (questionNum >= gLength) {
				timeOn = false;
				timer.cancel();
				endGame(true, "You have answered all " + questionNum
						+ " questions in " + ((double) time / 10) + " seconds.");
				return;
			}
			animOut(true);
		} else {
			animToolBox sAnim = new animToolBox(1, 0, 1, 0);
			sAnim.setScaleMode(Animation.RELATIVE_TO_SELF);
			sAnim.setTime(1000);
			sAnim.setS(0.5f, 0.5f);
			sAnim.animScale(btn[i]);
			acc_w++; // Wrong answers++
			rightStack = 0; // Reset stack/
			toastMsg = "Wrong";
			btn[i].setVisibility(View.INVISIBLE);
			btn[i].setClickable(false);
		}
		Log.d("Answer", i + " received.");
		Toast.makeText(this, toastMsg, 0).show();
		textV = (TextView) findViewById(R.id.status_acc);
		textV.setText(acc_r * 100 / (acc_r + acc_w) + "%");
	}

	private boolean checkRepeat(int value) {
		// If we allow repeat.
		if (repeat)
			return false;
		if (played.length < 1)
			return false;
		for (int i = 0, l = played.length; i < l; i++) {
			if (played[i] == value) {
				return true;
			}
		}
		return false;
	}

	// Core function.
	private void questionGiver() {
		if ((questionNum >= mFiles.size() && !repeat) || mFiles.size() < 1) {
			endGame(false, "No more question can be generate.");
			return;
		}
		// Update Question number.
		questionNum++;
		TextView tv = (TextView) findViewById(R.id.status_probNum);
		tv.setText(questionNum + "/" + gLength);

		int qCan;
		do {
			qCan = rng.nextInt(mFiles.size());
		} while (checkRepeat(qCan));
		played[questionNum - 1] = qCan;
		// Assign the right one.
		int theOne = qCan;
		int to = mFiles.size();
		int[] c = new int[3];
		c[0] = -1;
		c[1] = -1;
		c[2] = -1;
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
		int theOneBtn = rng.nextInt(3);
		// Assign to local var.
		btnRight = theOneBtn;
		btn[theOneBtn].setText((String) mFiles.get(theOne).get("mFile_name"));
		// Assign other choice in order.
		int j = 0;
		for (int i = 0, bLength = btn.length; i < bLength; i++) {
			if (i != theOneBtn)
				btn[i].setText((String) mFiles.get(c[j++]).get("mFile_name"));
		}

		try {
			mp.setDataSource((String) mFiles.get(qCan).get("mFile_path"));
			mp.prepare();
			mp.seekTo(Math.max(10000, (int) (rng.nextDouble() * 45000)));
			mp.setLooping(true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Message qReady = new Message();
		qReady.what = 2;
		handler.sendMessage(qReady);
	}

	private void animOut(final boolean direction) {
		AnimationListener animLis = new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				if (direction) {
					timeOn = false;
					questionGiver();
				} else {
					timeOn = true;
					mp.start();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		};
		Animation anim = null;
		Animation anim2 = null;
		if (direction == true) {
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
		anim2.setAnimationListener(animLis);
		btn[3].startAnimation(anim2);
	}

	private void readyStage() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setIcon(R.drawable.icon_common);
		alertDialogBuilder.setTitle("Let's Rock.");
		alertDialogBuilder.setMessage(R.string.tMode_startMsg);
		alertDialogBuilder.setPositiveButton("GO",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						questionNum = 0;
						played = new int[mFiles.size()];
						questionGiver();
						// Start time counting.
						timer = new Timer(true);
						timer.schedule(task, 1000, 100);
					}
				});
		final AlertDialog startDialog = alertDialogBuilder.create();
		startDialog.show();
		if (mFiles.size() - 2 < gLength) {
			AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(
					this);
			alertDialogBuilder2.setIcon(R.drawable.icon_question);
			alertDialogBuilder2.setTitle("Warning!");
			alertDialogBuilder2.setMessage(R.string.warn_notEnoughSound);
			alertDialogBuilder2.setPositiveButton(R.string.btn_continue, null);
			alertDialogBuilder2.setNegativeButton(R.string.btn_quit,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							ModeTime.this.finish();
						}
					});
			final AlertDialog startDialog1 = alertDialogBuilder2.create();
			startDialog1.show();
		}
	}

	public void endGame(final boolean toResult, String msg) {
		if (mp.isPlaying())
			mp.stop();
		timeOn = false;
		String title;
		if (toResult) {
			title = "Quiz Complete!";
		} else {
			title = "A Problem Has Occur.";
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
						if (toResult) {
							Intent i = new Intent(ModeTime.this,
									endResult.class);
							Bundle bundle = new Bundle();
							bundle.putDouble("time", time / 10.0);
							
							bundle.putInt("acc_r", acc_r);
							bundle.putInt("acc_w", acc_w);
							bundle.putInt("hStack", hStack);
							bundle.putInt("mode", mode);
							bundle.putInt("gLength", gLength);
							i.putExtras(bundle);
							startActivity(i);
							// Close current activity.
							ModeTime.this.finish();
						} else {
							startActivity(new Intent(ModeTime.this,
									MusicQuiz.class));
							ModeTime.this.finish();
						}
					}
				});
		final AlertDialog startDialog = alertDialogBuilder.create();
		startDialog.show();
	}

	public void onPause() {
		super.onPause();
		gamePause = true;
		timeOn = false;
		mp.reset();

	}

	public void onResume() {
		super.onResume();
		if (gamePause) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setIcon(R.drawable.icon_question);
			alertDialogBuilder.setMessage("Return to game?");
			alertDialogBuilder.setTitle("Return");
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							questionNum = questionNum - 1;
							gamePause = false;
							animOut(true);

						}
					});
			alertDialogBuilder.setNegativeButton("To Main Menu",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(ModeTime.this,
									MusicQuiz.class));
							ModeTime.this.finish();
						}
					});
			final AlertDialog startDialog = alertDialogBuilder.create();
			startDialog.show();
		}
	}

	private int hStack = 0, acc_r = 0, acc_w = 0, time = 0, rightStack = 0,
			btnRight = -1, questionNum = 0, gLength, screenWidth = 300;
	private boolean gamePause = false, timeOn = false, subFolder, repeat,
			id3tag;
	private Timer timer;
	private int[] played;
	private SharedPreferences prefs = null;
	private Random rng = new Random();
	private ArrayList<HashMap<String, Object>> mFiles = new ArrayList<HashMap<String, Object>>();
	private MediaPlayer mp = new MediaPlayer();
	private Button[] btn = new Button[4];
	private TextView textV, wait;
	private ProgressBar pb;
}
