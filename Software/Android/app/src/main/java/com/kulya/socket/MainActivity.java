package com.kulya.socket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button connection;
    private Button subs;
    private Button send;
    private Button disconnection;

    private MQTTentity mqttentity;
    private MqttClient client;
    private MqttConnectOptions options;
    private TextView text;
    private EditText message;
    private EditText subsName;

    /**
     * 获取手机imei
     *
     * @param context
     * @param slotId
     * @return
     */
    public static String getIMEI(Context context, int slotId) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getImei", int.class);
            String imei = (String) method.invoke(manager, slotId);
            return imei;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * handler事件处理
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                text.setText((String) msg.obj);
            } else if (msg.what == 2) {
                setSubs(mqttentity.getTimeTopic());
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 3) {
                Toast.makeText(MainActivity.this, "连接失败，系统正在重连", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });
    /**
     * 回调
     */
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void connectionLost(Throwable cause) {
            //连接断开
            if (!client.isConnected()) {
                connect();
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //subscribe后得到的消息会执行到这里面
            Log.d("66661", "3");
            Message msg = new Message();
            msg.what = 1;   //收到消息标志位
            msg.obj = topic + "_" + message.toString();
            handler.sendMessage(msg);    // hander 回传
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        connection = findViewById(R.id.connection);
        send = findViewById(R.id.send);
        subs = findViewById(R.id.subs);
        disconnection = findViewById(R.id.disconnection);
        text = findViewById(R.id.text);
        message = findViewById(R.id.message);
        subsName = findViewById(R.id.subsName);

        connection.setOnClickListener(this);
        send.setOnClickListener(this);
        subs.setOnClickListener(this);
        disconnection.setOnClickListener(this);
        text.setOnClickListener(this);
        message.setOnClickListener(this);
        subsName.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connection:
                MQTTInit();
                connect();
                break;
            case R.id.subs:
                if (client == null) {
                    Toast.makeText(this, "请先连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                String subsNameString = subsName.getText().toString().trim();
                if (TextUtils.isEmpty(subsNameString)) {
                    Toast.makeText(this, "订阅不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                setSubs(subsNameString);
                break;
            case R.id.disconnection:
                disconnect();
                break;
            case R.id.send:
                subsNameString = subsName.getText().toString().trim();
                if (client == null) {
                    Toast.makeText(this, "请先连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(subsNameString)) {
                    Toast.makeText(this, "订阅不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                String messageString = message.getText().toString().trim();
                if (TextUtils.isEmpty(messageString)) {
                    Toast.makeText(this, "消息不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                publish(subsNameString, messageString);
                break;
            default:
        }
    }

    private void MQTTInit() {

        try {
            mqttentity = new MQTTentity("tcp://60.205.229.216:1883", "root", "12342", "huang");
            String imei = getIMEI(MainActivity.this, 0);
            client = new MqttClient(mqttentity.getHost(), imei, new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(mqttentity.getUserName());
            options.setPassword(mqttentity.getPassWord().toCharArray());
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            client.setCallback(mqttCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /**
     * MQTT连接
     */
    private void connect() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(options);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * MQTT订阅连接
     */
    private void setSubs(String topic) {
        try {
            client.subscribe(topic);
            Toast.makeText(MainActivity.this, "订阅" + topic + "成功", Toast.LENGTH_SHORT).show();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Topic发送消息
     *
     * @param topic
     * @param sendMessage
     */
    private void publish(String topic, String sendMessage) {
        int qos = 0;
        boolean retained = false;
        try {
            if (client != null) {
                client.publish(topic, sendMessage.getBytes(), qos, retained);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * MQTT关闭连接
     */
    private void disconnect() {
        try {
            client.disconnect();
            Toast.makeText(MainActivity.this, "关闭连接成功", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

}
