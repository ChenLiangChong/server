package com.example.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private DataInputStream in;
    private Socket socket;
    private Handler handler;
    private DataOutputStream out;
    private String txt="";
    private static String[] PERMISSIONS_STORAGE = {
            //依次权限申请
            Manifest.permission.INTERNET
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applypermission();
        //权限申请

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what==24){
                    final EditText edittext3 = (EditText) findViewById(R.id.edittext3);
                    final TextView textview3 = (TextView) findViewById(R.id.textView3);
                    //获取组件
                    textview3.append("客户端:"+edittext3.getText().toString()+"\n");
                    edittext3.setText("");
                }else if(msg.what==98){
                    final TextView textview3 = (TextView) findViewById(R.id.textView3);
                    //获取组件
                    textview3.append(txt);
                }
            }
        };
    }

    public void connection(View view){
        connection.start();
    }

    public void stop(View view){
        try {
            socket.close();//关闭流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Thread connection = new Thread(
            //连接服务器
            new Runnable() {
                @Override
                public void run() {
                    try {
                        final EditText edittext = (EditText) findViewById(R.id.edittext);
                        final EditText edittext2 = (EditText) findViewById(R.id.edittext2);
                        //获取编辑框组件
                        socket = new Socket(edittext.getText().toString(), Integer.valueOf(edittext2.getText().toString()));
                        //连接服务器
                        out=new DataOutputStream(socket.getOutputStream());
                        // 创建DataOutputStream对象 发送数据
                        in = new DataInputStream(socket.getInputStream());
                    } catch (Exception e) {
                        // TODO Auto-generatetd catch block
                        e.printStackTrace();
                    }
                    while(true) {
                        try {
                            txt = in.readUTF()+"\n";
                            handler.sendEmptyMessage(98);
                            //发送空消息  1主要为了区分消息好执行改变组件信息的内容
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
            }
    );

    public void sentxt(View view){
        sendmessage.start();
        handler.sendEmptyMessage(24);
        //发送空消息  数字主要为了区分消息好执行改变组件信息的内容
    }

    Thread sendmessage = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    final EditText edittext3 = (EditText) findViewById(R.id.edittext3);
                    //获取组件
                    try {
                        out.writeUTF("客户端:"+edittext3.getText().toString());
                        //发送消息
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    //定义判断权限申请的函数，在onCreat中调用就行
    public void applypermission(){
        if(Build.VERSION.SDK_INT>=23){
            boolean needapply=false;
            for(int i=0;i<PERMISSIONS_STORAGE.length;i++){
                int chechpermission= ContextCompat.checkSelfPermission(getApplicationContext(),
                        PERMISSIONS_STORAGE[i]);
                if(chechpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply=true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,1);
            }
        }
    }
}