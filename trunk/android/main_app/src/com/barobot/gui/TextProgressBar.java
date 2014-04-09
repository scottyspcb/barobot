package com.barobot.gui;

import com.barobot.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class TextProgressBar extends ProgressBar {
	
	private String text = "";
    private int textColor = Color.BLACK;
    private float shadowRadius = 0;
    

	private float shadowDx = 0;
    private float shadowDy = 0;
    private int shadowColor = Color.BLACK;
    private float textSize = 15;
 
    public TextProgressBar(Context context) {
        super(context);
    }
 
    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(attrs);
    }
 
    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttrs(attrs);
    }
    
    private void setAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextProgressBar, 0, 0);
            setText(a.getString(R.styleable.TextProgressBar_text));
            setTextColor(a.getColor(R.styleable.TextProgressBar_textColor, Color.BLACK));
            setTextSize(a.getDimension(R.styleable.TextProgressBar_textSize, 15));
            
            setShadowRadius(a.getDimension(R.styleable.TextProgressBar_shadowRadius, 0));
            setShadowDx(a.getDimension(R.styleable.TextProgressBar_shadowDx, 0));
            setShadowDy(a.getDimension(R.styleable.TextProgressBar_shadowDy, 0));
            setShadowColor(a.getColor(R.styleable.TextProgressBar_shadowColor, R.color.Transparent));
            
            a.recycle();
        }
    }
    
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //create an instance of class Paint, set color and font size
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
        //In order to show text in a middle, we need to know its size
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        //Now we store font size in bounds variable and can calculate it's position
        int x = getWidth() / 2 - bounds.centerX();
        int y = getHeight() / 2 - bounds.centerY();
        //drawing text with appropriate color and size in the center
        canvas.drawText(text, x, y, textPaint);
    }
 
    public String getText() {
        return text;
    }
 
    public synchronized void setText(String text) {
        this.text = text;
    }
 
    public int getTextColor() {
        return textColor;
    }
 
    public synchronized void setTextColor(int textColor) {
        this.textColor = textColor;
    }
 
    public float getTextSize() {
        return textSize;
    }
 
    public synchronized void setTextSize(float textSize) {
        this.textSize = textSize;
    }
    
    public float getShadowRadius() {
    	return shadowRadius;
    }
    public float getShadowDx() {
		return shadowDx;
	}

	public void setShadowDx(float shadowDx) {
		this.shadowDx = shadowDx;
	}

	public float getShadowDy() {
		return shadowDy;
	}

	public void setShadowDy(float shadowDy) {
		this.shadowDy = shadowDy;
	}

	public int getShadowColor() {
		return shadowColor;
	}

	public void setShadowColor(int shadowColor) {
		this.shadowColor = shadowColor;
	}

	public void setShadowRadius(float shadowRadius) {
		this.shadowRadius = shadowRadius;
	}
 
}
