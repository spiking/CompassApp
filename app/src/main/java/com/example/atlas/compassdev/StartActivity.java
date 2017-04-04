package com.example.atlas.compassdev;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void letTheFunBegin(View view) {
        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);
    }

}
