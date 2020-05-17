package com.example.mycar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.iot.onenet.mqtt.MqttCallBack;
import com.chinamobile.iot.onenet.mqtt.MqttClient;
import com.chinamobile.iot.onenet.mqtt.MqttConnectOptions;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttConnAck;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttConnect;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttMessage;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttPubAck;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttPubComp;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttPublish;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttSubAck;
import com.chinamobile.iot.onenet.mqtt.protocol.MqttSubscribe;
import com.chinamobile.iot.onenet.mqtt.protocol.imp.QoS;
import com.chinamobile.iot.onenet.mqtt.protocol.imp.Type;

import java.io.IOException;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private TextView btn_left,btn_right,btn_qj,btn_ht;
    private Toolbar toolbar;
    private   Typeface iconfont,wrt;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what==0) {
                toolbar.setTitle("在线");
                MqttSubscribe mqttSubscribe1 = new MqttSubscribe("LED_STATUS", QoS.AT_LEAST_ONCE);
                MqttClient.getInstance().subscribe(mqttSubscribe1);
                MqttSubscribe mqttSubscribe = new MqttSubscribe("temp_humi", QoS.AT_LEAST_ONCE);
                MqttClient.getInstance().subscribe(mqttSubscribe);
            }else{
                toolbar.setTitle("离线");
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        wrt=Typeface.createFromAsset(getAssets(), "wrt.ttf");
        //改函数用来初始化控件
        init_KJ();
        init_MQtt();
        //按键控制开关灯
        btn_right.setOnTouchListener(this);
        btn_right.setTypeface(wrt);
        btn_left.setOnTouchListener(this);
        btn_left.setTypeface(wrt);
        btn_qj.setOnTouchListener(this);
        btn_qj.setTypeface(wrt);
        btn_ht.setOnTouchListener(this);
        btn_ht.setTypeface(wrt);
    }
    /**
     * 初始化控件
     */
    private void init_KJ(){
        btn_ht=(TextView) findViewById(R.id.ht);
        btn_qj=(TextView)findViewById(R.id.qj);
        btn_left=(TextView)findViewById(R.id.left);
        btn_right=(TextView)findViewById(R.id.right);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
    }

    /**
     * MQTT连接服务器
     */
    private void init_MQtt(){
        //初始化sdk
        MqttClient.initialize(this,"183.230.40.39",6002,"586234185","262685","HE4LCsIsYxF7WkDPVSV4ua7isfw=");
        //设置接受响应回调
        MqttClient.getInstance().setCallBack(callBack);
        //设置连接属性
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setKeepAlive(121);
        connectOptions.setWill(false);
        connectOptions.setWillQoS(QoS.AT_MOST_ONCE);
        connectOptions.setWillRetain(false);
        //建立TCP连接
        MqttClient.getInstance().connect(connectOptions);
    }

    /**
     * MQTT回调函数
     */
    private MqttCallBack callBack =new MqttCallBack() {
        @Override
        public void messageArrived(MqttMessage mqttMessage) {
            switch (mqttMessage.getMqttHeader().getType()){
                case CONNACK:
                    MqttConnAck mqttConnAck = (MqttConnAck) mqttMessage;
                    Message message=new Message();
                    message.what= mqttConnAck.getConnectionAck();
                    handler.sendMessage(message);
                    break;
                case PUBLISH:
                    MqttPublish mqttPublish = (MqttPublish) mqttMessage;
                    byte[] data = mqttPublish.getData();
                    String topic= mqttPublish.getTopicName();
                    String s=new String(data);
                    break;
                case SUBSCRIBE:
                    MqttSubscribe mqttSubscribe=(MqttSubscribe)mqttMessage;
                    break;
                case SUBACK:
                    MqttSubAck mqttSubAck = (MqttSubAck) mqttMessage;
                    break;
                case PINGRESP:
                    break;
                case PUBACK:
                    MqttPubAck mqttPubAck=(MqttPubAck) mqttMessage;
                    break;
                case PUBCOMP:
                    break;
            }
        }
        @Override
        public void connectionLost(Exception e) {

        }
        @Override
        public void disconnect() {

        }
    };

    /**
     * 自定义对话框
     * @param view
     */
    public  void opens(View view){
        // 加载布局
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog,null);
        final EditText editText=(EditText) dialogView.findViewById(R.id.edit1);
        final String topic = editText.getText().toString();
        new AlertDialog.Builder(view.getContext()) // 使用android.support.v7.app.AlertDialog
                .setView(dialogView) // 设置布局
                .setCancelable(true) // 设置点击空白处不关闭
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override public void onClick(DialogInterface dialog, int which) {
                       /* if(topic==""){
                           textView.setText("TOPIC不能为空！请重新订阅");
                        }else {
                            MqttSubscribe mqttSubscribe = new MqttSubscribe(topic, QoS.AT_LEAST_ONCE);
                            MqttClient.getInstance().subscribe(mqttSubscribe);
                            textView.setText("订阅成功");
                        }*/
                        setDialogIsShowing(dialog, true); // 设置关闭
                    }
                }) // 设置取消按钮，并设置监听事件

                .create() // 创建对话框
                .show(); // 显示对话框
    }

    /**
     * 设置对话框是否显示
     * @param dialog 对话框
     * @param isClose 是否显示. true为关闭，false为不关闭
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setDialogIsShowing(DialogInterface dialog, boolean isClose) {
        try{
            // 获取到android.app.Dialog类
            Field mShowing = dialog.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
            mShowing.setAccessible(true); // 设置可访问
            mShowing.set(dialog,isClose); // 设置是否关闭
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.left:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    btn_left.setTextColor(Color.parseColor("#6200EE"));
                    byte[] left={'0'};
                    MqttPublish mqttPublish_left=new MqttPublish("CAR",left,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_left);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    btn_left.setTextColor(Color.parseColor("#c0c0c0"));
                    byte[] left={'2'};
                    MqttPublish mqttPublish_zt=new MqttPublish("CAR",left,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_zt);
                }
                break;
            case R.id.right:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    btn_right.setTextColor(Color.parseColor("#6200EE"));
                    byte[] right={'1'};
                    MqttPublish mqttPublish_left=new MqttPublish("CAR",right,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_left);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    btn_right.setTextColor(Color.parseColor("#c0c0c0"));
                    byte[] left={'2'};
                    MqttPublish mqttPublish_zt=new MqttPublish("CAR",left,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_zt);
                }
                break;
            case R.id.qj:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    btn_qj.setTextColor(Color.parseColor("#6200EE"));
                    byte[] right={'4'};
                    MqttPublish mqttPublish_left=new MqttPublish("CAR",right,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_left);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    btn_qj.setTextColor(Color.parseColor("#c0c0c0"));
                    byte[] left={'5'};
                    MqttPublish mqttPublish_zt=new MqttPublish("CAR",left,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_zt);
                }
                break;
            case R.id.ht:
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    btn_ht.setTextColor(Color.parseColor("#6200EE"));
                    byte[] right={'3'};
                    MqttPublish mqttPublish_left=new MqttPublish("CAR",right,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_left);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    btn_ht.setTextColor(Color.parseColor("#c0c0c0"));
                    byte[] left={'5'};
                    MqttPublish mqttPublish_zt=new MqttPublish("CAR",left,QoS.AT_LEAST_ONCE);
                    MqttClient.getInstance().sendMsg(mqttPublish_zt);
                }
                break;
        }
        return true;
    }
}
