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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ModeOL extends Activity implements OnClickListener,
		OnTouchListener {
	private final int mode = 9, gLength = 15;
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
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
			case 2:
				// Clear off button vis and click.
				for (int k = 0; k < btn.length; k++) {
					btn[k].setVisibility(View.VISIBLE);
					btn[k].setClickable(true);
				}
				animOut(false);
				break;
			}
			super.handleMessage(msg);
		}
	};
	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar.
		setContentView(R.layout.mode_ol);
		// Load presences.
		prefs = getSharedPreferences("g.qmq.pro_preferences", 0);
		subFolder = prefs.getBoolean("music_searchSubFolder", true);
		id3tag = prefs.getBoolean("id3tag", false);
		repeat = false;

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
		for (int i = 0; i < btn.length; i++)
			btn[i].setVisibility(View.INVISIBLE);
		comFun cFun = new comFun();
		InputStream is = null;
		try {
			is = ModeOL.this.openFileInput("songlist.xml");
			if (!cFun.getAllMusicFiles(is, subFolder, mFiles, id3tag)) {
				endGame(false, "Fail to fetch music file, "
						+ "please check your music folder and try again.");
				return;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DisplayMetrics displayMetrics = new DisplayMetrics();  
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);    
        screenWidth = displayMetrics.widthPixels;  
		
		// Initialize played list.
		played = new int[mFiles.size()];

		readyStage();
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
		if (i == btnRight) {
			timeOn = false;
			mp.reset();
			if (questionNum >= gLength) {
				endGame(true, "You beat it! You have answered all "
						+ (--questionNum) + " questions in " + (time / 10.0)
						+ " seconds.");
			}
			animOut(true);
		} else {
			endGame(true, "Game Over! You have answered " + (--questionNum)
					+ " questions in " + (time / 10.0) + " seconds.");
		}
	}

	private boolean checkRepeat(int value) {
		// If we allow repeat.
		if (repeat)
			return false;
		if (played.length < 1)
			return false;
		for (int i = 0; i < played.length; i++) {
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
		for (int i = 0; i < btn.length; i++) {
			if (i != theOneBtn)
				btn[i].setText((String) mFiles.get(c[j++]).get("mFile_name"));
		}

		try {
			mp.setDataSource((String) mFiles.get(qCan).get("mFile_path"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp.seekTo(Math.max(20000, (int) (rng.nextDouble() * 50000)));
		mp.setLooping(true);

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
		if (mFiles.size() >= gLength + 5) {
			alertDialogBuilder.setIcon(R.drawable.icon_common);
			alertDialogBuilder.setTitle("Let's Rock.");
			alertDialogBuilder.setMessage(R.string.olMode_startMsg);
			alertDialogBuilder.setNeutralButton("GO",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							questionNum = 0;
							played = new int[mFiles.size()];
							ProgressBar pb = (ProgressBar) findViewById(R.id.time_pb);
							TextView wait = (TextView) findViewById(R.id.time_tvWait);
							pb.setVisibility(View.GONE);
							wait.setVisibility(View.GONE);
							questionGiver();
							// Start time counting.
							timeOn = true;
							timer = new Timer(true);
							timer.schedule(task, 1000, 100);
						}
					});
			final AlertDialog startDialog = alertDialogBuilder.create();
			startDialog.show();
		} else {
			alertDialogBuilder.setIcon(R.drawable.icon_problem);
			alertDialogBuilder.setTitle(R.string.insuf_title);
			alertDialogBuilder.setMessage(getResources().getString(
					R.string.insuf)
					+ " "
					+ (gLength + 5)
					+ " "
					+ getResources().getString(R.string.insuf_2));
			alertDialogBuilder.setNegativeButton(R.string.btn_quit,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							startActivity(new Intent(ModeOL.this,
									MusicQuiz.class));
						}
					});
			final AlertDialog startDialog1 = alertDialogBuilder.create();
			startDialog1.show();
		}
	}

	public void endGame(final boolean toResult, String msg) {
		if (mp.isPlaying())
			mp.stop();
		timeOn = false;
		String title;
		if (toResult) {
			title = getResources().getString(R.string.quizComplete);
		} else {
			title = getResources().getString(R.string.errorOccur);
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
							Intent i = new Intent(ModeOL.this,
									endResult_OL.class);
							Bundle bundle = new Bundle();
							bundle.putDouble("time", time / 10.0);
							bundle.putInt("qNum", questionNum);
							bundle.putInt("mode", mode);
							i.putExtras(bundle);
							startActivity(i);
							// Close current activity.
							ModeOL.this.finish();
						} else {
							startActivity(new Intent(ModeOL.this,
									MusicQuiz.class));
							ModeOL.this.finish();
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
			alertDialogBuilder.setTitle("Welcome Back");
			alertDialogBuilder.setMessage("Cannot resume in online mode.");
			alertDialogBuilder.setCancelable(false);
			// alertDialogBuilder.setPositiveButton("OK",
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// questionNum = questionNum - 1;
			// gamePause = false;
			// animOut(true);
			//
			// }
			// });
			alertDialogBuilder.setNegativeButton("To Main Menu",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(ModeOL.this,
									MusicQuiz.class));
							ModeOL.this.finish();
						}
					});
			final AlertDialog startDialog = alertDialogBuilder.create();
			startDialog.show();
		}
	}

	private int time = 0, btnRight = -1, questionNum = 0, screenWidth = 300;
	private boolean gamePause = false, timeOn = false, subFolder, repeat,
			id3tag;
	private Timer timer;
	private int[] played;
	private SharedPreferences prefs = null;
	private Random rng = new Random();
	private ArrayList<HashMap<String, Object>> mFiles = new ArrayList<HashMap<String, Object>>();
	private MediaPlayer mp = new MediaPlayer();
	private Button[] btn = new Button[4];
}