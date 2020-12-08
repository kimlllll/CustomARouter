package com.kimliu.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kimliu.arouter_annotation.ARouter;

@ARouter(path = "/personal/PersonalMainActivity",group = "personal")
public class PersonalMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_main);
    }
}