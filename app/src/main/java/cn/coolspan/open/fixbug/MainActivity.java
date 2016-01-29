package cn.coolspan.open.fixbug;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * MainActivity 2015-12-22 下午10:30:57
 *
 * @author 乔晓松 965266509@qq.com
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MyApplication myApplication = (MyApplication) getApplication();
                            File patch = new File(
                                    Environment.getExternalStorageDirectory(), "patch.jar");
//                    myApplication.fixBugManage.addPatch(patch.getAbsolutePath());
                            boolean isExists = patch.exists();

                            //解决Android 6.0读写文件无权限的问题，具体请参考博文: http://blog.csdn.net/qxs965266509/article/details/50606385
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                                int REQUEST_EXTERNAL_STORAGE = 1;
                                String[] PERMISSIONS_STORAGE = {
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                };
                                int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                                if (permission != PackageManager.PERMISSION_GRANTED) {
                                    // We don't have permission so prompt the user
                                    ActivityCompat.requestPermissions(
                                            MainActivity.this,
                                            PERMISSIONS_STORAGE,
                                            REQUEST_EXTERNAL_STORAGE
                                    );
                                }
                            }

                            if (patch.exists()) {
//                               Toast.makeText(MainActivity.this, "补丁存在:" + patch.getName(), Toast.LENGTH_SHORT).show();
                                myApplication.fixBugManage.addPatch(patch.getAbsolutePath());
                            } else {
//                               Toast.makeText(MainActivity.this, "补丁不存在", Toast.LENGTH_SHORT).show();
                            }
                        } catch (FixBugException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "...bug...", Toast.LENGTH_SHORT).show();
            }
        });
//        Log.e("qxs", R.drawable.class.getName());
//        Log.e("qxs", R.drawable.class.getCanonicalName());
    }
}
