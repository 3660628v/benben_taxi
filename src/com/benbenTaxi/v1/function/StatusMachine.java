package com.benbenTaxi.v1.function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

public class StatusMachine extends GetInfoTask {
	private static final int TYPE_GET_TAXI = 0;
	private static final int TYPE_REQ_TAXI = 1;
	private static final int TYPE_ASK_REQ = 2;
	private static final int TYPE_PAS_CON = 3;
	private static final int TYPE_PAS_CAN = 4;
	private static final int TYPE_DRV_REP = 5;
	private static final int TYPE_DRV_REQ = 6;
	private static final int TYPE_DRV_CON = 7;
	private static final int TYPE_DRV_ASK = 8;
	private static final int TYPE_CANCEL = 9;
	
	public static final int MSG_DATA_SENT = 0x1001;
	public static final int MSG_DATA_GETLIST = 0x1002;
	public static final int MSG_STAT_WAITING_DRV = 0x1003;
	public static final int MSG_STAT_TIMEOUT = 0x1004;
	public static final int MSG_STAT_WAITING_PASS = 0x1005;
	public static final int MSG_STAT_CANCEL = 0x1006;
	public static final int MSG_STAT_SUCCESS = 0x1007;
	
	public static final String PASS_CONFIRM = "confirm";
	public static final String PASS_CANCEL = "cancel";
	
	private final static String STAT_DRV_TRY_GET_REQUEST = "Driver_Try_Get_Request";	
	private final static String STAT_PASSENGER_CONFIRM = "Passenger_Confirm";
	private final static String STAT_PASSENGER_TRY_CONFIRM = "Passenger_Try_Confirm";	
	//private final static String STAT_PASSENGER_CANCEL = "Passenger_Cancel";
	private final static String STAT_PASSENGER_TRY_CANCEL = "Passenger_Try_cancel";
	
	public final static String STAT_WAITING_DRV_RESP = "Waiting_Driver_Response";
	public final static String STAT_WAITING_PAS_CONF = "Waiting_Passenger_Confirm";
	public final static String STAT_CANCEL = "Canceled_By_Passenger";
	public final static String STAT_SUCCESS = "Success";
	public final static String STAT_TIMEOUT = "TimeOut";
	
	private String _useragent = "ning@benbentaxi";
	private JSONObject _json_data;
	private int _type = -1;
	
	private Activity mContext;
	private Handler mH;
	private DataPreference mData;
	private String mHost, mTokenKey, mTokenVal, mMobile, mStatus;
	private JSONObject mCurrentObj;
	private JSONArray mReqList;
	private int mReqId = -1;
	
	public StatusMachine(Activity con, Handler h, DataPreference data, JSONObject curObj) {
		mContext = con;
		mH = h;
		mData = data;
		
		mHost = mData.LoadString("host");
		mTokenKey = mData.LoadString("token_key");
		mTokenVal = mData.LoadString("token_value");
		mMobile = mData.LoadString("user");
		
		mCurrentObj = curObj;
		
		try {
			mReqId = mCurrentObj.getInt("id");
		} catch (JSONException e) {
			mReqId = -1;
		}
	}
	
	public void CancelTaxi(int id) {
		// ȡ����: /api/v1/taxi_requests/:id/cancel
		_type = TYPE_CANCEL;
		String url = "http://"+mHost+"/api/v1/taxi_requests/"+id+"/cancel";
		_json_data = new JSONObject();
		doPOST(url);
	}
	
	public void driverReport(double lng, double lat, double radius, String cootype) {
		String url = "http://"+mHost+"/api/v1/driver_track_points";
		_type = TYPE_DRV_REP;
		
		_json_data = new JSONObject();
		try {
			//{"driver_track_point":{"mobile":"15910676326", "lat":"8", "png":"8", "radius":100, "coortype":"gsm"}} 
			JSONObject sess = new JSONObject();
			sess.put("mobile", mMobile);
			sess.put("lng", lng);
			sess.put("lat", lat);
			sess.put("radius", radius);
			sess.put("coortype", cootype);
			_json_data.put("driver_track_point", sess);
		} catch (JSONException e) {
			
		}
		
		doPOST(url);
	}
	
	public void driverGetRequest(double lng, double lat, double radius) {
		// /api/v1/taxi_requests?lat=8&lng=8&radius=10
		_type = TYPE_DRV_REQ;
		String url =  "http://"+mHost+"/api/v1/taxi_requests/nearby?lat="+lat+"&lng="+lng+"&radius=5000";
		super.initCookies(mTokenKey, mTokenVal, "42.121.55.211");
		execute(url, _useragent, GetInfoTask.TYPE_GET);
	}
	
	public void driverConfirm(double lng, double lat, int id) {
		_type = TYPE_DRV_CON;
		String url = "http://"+mHost+"/api/v1/taxi_requests/"+id+"/response";
		
		_json_data = new JSONObject();
		try {
			JSONObject sess = new JSONObject();
			sess.put("driver_mobile", mMobile);
			sess.put("driver_lat", lat);
			sess.put("driver_lng", lng);
			_json_data.put("taxi_response", sess);
		} catch (JSONException e) {
			
		}
		
		doPOST(url);
	}
	
