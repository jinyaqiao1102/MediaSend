package com.example.videotest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.videotest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button start, stop;
	private ListView listview;
	private String filePath;
	private List<String> listAudioFileName;
	private File dir, fileAudio;
	private String fileAudioName;
	private ArrayAdapter<String> adaperListAudio;
	private TextView tv_audio_state;
	private MediaRecorder mediaRecorder; // 录音控制
	private File fileAudioList;
	public static Handler mhander = null;
	private SeekBar seekbar;
	private Button delete;
	private ImageView iv_start_or_pause;
	private boolean isPlaying = false;
	private String fileAudioNameList;
	private ArrayList<String> list = new ArrayList<String>();
	private BaseAdapter baseAdapter;
	private AudioRecord audioRecord;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		start = (Button) findViewById(R.id.button_start);
		stop = (Button) findViewById(R.id.button_stop);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		delete = (Button) findViewById(R.id.button_delete);
		listview = (ListView) findViewById(R.id.listViewAudioFile);
		iv_start_or_pause = (ImageView) findViewById(R.id.iv_start);
		iv_start_or_pause.setEnabled(false);
		baseAdapter = new ListAdapter();
		// 播放和暂停按钮
		iv_start_or_pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isPlaying) {
					isPlaying = false;
					iv_start_or_pause
							.setBackgroundResource(R.drawable.btn_start);
					Intent intent = new Intent(MainActivity.this,
							MusicPlayer.class);
					intent.putExtra("state", "pause");
					startService(intent);
				} else {
					isPlaying = true;
					iv_start_or_pause
							.setBackgroundResource(R.drawable.btn_pause);
					Intent intent = new Intent(MainActivity.this,
							MusicPlayer.class);
					intent.putExtra("state", "restart");
					
					startService(intent);
				}
			}

		});
		// listview的点击事件
		listview.setOnItemClickListener(new listViewClicked());
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (delete.getText().toString().equals("删除")) {
					delete.setText("确定");
					if (isPlaying) {

						Toast.makeText(MainActivity.this, "正在播放！不能删除文件",
								Toast.LENGTH_SHORT).show();

						return;
					} else {
						listview.setAdapter(baseAdapter);
						listview.setOnItemClickListener(null);
					}
				} else {
					if(0!=list.size()){
					for(int i=0;i<list.size();i++){
						 String fileAudioNameList =listAudioFileName.get(Integer.parseInt(list.get(i)));
						 fileAudioList = new File(filePath + "/" + fileAudioNameList);
						 if ( null !=fileAudioList) {
							 fileAudioList.delete();
							 //adaperListAudio.remove(fileAudioNameList);
						 }
					}
					listAudioFileName.clear();
					listAudioFileName=getFileFormSDcard(dir, ".mp3");
					adaperListAudio.notifyDataSetChanged();
					}
					listview.setAdapter(adaperListAudio);
					delete.setText("删除");
					listview.setOnItemClickListener(new listViewClicked());
				}
			}
		});
