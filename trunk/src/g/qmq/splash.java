package g.qmq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
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
				startActivity(new Intent(splash.this, MusicQuiz.class));
				splash.this.finish();
			}
		});

		Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		fadeOut.setDuration((long) (WELCOME_TIME * 0.4));
		fadeOut.setStartOffset((long) (WELCOME_TIME * 0.6) - 500);
		fadeOut.setFillAfter(true);
		fadeOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				startActivity(new Intent(splash.this, MusicQuiz.class));
				splash.this.finish();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}
		});
		iv.startAnimation(fadeOut);

		// welcomeThread.start();
	}

	// private Thread welcomeThread = new Thread() {
	// @Override
	// public void run() {
	// try {
	// super.run();
	// while (wait < WELCOME_TIME) {
	// sleep(10);
	// wait += 10;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// startActivity(new Intent(splash.this, MusicQuiz.class));
	// finish();
	// }
	// }
	// };

	private int wait = 0;
	private ImageView iv, disk;
}