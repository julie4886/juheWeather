package com.juhe.juheweather;


import java.text.ParseException;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.Int2;
import android.renderscript.Script.FieldID;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.juhe.juheweather.R.drawable;
import com.juhe.juheweather.adapter.CityListAdatper;
import com.juhe.juheweather.bean.HoursWeatherBean;
import com.juhe.juheweather.bean.PMBean;
import com.juhe.juheweather.bean.WeatherBean;
import com.juhe.juheweather.service.WeatherService;
import com.juhe.juheweather.service.WeatherService.OnParserCallBack;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

/**
 * @author julie
 * @version 1.0
 * 用來用戶選擇城市
 *
 */
public class MainActivity extends Activity {
	private final static String TAG = "MainActivity";

	private TextView tv;  //城市
	private ImageView icon_city; //选择城市下拉
	private TextView refresh_time; //更新时间
	private ImageView weather_id; //天气ID
	private TextView weather; //天气
	private TextView temprature; //温度
	private TextView temp; //实时温度
	private TextView felt_temp,  //体感温度
					 wind,  //风力风向
					 uv_index, //紫外线指数
					 humidity, //濕度
					 dressing_index, //穿衣指数
					 quality,    //空氣質量
					 PM,    //PM2.5
					 hour_three,
					 hour_six,
					 hour_nine,
					 hour_tewle,
					 hour_fifty,  //未來三小時
					 future_one,
					 future_two,
					 future_three, //未來幾天
					 temp_future_one,
					 temp_future_two,
					 temp_future_three,
					 weather_future_one,
					 weather_future_two,
					 weather_future_three,
					 weather_hour_three, 
					 weather_hour_nine,
					 weather_hour_six,
					 weather_hour_tewle,
					 weather_hour_fifty;
	
	private ImageView icon_hour_three,
					  icon_hour_six,
					  icon_hour_nine,
					  icon_hour_tewle,
					  icon_hour_fifty,
					  icon_future_one,
					  icon_future_two,
					  icon_future_three;
						
	private Context mContext; 
	SharedPreferences preferences;
	private WeatherService mService;
	private String city;
	WeatherService.WeatherServiceBinder binder;
	PullToRefreshScrollView mPullToRefreshScrollView;
	
	
	ServiceConnection conn = new ServiceConnection() {    //服务连接
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService.removeCallBack();
			Log.i(TAG,"Service disconnected");
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "onServiceConnected");
			binder = (WeatherService.WeatherServiceBinder)service;  //获取Service的onBind方法binder对象
			mService = binder.getMyService();
			
			mService.setCallBack(parserCallBack);
			mService.getCityWeather(city);
			Log.i(TAG, "callBack");
			
			
		}
	};
	OnParserCallBack parserCallBack = new OnParserCallBack() {
		
		@Override
		public void OnParserComplete(List<HoursWeatherBean> list, PMBean pmBean,
				WeatherBean weatherBean) {
			mPullToRefreshScrollView.onRefreshComplete();
			if(weatherBean != null)
			{
				Log.i(TAG, "weather=" + weatherBean.getWeather_str());
				setWeatherView(weatherBean);
			}
		
		if (pmBean != null) {
			setPMView(pmBean);
		}
		
		if (list != null) {
			setHoursWeatherBean(list);
		}
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();  //初始化
		readCity(); //设置城市
		initService(); //初始化WeatherService
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        mPullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {


			@Override
			public void onRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ScrollView> refreshView) {
				mService.getCityWeather(city);
				
			}
            
        });
		
		
		
		
		
		
		/*跳转到城市选择界面，并將選擇的結果返回首頁，請求碼為1*/
		icon_city.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
								CityActivity.class);
				startActivityForResult(intent, 1);
			}
		});	
