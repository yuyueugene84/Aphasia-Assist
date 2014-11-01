package com.tomhw;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xBaseJ.DBF;
import xBaseJ.Field;
import xBaseJ.xBaseJException;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FindActivity extends Activity {

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case 0:
				flagQuerying = false;
				progress.dismiss();
				// for (int i = 0; i < 4; i++) {
				// if (names[i].equals("")) {
				// nameViews[i].setText(null);
				// imageButtons[i].setImageBitmap(null);
				// continue;
				// }
				// nameViews[i].setText(names[i]);
				// imageButtons[i].setImageBitmap(images[i]);
				//
				// }

				// if (nameList == null) {
				// nameList = new ArrayList<String>();
				// for (int i = 0; i < queryData.tagList.size(); i++) {
				// nameList.add(queryData.tagList.get(i).name);
				// }
				// } else {
				// for (int i = nameList.size() - 1; i >= 0; i--) {
				// boolean flagJoin = false;
				// String a = nameList.get(i);
				// for (int j = 0; j < queryData.tagList.size(); j++) {
				//
				// String b = queryData.tagList.get(j).name;
				//
				// if (a.equals(b)) {
				// flagJoin = true;
				// break;
				// }
				//
				// }
				//
				// if (!flagJoin) {
				// nameList.remove(i);
				// }
				// }
				// }
				// // ArrayAdapter adapter = new ArrayAdapter(FindActivity.this,
				// // android.R.layout.simple_list_item_1, nameList);
				// // listView.setAdapter(adapter);
				break;

			}
		}
	};
	// ListView listView;
	ArrayList<TagEdge> showingEdges;
	// ArrayList<DataSet> allData;
	InputMethodManager imm;
	// ImageButton imageButtons[] = new ImageButton[4];
	// Bitmap images[] = new Bitmap[4];
	// TextView nameViews[] = new TextView[4];
	// String names[] = new String[4];

	TextView textView;

	boolean flagQuerying = false;
	String queryTag;
	DataSet queryData;
	int dataCnt = 0;

	ProgressDialog progress;
	EditText editText;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finding);

		// listView = (ListView) findViewById(R.id.listView1);
		//
		// listView.setOnItemClickListener(new ListView.OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// // TODO Auto-generated method stub
		// Intent i = new Intent(FindActivity.this, ShowTagActivity.class);
		// i.putExtra("tag", nameList.get(arg2));
		// startActivity(i);
		// finish();
		//
		// }
		// });
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		textView = (TextView) findViewById(R.id.textView1);
		editText = (EditText) findViewById(R.id.editText1);
		editText.setText("");
		progress = new ProgressDialog(this);
		progress.setMessage("搜尋中");

		// query
		((Button) findViewById(R.id.button2))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {

						if (flagQuerying || editText.getText().toString().equals("")) {
							return;
						}
						imm.hideSoftInputFromWindow(editText.getWindowToken(),
								0);
						String s = editText.getText().toString();
						textView.setText(textView.getText() + s + " ");
						progress.show();
						query(s);
						editText.setText("");
						dataCnt++;

					}
				});

		// reset
		((Button) findViewById(R.id.button1))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						showingEdges = null;
						// listView.setAdapter(null);
						textView.setText(" ");
						dataCnt = 0;
						queryData = null;
						editText.setText("");
						//
						// for (int i = 0; i < 4; i++) {
						//
						// nameViews[i].setText(null);
						// imageButtons[i].setImageBitmap(null);
						//
						// }
					}
				});

	}

	void query(String tag) {
		flagQuerying = true;
		queryTag = tag;

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// query
				try {
					String tag = queryTag;
					queryTag = URLEncoder.encode(queryTag, "UTF-8");

					String ss = GetDataFromURL
							.getJsonResponseFromURL("http://conceptnet5.media.mit.edu/data/5.1/c/zh_TW/"
									+ queryTag
									+ "?get=incoming_edges+outgoing_edges&limit=50");
					JSONObject object = new JSONObject(ss);

					JSONArray edges = object.getJSONArray("edges");
					queryData = new DataSet(tag, edges);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// 取交集
				if (showingEdges == null) {
					showingEdges = new ArrayList<TagEdge>();
					showingEdges = (ArrayList<TagEdge>) (queryData.edgeList)
							.clone();
					// for (int i = 0; i < queryData.edgeList.size(); i++) {
					// showingEdges.add(queryData.edgeList.get(i));
					// }
				} else {
					// float ms1 = 1, ms2 = 1;
					// if (showingEdges.size() > 0
					// && queryData.edgeList.size() > 0) {
					// ms1 = showingEdges.get(0).score;
					// ms2 = queryData.edgeList.get(0).score;
					//
					// }
					for (int i = showingEdges.size() - 1; i >= 0; i--) {
						boolean flagJoin = false;
						String a = showingEdges.get(i).name;
						for (int j = 0; j < queryData.edgeList.size(); j++) {

							String b = queryData.edgeList.get(j).name;

							if (a.equals(b)) {
								flagJoin = true;

								Log.e("tag", showingEdges.get(i).name + ":"
										+ showingEdges.get(i).score + " "
										+ queryData.edgeList.get(j).score);

								if (showingEdges.get(i).score < queryData.edgeList
										.get(j).score) {
									showingEdges.get(i).score = showingEdges
											.get(i).score;
								} else {
									showingEdges.get(i).score = queryData.edgeList
											.get(j).score;
								}

								break;
							}

						}

						if (!flagJoin) {
							showingEdges.remove(i);
						}
					}
				}
				Collections.sort(showingEdges, new Comparator<TagEdge>() {

					@Override
					public int compare(TagEdge lhs, TagEdge rhs) {
						// TODO Auto-generated method stub
						return (int) (rhs.score - lhs.score);
					}

				});
				Log.e("tag", "交集");
				for (TagEdge e : showingEdges) {
					Log.e("tag", e.name + ":" + e.score);
				}
				// // 取文字
				// for (int i = 0; i < 4; i++) {
				// if (i < showingEdges.size()) {
				// names[i] = showingEdges.get(i).name;
				// } else {
				// names[i] = "";
				// }
				// }
				//
				// // 找圖片
				// for (int i = 0; i < 4; i++) {
				// if (names[i].equals("")) {
				// break;
				// }
				// URL url;
				// Bitmap b = null;
				// try {
				// // String s2 = textView.getText().toString();
				// // s2 = s2.substring(0, s2.length() - 1);
				// String s = URLEncoder.encode(names[i], "UTF-8");
				// // Log.e("search",
				// // "https://ajax.googleapis.com/ajax/services/search/images?"
				// // + "v=1.0&q=" + s + "&rsz=1&hl=zh-TW");
				// url = new URL(
				// "https://ajax.googleapis.com/ajax/services/search/images?"
				// + "v=1.0&q=" + s + "&rsz=1&hl=zh-TW");
				// URLConnection connection = url.openConnection();
				// connection.addRequestProperty("Referer",
				// "http://technotalkative.com");
				//
				// String line;
				// StringBuilder builder = new StringBuilder();
				// BufferedReader reader = new BufferedReader(
				// new InputStreamReader(connection
				// .getInputStream()));
				// while ((line = reader.readLine()) != null) {
				// builder.append(line);
				// }
				//
				// JSONObject json = new JSONObject(builder.toString());
				// JSONObject responseObject = json
				// .getJSONObject("responseData");
				// JSONArray resultArray = responseObject
				// .getJSONArray("results");
				// if (resultArray.length() > 0) {
				// JSONObject image = resultArray.getJSONObject(0);
				// // Log.e("json", image.getString("tbUrl"));
				// b = GetDataFromURL.getBitmapFromURL(image
				// .getString("tbUrl"));
				// }
				// images[i] = b;
				// } catch (MalformedURLException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (JSONException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }

				// UI
				handler.sendEmptyMessage(0);
				flagQuerying = false;

			}
		}).start();
	}

	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Speech recognition demo");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches.size() > 0) {
				editText.setText(matches.get(0));
			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

}
