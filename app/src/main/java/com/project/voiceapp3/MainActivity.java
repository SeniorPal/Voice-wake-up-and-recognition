package com.project.voiceapp3;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements EventListener {

    private boolean isEnd=false;
    private boolean isClose=true;
    private RadioButton chinese,english;
    private RadioGroup language;
    private TextView textView;

    private int id;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        textView = findViewById(R.id.text);
        chinese = findViewById(R.id.chinese);
        english = findViewById(R.id.english);
        language = findViewById(R.id.language);
        language.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId==R.id.chinese){
                    id = 0;
                }else {
                    id = 1;
                }
                VoiceUtil.wakeup(getApplicationContext(),id);
            }
        });
        if (language.getCheckedRadioButtonId()==R.id.chinese){
            id = 0;
        }else {
            id = 1;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        VoiceUtil.initKedaXun(getApplicationContext());
        VoiceUtil.wakeup(getApplicationContext(),id);
        RecognizerUtil.initAsr(getApplicationContext(),this);
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        if (!isEnd && name.equals("asr.end")) {
            isEnd = true;
        }
        if (isEnd && name.equals("asr.partial")) {
            isEnd = false;
            logTxt += " ;params :" + params;
            try {
                JSONObject jsonObject = new JSONObject(params);
                String results_recognition = jsonObject.getString("best_result");
                textView.setText("唤醒后语音识别结果"+"\n"+results_recognition);
                RecognizerUtil.stopAsr();
//                RecognizerUtil.cancelAsr();
//                RecognizerUtil.cancelAsr(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)) {
            if (isClose==false){
                Log.e("message1", "CALLBACK_EVENT_ASR_EXIT");
            }

        }


        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (params != null && params.contains("\"nlu_result\"")) {
                if (length > 0 && data.length > 0) {
                    try {
                        JSONObject allJson = new JSONObject(params);
                        String reslut = allJson.optString("best_result");
                        textView.setText(reslut);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
                }
            }
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        Log.e("message1chen", logTxt);
    }

    @SuppressLint("InlinedApi")
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.INTERNET,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.CHANGE_NETWORK_STATE,
                            android.Manifest.permission.READ_PHONE_STATE,
                            android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_SETTINGS,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.SEND_SMS,
                            android.Manifest.permission.RECEIVE_SMS},
                    6);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}