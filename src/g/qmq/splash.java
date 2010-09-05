package g.qmq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
 

public class splash extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		
		Thread welcomeThread = new Thread() {
			
			
			@Override
			public void run() {
				try{
					super.run();
					while (wait < welcomeScreenDisplay) {
						sleep(100);
						wait += 100;						
					}
				}catch(Exception e){
					System.out.println("EXc=" + e);
				}finally {
					startActivity(new Intent(splash.this, MusicQuiz.class));
					finish();
				}				
			}
		};
		welcomeThread.start();
		ImageView iv = (ImageView) findViewById(R.id.iv1);
		iv.setOnClickListener(
				new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						wait = welcomeScreenDisplay;
					}
			
		});		
	}
	private final int welcomeScreenDisplay = 3000;
	private int wait = 0;
}