package com.benbenTaxi.v1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.LocationData;
import com.benbenTaxi.R;
import com.benbenTaxi.v1.function.AudioProcessor;
import com.benbenTaxi.v1.function.BaseLocationActivity;
import com.benbenTaxi.v1.function.DelayTask;
import com.benbenTaxi.v1.function.PopupWindowSize;
import com.benbenTaxi.v1.function.RequestAdapter;
import com.benbenTaxi.v1.function.ShowDetail;
import com.benbenTaxi.v1.function.StatusMachine;
import com.benbenTaxi.v1.function.WaitingShow;

import android.content.Intent;
import android.database.DataSetObserver;
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
	private WaitingShow mWs; // �ȴ���Ӧpopwin
	private Handler waitingHandler, playHandler, delayHandler;
	
	private String tip_pos, tip_neg;
	protected View.OnClickListener mPosfunc, mNegfunc;
	
	private final static int CODE_SHOW_DETAIL = 0x101;
	private final static int CODE_SHOW_INFO = 0x102;
	private final static int CODE_SHOW_CONFIRM_INFO = 0x103;
	private final static int CODE_DELAY = 0x104;
	
	private AudioProcessor mAp = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listmode);
		
		Bundle tips = getIntent().getExtras();
		if ( tips != null ) {
			tip_pos = tips.getString("pos");
			tip_neg = tips.getString("neg");
		}
		
    	mAp = new AudioProcessor(true);
    	playHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case AudioProcessor.MSG_PLAY_READY:
					if ( msg.arg1 >=0 ) {
						mReqAdapter.setItemPlay(msg.arg1);
					}
					break;
				case AudioProcessor.MSG_PLAY_COMPLETE:
				case AudioProcessor.MSG_PLAY_ERROR:
					if ( msg.arg1 >=0 ) {
						mReqAdapter.setItemOrg(msg.arg1);
					}
					break;
				default:
					Toast.makeText(ListMode.this, "������Ϣ����["+msg.what+"]: "+(String)msg.obj, Toast.LENGTH_SHORT).show();
					break;
				}
			}
    	};
    	mAp.setHandler(playHandler);
    	
		init();
		
		waitingHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case WaitingShow.MSG_HANDLE_REQ_TIMEOUT:
					Toast.makeText(ListMode.this, "�����ѳ�ʱ��������ѡȡ��", Toast.LENGTH_SHORT).show();
					mReqAdapter.resetItemSelected();
					mWs.Dismiss();
					resetStatus();
					break;
				case WaitingShow.MSG_HANDLE_REQ_CANCEL:
					Toast.makeText(ListMode.this, "������ȡ����������ѡȡ��", Toast.LENGTH_SHORT).show();
					mReqAdapter.resetItemSelected();
					mWs.Dismiss();
					resetStatus();
					break;
				default:
					mWs.Dismiss();
					break;
				}
			}
		};
		mWs = new WaitingShow("�ȴ��˿���Ӧ", 30, PopupWindowSize.getPopupWindoWidth(this), 
				PopupWindowSize.getPopupWindowHeight(this), getLayoutInflater().inflate(R.layout.waiting_dialog, null));
    	mWs.SetNegativeOnclick("ȡ������", null);
    	mWs.setHandler(waitingHandler);
    	
    	super.setLocationRequest();
    	super.setLocationStart();
    	
    	mReqAdapter.registerDataSetObserver(new DataSetObserver(){  
    		
            public void onChanged() {  
            	//Toast.makeText(ListMode.this, "�����б�: "+mAp.getPlayListSize(), Toast.LENGTH_SHORT).show();
				//mAp.resetPlay();
				//mAp.batchPlay();
            }  
        });
    	//mAp.batchPlay();
    	
    	delayHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case DelayTask.MSG_DELAY_OK:
					if ( msg.arg1 == CODE_DELAY ) {
						Toast.makeText(ListMode.this, "�����б�: "+mAp.getPlayListSize(), Toast.LENGTH_SHORT).show();
						mAp.batchPlay();
					}
					break;
				default:
					break;
				}
			}
    	};
	}
	
	private void init() {
    	mLv = (ListView)findViewById(R.id.listView_listmode);
    	mBtnPos = (Button)findViewById(R.id.btnListOk_listmode);
    	mBtnNeg = (Button)findViewById(R.id.btnListCancel_listmode);
    	
    	do_init_functions();
    	
    	if ( tip_pos!=null ) {
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

	@Override
	protected void resetStatus() {
		resetAudioProcessor();
		super.resetStatus();
	}
	
	private void resetAudioProcessor() {
		mAp.resetPlay();
		mAp.resetPlayList();
	}

	protected void do_init_functions() {
		// ������������	
		mReqAdapter = new RequestAdapter(this, mLv, mApp, mAp);
		mLv.setAdapter(mReqAdapter);
		
		mLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int reqid = mApp.getRequestID();
				JSONObject lastobj = mApp.getCurrentObject();
				int lastid = -1, newid = -1;
				
				if ( lastobj != null ) {
					try {
						lastid = lastobj.getInt("id");
					} catch (JSONException e) {
						lastid = -1;
					}
				}
				
				JSONObject obj = (JSONObject) mReqAdapter.getItem(arg2);
				if ( obj != null ) {
					try {
						newid = obj.getInt("id");
					} catch (JSONException e) {
						newid = -1;
					}
				} else {
					Toast.makeText(ListMode.this, "��Ч��������Ϣ��������", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if ( reqid >= 0 || (lastid>=0 && lastid==newid && (mApp.getCurrentStat().equals(StatusMachine.STAT_SUCCESS) ||
						mApp.getCurrentStat().equals(StatusMachine.STAT_CANCEL) ||
						mApp.getCurrentStat().equals(StatusMachine.STAT_TIMEOUT))) )
				{
					Toast.makeText(ListMode.this, "��������["+reqid+"]�ڴ�����, ������["+lastid+"]�ѱ������", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				resetAudioProcessor();
				mReqAdapter.setItemSelected(arg2);
				ShowDetail.showPassengerRequestInfo(mApp, ListMode.this, obj, CODE_SHOW_DETAIL);
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
				resetAudioProcessor();
			}
		};
	}


	@Override
	protected void onResume() {
		super.onResume();
	}
	

	@Override
	protected void onDestroy() {
		mAp.release();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		int reqid = mApp.getRequestID();
		JSONObject reqobj = mApp.getCurrentObject();
		
		switch(requestCode) {
		case CODE_SHOW_DETAIL:
			// ���Ե���û�����ͼ�꣬˾�������û�����
			if ( resultCode > 0 ) {
				LocationData locData = mApp.getCurrentLocData();
				StatusMachine sm = new StatusMachine(mH, mData, reqobj);
	    		// �������ñ����reqid����ֹ������Ϊ��Чֵ
	    		sm.driverConfirm(locData.longitude, locData.latitude, reqid);
	    		
	    		// ��ʾ�ӳٽ��������ȴ�30s
	    		// �����ѽ��������ʹ��popwin��ע�ⲻҪ�ڻص�������dismiss��ǰ��popwin
	    		mWs.show();
			} else {
				mReqAdapter.resetItemSelected();
				resetStatus();
			}
			break;
		case CODE_SHOW_INFO:
			if ( resultCode > 0 ) {
				ShowDetail.showCall(this, reqobj);
			}
			break;
		case CODE_SHOW_CONFIRM_INFO:
			if ( resultCode > 0 ) {
				ShowDetail.showCall(this, mApp.getCurrentObject());
			}
			Toast.makeText(this, "�˿�����["+mApp.getRequestID()+"]��ȷ�ϣ���ǰ���˿����ڵأ�", Toast.LENGTH_SHORT).show();
			resetStatus();
			super.setLocationRequest();
			super.setLocationStart();
			break;
		default:
			break;
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list, menu);
        return super.onCreateOptionsMenu(menu);
    }  
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	if ( mWs.isShow() ) {
    		Toast.makeText(this, "���ڵȴ��У����Ժ�����", Toast.LENGTH_SHORT).show();
    		return super.onOptionsItemSelected(item);
    	}
    	
		switch(item.getItemId()) {
		case R.id.menu_list_info:
			Intent detail = new Intent(this, ListDetail.class);
			detail.putExtra("neg", "�ٿ���");
			if ( mApp.getCurrentStat().equals(StatusMachine.STAT_SUCCESS) ) {
				// ��ʾ�绰�˿Ͱ�ť
				detail.putExtra("pos", "�绰�˿�");
			}
			this.startActivityForResult(detail, CODE_SHOW_INFO);
			break;
		case R.id.menu_map_mode:
			// ���ص�ͼģʽ
			super.setLocationStop();
			this.setResult(0);
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
    
	@Override
	protected void doProcessMsg(Message msg) {
		int reqid = mApp.getRequestID();
		
		switch(msg.what) {
		case StatusMachine.MSG_STAT_CANCEL:
			Toast.makeText(this, "�˿�����["+reqid+"]�ѱ�ȡ��, ������"+mApp.getCurrentRequestList().length()+"���˿�", 
					Toast.LENGTH_SHORT).show();
			mApp.setCurrentStat((String) msg.obj);
			mWs.Dismiss();
			resetStatus();
			break;
		case StatusMachine.MSG_STAT_SUCCESS:
			// ˾��̬����ʾ����
			mApp.setCurrentStat((String) msg.obj);
			mReqAdapter.setItemConfirm();
			mWs.Dismiss();
			// ����ͣ��ѵ����ֹ��������ListDetail
			super.setLocationStop();
			ShowDetail.showPassengerConfirmInfo(this, CODE_SHOW_CONFIRM_INFO);
			break;
		case StatusMachine.MSG_STAT_TIMEOUT:
			Toast.makeText(this, "�˿�����["+reqid+"]�ѳ�ʱ, ������"+mApp.getCurrentRequestList().length()+"���˿�",
					Toast.LENGTH_SHORT).show();
			mApp.setCurrentStat((String) msg.obj);
			mWs.Dismiss();
			resetStatus();
			break;
		case StatusMachine.MSG_STAT_WAITING_PASS:
			Toast.makeText(this, "�ȴ��˿�ȷ������["+reqid+"], ������"+mApp.getCurrentRequestList().length()+"���˿�",
					Toast.LENGTH_SHORT).show();
			mApp.setCurrentStat((String) msg.obj);
			break;
		case StatusMachine.MSG_STAT_WAITING_DRV:
			Toast.makeText(this, "�˿�����["+reqid+"]�ȴ�������, ������"+mApp.getCurrentRequestList().length()+"���˿�",
					Toast.LENGTH_SHORT).show();
			mApp.setCurrentStat((String) msg.obj);
			break;
		case StatusMachine.MSG_DATA_GETLIST:
			// ����app��
			JSONArray obj = (JSONArray) msg.obj;
			resetAudioProcessor();
			mApp.setCurrentRequestList(obj);
			mReqAdapter.updateList();
			mReqAdapter.notifyDataSetChanged();
			Toast.makeText(this, "������"+obj.length()+"���˿�����", Toast.LENGTH_SHORT).show();
			//mAp.resetPlay();
			//mAp.batchPlay();
			
			// �ӳ٣�����reqadapterˢ��
			DelayTask dt = new DelayTask(CODE_DELAY, delayHandler);
			dt.execute(500);
			break;
		default:
			break;
		}
		
	}
}
