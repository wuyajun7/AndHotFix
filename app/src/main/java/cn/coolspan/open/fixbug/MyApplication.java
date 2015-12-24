package cn.coolspan.open.fixbug;

import android.app.Application;

public class MyApplication extends Application {

	public FixBugManage fixBugManage;

	@Override
	public void onCreate() {
		super.onCreate();
		this.fixBugManage = new FixBugManage(this);
		this.fixBugManage.init("1.0");
	}
}
