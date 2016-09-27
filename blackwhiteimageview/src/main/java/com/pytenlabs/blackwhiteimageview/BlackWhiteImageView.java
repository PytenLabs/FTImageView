package com.pytenlabs.blackwhiteimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ammu on 24-09-2016.
 */

public class BlackWhiteImageView extends ImageView {
    private Paint m_paint;
    Bitmap bitmap = null;
    private static final ScaleType SCALE_TYPE = ScaleType.FIT_XY;
    Boolean mChangeTouchColor = false;
    Boolean mChangeToBlacknWhite = false;
    Boolean mIsBlur = false;

    Canvas mCanvas;

    public BlackWhiteImageView(Context context) {
        super(context);
    }

    public BlackWhiteImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlackWhiteImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttributes(context, attrs, defStyleAttr);
        setBlacknWhite();
        setListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BlackWhiteImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        getAttributes(context, attrs, defStyleAttr);
        setBlacknWhite();
        setListener();

    }

    public void getAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlackWhiteImageView, defStyleAttr, 0);
        mChangeTouchColor = a.getBoolean(R.styleable.BlackWhiteImageView_changeTouchColor, false);
        mChangeToBlacknWhite = a.getBoolean(R.styleable.BlackWhiteImageView_changeToBlacknWhite, false);
        mIsBlur = a.getBoolean(R.styleable.BlackWhiteImageView_isBlur, false);
        a.recycle();
    }

    public void setListener() {
        if (mChangeTouchColor) {
            setOnTouchListener(onTouchListener);
        }
    }

    public void setBlacknWhite() {
        if (mChangeToBlacknWhite) {
            initColorFilter();
        } else {
            init();
        }
    }

    OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    init();
                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:
                    initColorFilter();
                    invalidate();
                    break;
            }

            return true;
        }
    };

    private void init() {
        m_paint = new Paint();
    }

    private void initColorFilter() {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        m_paint = new Paint();
        m_paint.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable d = getDrawable();
        if (d != null) {
            if (mIsBlur) {
                bitmap = blurRenderScript(((BitmapDrawable) getDrawable()).getBitmap(), 24);

            } else {
                bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

            }

            bitmap = Bitmap.createScaledBitmap(bitmap,  canvas.getWidth(),canvas.getHeight(),true);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, m_paint);

        } else {
            super.onDraw(canvas);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }

    private Bitmap blurRenderScript(Bitmap smallBitmap, int radius) {

        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(getContext());

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

}
