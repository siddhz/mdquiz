package g.qmq;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

public class sBoardView extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.s_board);

		//Grasp views ready to use.
		waitRela = (RelativeLayout) findViewById(R.id.rela_wait);
		
		//Show progress bar.
		waitRela.setVisibility(View.VISIBLE);
		initThd.start();
	}
	
	private void xmlReader(String name){
	}
	

	/*
	 * Thread	***START***
	 */
	private Thread initThd = new Thread(new Runnable() {
		@Override
		public void run() {
		}
	});
	/*
	 * Thread	***END***
	 */

	/*
	 * Handler	***START***
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		}
	};
	/*
	 * Handler	***END***
	 */
	

	
	private RelativeLayout waitRela;
}
