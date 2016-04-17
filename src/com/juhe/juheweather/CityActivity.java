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
 * 用来选择城市界面
 */
public class CityActivity extends Activity {
	ImageView back; //返回主界面的图标
	GridView listView; //网格视图
	private Context mContext;
	List<String> cityList;    //城市列表

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		init();     //初始化
		getJsonData();
		getCities(); // 獲取城市
	}

	private void init() {
		back = (ImageView) findViewById(R.id.icon_back);
		listView = (GridView) findViewById(R.id.listView);
		cityList = new ArrayList<String>();
		mContext = this;
		/**
		 * 結束城市選擇活動
		 */
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


    private void getCities() {    //將結果傳給主界面
	listView.setOnItemClickListener(new OnItemClickListener() { //網格監聽事件
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
		 * 请求的方法 参数: 第一个参数 当前请求的context 第二个参数 接口id 第三个参数 接口请求的json 第四个参数 接口请求的方式
		 * 第五个参数 接口请求的参数,键值对com.thinkland.sdk.android.Parameters类型; 第六个参数
		 * 请求的回调方法,com.thinkland.sdk.android.DataCallBack;
		 * 
		 */
		
		JuheData.executeWithAPI(mContext, 39, "http://v.juhe.cn/weather/citys",
				JuheData.GET, params, new DataCallBack() {
					/**
					 * 请求成功时调用的方法 statusCode为http状态码,responseString为请求返回数据.
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
					 * 请求完成时调用的方法,无论成功或者失败都会调用.
					 */
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "finish",
								Toast.LENGTH_SHORT).show();
					}

					/**
					 * 请求失败时调用的方法,statusCode为http状态码,throwable为捕获到的异常
					 * statusCode:30002 没有检测到当前网络. 30003 没有进行初始化. 0
					 * 未明异常,具体查看Throwable信息. 其他异常请参照http状态码.
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
		 * 关闭当前页面正在进行中的请求.
		 */
		JuheData.cancelRequests(mContext);
	}

}
