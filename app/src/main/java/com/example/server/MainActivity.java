package com.example.server;

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
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private ServerSocket server;
    private Socket socket;
    private DataInputStream in;
    private String txt="";
    private Handler handler;
    private DataOutputStream out;
    private static String[] PERMISSIONS_STORAGE = {
            //依次权限申请
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applypermission();
        //权限申请
        handler = new Handler(){
            //handler用于处理更新组件属性
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what==1){
                    //判断消息
                    final TextView textview = (TextView) findViewById(R.id.textView);
                    //获取文本组件 TextView
                    textview.append(txt);
                }else if(msg.what==2){
                    final TextView textview = (TextView) findViewById(R.id.textView);
                    final EditText edittext = (EditText) findViewById(R.id.edittext2);
                    textview.append("服务器:"+edittext.getText()+"\n");
                    edittext.setText("");
                }
            }
        };
    }

    public void start_serv(View view){
        //启动服务器
        server_start.start();
    }
    public void close_server(View view){
        //关闭服务器
        try {
            server.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(View view){
        //发送消息
        send_message.start();
    }

    Thread send_message = new Thread(
            //构建发送消息线程
            new Runnable() {
                @Override
                public void run() {
                    try {
                        final EditText edittext = (EditText) findViewById(R.id.edittext2);
                        //获取组件
                        out.writeUTF("服务器:"+edittext.getText().toString());
                        handler.sendEmptyMessage(2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    Thread server_start = new Thread(
            //构造线程启动socket通信 获取客户端发送的内容
            new Runnable() {
                public void run() {
                    try {
                        final EditText edittext = (EditText) findViewById(R.id.edittext1);
                        //获取组件
                        server = new ServerSocket(Integer.valueOf(edittext.getText().toString()));
                        //设置端口
                        socket = server.accept();
                        //创建一个socket连接对象socket，等待服务器有客户端访问
                        //server.accept();调用服务器的server的接受方法
                        in = new DataInputStream(socket.getInputStream());
                        out=new DataOutputStream(socket.getOutputStream());
                        // 创建DataOutputStream对象 发送数据
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    while(true) {
                        try {
                            txt = in.readUTF()+"\n";
                            handler.sendEmptyMessage(1);
                            //发送空消息  1主要为了区分消息好执行改变组件信息的内容
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });

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