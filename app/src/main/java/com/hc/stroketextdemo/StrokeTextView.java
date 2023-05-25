package com.hc.stroketextdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 支持描边的TextView
 *
 * @note 支持设置padding、gravity
 */
public class StrokeTextView extends AppCompatTextView {

    private TextPaint mStrokePaint;
    private int mStrokeColor;
    private float mStrokeWidth;
    private Rect mTextRect;
    private int mCallCount = 0;

    public StrokeTextView(Context context) {
        this(context, null);
    }

    public StrokeTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义的XML属性名称
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
        this.mStrokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, getResources().getColor(R.color.black));
        this.mStrokeWidth = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeWidth, 3);
        a.recycle();
        init();
    }

    private void init() {
        // lazy load
        if (mStrokePaint == null) {
            mStrokePaint = new TextPaint();
        }

        if (mTextRect == null) {
            mTextRect = new Rect();
        }

        // 复制原来TextView画笔中的一些参数
        TextPaint paint = getPaint();
        mStrokePaint.setTextSize(paint.getTextSize());
        mStrokePaint.setTypeface(paint.getTypeface());
        mStrokePaint.setFlags(paint.getFlags());
        mStrokePaint.setAlpha(paint.getAlpha());

        // 自定义描边效果
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
    }

    // 适配：当设置的宽高不够显示时（width = 1dp），自动扩充为wrap_content的区域大小，并居中显示，同时保留padding(如果有)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mCallCount++;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int modifiedWidth = widthSize;
        int modifiedHeight = heightSize;

        String text = getText().toString();
        // 每次重新计算text的宽度，避免list复用问题
        float textWidth = getPaint().measureText(text);
        float textHeight = 0;

        if (textWidth == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        } else {
            // 获取text的高度
            Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
            textHeight = fontMetrics.descent - fontMetrics.top; // descent线上面才有文字
        }

        float widthWeNeed = getCompoundPaddingRight() + getCompoundPaddingLeft() +
                textWidth + mStrokeWidth;
        // 因为text绘制的特性，文字上面的描边已经足够位置(ascent - top)画了
        // 而bottom - descent之间的区域可能不够 strokeWidth / 2
        float heightWeNeed = getCompoundPaddingTop() + getCompoundPaddingBottom() +
                textHeight + mStrokeWidth / 2;
        // specific size or match_parent，but we only handle specific size here
        if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) {

            // 第二次onMeasure()时，传回来的widthMode = MeasureSpec.EXACTLY
            // 会导致最终Stroke的描边位置不准确，所以这里控制第二次onMeasure()时，不对width进行特殊处理
            if (widthMode == MeasureSpec.EXACTLY && mCallCount < 2) {
                modifiedWidth = (int) Math.max(widthSize, widthWeNeed);

                // 如果值没有改变，说明是match_parent，足够位置显示，所以不特殊处理，否则居中显示
                if (modifiedWidth != widthSize) {
                    setGravity(Gravity.CENTER);
                }
            }

            if (heightMode == MeasureSpec.EXACTLY) {
                modifiedHeight = (int) Math.max(heightSize, heightWeNeed);

                // 如果值没有改变，说明是match_parent，足够位置显示，所以不特殊处理，否则居中显示
                if (modifiedHeight != heightSize) {
                    setGravity(Gravity.CENTER);
                }
            }

            super.onMeasure(MeasureSpec.makeMeasureSpec(modifiedWidth, widthMode),
                    MeasureSpec.makeMeasureSpec(modifiedHeight, heightMode));
        }

        // wrap_content
        else if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {

            if (widthMode == MeasureSpec.AT_MOST) {
                modifiedWidth = (int) (widthWeNeed);

                setGravity(Gravity.CENTER);
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                modifiedHeight = (int) (heightWeNeed);

                setGravity(Gravity.CENTER);
            }

            super.onMeasure(MeasureSpec.makeMeasureSpec(modifiedWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(modifiedHeight, MeasureSpec.EXACTLY));
        }

        // mCallCount = 2时重置，避免list复用问题
        mCallCount %= 2;
    }

    @Override
    public void onDraw(Canvas canvas) {

        String text = getText().toString();

        if (!TextUtils.isEmpty(text)) {

            //在文本底层画出带描边的文本
            int gravity = getGravity();

            // 优化描边位置的计算，同时支持左、右padding
            if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
                canvas.drawText(text, getCompoundPaddingLeft(),
                        getBaseline(), mStrokePaint);
            } else if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
                canvas.drawText(text, getWidth() - getCompoundPaddingRight() - getPaint().measureText(text),
                        getBaseline(), mStrokePaint);
            } else {
                // 除去左、右padding后，在剩下的空间中paint落笔的位置
                float xInLeftSpace = (getWidth() - getCompoundPaddingRight() - getCompoundPaddingLeft() - getPaint().measureText(text)) / 2;
                // 最终落笔点位置 [x = paddingLeft + xInLeftSpace, y = getBaseLine()]
                canvas.drawText(text, getPaddingLeft() + xInLeftSpace,
                        getBaseline(), mStrokePaint);
            }

        }

        super.onDraw(canvas);
    }

    @Override
    public void setTypeface(@androidx.annotation.Nullable Typeface tf) {
        // 模仿TextView的设置
        // 需在super.setTypeface()调用之前，不然没有效果
        if (mStrokePaint != null && mStrokePaint.getTypeface() != tf) {
            mStrokePaint.setTypeface(tf);
        }

        super.setTypeface(tf);
    }

    @Override
    public void setTypeface(@Nullable Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }

            setTypeface(tf);
            // now compute what (if any) algorithmic styling is needed
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            getPaint().setFakeBoldText((need & Typeface.BOLD) != 0);
            getPaint().setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);

            // 同步设置mStrokeTextPaint
            if (mStrokePaint != null) {
                mStrokePaint.setFakeBoldText((need & Typeface.BOLD) != 0);
                mStrokePaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
            }
        } else {
            getPaint().setFakeBoldText(false);
            getPaint().setTextSkewX(0);

            // 同步设置mStrokeTextPaint
            if (mStrokePaint != null) {
                mStrokePaint.setFakeBoldText(false);
                mStrokePaint.setTextSkewX(0);
            }

            setTypeface(tf);
        }
    }

}
