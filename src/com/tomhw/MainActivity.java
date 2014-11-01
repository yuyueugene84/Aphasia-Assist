/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomhw;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xBaseJ.DBF;
import xBaseJ.Field;
import xBaseJ.xBaseJException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Example of writing an input method for a soft keyboard. This code is focused
 * on simplicity over completeness, so it should in no way be considered to be a
 * complete soft keyboard implementation. Its purpose is to provide a basic
 * example for how you would get started writing an input method, to be fleshed
 * out as appropriate.
 */
@SuppressLint("NewApi")
public class MainActivity extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener {

	private InputMethodManager mInputMethodManager;
	static final boolean DEBUG = false;
	static final boolean PROCESS_HARD_KEYS = true;

	static String testString = "";
	final int initKeyboard = 0;

	// 鍵盤
	int nowKeyboard = initKeyboard;
	View[] keyBoardView = new View[4];
	WriteView writeView;
	TextView typingView;
	SelectKeyboardView selectKeyboardView;

	// 選字
	// LinearLayout handCandidateLayout;
	HandCandidateView handCandidateView;
	ChineseCandidateView chineseCandidateView;
	List<String> candidateList;

	// 建立手写输入对象
	long recognizer = 0;
	long character = 0;
	long result = 0;
	int modelState = 0; // 显示model文件载入状态
	int strokes = 0; // 总笔画数
	int handwriteCount = 0; // 笔画数
	Path mPath = null;
	boolean flagWriting = false;
	int str = 0;
	String writeWord = "";

	// 注音
	HashMap<String, Integer> chineseWeight = new HashMap<String, Integer>();
	HashMap<String, ArrayList<String>> chineseSound = new HashMap<String, ArrayList<String>>();
	String[] chineseAll = { "", "", "", "" };

	// ----------------------------------------------------------------------------
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case 0:
				str--;
				if (str <= 0) {
					str = 0;
					// getCurrentInputConnection().commitText(writeWord,
					// writeWord.length());
					getCurrentInputConnection().setComposingText(writeWord,
							writeWord.length());
					characterClear(character);
					strokes = 0;
					mPath.reset();// 触摸结束即清除轨迹
					handwriteCount = 0;
					writeView.clear();
				}
				break;
			case 1:
				str = 0;

				characterClear(character);
				strokes = 0;
				mPath.reset();// 触摸结束即清除轨迹
				handwriteCount = 0;
				setCandidatesViewShown(false);
				writeWord = "";
				writeView.clear();

				break;

			case 3:
				if (!writeView.clear()) {
					sendEmptyMessageDelayed(3, 100);
				}
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		// 初始化注音字庫-------------------------------------
		String file = Environment.getExternalStorageDirectory()
				+ "/Gotcha/86tzword.dbf";

