package com.tomhw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class DataSet {
	String tagName;
	ArrayList<TagEdge> edgeList;

	DataSet(String n, JSONArray edges) {
		tagName = n;
		Log.e("tag", tagName);
		edgeList = new ArrayList<TagEdge>();
		for (int i = 0; i < edges.length(); i++) {
			try {
				String s1 = edges.getJSONObject(i).getString("startLemmas");
				String s2 = edges.getJSONObject(i).getString("endLemmas");
				String r = edges.getJSONObject(i).getString("rel");
				float w = (float) edges.getJSONObject(i).getDouble("score");
				if (w < 1) {
					break;
				}
				String s = s1;
				if (s1.equals(tagName)) {
					s = s2;
				}
				// Log.e("tag",s+" "+s1+":"+s2);
				boolean flag = false;
				for (TagEdge e : edgeList) {
					if (e.name.equals(s)) {
						flag = true;
						if (e.score < w) {
							e.score = w;
						}
						break;
					}
				}
				if (!flag) {
					edgeList.add(new TagEdge(s, w, r));
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Collections.sort(edgeList, new Comparator<TagEdge>() {

			@Override
			public int compare(TagEdge lhs, TagEdge rhs) {
				// TODO Auto-generated method stub
				return (int) (rhs.score - lhs.score);
			}

		});
		int c = 0;
		for (TagEdge e : edgeList) {
			c++;
			Log.e("tag", c + ":" + e.rel + " " + e.name + ":" + e.score);
		}

	}
}
