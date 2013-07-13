package com.barobot;

import android.view.View;
import android.view.View.OnClickListener;
public class button_click implements OnClickListener{
	@Override
	public void onClick(View v) {
		queue q = queue.getInstance();
		switch (v.getId()) {
		case R.id.set_x_1000:
			q.send("SET X -1000");
			break;
		case R.id.set_x_100:
			q.send("SET X -100");
			break;
		case R.id.set_x_10:
			q.send("SET X -10");
			break;
		case R.id.set_x_1:
			q.send("SET X 0");
			break;
		case R.id.set_x10:
			q.send("SET X +10");
			break;
		case R.id.set_x100:
			q.send("SET X +100");
			break;
		case R.id.set_x1000:
			q.send("SET X +1000");
			break;
  
		case R.id.set_y_600:
			q.send("SET Y -600");
			break;
		case R.id.set_y_100:
			q.send("SET Y -100");
			break;
		case R.id.set_y_10:
			q.send("SET Y -10");
			break;
		case R.id.set_y_0:
			q.send("SET Y 0");
			break;
		case R.id.set_y10:
			q.send("SET Y +10");
			break;
		case R.id.set_y100:
			q.send("SET Y +100");
			break;
		case R.id.set_y600:
			q.send("SET Y +600");
			break;          
	          
	          

		case R.id.kalibrujx:
			q.send("KALIBRUJX");
			break;
		case R.id.kalibrujy:
			q.send("KALIBRUJY");
			break;
		case R.id.kalibrujz:
			q.send("KALIBRUJZ");
			break;
		case R.id.machajx:
			q.send("MACHAJX");
			break;
		case R.id.machajy:
			q.send("MACHAJY");
			break;
		case R.id.machajz:
			q.send("MACHAJZ");
			break;
		case R.id.losujx:
			q.send("LOSUJX");
			break;
		case R.id.losujy:
			q.send("LOSUJY");
			break;
		case R.id.losujz:
			q.send("LOSUJZ");
			break;
		case R.id.glweight:
			q.send("GET GLASS");
			break;
		case R.id.bottweight:
			q.send("GET WEIGHT");
			break;
		case R.id.fill5000:
			q.send("FILL 5000");
			break;
	   }
	}
}
