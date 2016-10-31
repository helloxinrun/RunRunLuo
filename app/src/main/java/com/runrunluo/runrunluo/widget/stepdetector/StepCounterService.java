package com.runrunluo.runrunluo.widget.stepdetector;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.runrunluo.runrunluo.R;
import com.runrunluo.runrunluo.common.Constents;
import com.runrunluo.runrunluo.ui.MainActivity;
import com.runrunluo.runrunluo.util.SharePreferencesUtil;


public class StepCounterService extends Service{
	private static String TAG = "StepCounterService";
	public static Boolean FLAG = false;// 服务运行标志

	private SensorManager mSensorManager;// 传感器服务
	private StepDetector detector;// 传感器监听对象

	private PowerManager mPowerManager;// 电源管理服务
	private WakeLock mWakeLock;// 屏幕灯

	private Notification notification;

	public static int mLastStep;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		FLAG = true;//标记为服务正在运行

		mLastStep = (int) SharePreferencesUtil.get(this, Constents.SP_USER, Constents.TODAY_SETP, 0);
		updateNotification(mLastStep);

		// 创建监听器类，实例化监听对象
		detector = StepDetector.getInstence(this);
		// 获取传感器的服务，初始化传感器
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		// 注册传感器，注册监听器
		mSensorManager.registerListener(detector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		detector.setStepChangedListener(new IStepChangedListener() {
			@Override
			public void onStepChanged(int currentStep) {
				updateNotification(currentStep);
				mLastStep = currentStep;
			}
		});

		// 电源管理服务
		mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , TAG);
		mWakeLock.acquire();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		FLAG = false;// 服务停止
		if (detector != null) {
			mSensorManager.unregisterListener(detector);
		}

		if (mWakeLock != null) {
			mWakeLock.release();
		}

		//关闭通知栏显示
		stopForeground(true);

		//重新启动Service
		startService(new Intent(this, StepCounterService.class));
	}

	private void updateNotification(int step){
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.SELECT_TAB,1);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
		Notification.Builder builder = new Notification.Builder(this)
				.setAutoCancel(true)
				.setContentTitle("今日："+step+"步")
				.setContentText("目标：10000步")
				.setContentIntent(pendingIntent)
				.setLargeIcon(bm)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(false)  //不能自动取消
				.setOngoing(true);     //不能滑动删除
		notification = builder.getNotification();
		startForeground(1, notification); //确定service前台运行
	}
}