	public void driverAskRequest(int id) {
		_type = TYPE_DRV_ASK;
		
		String url = "http://"+mHost+"/api/v1/taxi_requests/"+id;
		super.initCookies(mTokenKey, mTokenVal, "42.121.55.211");
		execute(url, _useragent, GetInfoTask.TYPE_GET);
	}
	
	private void doPOST(String url) {
		// һ��Ҫ��ʼ��cookie��content-type!!!!!
		super.initCookies(mTokenKey, mTokenVal, "42.121.55.211");
		super.initHeaders("Content-Type", "application/json");
		
		execute(url, _useragent, GetInfoTask.TYPE_POST);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if ( values[0] >= GetInfoTask.REQUEST_SEND && mH != null ) {
			// ������ر�¼���Ի�������ӳ�Ч��
			mH.dispatchMessage(mH.obtainMessage(MSG_DATA_SENT));
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected void initPostValues() {
		//sess_params.add(new BasicNameValuePair("","{\"session\":{\"name\":\"ceshi001\",\"password\":\"8\"}}"));
		//post_param = "{\"session\":{\"name\":\"ceshi_ning\",\"password\":\"8\"}}";
		if ( _json_data != null ) {
			post_param = _json_data.toString();
		}
	}
	
	@Override
	protected void onPostExecGet(Boolean succ) {
		//_info.setText("Get "+this.getHttpCode()+"\n");
		if ( succ ) {
			String data = this.toString();
			//_info.append("get result: \n"+data);
			JSONTokener jsParser = new JSONTokener(data);
			
			try {
				switch ( _type ) {
				case TYPE_GET_TAXI:
				case TYPE_DRV_REQ:
					doGetList(data);
					break;
				case TYPE_ASK_REQ:
				case TYPE_DRV_ASK:
					doGetRequest(jsParser);
					break;
				default:
					break;
				} 
			} catch (JSONException e) {
				//e.printStackTrace();
				try {
					JSONObject retobj = (JSONObject)jsParser.nextValue();
					JSONObject err = retobj.getJSONObject("errors");
					if ( mContext != null ) {
						Toast.makeText(mContext, "����: "+retobj.toString(), Toast.LENGTH_LONG).show();
					}
					//_info.append("errmsg \""+err.getJSONArray("base").getString(0)+"\"");
					//_info.append("\ncookies: "+_sess_key.getName()+" "+_sess_key.getValue()+"\n");
				} catch (JSONException ee) {
					//_info.append("json error: "+ee.toString()+"\n"+"ret: "+data);
				}
			} catch (Exception e) {
				if ( mContext != null ) {
					Toast.makeText(mContext, "���󷵻�: "+data, Toast.LENGTH_LONG).show();
				}
			}
		} else {
			//_info.append("get errmsg: \n"+_errmsg);
		}
	}
	
	@Override
	protected void onPostExecPost(Boolean succ) {
		String data = this.toString();
		//_info.setText("Post "+this.getHttpCode()+"\n");
		if ( succ ) {
			//_info.append("result: "+this.getHttpCode()+"\n"+this.toString());
			JSONTokener jsParser = new JSONTokener(data);

			try {
				
				switch ( _type ) {
				case TYPE_REQ_TAXI:
					doCreateRequest(jsParser);
					break;
				case TYPE_PAS_CON:
					doPassengerConfirm(jsParser);
					break;
				case TYPE_PAS_CAN:
					doCancel(jsParser);
					break;
				case TYPE_DRV_CON:
					doDriverConfirm(jsParser);
					break;
				case TYPE_DRV_REP:
					doDriverReport(jsParser);
					break;
				case TYPE_CANCEL:
					doCancel(jsParser);
					break;
				default:
					break;
				}
				
				//_info.append("result \n"+ret.getString("token_key")+": "+ret.getString("token_value"));
				//_sess_key = new BasicNameValuePair(ret.getString("token_key"), ret.getString("token_value"));
			} catch (JSONException e) {
				//e.printStackTrace();
				try {
					JSONObject ret = (JSONObject) jsParser.nextValue();
					JSONObject err = ret.getJSONObject("errors");
					//_info.append("errmsg \""+err.getJSONArray("base").getString(0)+"\"");
					_errmsg = err.getJSONArray("base").getString(0);
					succ = false;
				} catch (Exception ee) {
					//_info.append("json error: "+ee.toString()+"\n");
					//_info.append("to json: "+_json_data.toString());
					_errmsg = "����ͨ���쳣�������Ʒ��������ã�����ϵ������";
					succ = false;
				}
			} catch (Exception e) {
				_errmsg = "������������Ʒ��������ã���ȷ����������������";
				succ = false;
			}
			
		} else {
			//_info.append("errmsg: \n"+_errmsg);
		}
		
		if( succ == false && mContext != null ) {
			Toast.makeText(mContext, "���󷵻�: "+_errmsg+"\n"+data, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void doGetList(String data) throws JSONException {
		mReqList = new JSONArray(data);
		
		if ( mH != null ) {
			mH.dispatchMessage(mH.obtainMessage(MSG_DATA_GETLIST));
		}
	}
	
	private void doGetRequest(JSONTokener jsParser) throws JSONException {
		// {"id":28,"state":"Waiting_Driver_Response","passenger_lat":8.0,"passenger_lng":8.0,"passenger_voice_url":"/uploads/taxi_request/voice/2013-05-31/03bd766e8ecc2e2429f1610c7bf6c3ec.m4a"}
		// �û�ֻҪ����state����
		mStatus = ((JSONObject)jsParser.nextValue()).getString("state");
		
		String msg = null;
		int stat = -1;
		if ( mStatus.equals(STAT_WAITING_DRV_RESP) ) {
			// �����ȴ�
			if ( _type == TYPE_ASK_REQ ) {
				// �˿�̬
				msg = "����["+mReqId+"]�ȴ�˾����Ӧ, ����"+mReqList.length()+"��";
			} else {
				// ˾��̬
				msg = "�˿�����["+mReqId+"]�ȴ�������, ������"+mReqList.length()+"���˿�";
			}
			stat = StatusMachine.MSG_STAT_WAITING_DRV;
			
		} else if ( mStatus.equals(STAT_WAITING_PAS_CONF) ) {
			// ˾����Ӧ�𣬵ȴ��û�ȷ��
			if ( _type == TYPE_ASK_REQ ) {
				// �˿�̬
				msg = "����["+mReqId+"]����˾��Ӧ��, ����"+mReqList.length()+"��";
			} else {
				// ˾��̬�����³˿�ͼ��
				msg = "�˿�����["+mReqId+"]���ѽ���, ������"+mReqList.length()+"���˿�";
			}
			stat = StatusMachine.MSG_STAT_WAITING_PASS;
			
		} else if ( mStatus.equals(STAT_TIMEOUT) ) {
			// ��ʱ
			if ( _type == TYPE_ASK_REQ ) {
				// �˿�̬
				msg = "����["+mReqId+"]�ѳ�ʱ, ����"+mReqList.length()+"��";
			} else {
				// ˾��̬
				msg = "�˿�����["+mReqId+"]�ѳ�ʱ, ������"+mReqList.length()+"���˿�";
			}
			stat = StatusMachine.MSG_STAT_TIMEOUT;
			
		} else if ( mStatus.equals(STAT_CANCEL) ) {
			// �û�ȡ�������³˿�/˾��ͼ��
			if ( _type == TYPE_ASK_REQ ) {
				// �˿�̬
				msg = "����["+mReqId+"]�ѱ��˿�ȡ��, ����"+mReqList.length()+"��";
			} else {
				// ˾��̬
				msg = "�˿�����["+mReqId+"]�ѱ�ȡ��, ������"+mReqList.length()+"���˿�";
			}
			stat = StatusMachine.MSG_STAT_CANCEL;
			
		} else if ( mStatus.equals(STAT_SUCCESS) ) {	
			// �û�ȷ�ϣ����δ򳵳ɹ�
			if ( _type == TYPE_ASK_REQ ) {
				// �˿�̬
				msg = "����["+mReqId+"]�򳵳ɹ���";
			} else {
				// ˾��̬����ͣ��ʱ
				msg = "�˿�����["+mReqId+"]��ȷ�ϣ���ǰ���˿����ڵأ�";
			}
			stat = StatusMachine.MSG_STAT_SUCCESS;
		}
		
		if ( msg != null && mContext != null ) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
		
		if ( mH != null && stat > 0 ) {
			mH.dispatchMessage(mH.obtainMessage(stat));
		}
	}
	
	private void doCreateRequest(JSONTokener jsParser) throws JSONException {
		JSONObject ret = (JSONObject)jsParser.nextValue();
		mReqId = ret.getInt("id");
	}
	
	private void doPassengerConfirm(JSONTokener jsParser) throws JSONException {
		// ���沢��ʾ˾����Ϣ
		// {"id":53,"state":"Success","passenger_mobile":"15910676326","driver_mobile":"15910676326","passenger_lat":8.0,"passenger_lng":8.0,"passenger_voice_url":"/uploads/taxi_request/voice/2013-06-01/e6d709e0158d6b312e0a30e24a656347.m4a","driver_lat":8.0,"driver_lng":8.0}
		JSONObject obj = (JSONObject)jsParser.nextValue();
		mStatus = obj.getString("state");
	}
	
	private void doCancel(JSONTokener jsParser) throws JSONException {
		// ���账����doGetRequest��ѯ�õ�
		mStatus = STAT_CANCEL;
	}
	
	private void doDriverConfirm(JSONTokener jsParser) throws JSONException {
		JSONObject ret = (JSONObject) jsParser.nextValue();
		mStatus = ret.getString("state");
	}
	
	private void doDriverReport(JSONTokener jsParser) throws JSONException {
		JSONObject ret = (JSONObject) jsParser.nextValue();
		if ( ! ret.getString("response").equals("ok") ) {
			Toast.makeText(mContext, "˾���ϱ�ʧ��: "+jsParser.toString(), Toast.LENGTH_SHORT).show();
		}
	}
	
}
