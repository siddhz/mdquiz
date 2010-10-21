package g.qmq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class splash extends Activity {
	// private final String TAG = "Splash";
	private final int WELCOME_TIME = 3000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		iv = (ImageView) findViewById(R.id.iv1);
		iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				wait = WELCOME_TIME;
			}
		});

		disk = (ImageView) findViewById(R.id.disk);
		Animation am_move = new TranslateAnimation(0, 0, -50f, 0);
		am_move.setInterpolator(new AccelerateDecelerateInterpolator());
		am_move.setFillAfter(true);
		am_move.setDuration(WELCOME_TIME / 3);

		Animation am_spin = new RotateAnimation(0, 7200f, 36f, 36f);
		am_spin.setInterpolator(new AccelerateInterpolator());
		am_spin.setFillAfter(true);
		am_spin.setDuration(WELCOME_TIME + 1000);

		disk.startAnimation(am_move);
		disk.startAnimation(am_spin);

		welcomeThread.start();
	}

	private Thread welcomeThread = new Thread() {
		@Override
		public void run() {
			try {
				super.run();
				while (wait < WELCOME_TIME) {
					sleep(10);
					wait += 10;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				startActivity(new Intent(splash.this, MusicQuiz.class));
				finish();
			}
		}
	};

	private int wait = 0;
	private ImageView iv, disk;
}