package com.benbenTaxi.v1.function.taxirequest;

public enum TaxiRequestState {
	Waiting_Driver_Response(0,"�ȴ�˾����Ӧ��","�ȴ�"),
	Success(2,"�򳵳ɹ���","�ɹ�"),
	TimeOut(3,"�û���Ӧ��ʱ,�����´򳵣�","ʧ��"),
	Canceled_By_Passenger(4,"���˿�ȡ��������","ȡ��"),	
	UNKONW(5,"δ֪״̬","δ֪");
	
	private int mIndex;
	private String mHumanText;
	private String mHumanBreifText;
	private TaxiRequestState(int index,String text,String bref)
	{
		this.mIndex = index;
		this.mHumanText = text;
		this.mHumanBreifText =  bref;
	}
	public int getIndex()
	{
		return this.mIndex;
	}
	public String getHumanText()
	{
		return this.mHumanText;
	}
	public String getHumanBreifText()
	{
		return this.mHumanBreifText;
	}
}
