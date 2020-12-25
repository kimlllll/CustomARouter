package com.kimliu.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kimliu.arouter_annotation.ARouter;
import com.kimliu.arouter_annotation.Parameter;

@ARouter(path = "/order/OrderMainActivity",group = "order")
public class OrderMainActivity extends AppCompatActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_main);
    }
}