package com.kimliu.customarouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kimliu.arouter_annotation.ARouter;
import com.kimliu.arouter_annotation.Parameter;

@ARouter(path = "/app/MainActivity",group = "app")
public class MainActivity extends AppCompatActivity {


    @Parameter
    String name;
    @Parameter
    String sex;
    @Parameter
    int age = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}