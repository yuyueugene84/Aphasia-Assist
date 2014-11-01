package com.tomhw;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class InputVoiceActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EditText e = new EditText(this);
		e.setVisibility(View.GONE);
		setContentView(e);
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, "語音輸入");
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(i, 0);
	}

	@Override
	protected void onActivityResult(int request, int reslut, Intent data) {
		if (request == 0 && reslut == RESULT_OK) {
			ArrayList<String> s = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (s.size() > 0) {
				MainActivity.testString = s.get(0);
			}
		}
		finish();
	}
}
