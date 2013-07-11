package com.benbenTaxi.v1.function;

import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.LocationData;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.benbenTaxi.v1.BenbenApplication;
import com.benbenTaxi.v1.BenbenLocationMain.MyLocationListenner;
import com.benbenTaxi.v1.BenbenLocationMain.NotifyLister;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BaseLocationActivity extends Activity {
	// ��λ���
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
    public NotifyLister mNotifyer=null;
    
    protected LocationData locData = null;
    protected MsgHandler mH = null;
    protected BenbenApplication mApp = null;
    protected DataPreference mData;
    
    protected int mReqId = -1;
    protected JSONObject mConfirmObj;
    
	public final static int MSG_HANDLE_POS_REFRESH = 2;
	public final static int MSG_HANDLE_REQ_TIMEOUT = 3;
	public final static int MSG_HANDLE_ITEM_TOUCH = 10000;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locData = new LocationData();
		mH = new MsgHandler();
		mApp = (BenbenApplication) this.getApplicationContext();
		mData = new DataPreference(this.getApplicationContext());
	}
    
    protected void refreshProcess() {
    	// �ϱ�˾��λ��
    	StatusMachine dtt = new StatusMachine(this, mH, mData, null);
    	dtt.driverReport(locData.longitude, locData.latitude, locData.accuracy, "gsm");
    	
    	// ��ȡtaxirequest
    	if ( mReqId < 0 ) {
    		StatusMachine drvreq = new StatusMachine(this, mH, mData, null);
    		drvreq.driverGetRequest(locData.longitude, locData.latitude, locData.accuracy);       	
    	} else {
    	// ��ѯrequest
    		StatusMachine drvask = new StatusMachine(this, mH, mData, mConfirmObj);
    		drvask.driverAskRequest(mReqId);
    	}
    }

	/**
     * ��������������λ�õ�ʱ�򣬸�ʽ�����ַ������������Ļ��
     */
    public class MyLocationListenner implements BDLocationListener {
        //private int mCountFactor = 0; // ������������ִ��Ƶ��
        
    	@Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;
            
            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();
            locData.accuracy = location.getRadius();
            locData.direction = location.getDerect();
            
            refreshProcess();
            mApp.setCurrentLocData(locData);
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }
    
    public class MsgHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
    	
    }
}
