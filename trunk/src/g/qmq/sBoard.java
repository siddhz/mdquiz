package g.qmq;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class sBoard extends TabActivity {
	int UID = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		UID = this.getIntent().getExtras().getInt("UID");
		int mode = this.getIntent().getExtras().getInt("mode");
		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost
				.newTabSpec("timed")
				.setIndicator(getResources().getString(R.string.quizMode_ta_s),
						getResources().getDrawable(R.drawable.tab_time))
				.setContent(
						new Intent(this, sBoardView.class).putExtra("mode", 0)
								.putExtra("UID", UID)));

		tabHost.setCurrentTab(mode);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				UID = -1;
			}
		});
	}
}