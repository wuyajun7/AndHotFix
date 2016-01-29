package cn.coolspan.open.fixbug;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.File;

import cn.coolspan.open.fixbug.lib.FixBugException;

/**
 * MainActivity 2015-12-22 下午10:30:57
 *
 * @author 乔晓松 965266509@qq.com
 */
public class MainActivity extends Activity {

    private Handler handler = new Handler();

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
                        MyApplication myApplication = (MyApplication) getApplication();
                        try {
                            final File patch = new File(
                                    Environment.getExternalStorageDirectory(), "patch.jar");
                            //解决Android 6.0读写文件无权限的问题，具体请参考博文: http://blog.csdn.net/qxs965266509/article/details/50606385
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//>=23
                                int REQUEST_EXTERNAL_STORAGE = 1;
                                String[] PERMISSIONS_STORAGE = {
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                };
                                //判断是否已经有写的权限
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
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "补丁存在:" + patch.getName(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                myApplication.fixBugManage.addPatch(patch.getAbsolutePath());
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "补丁不存在", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (FixBugException e) {
                            //出现异常，把所有的补丁文件全部清除，避免因为补丁文件导致应用无法再次启动
                            myApplication.fixBugManage.removeAllPatch();
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "修复补丁出现异常，补丁已全部清除", Toast.LENGTH_SHORT).show();
                                }
                            });
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

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("MainActivity", requestCode + "---" + permissions.toString() + "---" + grantResults.toString());
    }
}
