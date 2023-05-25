package com.hc.stroketextdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 支持描边的TextView
 *
 * @Note 效果最佳
 */
public class StrokeTextView1 extends AppCompatTextView {
    private TextView outlineTextView = null;
    private float mStrokeWidth;
    private int mStrokeColor;

    public StrokeTextView1(Context context) {
        this(context, null);
    }

    public StrokeTextView1(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeTextView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        outlineTextView = new TextView(context, attrs);
        //取得客製化的xml名稱
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
        //取得對應的屬性值
        this.mStrokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, getResources().getColor(R.color.black));
        this.mStrokeWidth = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeWidth, 3);
        init();
    }

    public void init() {
        TextPaint paint = outlineTextView.getPaint();
        paint.setStrokeWidth(mStrokeWidth);// 描邊寬度
        paint.setStyle(Paint.Style.STROKE);
        outlineTextView.setTextColor(mStrokeColor);// 描邊顏色
        outlineTextView.setGravity(getGravity());
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        outlineTextView.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 設定描邊文字
        CharSequence outlineText = outlineTextView.getText();
        if (outlineText == null || !outlineText.equals(this.getText())) {
            outlineTextView.setText(getText());
            postInvalidate();
        }
        outlineTextView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        outlineTextView.layout(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        outlineTextView.draw(canvas);
        super.onDraw(canvas);
    }
}
