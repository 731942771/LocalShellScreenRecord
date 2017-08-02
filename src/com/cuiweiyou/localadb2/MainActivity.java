package com.cuiweiyou.localadb2;

import java.io.DataOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <b>类名</b>: 手机录屏测试 <br/>
 * <b>说明</b>: <br/>
 * <b>创建</b>: 2016-2016年1月3日_下午2:32:32 <br/>
 * 
 * @author cuiweiyou.com <br/>
 * @version 1 <br/>
 */
public class MainActivity extends Activity implements RecordBack {

	private Spinner mSize, mBitrate;
	private EditText mLimit, mFile;
	private TextView mState;

	private Boolean isRoot = false;
	private int times = 0;
	private String file;
	private Button mRecord;

	// UI更新器
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case 0:
				String toa = "录制结束，文件路径：" + file;
				mState.setText(toa);

				Toast toast = new Toast(MainActivity.this);
				toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 100);
				toast.setDuration(Toast.LENGTH_LONG);
				View view = View.inflate(MainActivity.this, R.layout.toast, null);
				((TextView) view.findViewById(R.id.toast)).setText(toa);
				toast.setView(view);
				toast.show();
				break;

			default:
				mState.setText("因权限或其它问题导致录制手机屏幕失败");
				Toast.makeText(MainActivity.this, "因权限或其它问题导致录制手机屏幕失败", 0).show();
				break;
			}

			mRecord.setEnabled(true);
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mState = (TextView) findViewById(R.id.state);

		// 申请root权限
		isRoot = upgradeRootPermission(getPackageCodePath());

		if (!isRoot) {
			Toast.makeText(this, "系统未ROOT，不能实现原生录制屏幕", 0).show();
			mState.setText("系统未ROOT，不能实现原生录制屏幕\n");
		}

		// 判断系统版本
		final int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < 19) {
			Toast.makeText(this, "低于 Android 4.4 版系统中不能实现原生录制屏幕", 0).show();
			mState.append("系统未ROOT，不能实现原生录制屏幕");
		}

		mBitrate = (Spinner) findViewById(R.id.bitrate);
		mSize = (Spinner) findViewById(R.id.size);

		mLimit = (EditText) findViewById(R.id.limit);
		mFile = (EditText) findViewById(R.id.file);

		mRecord = (Button) findViewById(R.id.record);
		mRecord.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isRoot && currentapiVersion >= 19) {

					mRecord.setEnabled(false);

					// 切换到桌面
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					startActivity(intent);

					// 子线程录屏
					new RecordTask(MainActivity.this).execute(getInstruction(), file);
				}
			}
		});

		findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				times++;
				if (times > 6) {
					Intent i = new Intent(MainActivity.this, AboutActivity.class);
					startActivity(i);
				}
			}
		});
	}

	/** 拼接指令 */
	private String getInstruction() {
		file = mFile.getText().toString();
		if (null == file || "".equals(file)) {
			Toast.makeText(this, "文件名是必须的", 0).show();
			return null;
		}

		file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + file;

		String size = mSize.getSelectedItem().toString();
		String rate = mBitrate.getSelectedItem().toString();
		String limit = mLimit.getText().toString();

		StringBuilder sb = new StringBuilder();

		sb.append("screenrecord");

		if (null != size && !"".equals(size) && !"driver screen".equals(size)) {
			sb.append(" --size " + size);        // 尺寸
		}
		if (null != rate && !"".equals(rate)) {
			sb.append(" --bit-rate " + rate);    // 帧频
		}
		if (null != limit && !"".equals(limit)) {
			sb.append(" --time-limit " + limit); // 时长
		}

		sb.append(" " + file);                   // 文件名

		return sb.toString();
	}

	/** 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限) */
	public boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}

		return true;
	}

	/** consoleAdb，打印adb日志 */
	@Override
	public void done(String result) {

		Message msg = handler.obtainMessage();

		if ("0".equals(result)) {
			msg.arg1 = 0;
		} else {
			msg.arg1 = 1;
		}

		handler.sendMessage(msg);
	}
}
