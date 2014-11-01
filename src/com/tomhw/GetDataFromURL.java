package com.tomhw;
/*
 * 
 * ReadUrl.java
 * ggm
 * 
 * Read url
 *
 * Copyright 2010 NTU CSIE Mobile & HCI Lab
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GetDataFromURL {

	public static Bitmap getBitmapFromURL(String imageFileURL){
		Bitmap bitmap = BitmapFactory.decodeFile("file:///android_res/drawable/test.jpg");
		try {
	         URL url = new URL(imageFileURL);
	         URLConnection conn = url.openConnection();
	          
	         HttpURLConnection httpConn = (HttpURLConnection)conn;
	         httpConn.setRequestMethod("GET");
	         httpConn.connect();
	       
	         if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	          InputStream inputStream = httpConn.getInputStream();
	           
	          bitmap = BitmapFactory.decodeStream(inputStream);
	          inputStream.close();
	          return bitmap;
	         }
	        } catch (MalformedURLException e1) {
	         // TODO Auto-generated catch block
	         e1.printStackTrace();
	        } catch (IOException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	        }
		return bitmap;
	        
	}
	public static String getJsonResponseFromURL(String requestURL){

		String  response = "";
		try {
	         URL url = new URL(requestURL);
	         URLConnection conn = url.openConnection();
	          
	         HttpURLConnection httpConn = (HttpURLConnection)conn;
	         httpConn.setRequestMethod("GET");
	         httpConn.connect();
	       
	         if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	          InputStream inputStream = httpConn.getInputStream();

	          String line;	
	          StringBuilder builder = new StringBuilder();
	          BufferedReader reader;
	  		  reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
	  		  while ((line = reader.readLine()) != null)
	  			builder.append(line + "\n");
	  		  response = builder.toString();
	  		  Log.d("DebugLog","response >>"+response);
	  		  inputStream.close();
	  		  return response;
	          
	         }
	        } catch (MalformedURLException e1) {
	         // TODO Auto-generated catch block
	         e1.printStackTrace();
	        } catch (IOException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	        }
		return response;
	        
	}
}
