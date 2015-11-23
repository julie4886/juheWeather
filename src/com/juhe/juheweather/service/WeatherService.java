package com.juhe.juheweather.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.juhe.juheweather.bean.FutureWeatherBean;
import com.juhe.juheweather.bean.HoursWeatherBean;
import com.juhe.juheweather.bean.PMBean;
import com.juhe.juheweather.bean.WeatherBean;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

public class WeatherService extends Service {
	private final static String TAG = "WeatherService";
	private WeatherBean weatherBean;
	
	private PMBean pmBean;
	private List<HoursWeatherBean> hlist;
	private String city;
	private OnParserCallBack callBack;
	
	private final static int REPEAT_MSG = 0x01;
	private final static int CALLBACK_OK = 0x02;
	private final static int CALLBACK_ERROR = 0x04;
	final CountDownLatch count = new CountDownLatch(3);
	private WeatherServiceBinder binder = new WeatherServiceBinder();

	public class WeatherServiceBinder extends Binder {
		public WeatherService getMyService() {
			Log.i(TAG, "GETSERVICE");
			return WeatherService.this;

		}

	}

	/*
	 * 解析数据回调借口
	 */
	public interface OnParserCallBack {
		public void OnParserComplete(List<HoursWeatherBean> list,
				PMBean pmBean, WeatherBean weatherBean);
	}
	