//		tv.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				mService.getCityWeather(city);
//				
//			}
//		});
		
	}
		
	

	private void initService(){
		final Intent intent1 = new Intent(this, WeatherService.class);
	    bindService(intent1, conn, BIND_AUTO_CREATE);
	}
	
	private void init(){
		tv = (TextView)findViewById(R.id.tv_city);   //城市選擇
		icon_city =(ImageView)findViewById(R.id.image_arrow); //下拉箭頭
		refresh_time = (TextView) findViewById(R.id.refresh_time);
		weather = (TextView) findViewById(R.id.tv_weather);
		temprature = (TextView) findViewById(R.id.tv_temprature);
		temp = (TextView) findViewById(R.id.tv_temp);
		
		felt_temp = (TextView)findViewById(R.id.tv_valueoftempphsical);
		uv_index = (TextView)findViewById(R.id.tv_valueofuvindex);
		dressing_index = (TextView)findViewById(R.id.tv_valueofwrap);
		wind = (TextView)findViewById(R.id.tv_valueofwind);
		humidity = (TextView)findViewById(R.id.tv_valueofhumidity);
		
		weather_id = (ImageView)findViewById(R.id.icon_weather);
		quality = (TextView)findViewById(R.id.quality);
		PM = (TextView)findViewById(R.id.tv_valuePM2_5);
		hour_three = (TextView)findViewById(R.id.tv_futrue_three);
		hour_six = (TextView)findViewById(R.id.tv_futrue_six);
		hour_nine =(TextView)findViewById(R.id.tv_futrue_nine);
		hour_tewle = (TextView)findViewById(R.id.tv_futrue_twelve);
		hour_fifty =(TextView)findViewById(R.id.tv_futrue_fifty);
		weather_hour_three = (TextView)findViewById(R.id.tv_futrue_threevalue);
		weather_hour_six = (TextView)findViewById(R.id.tv_futrue_sixvalue);
		weather_hour_nine = (TextView)findViewById(R.id.tv_futrue_ninevalue);
		weather_hour_tewle = (TextView)findViewById(R.id.tv_futrue_twelvevalue);
		weather_hour_fifty = (TextView)findViewById(R.id.tv_futrue_fiftyvalue);
		
		future_one = (TextView)findViewById(R.id.tv_tomorrow);
		future_two = (TextView)findViewById(R.id.tv_todaytwo);
		future_three = (TextView)findViewById(R.id.tv_todaythree);
		weather_future_one = (TextView)findViewById(R.id.tv_maxWeather);
		weather_future_two =(TextView)findViewById(R.id.tv_maxWeathertwo);
		weather_future_three = (TextView)findViewById(R.id.tv_maxWeatherthree);
		temp_future_one  = (TextView)findViewById(R.id.tv_minWeather);
		temp_future_two =(TextView)findViewById(R.id.tv_minWeathertwo);
		temp_future_three = (TextView)findViewById(R.id.tv_minWeatherthree);
		
		
		icon_hour_three = (ImageView)findViewById(R.id.icon_three);
		icon_hour_six = (ImageView)findViewById(R.id.icon_six);
		icon_hour_nine = (ImageView)findViewById(R.id.icon_nine);
		icon_hour_tewle = (ImageView)findViewById(R.id.icon_twelve);
		icon_hour_fifty = (ImageView)findViewById(R.id.icon_fifty);
		icon_future_one = (ImageView)findViewById(R.id.icon_weathertoday);
		icon_future_two = (ImageView)findViewById(R.id.icon_weathertodaytwo);
		icon_future_three = (ImageView)findViewById(R.id.icon_weathertodaythree);
		 
				
		
		mContext = this;
		preferences = getSharedPreferences("cityInfo", MODE_PRIVATE);
		
	   
	   
	    
//	    scrollView.onRefreshComplete();
//	    Log.i("city", "hello" + binder.getMyService().getJsonData());
	   
	}
