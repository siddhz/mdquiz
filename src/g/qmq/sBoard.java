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
		tabHost.addTab(tabHost.newTabSpec("0").setIndicator(
				this.getResources().getString(R.string.quizMode_ta_s),
				getResources().getDrawable(R.drawable.tab_time)).setContent(
				new Intent(this, sBoardView.class).putExtra("mode", 0)
						.putExtra("UID", UID)));
		tabHost.addTab(tabHost.newTabSpec("1").setIndicator(
				this.getResources().getString(R.string.quizMode_mo_s),
				getResources().getDrawable(R.drawable.tab_memory)).setContent(
				new Intent(this, sBoardView.class).putExtra("mode", 1)
						.putExtra("UID", UID)));
		tabHost.addTab(tabHost.newTabSpec("2").setIndicator(
				this.getResources().getString(R.string.quizMode_sd_s),
				getResources().getDrawable(R.drawable.tab_sd)).setContent(
				new Intent(this, sBoardView.class).putExtra("mode", 2)
						.putExtra("UID", UID)));
		tabHost.addTab(tabHost.newTabSpec("3").setIndicator(
				this.getResources().getString(R.string.quizMode_ol_s),
				getResources().getDrawable(R.drawable.tab_ol)).setContent(
				new Intent(this, sBoardView.class).putExtra("mode", 9)));
		tabHost.setCurrentTab(mode);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				// Show ProgressBar on new tabshow.
				UID = -1;
			}
		});
	}
}