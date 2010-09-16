package g.qmq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
 

public class splash extends Activity {
//	private final String TAG = "Splash";
	private final int WELCOME_TIME = 3000;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		welcomeThread.start();
		ImageView iv = (ImageView) findViewById(R.id.iv1);
		iv.setOnClickListener(
				new OnClickListener(){
					@Override
					public void onClick(View v) {
						wait = WELCOME_TIME;
					}
		});		
	}
	
	private Thread welcomeThread = new Thread() {		
		@Override
		public void run() {
			try{
				super.run();
				while (wait < WELCOME_TIME) {
					sleep(100);
					wait += 100;						
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally {
				startActivity(new Intent(splash.this, MusicQuiz.class));
				finish();
			}				
		}
	};
	
	private int wait = 0;
}