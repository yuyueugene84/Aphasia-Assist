package com.tomhw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class HandDrawView extends SurfaceView {

	Path mPath = null;
	int mColor = Color.BLACK;
	ArrayList<MyPath> allPath = new ArrayList<MyPath>();
	SurfaceHolder mSurfaceHolder = null;

	Paint mPaint = null;
	Paint mTextPaint = null; // 文字画笔
	public static final int FRAME = 60;// 画布更新帧数
	boolean mIsRunning = false; // 控制是否更新
	float posX, posY; // 触摸点当前座标
	Handler h = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (!clear()) {
				sendEmptyMessage(0);
			}
		}
	};

	public HandDrawView(Context context) {
		super(context);

		// 设置拥有焦点
		this.setFocusable(true);
		// 设置触摸时拥有焦点
		this.setFocusableInTouchMode(true);

		// 获取holder
		mSurfaceHolder = this.getHolder();
		// mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		// 添加holder到callback函数之中
		// mSurfaceHolder.addCallback(this);

		// 创建画笔
		mPaint = new Paint();
		mPaint.setColor(Color.BLUE);// 颜色
		mPaint.setAntiAlias(true);// 抗锯齿
		// Paint.Style.STROKE 、Paint.Style.FILL、Paint.Style.FILL_AND_STROKE
		// 意思分别为 空心 、实心、实心与空心
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔为圆滑状
		mPaint.setStrokeWidth(10);// 设置线的宽度

		// 创建路径轨迹
		mPath = new Path();

	}

	public HandDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		// 设置拥有焦点
		this.setFocusable(true);
		// 设置触摸时拥有焦点
		this.setFocusableInTouchMode(true);
		// 获取holder
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		this.setBackgroundColor(Color.TRANSPARENT);
		// 添加holder到callback函数之中
		// mSurfaceHolder.addCallback(this);

		// 创建画笔
		mPaint = new Paint();
		mPaint.setColor(Color.BLUE);// 颜色
		mPaint.setAntiAlias(true);// 抗锯齿
		// Paint.Style.STROKE 、Paint.Style.FILL、Paint.Style.FILL_AND_STROKE
		// 意思分别为 空心 、实心、实心与空心
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔为圆滑状
		mPaint.setStrokeWidth(10);// 设置线的宽度

		// 创建路径轨迹
		mPath = new Path();
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
			mPath.moveTo(x, y);// 设定轨迹的起始点
			break;

		case MotionEvent.ACTION_MOVE:
			mPath.quadTo(posX, posY, x, y); // 随触摸移动设置轨迹
			break;

		case MotionEvent.ACTION_UP:
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
			mCanvas.drawColor(Color.WHITE); // 清空画布
			for (MyPath p : allPath) {
				mPaint.setColor(p.color);
				if (p.color == Color.WHITE) {
					mPaint.setStrokeWidth(20);
				} else {
					mPaint.setStrokeWidth(10);
				}
				mCanvas.drawPath(p.path, mPaint); // 画出轨迹
			}
			mPaint.setColor(mColor);
			if (mColor == Color.WHITE) {
				mPaint.setStrokeWidth(20);
			} else {
				mPaint.setStrokeWidth(10);
			}
			mCanvas.drawPath(mPath, mPaint); // 画出轨迹
			mSurfaceHolder.unlockCanvasAndPost(mCanvas);
		}

	}

	Uri save() {
		// 防止canvas为null导致出现null pointer问题

		Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(b);
		canvas.drawColor(Color.WHITE); // 清空画布
		for (MyPath p : allPath) {
			mPaint.setColor(p.color);
			if (p.color == Color.WHITE) {
				mPaint.setStrokeWidth(20);
			} else {
				mPaint.setStrokeWidth(10);
			}
			canvas.drawPath(p.path, mPaint); // 画出轨迹
		}
		if (mColor == Color.WHITE) {
			mPaint.setStrokeWidth(20);
		} else {
			mPaint.setStrokeWidth(10);
		}
		mPaint.setColor(mColor);
		canvas.drawPath(mPath, mPaint); // 画出轨迹
		File dir = Environment.getExternalStorageDirectory();
		// 產生要寫入的檔案
		File upload = new File(dir, "/gotcha/handdraw/test2.jpg");
		try {
			// 產生OutputStream
			FileOutputStream out = new FileOutputStream(upload);
			// 將Bitmap透過OutputStream寫成檔案
			b.compress(Bitmap.CompressFormat.JPEG, 90, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//
		// if (mCanvas != null) {
		// mCanvas.drawColor(Color.WHITE); // 清空画布
		// mCanvas.drawPath(mPath, mPaint); // 画出轨迹
		// }
		return Uri.fromFile(upload);
	}

	boolean clear() {
		allPath.clear();
		mPath.reset();
		Canvas mCanvas = mSurfaceHolder.lockCanvas();
		if (mCanvas != null) {
			mCanvas.drawColor(Color.WHITE); // 清空画布
			mSurfaceHolder.unlockCanvasAndPost(mCanvas);
			return true;
		}
		return false;
	}

	void init() {
		h.sendEmptyMessage(0);
	}

	void setColor(int c) {
		MyPath p = new MyPath(mPath, mColor);
		allPath.add(p);
		mPath = new Path();
		mColor = c;

	}

	class MyPath {
		Path path;
		int color;

		MyPath(Path p, int c) {
			path = p;
			color = c;
		}
	}
}
