package io.github.stevenzack.pm25reminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pm25.Pm25;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private String city;
    private int bound;
    private Button button;
    private EditText editText_bound,editText_city;
    private String TAG="MAIN";
    private Handler handler=new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 0:
                    textView.setText(msg.obj.toString());
                    break;
                case 1:
                    Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PM25Service.resources=getResources();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.main_txt);
        button = findViewById(R.id.main_save);
        editText_bound = findViewById(R.id.main_edit);
        editText_city = findViewById(R.id.main_edit_city);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        city = sp.getString("city", "北京");
        bound = sp.getInt("bound", 180);
        editText_city.setText(city);
        editText_bound.setText(String.valueOf(bound));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                city=editText_city.getText().toString();
                try {
                    bound = Integer.parseInt(editText_bound.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sp.edit().putString("city",city).putInt("bound",bound).commit();
                startService(new Intent(MainActivity.this, PM25Service.class));
            }
        });
        getData();
        startService(new Intent(MainActivity.this, PM25Service.class));
    }
    public void getData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long i = Pm25.getData(city);
                    showTxt("当前空气质量指数:\n"+String.valueOf(i));
                } catch (Exception e) {
                    e.printStackTrace();
                    toastMe(e.toString());
                }
            }
        }).start();
    }

    public void toastMe(String string) {
        Message msg=new Message();
        msg.arg1=1;
        msg.obj=string;
        handler.sendMessage(msg);
    }

    public void showTxt(String string) {
        Message msg=new Message();
        msg.arg1=0;
        msg.obj=string;
        handler.sendMessage(msg);
    }
}
