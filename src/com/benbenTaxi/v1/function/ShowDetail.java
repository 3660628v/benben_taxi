package com.benbenTaxi.v1.function;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.benbenTaxi.v1.BenbenApplication;
import com.benbenTaxi.v1.ListDetail;

public class ShowDetail {
	private final static DecimalFormat mDF = new DecimalFormat("#.##");
	
	static public void showPassengerRequestInfo(BenbenApplication app, Activity con, int idx, final JSONObject obj) throws JSONException {
    	String[] voiceUrl = new String[5];
    	
		try {
			voiceUrl[0] = "ID"+obj.getInt("id");
			voiceUrl[1] = obj.getString("passenger_mobile");
			voiceUrl[2] = mDF.format(obj.getDouble("passenger_lat"))+"/"+mDF.format(obj.getDouble("passenger_lng"));
			voiceUrl[3] = "������·120��";
			voiceUrl[4] = "2013-06-25 00:44:22";
			//voiceUrl[3] = obj.getString("passenger_voice_url");
		} catch (JSONException e) {
			voiceUrl[0] = "δ֪";
			voiceUrl[1] = "δ֪";
			voiceUrl[2] = "δ֪";
			voiceUrl[3] = "δ֪";
			voiceUrl[4] = "δ֪";
			//voiceUrl[3] = "�˿���Ϣ��ȡ����: "+e.toString();
		}
				
		app.setCurrentInfo(voiceUrl);
		app.setCurrentObject(obj);
		app.setRequestID(idx);
		
		Bundle tips = new Bundle();
		tips.putString("pos", "ȷ�ϳ˿�");
		tips.putString("neg", "�ٿ���");
		Intent detail = new Intent(con, ListDetail.class);
		detail.putExtras(tips);
		con.startActivityForResult(detail, 1);
    }
}
