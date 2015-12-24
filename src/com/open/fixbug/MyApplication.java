package com.open.fixbug;

import android.app.Application;

import com.open.fixbug.lib.FixBugManage;

public class MyApplication extends Application {

	public FixBugManage nuwaManage;

	@Override
	public void onCreate() {
		super.onCreate();
		this.nuwaManage = new FixBugManage(this);
		this.nuwaManage.init("1.0");
	}
}