//		 listview.setOnItemLongClickListener(new OnItemLongClickListener() {
//		
//		 @Override
//		 public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
//		 int arg2, long arg3) {
//		 // TODO Auto-generated method stub
//		 seekbar.setProgress(0);
//		 String fileAudioNameList = ((TextView) arg1).getText()
//		 .toString();
//		 fileAudioList = new File(filePath + "/" + fileAudioNameList);
//		 // openFile(fileAudioList);
//		 if (fileAudioList != null) {
//		 fileAudio = fileAudioList;
//		 fileAudioName = fileAudioNameList;
//		 showDeleteAudioDialog("是否删除" + fileAudioName + "文件", "不删除",
//		 "删除");
//		 } else {
//		 showTheAlertDialog(MainActivity.this, "该文件不存在");
//		 }
//		 return false;
//		 }
//		 });
		tv_audio_state = (TextView) findViewById(R.id.tv_audio_state);
		// 开始录音按钮
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isHaveSDcard()) {
					Toast.makeText(MainActivity.this, "请插入SD卡以便存储录音",
							Toast.LENGTH_LONG).show();
					return;
				}
				Time localTime = new Time();
				localTime.setToNow();
				localTime.format("%Y%m%d%H%M%S");
				fileAudioName = "audio" + localTime.format("%Y%m%d%H%M%S")
						+ ".mp3";
				
				mediaRecorder = new MediaRecorder();
				// 设置录音的来源为麦克风
				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				mediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				mediaRecorder.setOutputFile(filePath + "/" + fileAudioName);
				try {
					mediaRecorder.prepare();
					mediaRecorder.start();
					tv_audio_state.setText("录音中。。。");

					fileAudio = new File(filePath + "/" + fileAudioName);
					start.setEnabled(false);
					stop.setEnabled(true);
					delete.setEnabled(false);
					// listViewAudio.setEnabled(false);
					// isLuYin = true;
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		// 录音停止按钮
		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (null != mediaRecorder) {
					// 停止录音
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
					tv_audio_state.setText("录音停止了");
					// 开始键能够按下
					start.setEnabled(true);
					stop.setEnabled(false);
					listview.setEnabled(true);
					// 删除键能按下
					delete.setEnabled(true);
					adaperListAudio.add(fileAudioName);

				}
			}

		});
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (isPlaying) {
					Intent intent = new Intent(MainActivity.this,
							MusicPlayer.class);
					intent.putExtra("state", "seek");
					intent.putExtra("position", seekBar.getProgress());
					startService(intent);
				}
			}

		});
		if (!isHaveSDcard()) {
			Toast.makeText(MainActivity.this, "请插入SD卡以便存储录音", Toast.LENGTH_LONG)
					.show();
			return;
		}

		// 要保存的文件的路径
		filePath = getSDPath() + "/" + "myAudio";
		// 实例化文件夹
		dir = new File(filePath);
		if (!dir.exists()) {
			// 如果文件夹不存在 则创建文件夹
			dir.mkdir();
		}
		Log.i("test", "要保存的录音的文件名为" + fileAudioName + "路径为" + filePath);
		listAudioFileName = getFileFormSDcard(dir, ".mp3");
		adaperListAudio = new ArrayAdapter<String>(MainActivity.this,
				android.R.layout.simple_list_item_1, listAudioFileName);
		listview.setAdapter(adaperListAudio);
		// 通过hander来对seekbar的控制
		mhander = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				try {
					switch (msg.what) {
					case 0:
						// 播放时更新seekBar
						int positon = msg.arg1;
						Log.i("chang", "msg.arg1:" + positon);
						seekbar.setProgress(positon);
						break;
					case 1:
						// try {
						// Button b1 = (Button) fragment1.goup
						// .findViewById(R.id.play);
						// Button b = (Button) fragment2.goup
						// .findViewById(R.id.play);
						//
						// b.setBackgroundResource(R.drawable.pause_selector);
						// b1.setBackgroundResource(R.drawable.pause_selector);
						// MyService.mfrag2.updatevie(MyService.msindex);
						// } catch (Exception e) {
						// // TODO: handle exception
						// }

						break;
					case 2:
						// 设置seekbar长度
						seekbar.setMax(msg.arg1);
						// Log.e("=========================================",
						// msg.arg1+"");
						break;
					case 3:
						// 播放完毕
						seekbar.setProgress(msg.arg1);
						seekbar.setProgress(0);
						// Log.e("=========================================3",
						// msg.arg1+"");
						break;
					default:

						break;

					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				// TODO Auto-generated method stub

			}

		};
	}

	/****************************************************************
	 * 
	 * 判断SD卡是否存在
	 * 
	 * @return
	 */
	public static boolean isHaveSDcard() {
		// 判断SD卡是否存在 存在返回true 不存在返回false
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/*****************************************************************
	 *
	 * 把文件保存到SD卡中
	 * 
	 * @param data
	 * @param pathName
	 * @param fileName
	 * @throws IOException
	 */
	public static void saveFileToSDcard(byte[] data, String pathName,
			String fileName) throws IOException {
		// 要保存的文件的路径
		String filePath = getSDPath() + "/" + pathName;
		Log.i("test", "pathName====" + filePath + fileName);
		// 实例化文件夹
		File dir = new File(filePath);
		if (!dir.exists()) {
			// 如果文件夹不存在 则创建文件夹
			dir.mkdir();
		}
		Log.i("test", filePath);
		File file = new File(filePath + "/" + fileName);
		if (!file.exists()) {
			// 如果文件不存在 创建
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(data);
			bos.flush();
			bos.close();
			fos.close();
		}
	}

	/*****************************************************************
	 * 
	 * 获得SD卡的路径
	 */
	public static String getSDPath() {
		String sdDir = null;
		if (isHaveSDcard()) {
			sdDir = Environment.getExternalStorageDirectory().toString();// 获得根目录
		}
		return sdDir;
	}

	/**
	 * *************************************************************************
	 * 
	 * 从SD卡的dir目录下得到type类型的文件
	 * 
	 * @param path
	 * @param type
	 * @return
	 */
	public static List<String> getFileFormSDcard(File dir, String type) {
		List<String> listFilesName = new ArrayList<String>();
		if (isHaveSDcard()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().indexOf(".") >= 0) {
						// 只取Type类型的文件
						String filesResult = files[i].getName().substring(
								files[i].getName().indexOf("."));
						if (filesResult.toLowerCase()
								.equals(type.toLowerCase())) {
							listFilesName.add(files[i].getName());
						}

					}
				}
			}
		}
		return listFilesName;
	}

	@SuppressWarnings("deprecation")
	public void showDeleteAudioDialog(String messageString,
			String button1Title, String button2Title) {
		AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
				.create();
		dialog.setTitle("提示");
		dialog.setMessage(messageString);
		dialog.setButton(button1Title, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		dialog.setButton2(button2Title, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				fileAudio.delete();
				adaperListAudio.remove(fileAudioName);
				fileAudio = null;
			}
		});

		dialog.show();
	}

	@SuppressWarnings("deprecation")
	public static void showTheAlertDialog(Context context, String text) {
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle("提示");
		dialog.setMessage(text);
		dialog.setButton3("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		dialog.show();
	}

	class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listAudioFileName.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
			View view = inflater.inflate(R.layout.list_item, null);
			TextView tv = (TextView) view.findViewById(R.id.item_name);
			tv.setText(listAudioFileName.get(position));
			CheckBox cb = (CheckBox) view.findViewById(R.id.item_check);
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked) {
						if (list.contains(String.valueOf(position))) {
							return;
						} else {
							list.add(String.valueOf(position));
						}
					} else {
						list.remove(String.valueOf(position));
					}
				}

			});
			return view;
		}

	}
	public class listViewClicked implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			isPlaying = true;
			iv_start_or_pause.setEnabled(true);
			iv_start_or_pause.setBackgroundResource(R.drawable.btn_pause);
			Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
			intent.putExtra("state", "play");
			fileAudioNameList = listAudioFileName.get(position);
			intent.putExtra("file", filePath + "/" + fileAudioNameList);
			startService(intent);
		}
		
	}
}