		try {

			DBF aDBF = new DBF(file);
			Field ss = aDBF.getField("DWORD");
			Field ff = aDBF.getField("KYJD");
			Field ww = aDBF.getField("DPIWN");
			// read chinese word
			for (int i = 1; i <= aDBF.getRecordCount(); i++) {
				aDBF.read();
				// 拿字
				String s = new String(ss.getBytes(), "big5");
				// 拿注音處理空白跟輕聲
				String u = new String(ff.getBytes(), "big5");
				char[] c = u.toCharArray();
				for (int j = 0; j < c.length; j++) {
					if ((int) c[j] == 32) {
						CharSequence cs = u.subSequence(0, j);
						u = cs.toString();
						break;
					}
				}
				if (u.contains("˙")) {
					u = u.substring(1) + "˙";
				}
				// 拿weight處理空白
				String ws = new String(ww.getBytes(), "big5");
				c = ws.toCharArray();
				for (int j = 0; j < c.length; j++) {
					if ((int) c[j] != 32) {
						CharSequence cs = ws.subSequence(j, c.length);
						ws = cs.toString();
						break;
					}
				}
				Integer w = Integer.parseInt(ws);

				if (chineseSound.containsKey(u)) {
					chineseSound.get(u).add(s);
				} else {
					ArrayList<String> a = new ArrayList<String>();
					a.add(s);
					chineseSound.put(u, a);
				}
				chineseWeight.put(s, w);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (xBaseJException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.e("IME", "onCreate");
	}

	@Override
	public View onCreateCandidatesView() {
		// handCandidateView = new HandCandidateView(this);
		// handCandidateView.setService(this);
		//
		// handCandidateView.setX(204);
		// handCandidateView.setY(0);
		//
		// if (initKeyboard == 0) {
		// setCandidatesViewShown(true);
		// return handCandidateView;
		// }

		chineseCandidateView = new ChineseCandidateView(this);
		chineseCandidateView.setService(this);

		return chineseCandidateView;
	}

	@Override
	public View onCreateInputView() {
		Log.e("IME", "create input view");

		// mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
		// R.layout.input, null);
		// mInputView.setOnKeyboardActionListener(this);
		// mInputView.setKeyboard(mQwertyKeyboard);

		// 鍵盤按到提示---------------------------------
		RelativeLayout layout = new RelativeLayout(this);
		this.getWindow().addContentView(
				layout,
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		typingView = new TextView(this);
		typingView.setTextSize(36);
		typingView.setBackgroundColor(Color.WHITE);
		typingView.setVisibility(View.GONE);
		layout.addView(typingView);

		selectKeyboardView = new SelectKeyboardView(this);
		selectKeyboardView.setVisibility(View.GONE);
		layout.addView(selectKeyboardView, new LayoutParams(135, 300));
		// 手寫輸入---------------------------------
		keyBoardView[0] = (View) getLayoutInflater().inflate(
				R.layout.handwrite, null);
		writeView = new WriteView(this);
		writeView.setId(123);
		handler.sendEmptyMessage(3);
		((LinearLayout) keyBoardView[0].findViewById(R.id.linearLayout2))
				.addView(writeView);

		handCandidateView = new HandCandidateView(this);
		handCandidateView.setService(this);
		handCandidateView.setLayoutParams(new RelativeLayout.LayoutParams(292,
				584));
		handCandidateView.setX(788);
		((RelativeLayout) keyBoardView[0].findViewById(R.id.relative))
				.addView(handCandidateView);

		// 注音輸入---------------------------------
		keyBoardView[1] = (View) getLayoutInflater().inflate(R.layout.chinese,
				null);
		for (int i = 1; i <= 41; i++) {
			int id = getResources().getIdentifier("chinese" + i, "id",
					getPackageName());
			Button btn = ((Button) keyBoardView[1].findViewById(id));
			btn.setTag(i);
			btn.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int i = (Integer) v.getTag();
					typeChinese(((Button) v).getText().toString(), i);

				}
			});
			btn.setOnTouchListener(new Button.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_MOVE:
						showTypingWord(((Button) v).getText().toString(),
								v.getX(), ((LinearLayout) v.getParent()).getY());
						break;
					case MotionEvent.ACTION_UP:
						hideTypingWord();
						break;
					}
					return false;
				}
			});
		}

		// 英文鍵盤
		keyBoardView[2] = (View) getLayoutInflater().inflate(R.layout.english,
				null);
		for (int i = 1; i <= 26; i++) {
			// Log.e("test", i + "");
			int id = getResources().getIdentifier("english" + i, "id",
					getPackageName());
			((Button) keyBoardView[2].findViewById(id))
					.setOnClickListener(new Button.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							typeEnglish(((Button) v).getText().toString());

						}
					});
			((Button) keyBoardView[2].findViewById(id))
					.setOnTouchListener(new Button.OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:
							case MotionEvent.ACTION_MOVE:
								showTypingWord(((Button) v).getText()
										.toString(), v.getX(),
										((LinearLayout) v.getParent()).getY());
								break;
							case MotionEvent.ACTION_UP:
								hideTypingWord();
								break;
							}
							return false;
						}
					});
		}

		// return keyBoardView[1];

		// 共通按鈕---------------------------------------------------
		for (int i = 0; i < 4; i++) {
			if (keyBoardView[i] != null) {
				// 切換鍵盤
				Button b = ((Button) keyBoardView[i]
						.findViewById(R.id.function3));
				if (b != null) {

					b.setOnTouchListener(new Button.OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							Log.e("touch", event.getX() + ":" + event.getY());
							switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								selectKeyboardView.nextKeyboard();
								showSelectKeyboard(
										v.getX(),
										((LinearLayout) v.getParent()).getY() - 160);
								break;
							case MotionEvent.ACTION_MOVE:
								if (event.getY() < 0) {
									int f = (int) ((event.getY() + 300) / 100);
									if (f < 0) {
										f = 0;
									}
									selectKeyboardView.setTarget(f);
								}
								break;
							case MotionEvent.ACTION_UP:
								hideSelectKeyboard();
								setKeyboard(selectKeyboardView.getSelect());
								break;
							}
							return false;
						}
					});
				}
				// 手繪
				b = ((Button) keyBoardView[i].findViewById(R.id.function4));
				if (b != null) {
					b.setOnClickListener(new Button.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent i = new Intent(MainActivity.this,
									DrawActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(i);

						}
					});

					// 聯想
					b = ((Button) keyBoardView[i].findViewById(R.id.function6));
					if (b != null) {
						b.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent i = new Intent(MainActivity.this,
										FindActivity.class);
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(i);
							}
						});
					}

					// 空白

					b = ((Button) keyBoardView[i].findViewById(R.id.function7));
					if (b != null) {
						b.setOnClickListener(new Button.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								getCurrentInputConnection().commitText(
										writeWord + " ", 1);
								writeWord = "";
								handCandidateView.setSuggestions(null, true,
										true);

							}
						});
					}

					b = ((Button) keyBoardView[i].findViewById(R.id.function5));
					if (b != null) {
						b.setOnClickListener(new Button.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent i = new Intent(MainActivity.this,
										InputVoiceActivity.class);
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(i);

							}
						});
					}
				}
			}
		}
		selectKeyboardView.setKeyboard(initKeyboard);
		return keyBoardView[initKeyboard];
	}

	// handle typing----------------------------------------------------------
	// 0手寫1注音2英文3符號
	void setKeyboard(int num) {
		setInputView(keyBoardView[num]);
		if (num == 0) {
			handler.sendEmptyMessage(3);
		}
		chineseCandidateView.setSuggestions(null, true, true);
		handCandidateView.setSuggestions(null, true, true);
		setCandidatesViewShown(false);
		// if (num == 1) {
		// setCandidatesView(chineseCandidateView);
		//
		// } else if (num == 0) {
		// setCandidatesView(handCandidateView);
		// setCandidatesViewShown(true);
		//
		// }
		nowKeyboard = num;
	}

	void showTypingWord(String w, float x, float y) {
		typingView.setText(w);
		typingView.setX(x);
		typingView.setY(y);
		typingView.setVisibility(View.VISIBLE);

	}

	void hideTypingWord() {
		typingView.setVisibility(View.GONE);
	}

	void showSelectKeyboard(float x, float y) {
		selectKeyboardView.setX(x);
		selectKeyboardView.setY(y);
		selectKeyboardView.setVisibility(View.VISIBLE);
	}

	void hideSelectKeyboard() {
		selectKeyboardView.setVisibility(View.GONE);
	}

	void typeEnglish(String s) {
		getCurrentInputConnection().commitText(s, 1);
	}

	void typeChinese(String s, int n) {
		String[] tmp = chineseAll.clone();
		if (n <= 21) {
			tmp[0] = s;
		} else if (n <= 24) {
			tmp[1] = s;
		} else if (n <= 37) {
			tmp[2] = s;
		} else if (n <= 41) {
			tmp[3] = s;
		}
		String q = tmp[0] + tmp[1] + tmp[2] + tmp[3];
		chineseAll = tmp;

		if (!chineseSound.containsKey(q)) {
			candidateList = new ArrayList<String>();
		} else {
			candidateList = (List<String>) (chineseSound.get(q)).clone();
		}
		candidateList.add(0, q);
		chineseCandidateView.setSuggestions(candidateList, true, true);
		setCandidatesViewShown(true);

	}

	void openHandCandidate(boolean flag, int count) {
		// Toast.makeText(this, "open hand " + flag, Toast.LENGTH_SHORT).show();
		if (flag) {
			int x = 204 + ((count / 4) + 1) * 146;
			handCandidateView.setLayoutParams(new RelativeLayout.LayoutParams(
					x, 584));
			handCandidateView.setX(1080 - x);
		} else {
			handCandidateView.setLayoutParams(new RelativeLayout.LayoutParams(
					292, 584));
			handCandidateView.setX(788);
		}
	}

	// 能显示出手写轨迹的view
	public class WriteView extends SurfaceView {

		SurfaceHolder mSurfaceHolder = null;

		Paint mPaint = null;
		Paint mTextPaint = null; // 文字画笔
		public static final int FRAME = 60;// 画布更新帧数
		boolean mIsRunning = false; // 控制是否更新
		float posX, posY; // 触摸点当前座标
		Bitmap back;

		public WriteView(Context context) {
			super(context);
			back = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.hwbk);
			// 设置拥有焦点
			this.setFocusable(true);
			// 设置触摸时拥有焦点
			this.setFocusableInTouchMode(true);
			// 获取holder
			mSurfaceHolder = this.getHolder();
			// 添加holder到callback函数之中
			// mSurfaceHolder.addCallback(this);

			// 创建画笔
			mPaint = new Paint();
			mPaint.setColor(Color.argb(0xff, 0xff, 0xcc, 0));// 颜色
			mPaint.setAntiAlias(true);// 抗锯齿
			// Paint.Style.STROKE 、Paint.Style.FILL、Paint.Style.FILL_AND_STROKE
			// 意思分别为 空心 、实心、实心与空心
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔为圆滑状
			mPaint.setStrokeWidth(15);// 设置线的宽度

			// 创建路径轨迹
			mPath = new Path();

			// 创建文字画笔
			mTextPaint = new Paint();
			mTextPaint.setColor(Color.BLACK);
			mTextPaint.setTextSize(48);

			// 创建手写识别
			if (character == 0) {
				character = characterNew();
				characterClear(character);
				characterSetWidth(character, 1000);
				characterSetHeight(character, 1000);
			}
			if (recognizer == 0) {
				recognizer = recognizerNew();
			}

			// 打开成功返回1
			// modelState = recognizerOpen(recognizer,
			// "/data/data/handwriting-zh_CN.model");
			String s = Environment.getExternalStorageDirectory()
					+ "/download/handwriting-zh_TW2.model";
			Log.e("IME", "讀model");
			modelState = recognizerOpen(recognizer, s);
			if (modelState != 1) {
				System.out.println("model文件打开失败");
				return;
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			// 获取触摸动作以及座标
			int action = event.getAction();
			float x = event.getX();
			float y = event.getY();

			// 按触摸动作分发执行内容
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (str == 0) {
					if (handCandidateView.flagOpen) {
						handCandidateView.flagOpen = false;
						openHandCandidate(false, 0);
					}
				}
				flagWriting = true;
				mPath.moveTo(x, y);// 设定轨迹的起始点

				if (str == 0 && !writeWord.equals("")) {
					getCurrentInputConnection().commitText(writeWord,
							writeWord.length());
					writeWord = "";
				}
				str++;
				break;

			case MotionEvent.ACTION_MOVE:
				mPath.quadTo(posX, posY, x, y); // 随触摸移动设置轨迹
				characterAdd(character, handwriteCount,
						(int) (x * (1000f / 600)), (int) (y * (1000f / 600)));
				break;

			case MotionEvent.ACTION_UP:
				handwriteCount++;

				strokes = (int) characterStrokesSize(character);
				// 进行文字检索
				if (strokes > 0) {
					result = recognizerClassify(recognizer, character, 10);
					if (result > 0) {

						candidateList = new ArrayList<String>();

						for (int i = 0; i < result; i++) {
							String s = resultValue(result, i);
							if (s != null) {
								candidateList.add(s);
							}
							if (i > 10) {
								break;
							}
						}

						// 依照使用頻率重新排序
						ArrayList<String> tmp = new ArrayList<String>();
						for (int i = candidateList.size() - 1; i >= 0; i--) {
							if (chineseWeight.get(candidateList.get(i)) == null) {
								tmp.add(candidateList.remove(i));
							}
						}
						for (int i = tmp.size() - 1; i >= 0; i--) {
							candidateList.add(tmp.get(i));
						}
						writeWord = candidateList.get(0);
						handCandidateView.setSuggestions(candidateList, true,
								true);
					}
				}
				handler.sendEmptyMessageDelayed(0, 1000);
				break;
			}

			// 记录当前座标
			posX = x;
			posY = y;
			Draw();
			return true;
		}

		void Draw() {
			// 防止canvas为null导致出现null pointer问题
			Canvas mCanvas = mSurfaceHolder.lockCanvas();
			if (mCanvas != null) {
				// mCanvas.drawColor(Color.WHITE); // 清空画布
				mCanvas.drawBitmap(back, 0, 0, null);
				mCanvas.drawPath(mPath, mPaint); // 画出轨迹

				// mCanvas.drawLine(0, 600, 600, 600, mPaint);
				// mCanvas.drawLine(600, 0, 600, 600, mPaint);
				// // 数据记录
				// mCanvas.drawText("model打开状态 : " + modelState, 5, 50,
				// mTextPaint);
				// mCanvas.drawText("触点X的座标 : " + posX, 5, 100, mTextPaint);
				// mCanvas.drawText("触点Y的座标 : " + posY, 5, 150, mTextPaint);
				//
				// // mCanvas.drawText("总笔画数 : " + strokes, 5, 200, mTextPaint);
				//
				// // 显示识别出的文字
				// if (result != 0) {
				// for (int i = 0; i < resultSize(result); i++) {
				// mCanvas.drawText(resultValue(result, i) + " : "
				// + resultScore(result, i), 5, 300 + i * 50,
				// mTextPaint);
				// }
				// }
				mSurfaceHolder.unlockCanvasAndPost(mCanvas);
			}

		}

		boolean clear() {
			Canvas mCanvas = mSurfaceHolder.lockCanvas();
			if (mCanvas != null) {
				mCanvas.drawBitmap(back, 0, 0, null);
				mSurfaceHolder.unlockCanvasAndPost(mCanvas);
				return true;
			}
			return false;
		}
		/*
		 * @Override public void run() {
		 * 
		 * while (mIsRunning) { // 更新前的时间 long startTime =
		 * System.currentTimeMillis();
		 * 
		 * // 线程安全锁 synchronized (mSurfaceHolder) { mCanvas =
		 * mSurfaceHolder.lockCanvas(); Draw();
		 * mSurfaceHolder.unlockCanvasAndPost(mCanvas); } // 获取更新后的时间 long
		 * endTime = System.currentTimeMillis(); // 获取更新时间差 int diffTime = (int)
		 * (endTime - startTime); // 确保每次更新都为FRAME while (diffTime <= FRAME) {
		 * diffTime = (int) (System.currentTimeMillis() - startTime); //
		 * Thread.yield(): 与Thread.sleep(long millis):的区别， // Thread.yield():
		 * 是暂停当前正在执行的线程对象 ，并去执行其他线程。 // Thread.sleep(long
		 * millis):则是使当前线程暂停参数中所指定的毫秒数然后在继续执行线程 Thread.yield(); } }
		 * 
		 * }
		 */

		/*
		 * @Override public void surfaceCreated(SurfaceHolder holder) {
		 * mIsRunning = true; // mThread = new Thread(this); // mThread.start();
		 * }
		 * 
		 * @Override public void surfaceChanged(SurfaceHolder holder, int
		 * format, int width, int height) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void surfaceDestroyed(SurfaceHolder holder) {
		 * resultDestroy(result); characterDestroy(character);
		 * recognizerDestroy(recognizer); mIsRunning = false; mThread = null; }
		 */
	}

	// IME------------------------------------------------------------
	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		Log.e("IME", "onStart");
		if (!testString.equals("")) {
			getCurrentInputConnection().commitText(testString,
					testString.length());
			testString = "";
			// mInputMethodManager.showSoftInput( getCurrentInputConnection().,
			// 0);
		}
	}

	@Override
	public void onFinishInput() {
		super.onFinishInput();
		if (handCandidateView != null) {
			handCandidateView.setSuggestions(null, true, true);
			str = 0;

			characterClear(character);
			strokes = 0;
			mPath.reset();// 触摸结束即清除轨迹
			handwriteCount = 0;
			writeWord = "";
			writeView.clear();
		}
		if (chineseCandidateView != null) {
			for (int i = 0; i < 4; i++) {
				chineseAll[i] = "";
			}
			chineseCandidateView.setSuggestions(null, true, true);
			setCandidatesViewShown(false);
		}
		Log.e("IME", "onFinish");
	}

	public void pickSuggestionManually(int index) {
		// Toast.makeText(this, "" + index, Toast.LENGTH_LONG).show();
		getCurrentInputConnection().commitText(candidateList.get(index),
				candidateList.get(index).length());
		writeWord = "";

		if (nowKeyboard == 1) {
			for (int i = 0; i < 4; i++) {
				chineseAll[i] = "";
			}
			chineseCandidateView.setSuggestions(null, true, true);
			setCandidatesViewShown(false);
		} else if (nowKeyboard == 0) {
			handCandidateView.setSuggestions(null, true, true);
		}
	}

	// ------------------------------------------------------------------------------------
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub

	}

	// --------------------------------------------------------------
	// jni封装方法的声明
	// charater
	public native long characterNew();

	public native void characterDestroy(long c);

	public native void characterClear(long stroke);

	public native int characterAdd(long character, long id, int x, int y);

	public native void characterSetWidth(long character, long width);

	public native void characterSetHeight(long character, long height);

	public native long characterStrokesSize(long character);

	// recognizer
	public native long recognizerNew();

	public native void recognizerDestroy(long recognizer);

	public native int recognizerOpen(long recognizer, String filename);

	public native String recognizerStrerror(long recognizer);

	public native long recognizerClassify(long recognizer, long character,
			long nbest);

	// result
	public native String resultValue(long result, long index);

	public native float resultScore(long result, long index);

	public native long resultSize(long result);

	public native void resultDestroy(long result);

	// 载入.so文件
	static {
		Log.e("IME", "讀jni");
		System.loadLibrary("zinniajni");
	}

}
