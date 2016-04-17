package com.juhe.juheweather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.juhe.juheweather.adapter.CityListAdatper;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

/**
 * @author julie
 * @version 1.0
 * ����ѡ����н���
 */
public class CityActivity extends Activity {
	ImageView back; //�����������ͼ��
	GridView listView; //������ͼ
	private Context mContext;
	List<String> cityList;    //�����б�

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		init();     //��ʼ��
		getJsonData();
		getCities(); // �@ȡ����
	}

	private void init() {
		back = (ImageView) findViewById(R.id.icon_back);
		listView = (GridView) findViewById(R.id.listView);
		cityList = new ArrayList<String>();
		mContext = this;
		/**
		 * �Y�������x����
		 */
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


    private void getCities() {    //���Y�����o������
	listView.setOnItemClickListener(new OnItemClickListener() { //�W��O �¼�
		@Override
		public void onItemClick(
				AdapterView<?> arg0, View arg1,
				int position, long id) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("city",
					cityList.get(position));
			setResult(1, intent);
			finish();
		}
	});
}
	
	private void getJsonData() {
		Parameters params = new Parameters();
		params.add("dtype", "json");
		params.add("key", "b3e87e4f0be7ba7fff1aa1d572341ced");
		/**
		 * ����ķ��� ����: ��һ������ ��ǰ�����context �ڶ������� �ӿ�id ���������� �ӿ������json ���ĸ����� �ӿ�����ķ�ʽ
		 * ��������� �ӿ�����Ĳ���,��ֵ��com.thinkland.sdk.android.Parameters����; ����������
		 * ����Ļص�����,com.thinkland.sdk.android.DataCallBack;
		 * 
		 */
		
		JuheData.executeWithAPI(mContext, 39, "http://v.juhe.cn/weather/citys",
				JuheData.GET, params, new DataCallBack() {
					/**
					 * ����ɹ�ʱ���õķ��� statusCodeΪhttp״̬��,responseStringΪ���󷵻�����.
					 */
					@Override
					public void onSuccess(int statusCode, String responseString) {
						// TODO Auto-generated method stub
						Log.i("juhe", "success" + responseString);
						Log.i("juhe", "success" + statusCode);
						try {
							JSONObject json = new JSONObject(responseString);
							int code = json.getInt("resultcode");
							int error_code = json.getInt("error_code");
							Log.i("juhe", "success" + code);
							Log.i("juhe", "success" + error_code);
							if (error_code == 0 && code == 200) {
								JSONArray resultArray = json.getJSONArray("result");
								Set<String> citySet = new HashSet<String>();
								for (int i = 0; i < resultArray.length(); i++) {
									String city = resultArray.getJSONObject(i)
											.getString("city");
									citySet.add(city);
								}
								cityList.addAll(citySet);
								CityListAdatper adatper = new CityListAdatper(
										mContext, cityList);
								listView.setAdapter(adatper);
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
					}

					/**
					 * �������ʱ���õķ���,���۳ɹ�����ʧ�ܶ������.
					 */
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "finish",
								Toast.LENGTH_SHORT).show();
					}

					/**
					 * ����ʧ��ʱ���õķ���,statusCodeΪhttp״̬��,throwableΪ���񵽵��쳣
					 * statusCode:30002 û�м�⵽��ǰ����. 30003 û�н��г�ʼ��. 0
					 * δ���쳣,����鿴Throwable��Ϣ. �����쳣�����http״̬��.
					 */
					@Override
					public void onFailure(int statusCode,
							String responseString, Throwable throwable) {
						// TODO Auto-generated method stub
						Toast.makeText(mContext, "fail", Toast.LENGTH_LONG)
								.show();
					}
				});

	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		/**
		 * �رյ�ǰҳ�����ڽ����е�����.
		 */
		JuheData.cancelRequests(mContext);
	}

}
