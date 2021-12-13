package com.example.secondtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences editor_1 = this.getSharedPreferences("user", MODE_PRIVATE);
        boolean isFirst = editor_1.getBoolean("isFirst",true);

        Button button_user = findViewById(R.id.butLog);
        button_user.setOnClickListener(this);
        EditText user = findViewById(R.id.etext1);

        if(!isFirst){
            Toast.makeText(this,"已存在用户，请填写密码登录",Toast.LENGTH_SHORT).show();
            user.setText(editor_1.getString("name",null));
        }

    }

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.butLog:
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                SharedPreferences editor_1 = this.getSharedPreferences("user", MODE_PRIVATE);
                Random random = new Random();
                EditText user = (EditText) findViewById(R.id.etext1);
                EditText password = (EditText) findViewById(R.id.etext2);
                //获取密码的值
                String checkPassWord = editor_1.getString("password",null);
                //设置一个值，用来判断是否是第一次登陆
                boolean checkIsFirst = editor_1.getBoolean("isFirst",true);

                Intent intent = new Intent(MainActivity.this,listActivity.class);
                //如果已存在用户，判断登陆密码是否正确
                if(checkIsFirst == false && !password.getText().toString().equals(checkPassWord)) {
                    Toast.makeText(this,"密码错误，请重新登录",Toast.LENGTH_SHORT).show();
                    password.setText("");
                }

                //不存在用户则判断是否输入了用户名，如果没有输入用户名，则随机给一个名字
                else if(TextUtils.isEmpty(user.getText().toString())){
                    //生成0-10的随机数
                    int s = random.nextInt(10 + 1) + 0;
                    editor.putString("name", "user_" + s);
                    String str = "您没有输入用户名，因此使用随机用户名为：user_"+s;
                    editor.putString("password",password.getText().toString());
                    editor.putBoolean("isFirst",false);
                    editor.commit();
                    Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
                else {
                    editor.putString("name", user.getText().toString());
                    editor.putString("password",password.getText().toString());
                    editor.putBoolean("isFirst",false);
                    editor.commit();
                    startActivity(intent);
                }
                break;

        }

    }
}