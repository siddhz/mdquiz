package g.qmq;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class showDetail extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.show_detail);

		try {
			Bundle bundle = this.getIntent().getExtras();
			data = bundle.getStringArray("detail");
			head = bundle.getStringArray("head");

			LinearLayout ly = (LinearLayout) findViewById(R.id.world);
			LinearLayout.LayoutParams Param = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);

			String headStr[] = new String[] { "Player Name", head[1] }; //TODO TO XML
			ly.addView(relayFactory(headStr), Param);
			String headStr_1[] = new String[] { "Date", head[2] }; //TODO TO XML
			ly.addView(relayFactory(headStr_1), Param);

			for (int i = 0, j = data.length; i < j;) {
				String dataStr[] = new String[] { data[i++], data[i++] + data[i++] };
				ly.addView(relayFactory(dataStr), Param);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Detail Not found.", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

	}

	private RelativeLayout relayFactory(String text[]) {
		RelativeLayout rl = new RelativeLayout(this);
		TextView tv[] = new TextView[2];

		// Add Left TV
		tv[0] = new TextView(this);
		tv[0].setText(text[0]);
		RelativeLayout.LayoutParams ParamL = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		ParamL.addRule(RelativeLayout.CENTER_VERTICAL);
		ParamL.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rl.addView(tv[0], ParamL);

		// Add right TV
		tv[1] = new TextView(this);
		tv[1].setText(text[1]);
		RelativeLayout.LayoutParams ParamR = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		ParamR.addRule(RelativeLayout.CENTER_VERTICAL);
		ParamR.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rl.addView(tv[1], ParamR);

		return rl;
	}

	private String data[];
	private String head[];
}
