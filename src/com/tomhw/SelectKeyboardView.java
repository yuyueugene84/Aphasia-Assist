package com.tomhw;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class SelectKeyboardView extends LinearLayout {
	ImageView[] btn = new ImageView[3];
	int selectKeyboard;
	Drawable[][] draw = new Drawable[3][2];

	// String[] name = { "手寫", "注音", "英文" };

	public SelectKeyboardView(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setOrientation(LinearLayout.VERTICAL);
		setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		for (int i = 0; i < 3; i++) {
			draw[i] = new Drawable[2];

		}
		draw[0][0] = getResources().getDrawable(R.drawable.sk21);
		draw[0][1] = getResources().getDrawable(R.drawable.sk22);
		draw[1][0] = getResources().getDrawable(R.drawable.sk31);
		draw[1][1] = getResources().getDrawable(R.drawable.sk32);
		draw[2][0] = getResources().getDrawable(R.drawable.sk11);
		draw[2][1] = getResources().getDrawable(R.drawable.sk12);
		// TODO Auto-generated constructor stub
		for (int i = 0; i < 3; i++) {
			btn[i] = new ImageView(context);
			// btn[i].setBackgroundColor(Color.GREEN);
			btn[i].setImageDrawable(draw[i][0]);
			btn[i].setScaleType(ScaleType.FIT_XY);
			addView(btn[i], new LayoutParams(135, 100));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		Log.e("touch", e.getAction() + "!!");
		return false;
	}

	void setTarget(int x) {
		if (x == selectKeyboard) {
			return;
		}
		selectKeyboard = x;
		for (int i = 0; i < 3; i++) {
			if (selectKeyboard == i) {
				btn[i].setImageDrawable(draw[i][1]);
			} else {
				btn[i].setImageDrawable(draw[i][0]);
			}
		}
	}

	void nextKeyboard() {
		setTarget((selectKeyboard + 1) % 3);
	}

	int getSelect() {
		return (selectKeyboard + 1) % 3;
	}

	void setKeyboard(int k) {
		selectKeyboard= (k - 1) % 3;
	}
}
