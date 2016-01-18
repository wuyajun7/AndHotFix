package cn.coolspan.open.fixbug;

import android.app.Application;

public class MyApplication extends Application {

	public FixBugManage fixBugManage;

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			this.fixBugManage = new FixBugManage(this);
			this.fixBugManage.init("1.0");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