	final Handler mHander = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REPEAT_MSG:
				getCityWeather(city);
				sendEmptyMessageDelayed(REPEAT_MSG, 30 * 60 * 1000);
				break;
			case CALLBACK_OK:
				if (callBack != null) {
					callBack.OnParserComplete(hlist, pmBean, weatherBean);
				}
				break;
			case CALLBACK_ERROR:
				Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		};
	};

	/*
	 * 根据城市名获取天气信息，PM2.5，未来三天天气，未来3小时天气
	 */
	public void getCityWeather(String city) {
		this.city = city;
		weatherBean = getJsonData();
		pmBean = getPMDate();
		hlist =  getHoursWeather();
		
		 new Thread() {

			@Override
			public void run() {
				try {
					count.await();
					mHander.sendEmptyMessage(CALLBACK_OK);
				} catch (Exception e) {
					mHander.sendEmptyMessage(CALLBACK_ERROR);
					return;
				}
			}
			 
			 
		 }.start();
	
		

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onBind");
		return binder;
		
	}

	@Override
	public void onCreate() {
		city = "北京";
		super.onCreate();
		mHander.sendEmptyMessage(REPEAT_MSG);
		Log.i(TAG, "Service create");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "Serview destroy");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
		
	}
	//解析天氣數據
	public WeatherBean getJsonData() {
//	 WeatherBean weatherBean = null ;
		Parameters params = new Parameters();
		params.add("cityname", city);
		params.add("dtype", "json");
		Log.i(TAG, "JSON01");
//		 weatherBean1 = new WeatherBean();
//		 bean = new FutureWeatherBean();
//		 fBean = new LinkedList<FutureWeatherBean>();
		JuheData.executeWithAPI(getBaseContext(), 39,
				"http://v.juhe.cn/weather/index", JuheData.GET, params,
				new DataCallBack() {
			
					@Override
					
					public void onSuccess(int arg0, String arg1) {
						Log.i(TAG, "JSON02");
						weatherBean = new WeatherBean();
						try {
							JSONObject json = new JSONObject(arg1);
							int resultcode = json.getInt("resultcode");
							int error_code = json.getInt("error_code");
							if (resultcode == 200 && error_code == 0) {
								JSONObject jsonResult = json.getJSONObject("result");
								//today
								JSONObject jsontoday = jsonResult.getJSONObject("today"); // 今天天气
								Log.i(TAG, "jsontoday=" + jsontoday);
								WeatherBean weatherBean1 = new WeatherBean();
								weatherBean1.setCity(jsontoday.getString("city")); // 城市
								weatherBean1.setWeather_str(jsontoday.getString("weather")); // 当前天气
								weatherBean1.setTemp(jsontoday.getString("temperature")); // 今日温度
								weatherBean1.setWeather_id(jsontoday.getJSONObject("weather_id").getString("fa")); // 天气标示
								weatherBean1.setWind(jsontoday.getString("wind"));
								weatherBean1.setFelt_temp(jsontoday.getString("exercise_index"));
								weatherBean1.setUv_index(jsontoday.getString("wash_index"));
								
								//sk
								JSONObject jsonsk = jsonResult.getJSONObject("sk");
								Log.i(TAG, "jsonsk=" + jsonsk);
								weatherBean1.setNow_temp(jsonsk.getString("temp")); /* 当前温度 */
								weatherBean1.setRelease(jsonsk.getString("time")); // 更新时间
								weatherBean1.setHumidity(jsonsk.getString("humidity")); // 当前湿度
								Log.i("json", "jsonsk=" + jsonsk.getString("humidity"));
							
								
							    // 当前风向
								Log.i("json", weatherBean1.getWind());
								weatherBean1.setDressing_index(jsontoday.getString("dressing_index")); // 穿衣指数
								Log.i("json", weatherBean1.getDressing_index());
						
								//future
								JSONObject jsonfuture = jsonResult.getJSONObject("future");
								Log.i(TAG, "jsonFuture" + jsonfuture);
         					    Date date = new Date(System.currentTimeMillis());
         					   List<FutureWeatherBean> fBean = new ArrayList<FutureWeatherBean>();
         					    	FutureWeatherBean bean = new FutureWeatherBean();
         					    	FutureWeatherBean bean1 = new FutureWeatherBean();
         					    	FutureWeatherBean bean2 = new FutureWeatherBean();
         					    	
         						  Calendar calendar = Calendar.getInstance();
         						  calendar.add(Calendar.DATE, + + 1);
         						   JSONObject jsonfutureArray = jsonfuture.getJSONObject("day_" + 
         								   						calendar.get(Calendar.YEAR) +
         								   					    (calendar.get(Calendar.MONTH) + 1) +
         								   					    calendar.get(Calendar.DATE)) ;
         						    Log.i(TAG, "jsonfutureArray" + jsonfutureArray);
         						 
         						    bean.setTemp(jsonfutureArray.getString("temperature"));
									bean.setWeather_id(jsonfutureArray.getJSONObject("weather_id").getString("fa"));
									bean.setWeek(jsonfutureArray.getString("week"));
									bean.setDate(jsonfutureArray.getString("date"));
									
									Log.i(TAG, "bean=" + bean.getDate());
									fBean.add(bean);
									Log.i(TAG, fBean.get(0).getDate() + "fBean的大小=" + fBean.size());
									
									calendar.add(Calendar.DATE, + + 1);
	         						    jsonfutureArray = jsonfuture.getJSONObject("day_" + 
	         								   						calendar.get(Calendar.YEAR) +
	         								   					    (calendar.get(Calendar.MONTH) + 1) +
	         								   					    calendar.get(Calendar.DATE)) ;
	         						    Log.i(TAG, "jsonfutureArray" + jsonfutureArray);
	         						 
	         						    bean1.setTemp(jsonfutureArray.getString("temperature"));
										bean1.setWeather_id(jsonfutureArray.getJSONObject("weather_id").getString("fa"));
										bean1.setWeek(jsonfutureArray.getString("week"));
										bean1.setDate(jsonfutureArray.getString("date"));
										
										Log.i(TAG, "bean=" + bean1.getDate());
										fBean.add(bean1);
         					    
										calendar.add(Calendar.DATE, + + 1);
	         						    jsonfutureArray = jsonfuture.getJSONObject("day_" + 
	         								   						calendar.get(Calendar.YEAR) +
	         								   					    (calendar.get(Calendar.MONTH) + 1) +
	         								   					    calendar.get(Calendar.DATE)) ;
	         						    Log.i(TAG, "jsonfutureArray" + jsonfutureArray);
	         						 
	         						    bean2.setTemp(jsonfutureArray.getString("temperature"));
										bean2.setWeather_id(jsonfutureArray.getJSONObject("weather_id").getString("fa"));
										bean2.setWeek(jsonfutureArray.getString("week"));
										bean2.setDate(jsonfutureArray.getString("date"));
										
										Log.i(TAG, "bean=" + bean2.getDate());
										fBean.add(bean2);
         					    Log.i(TAG, fBean.get(0).getDate());
								weatherBean1.setFutureList(fBean);
								Log.i(TAG, "weatherBean1=" + weatherBean1.getFutureList().size() 
										+ weatherBean1.getFutureList().get(0).getDate()
										+ weatherBean1.getFutureList().get(1).getDate()
										+ weatherBean1.getFutureList().get(2).getDate());
								weatherBean = weatherBean1;
								
								
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} finally{
							count.countDown();
						}
						
						
					}

					@Override
					public void onFinish() {
					
						Log.i(TAG, "finish");
					}

					@Override
					public void onFailure(int arg0, String arg1, Throwable arg2) {
						Log.i(TAG, "fail");
					}
				});
		
		return weatherBean;
	}
	
	//解析PM2.5數據
	private PMBean getPMDate(){
		Parameters params = new Parameters();
		params.add("city", city);
//		final PMBean pmBean1 = new PMBean();
		
		JuheData.executeWithAPI(getBaseContext(), 33, "http://web.juhe.cn:8080/environment/air/pm",
				JuheData.GET, params, new DataCallBack() {
					@Override
					public void onSuccess(int arg0, String arg1) {
						
						try {
							pmBean = new PMBean();
							JSONObject jsonResult = new JSONObject(arg1);
							Log.i(TAG, "PMjsonResult =" + jsonResult);
							int result_code = jsonResult.getInt("resultcode");
							int error_code= jsonResult.getInt("error_code");
							if (result_code == 200 && error_code == 0) {
								JSONArray jsonarray = jsonResult.getJSONArray("result");
								
									JSONObject json = jsonarray.getJSONObject(0);
									pmBean.setAqi(json.getString("PM2.5"));
									Log.i(TAG,pmBean.getAqi());
									pmBean.setQuality(json.getString("quality"));
									Log.i(TAG, pmBean.getQuality());
								
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						count.countDown();
						
					}
					
					@Override
					public void onFinish() {
						Log.i(TAG, "PMfinish");
						
					}
					
					@Override
					public void onFailure(int arg0, String arg1, Throwable arg2) {
						Log.i(TAG,"PMfail");
					}
				});
			return pmBean;
	
	}
	
	//解析未來三小時數據
	private List<HoursWeatherBean> getHoursWeather() {
		Parameters params = new Parameters();
		params.add("cityname", city);
		
		final Date date = new Date(System.currentTimeMillis());
		
		JuheData.executeWithAPI(getBaseContext(), 39, "http://v.juhe.cn/weather/forecast3h", 
				JuheData.GET, params, new DataCallBack() {
					
					@Override
					public void onSuccess(int arg0, String arg1) {
							hlist = new ArrayList<HoursWeatherBean>();
						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
							JSONObject jsonResult = new JSONObject(arg1);
							int resultcode = jsonResult.getInt("resultcode");
							int error_code = jsonResult.getInt("error_code");
							Log.i(TAG, "HoursjsonResult =" + jsonResult);
							if (resultcode == 200 && error_code == 0) {
								JSONArray resultArray = jsonResult.getJSONArray("result");
								for (int i = 0; i < resultArray.length(); i++) {
									JSONObject jsonHour = resultArray.getJSONObject(i);
									try {
										Date datef = sdf.parse(jsonHour.getString("sfdate"));
										
										if (!datef.after(date)) {
											continue;
										}
										HoursWeatherBean hoursWeatherBean = new HoursWeatherBean();
										hoursWeatherBean.setTemp(jsonHour.getString("temp1"));
										hoursWeatherBean.setWeather_id(jsonHour.getString("weatherid"));
										Calendar c = Calendar.getInstance();
										c.setTime(datef);
										hoursWeatherBean.setTime(c.get(Calendar.HOUR_OF_DAY) + " ");
										hlist.add(hoursWeatherBean);
										Log.i(TAG, " " + hlist.size());
										if (hlist.size() == 5) {
											break;
										}
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									 
									
								}
								
//							hlist.addAll(list);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						count.countDown();
						
					}
					
					@Override
					public void onFinish() {
						Log.i(TAG, "HOURSFinish");
						
					}
					
					@Override
					public void onFailure(int arg0, String arg1, Throwable arg2) {
						Log.i(TAG, "fAILHOUR");
						
					}
				});
		return hlist;
		
	}

	public void setCallBack(OnParserCallBack callBack) {
		Log.i(TAG, "callBack");
		this.callBack = callBack;
	}

	public void removeCallBack() {
		callBack = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub?"?
		Log.i(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

}
