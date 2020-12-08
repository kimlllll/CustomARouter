package com.kimliu.customarouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kimliu.arouter_annotation.ARouter;

@ARouter(path = "/app/MainActivity",group = "app")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}