package com.example.servicepractice;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener{

    Button startButton,pauseButton,cancelButton;
    LinearLayout linearLayout;

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout);
        initView();

        startButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        Intent intent = new Intent(MainActivity.this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    private void initView() {
        startButton = new Button(this);
        startButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        startButton.setText("开始下载");
        startButton.setId(R.id.startDownload);
        linearLayout.addView(startButton);

//        pauseButton = new Button(this);
//        pauseButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        pauseButton.setText("暂停下载");
//        pauseButton.setId(R.id.pauseDownload);
//        linearLayout.addView(pauseButton);

//        cancelButton = new Button(this);
//        cancelButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        cancelButton.setText("取消下载");
//        cancelButton.setId(R.id.cancelDownload);
//        linearLayout.addView(cancelButton);
    }

    @Override
    public void onClick(View v) {
        if(downloadBinder == null)
            return;
        switch (v.getId()){
            case R.id.startDownload:
                String url = "https://dl.google.com/android/repository/sdk-tools-windows-3859397.zip";
                downloadBinder.sartDownload(url);
                break;
            case R.id.pauseDownload:
                downloadBinder.pauseDownload();
                startButton.setText("继续下载");
                break;
            case R.id.cancelDownload:

                downloadBinder.cancelDownload();
                startButton.setText("开始下载");
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
