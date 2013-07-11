package com.benbenTaxi.v1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.LocationData;
import com.benbenTaxi.R;
import com.benbenTaxi.v1.function.BaseLocationActivity;
import com.benbenTaxi.v1.function.DataPreference;
import com.benbenTaxi.v1.function.RequestAdapter;
import com.benbenTaxi.v1.function.ShowDetail;
import com.benbenTaxi.v1.function.StatusMachine;
import com.benbenTaxi.v1.function.WaitingShow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ListMode extends BaseLocationActivity {
	protected ListView mLv;
	protected Button mBtnPos, mBtnNeg;
	private RequestAdapter mReqAdapter;
	private int mReqId = -1;
	private JSONObject mConfirmObj;
	private WaitingShow mWs; // �ȴ���Ӧpopwin
	private DataPreference mData;
	
	private String tip_pos, tip_neg;
	
	private Handler MsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WaitingShow.MSG_HANDLE_REQ_TIMEOUT:
				Toast.makeText(ListMode.this, "����ʱ��������ѡ��", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_dialog);
		
		mApp = (BenbenApplication) this.getApplicationContext();
		Bundle tips = getIntent().getExtras();
		if ( tips != null ) {
			tip_pos = tips.getString("pos");
			tip_neg = tips.getString("neg");
		}
		
		init();
		
		mData = new DataPreference(this.getApplicationContext());
		mWs = new WaitingShow("�ȴ��˿���Ӧ", 30, getLayoutInflater().inflate(R.layout.waiting_dialog, null));
    	mWs.SetNegativeOnclick("ȡ������", null);
    	mWs.setHandler(MsgHandler);
	}
	
	private void init() {
    	mLv = (ListView)findViewById(R.id.listView);
    	mBtnPos = (Button)findViewById(R.id.btnListOk);
    	mBtnNeg = (Button)findViewById(R.id.btnListCancel);
    	
    	do_init_functions();
    	
    	if ( tip_pos!=null && mApp.getRequestID()>0 ) {
			mBtnPos.setText(tip_pos);
			mBtnPos.setOnClickListener(mPosfunc);
		} else {
			mBtnPos.setVisibility(View.GONE);
		}
		if ( tip_neg != null ) {
			mBtnNeg.setText(tip_neg);
			mBtnNeg.setOnClickListener(mNegfunc);
		} else {
			mBtnNeg.setVisibility(View.GONE);
		}
	}

	protected void do_init_functions() {
		// ������������
		JSONArray req = super.mApp.getCurrentRequestList();
		
		mReqAdapter = new RequestAdapter(req, this, mApp);
		mLv.setAdapter(mReqAdapter);
		
		mLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if ( mReqId >= 0 ) {
					Toast.makeText(ListMode.this, "���������ڴ�����", Toast.LENGTH_SHORT).show();
					return;
				}
				
				JSONObject obj = (JSONObject) mReqAdapter.getItem(arg2);
				if ( obj != null ) {
					mConfirmObj = obj;
					try {
						mReqId = mConfirmObj.getInt("id");
						ShowDetail.showPassengerRequestInfo(mApp, ListMode.this, mReqId, mConfirmObj);
					} catch (JSONException e) {
						mReqId = -1;
						Toast.makeText(ListMode.this, "ѡȡ��������Ϣ��������", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(ListMode.this, "��Ч��������Ϣ��������ѡȡ��", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		mPosfunc = new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				if ( mReqAdapter.isLastPage() ) {
					Toast.makeText(ListMode.this, "�������һ���˿�����", Toast.LENGTH_SHORT).show();
				} else {
					mReqAdapter.refreshIdx();
				}
			}
		};
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
		case 1:
			// ���Ե���û�����ͼ�꣬˾�������û�����
			if ( resultCode > 0 && mReqId >= 0 ) {
				LocationData locData = mApp.getCurrentLocData();
				StatusMachine sm = new StatusMachine(this, MsgHandler, mData, mConfirmObj);
	    		// �������ñ����reqid����ֹ������Ϊ��Чֵ
	    		sm.driverConfirm(locData.longitude, locData.latitude, mReqId);
	    		
	    		// ��ʾ�ӳٽ��������ȴ�30s
	    		// �����ѽ��������ʹ��popwin��ע�ⲻҪ�ڻص�������dismiss��ǰ��popwin
	    		mWs.show();

			} else if ( resultCode > 0 ) {
				Toast.makeText(this, "����������Ч����ѡȡ��������", Toast.LENGTH_SHORT).show();
			} else {
			}
			break;
		default:
			break;
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list, menu);
        return true;
    }  
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_map_mode:
			// ���ص�ͼģʽ
			this.setResult(0);
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