private void setWeatherView(WeatherBean weatherBean) {
		 weather.setText(weatherBean.getWeather_str());
		 refresh_time.setText("更新时间：" + weatherBean.getRelease());
//		 getResources().getIdentifier("d" + bean.getWeather_id(), "drawable", "com.juhe.weather")
		 weather_id.setImageResource(getResources().getIdentifier("d"+weatherBean.getWeather_id(),
				 	"drawable","com.juhe.juheweather"));
		 weather.setText(weatherBean.getWeather_str());
		 temprature.setText(weatherBean.getTemp());
		 temp.setText(weatherBean.getNow_temp()  + "℃");
		 felt_temp.setText(weatherBean.getFelt_temp());
		 uv_index.setText(weatherBean.getUv_index());
		 dressing_index.setText(weatherBean.getDressing_index());
		 wind.setText(weatherBean.getWind());
		 humidity.setText(weatherBean.getHumidity());
		 
		
		 future_one.setText(weatherBean.getFutureList().get(0).getWeek());
		 weather_future_one.setText(weatherBean.getFutureList().get(0).getTemp());
		 temp_future_one.setText(weatherBean.getFutureList().get(0).getDate());
		 icon_future_one.setImageResource(getResources().
				 			getIdentifier("d" + weatherBean.getFutureList().get(0).getWeather_id(),
			                             "drawable", "com.juhe.juheweather"));
		 
		 
		 future_two.setText(weatherBean.getFutureList().get(1).getWeek());
		 weather_future_two.setText(weatherBean.getFutureList().get(1).getTemp());
		 temp_future_two.setText(weatherBean.getFutureList().get(01).getDate());
		 icon_future_two.setImageResource(getResources().
				 			getIdentifier("d" + weatherBean.getFutureList().get(1).getWeather_id(),
			                             "drawable", "com.juhe.juheweather"));

		 future_three.setText(weatherBean.getFutureList().get(2).getWeek());
		 weather_future_three.setText(weatherBean.getFutureList().get(2).getTemp());
		 temp_future_three.setText(weatherBean.getFutureList().get(2).getDate());
		 icon_future_three.setImageResource(getResources().
				 			getIdentifier("d" + weatherBean.getFutureList().get(2).getWeather_id(),
			                             "drawable", "com.juhe.juheweather"));
		
		
	}

private void setPMView(PMBean bean) {
	PM.setText(bean.getAqi());
	quality.setText(bean.getQuality());
}

private void setHoursWeatherBean(List<HoursWeatherBean> list) {
	
	
	hour_three.setText(list.get(0).getTime());
	weather_hour_three.setText(list.get(0).getTemp());
	icon_hour_three.setImageResource(getResources().getIdentifier("d" + list.get(0).getWeather_id(),
			"drawable", "com.juhe.juheweather"));
	
	hour_six.setText(list.get(1).getTime());
	weather_hour_six.setText(list.get(1).getTemp());
	icon_hour_six.setImageResource(getResources().getIdentifier("d" + list.get(1).getWeather_id(),
			"drawable", "com.juhe.juheweather"));
	
	hour_nine.setText(list.get(2).getTime());
	weather_hour_nine.setText(list.get(2).getTemp());
	icon_hour_nine.setImageResource(getResources().getIdentifier("d" + list.get(2).getWeather_id(),
			"drawable", "com.juhe.juheweather"));
	
	hour_tewle.setText(list.get(3).getTime());
	weather_hour_tewle.setText(list.get(3).getTemp());
	icon_hour_tewle.setImageResource(getResources().getIdentifier("d" + list.get(3).getWeather_id(),
			"drawable", "com.juhe.juheweather"));
	
	hour_fifty.setText(list.get(4).getTime());
	weather_hour_fifty.setText(list.get(4).getTemp());
	icon_hour_fifty.setImageResource(getResources().getIdentifier("d" + list.get(4).getWeather_id(),
			"drawable", "com.juhe.juheweather"));
	
	
	
}
	/**
	 * 城市選擇結果顯示，结果码为1
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == 1 && resultCode == 1) {
			city = data.getStringExtra("city");
			saveCity();
			Log.i(TAG, city);
			mService.getCityWeather(city);
			
		}
	}
	
	private void saveCity() {    //保存城市数据
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("city", city);
		editor.commit();
		tv.setText(city);
	}
	
	private void readCity() {    //读取城市数据
		city = preferences.getString("city", "北京");
		tv.setText(city);
	
		
		
	}





	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(conn);
	}



	
}
