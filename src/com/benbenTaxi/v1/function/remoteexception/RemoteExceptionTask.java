package com.benbenTaxi.v1.function.remoteexception;


import android.util.Log;

import com.benbenTaxi.v1.function.Configure;
import com.benbenTaxi.v1.function.GetInfoTask;

/*
 * һҪ�ǲ���Զ���쳣���񣬿���ͨ�� 42.121.55.211:8081��һ��
 * �û��� super_admin ������ 8
 */
public class RemoteExceptionTask extends GetInfoTask{
	private final static String API						=	"/api/v1/client_exceptions";
	private final String TAG			     			= RemoteExceptionTask.class.getName();
	private Configure       mConfigure					=  null;
	private RemoteExceptionRequest	mExceptionRequest		= null;
	
	public RemoteExceptionTask(String exception)
	{
		mExceptionRequest		= new RemoteExceptionRequest(exception);
		mConfigure				= new Configure();
	}
	protected String getPostParams() {
		String str = mExceptionRequest.toJson().toString();
		Log.d(TAG,"get exception data " + str);
		return str;
	}

	protected String getApiUrl() {
		return "http://"+mConfigure.getService()+API;
	}
	protected void onPostExecute(Boolean succ) 
	{
		
		//Log.d(TAG,"the result is "+this.getResult() + "|"+succ);
	}

	public void go() 
	{
		execute();
		//boolean s = mJsonHttpRequest.post(getApiUrl(), getPostParams());
		//Log.d(TAG,"============|"+s);
		
	}
	
	
	@Override
	protected void onPostExecGet(Boolean succ) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onPostExecPost(Boolean succ) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onPostExecError(String type, int code) {
		// TODO Auto-generated method stub
		
	}
}
