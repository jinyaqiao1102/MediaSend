package com.example.videotest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.media.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UDPTest extends Activity {
	Button btn;
	EditText edit;
	public static final int SERVERPORT = 4444;
	private boolean isRecord = false;// 设置正在录制的状态s
	private AudioRecord audioRecord;
	// 音频获取源
	private int audioSource = MediaRecorder.AudioSource.MIC;
	// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
	private static int sampleRateInHz = 22050;
	// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	// 缓冲区字节大小
	private int bufferSizeInBytes = 0;
	private String IPAddress;
	//AudioTrack audio;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udp);
		edit = (EditText) findViewById(R.id.udp_ip);
		btn = (Button) findViewById(R.id.udp_btn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				audioRecord.startRecording();
				// 让录制状态为true
				isRecord = true;
				IPAddress=edit.getText()
						.toString().trim();
				new Thread(new Client()).start();

			}

		});
		// 获得缓冲区字节大小
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		Log.e("======bufferSizeInBytes",
				bufferSizeInBytes + "");
		// 创建AudioRecord对象
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
		
	}

	public class Client implements Runnable {
		@Override
		public void run() {

			try {
				byte[] audiodata = new byte[7104];
				int readsize = 0;
				//audio = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				//		AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				//		bufferSizeInBytes, AudioTrack.MODE_STREAM);
				//audio.play();
				while (isRecord == true) {
					readsize = audioRecord.read(audiodata, 0, 7104);
//					Log.e("=============","readSize"+bufferSizeInBytes);
//					Log.e("=============","AudioRecord.ERROR_INVALID_OPERATION "+AudioRecord.ERROR_INVALID_OPERATION );
//					Log.e("=============","readsize "+readsize);
					if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
						InetAddress serverAddr = InetAddress.getByName(IPAddress);
						DatagramSocket socket = new DatagramSocket();
						Log.e("=============","AudioRecord.ERROR_INVALID_OPERATION "+AudioRecord.ERROR_INVALID_OPERATION );
						//byte[] buf;
						//buf = ("Default message").getBytes();
						DatagramPacket packet = new DatagramPacket(audiodata, audiodata.length,
								serverAddr, SERVERPORT);
						socket.send(packet);
						//audio.write(audiodata, 0, 7104);
						
					}
					
					//Thread.sleep(20);
				}
			//	audio.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
