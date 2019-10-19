package com.nairobijvm.mqtt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    TextView dataReceived;
    EditText editText;
    Button button;

    public MqttAndroidClient mqttAndroidClient;
    private static final int READ_PHONE_STATE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    String DeviceId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataReceived=findViewById(R.id.text);
        editText=findViewById(R.id.sample_text);
        button=findViewById(R.id.send);

        proceed();

        startMqtt("mqtt://localhost:1883",DeviceId);

        try {
            connect(DeviceId+"/status","online");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        try {
            SubscribeToTopic(DeviceId);
        } catch (MqttException e) {
            e.printStackTrace();
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    publishMessage("test_topic",editText.getText().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        });



    }

    private void startMqtt(String serverUri,String clientId){
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getApplicationContext(),"Connection Lost",Toast.LENGTH_LONG).show();

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {

                String payload = new String(message.getPayload());

                Toast.makeText(getApplicationContext(),payload,Toast.LENGTH_LONG).show();

                dataReceived.setText(payload);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

                Toast.makeText(getApplicationContext(),"Delivery Complete",Toast.LENGTH_LONG).show();

            }
        });
    }

    private void connect(String deviceID,String will) throws UnsupportedEncodingException {

        byte[] encodedPayload;
        encodedPayload = will.getBytes("UTF-8");


        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setWill(deviceID,encodedPayload,1,true);
        mqttConnectOptions.setKeepAliveInterval(30);
        mqttConnectOptions.setMqttVersion(5);

        //mqttConnectOptions.setUserName(username);
        //mqttConnectOptions.setPassword(password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(),"Connection Successful",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_LONG).show();
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void publishMessage(String topic, String msg) throws UnsupportedEncodingException, MqttException {

        byte[] encodedPayload;

        encodedPayload = msg.getBytes("UTF-8");

        MqttMessage message = new MqttMessage(encodedPayload);
        message.setRetained(true);
        message.setQos(1);

        mqttAndroidClient.publish(topic,message);
    }

    private void SubscribeToTopic(String topic) throws MqttException {

        mqttAndroidClient.subscribe(topic,1);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private void proceed() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {

                Toast.makeText(MainActivity.this,"I need Permissions",Toast.LENGTH_LONG).show();

            } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {

                Toast.makeText(MainActivity.this,"I need Permissions",Toast.LENGTH_LONG).show();

            } else {
                //just request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_PHONE_STATE_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
            editor.apply();


        } else {
            @SuppressLint("HardwareIds")
            String IMEI = telephonyManager.getDeviceId();

            DeviceId= IMEI;


        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PHONE_STATE_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                proceed();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {
                    Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceed();
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceed();
            }
        }
    }

}
