package com.tomhw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DrawActivity extends Activity {
	HandDrawView drawView;
	Button[] penColor = new Button[5];
	int[][] colors = new int[5][2];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawactivity);

		drawView = (HandDrawView) findViewById(R.id.surfaceView1);
		drawView.init();
		((Button) findViewById(R.id.button1))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						Uri upload = drawView.save();

						// b=drawView.getHolder().get'
						if (upload != null) {

							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.setType("image/*");
							intent.putExtra(Intent.EXTRA_STREAM, upload);
							intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
							intent.putExtra(Intent.EXTRA_TEXT, "你好 ");
							intent.putExtra(Intent.EXTRA_TITLE, "我是标题");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(Intent.createChooser(intent, "请选择"));
						} else {
							Toast.makeText(DrawActivity.this, "fail",
									Toast.LENGTH_LONG).show();
						}

					}
				});
		((Button) findViewById(R.id.button2))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						drawView.clear();
					}
				});
		((Button) findViewById(R.id.button3))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						finish();
					}

				});
		for (int i = 0; i < 5; i++) {
			int id = getResources().getIdentifier("color" + (i + 1), "id",
					getPackageName());
			penColor[i] = (Button) findViewById(id);
			penColor[i].setTag(i);
			penColor[i].setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int i = (Integer) v.getTag();
					setPen(i);
				}
			});
		}
		for (int i = 0; i < 5; i++) {
			colors[i] = new int[2];
			for (int j = 0; j < 2; j++) {
				int id = getResources()
						.getIdentifier("hd4" + (i + 1) + (j + 1), "drawable",
								getPackageName());
				colors[i][j] = id;
			}
		}
		
		setPen(0);

	}

	void setPen(int p) {
		for (int i = 0; i < 5; i++) {
			if (i == p) {
				penColor[i].setBackgroundResource(colors[i][1]);
			} else {
				penColor[i].setBackgroundResource(colors[i][0]);
			}
		}
		switch(p){
		case 0:
			drawView.setColor(Color.BLACK);
			break;
		case 1:
			drawView.setColor(Color.RED);
			break;
		case 2:
			drawView.setColor(Color.BLUE);
			break;
		case 3:
			drawView.setColor(Color.GREEN);
			break;
		case 4:
			drawView.setColor(Color.WHITE);
			break;
		}

	}
}
