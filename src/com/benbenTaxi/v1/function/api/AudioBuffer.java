package com.benbenTaxi.v1.function.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.os.Handler;

import com.benbenTaxi.v1.function.Configure;
import com.benbenTaxi.v1.function.GetInfoTask;

public class AudioBuffer {
	private Handler mH = null;
	
	public static final int MSG_PREPARE_OK = 0x9001;
	public static final int MSG_PREPARE_FAIL = 0x9002;
	
	public AudioBuffer(Handler h) {
		mH = h;
	}
	
	public void PrepareFile(String uri) {
		new GetFileTask(uri);
	}

	public String GetFile(String uri) {
		File sdCardDir = Environment.getExternalStorageDirectory();
		try {
			String fullPath = sdCardDir.getCanonicalPath()+ "benbenTaxi/audioBuf"+uri;
			File targetFile = new File(fullPath);
			if ( targetFile.exists() ) {
				return fullPath;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void RemoveFile(String uri) {
		File sdCardDir = Environment.getExternalStorageDirectory();
		try {
			String fullPath = sdCardDir.getCanonicalPath()+ "benbenTaxi/audioBuf"+uri;
			File targetFile = new File(fullPath);
			if ( targetFile.exists() ) {
				targetFile.delete();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class GetFileTask extends GetInfoTask {
		private String mUri = null;
		
		public GetFileTask(String uri) {
			mUri = uri;
			execute(mUri, Configure.getUserAgent(), GetInfoTask.TYPE_GET);
		}
		
		@Override
		protected void onPostExecGet(Boolean succ) {
			if ( succ ) {
				// �����ļ���������Ϣ
				File sdCardDir = Environment.getExternalStorageDirectory();
				try {
					String fullPath = sdCardDir.getCanonicalPath()+ "benbenTaxi/audioBuf"+mUri;
					File targetFile = new File(fullPath);
					DirRecursion.mkDir(targetFile.getParentFile());
					
					FileOutputStream ss = new FileOutputStream(targetFile);
					ss.write(this.toByte());
					ss.close();
					
					mH.dispatchMessage(mH.obtainMessage(MSG_PREPARE_OK, mUri));
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPostExecPost(Boolean succ) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onPostExecError(String type, int code) {
			mH.dispatchMessage(mH.obtainMessage(MSG_PREPARE_FAIL));
		}
		
	}
	
	public static class DirRecursion { //mkdirs()
		 public static void mkDir(File file){
			 if ( file.exists() ) 
				 return;
			 
			  if(file.getParentFile().exists()){
			   file.mkdir();
			  }else{
			   mkDir(file.getParentFile());
			   file.mkdir();
			  }
			 }
	}
}
