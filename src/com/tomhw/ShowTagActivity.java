package com.tomhw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowTagActivity extends Activity {
	String findTag;
	ImageView imageView;
	MediaPlayer mediaPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showtag);

		findTag = getIntent().getStringExtra("tag");
		mediaPlayer = new MediaPlayer();
		((TextView) findViewById(R.id.textView1)).setText(findTag);
		imageView = (ImageView) findViewById(R.id.imageView1);

		((Button) findViewById(R.id.button1))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Toast.makeText(ShowTagActivity.this, "發音",
								Toast.LENGTH_LONG).show();
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								URI uri;
								try {
									uri = new URI(

									"http://translate.google.com/translate_tts?tl=zh-TW&q="
											+ findTag);
									URL u = new URL(uri.toASCIIString());
									HttpURLConnection c = (HttpURLConnection) u
											.openConnection();
									c.addRequestProperty("User-Agent",
											"Mozilla/5.0");
									c.setRequestMethod("GET");
									c.setDoOutput(true);
									c.connect();
									File file = new File(Environment
											.getExternalStorageDirectory()
											+ "/download/" + "testsound");
									FileOutputStream f = new FileOutputStream(
											file);
									InputStream in = c.getInputStream();
									byte[] buffer = new byte[1024];
									int len1 = 0;
									while ((len1 = in.read(buffer)) > 0) {
										f.write(buffer, 0, len1);
									}
									f.close();

									MediaPlayer mediaPlayer = new MediaPlayer();

									// 設定外部音訊檔路徑,此路徑可為本機SD卡或是網路資源
									String path = Environment
											.getExternalStorageDirectory()
											.getAbsolutePath()
											+ "/download/" + "testsound";

									mediaPlayer.setDataSource(path);

									// 外部資源需先執行prepare預載
									mediaPlayer.prepare();
									mediaPlayer.start();

								} catch (URISyntaxException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (MalformedURLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();

					}
				});
		((Button) findViewById(R.id.button2))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						MainActivity.testString = findTag;
						finish();

					}
				});
		new getImagesTask().execute();
	}

	public class getImagesTask extends AsyncTask<Void, Void, Bitmap> {
		JSONObject json;

		@Override
		protected Bitmap doInBackground(Void... params) {
			// TODO Auto-generated method stub

			URL url;
			Bitmap b = null;
			try {
				String s = URLEncoder.encode(findTag, "UTF-8");
				url = new URL(
						"https://ajax.googleapis.com/ajax/services/search/images?"
								+ "v=1.0&q=" + s + "&rsz=8");
				// &key=ABQIAAAADxhJjHRvoeM2WF3nxP5rCBRcGWwHZ9XQzXD3SWg04vbBlJ3EWxR0b0NVPhZ4xmhQVm3uUBvvRF-VAA&userip=192.168.0.172");

				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						"http://technotalkative.com");

				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				System.out.println("Builder string => " + builder.toString());

				json = new JSONObject(builder.toString());
				JSONObject responseObject = json.getJSONObject("responseData");
				JSONArray resultArray = responseObject.getJSONArray("results");
				if (resultArray.length() > 0) {
					JSONObject image = resultArray.getJSONObject(0);
					// Log.e("json", image.getString("tbUrl"));
					b = GetDataFromURL.getBitmapFromURL(image
							.getString("tbUrl"));
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return b;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				Log.e("json", "setImage");
				imageView.setImageBitmap(result);
			}

		}
	}

}
