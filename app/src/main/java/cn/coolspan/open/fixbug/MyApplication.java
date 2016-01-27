package cn.coolspan.open.fixbug;

import android.app.Application;

/**
 * MainActivity 2015-12-22 下午10:30:57
 *
 * @author 乔晓松 965266509@qq.com
 */
public class MyApplication extends Application {

    public FixBugManage fixBugManage;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            this.fixBugManage = new FixBugManage(this);
            this.fixBugManage.init("1.0");
        } catch (FixBugException e) {
            e.printStackTrace();
        }
    }
}
