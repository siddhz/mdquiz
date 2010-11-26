package g.qmq;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

public class ModeSelection extends Activity implements OnTouchListener,
		OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mode_selection);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		ImageView iv = (ImageView) findViewById(R.id.timeMode);
		iv.setOnTouchListener(this);
		iv.setOnClickListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_UP:

			break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

		// Intent it = new Intent(this, Result.class);
		// Random rd = new Random();
		// Bundle bundle = new Bundle();
		// String[] test = new String[] {
		// "0","Total Questions", "100", "Q",
		// "0","Corrects", "5", "Q",
		// "0","Incorrects", "5", "Q",
		// "1","Total Time",
		// String.valueOf(rd.nextInt(100)), "S" };
		// bundle.putStringArray("resultData", test);
		// bundle.putChar("MODE", 'T');
		// it.putExtras(bundle);
		// startActivity(it);

		startActivity(new Intent(this, GamePlay.class));
		this.finish();
	}
}
