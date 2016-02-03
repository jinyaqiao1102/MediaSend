package com.example.videotest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MusicPlayer extends Service {
	private static final String PLAY = "play";
	private static final String PAUSE = "pause";
	private static final String RESTART = "restart";
	private static final String SEEK = "seek";
	public Thread mseekbarthread = null;
	static MediaPlayer mediaPlayer;
	String filepath;
	String state;
	int audioLength;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mediaPlayer = new MediaPlayer();
		// 播放完成执行的方法，将seekbar置0
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Log.e("completion=====", audioLength + "");
				Message msg = MainActivity.mhander.obtainMessage();
				msg.what = 3;
				msg.arg1 = audioLength;
				msg.sendToTarget();
			}

		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		// 初始化seekBar更新线程
		getinitseekhread(getApplicationContext());
		// 通过state来控制播放
		if (null != intent) {
			state = intent.getStringExtra("state");
			if (PLAY.equals(state)) {
				filepath = intent.getStringExtra("file");
				this.Play(filepath);
			} else if (PAUSE.equals(state)) {
				mediaPlayer.pause();
			} else if (RESTART.equals(state)) {
				mediaPlayer.start();
			} else if (SEEK.equals(state)) {
				int position = intent.getIntExtra("position", 0);
				mediaPlayer.seekTo(position);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mediaPlayer.release();
	}

	// 播放文件
	public void Play(String path) {
		filepath = path;
		try {

			mediaPlayer.reset();
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			int buffersize = fis.available();
			byte[] buffer = new byte[buffersize];
			fis.read(buffer);
			FileOutputStream fos = new FileOutputStream("/sdcard/a.mp3");
			fos.write(buffer);
			fis.close();
			fos.close();
			mediaPlayer.setDataSource("/sdcard/a.mp3");
			mediaPlayer.prepare();
			mediaPlayer.start();
			Message msg = MainActivity.mhander.obtainMessage();
			audioLength = mediaPlayer.getDuration();
			msg.what = 2;
			msg.arg1 = audioLength;
			msg.sendToTarget();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 暂停播放
	public void Purse() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		}

	}

	// 继续播放
	public void ReStart() {
		if (mediaPlayer != null) {
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
			}
		}
	}

	// 更新seekbar线程
	static public class myseekbarThread extends Thread {

		private Context mcont = null;

		public static Boolean isrun = true;

		public myseekbarThread(Context con) {
			mcont = con;
			start();
		}

		@Override
		public void run() {

			while (true) {
				// Mp3Thread.state = Myconst.SEEKBAR_PROGRESS;
				try {

					// System.out.println(mp3Player.GetPlayerTime());
					if (mediaPlayer.isPlaying() == true) {

						Message msg = MainActivity.mhander.obtainMessage();
						msg.what = 0;
						msg.arg1 = GetPlayerTime();
						msg.sendToTarget();
					}
					sleep(50);
				} catch (Exception e) {
				}
			}
		}
	}

	// 获得当前的播放进度
	public static int GetPlayerTime() {
		int i = 10000;
		try {
			i = mediaPlayer.getCurrentPosition();

		} catch (Exception e) {
			// TODO: handle exception
			// initPlayer(mplayfilename);
			// i = mediaPlayer.getCurrentPosition();
		}
		return i;
	}

	// 初始化seekbar线程
	public Thread getinitseekhread(Context con) {

		if (mseekbarthread == null)
			mseekbarthread = new myseekbarThread(con);
		return mseekbarthread;
	}
}
