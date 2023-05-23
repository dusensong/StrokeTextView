package com.hc.stroketextdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 番外篇
 */
public class StrokeTextViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroke_text_view);

        initTextViews();

    }

    private void initTextViews() {
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "Dressel Medium Regular.ttf");
//        if (typeface != null) {
//            TextView tvOld = findViewById(R.id.tv_old);
//            TextView tvOld2 = findViewById(R.id.tv_old_2);
//
//            tvOld2.setTypeface(typeface);
//        }
    }
}