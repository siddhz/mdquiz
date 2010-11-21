package g.qmq;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class showDetail extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.show_detail);
	}

	private RelativeLayout relayFactory(String text[]) {
		RelativeLayout rl = new RelativeLayout(this);
		TextView tv[] = new TextView[2];
		RelativeLayout.LayoutParams Param = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		tv[0].setText("1");

		return null;
	}
}
