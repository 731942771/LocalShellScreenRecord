package com.cuiweiyou.localadb2;

import java.io.IOException;
import java.io.OutputStream;

import android.os.AsyncTask;

public class RecordTask extends AsyncTask<String, Void, String> {
	
	private RecordBack ctx;
	private Process adb;
	private String file;

	public RecordTask(RecordBack ctx) {
		this.ctx = ctx;
	}

	@Override
	protected String doInBackground(String... params) {
		
		file = params[1];
		String result = execAdb(params[0]);
		
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		ctx.done(result);
	}
	
	/** execAdb， 执行adb命令 */
	private String execAdb(String command) {
		
		try {
			Thread.sleep(3000); // 延迟录制
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			adb = Runtime.getRuntime().exec("su", null, null);
			OutputStream os = adb.getOutputStream();
			// 这个command即关键所在
			os.write(("/system/bin/" + command).getBytes("ASCII"));
			os.flush();
			os.close();
			int res = adb.waitFor();
			return "" + res;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			adb.destroy();
		}
	}
}
