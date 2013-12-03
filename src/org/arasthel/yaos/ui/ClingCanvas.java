package org.arasthel.yaos.ui;

import org.arasthel.yaos.R;
import org.arasthel.yaos.app.YAOS;

import android.app.Application;
import android.app.LauncherActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class ClingCanvas extends RelativeLayout{


	public ClingCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap b = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
		float density = getResources().getDisplayMetrics().density;
		Canvas c = new Canvas(b);
		Drawable back = getResources().getDrawable(R.drawable.bg_cling2);
		back.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
		back.setAlpha(254);
		back.draw(c);
		Paint mErasePaint = new Paint();
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        mErasePaint.setColor(0xFFFFFF);
        mErasePaint.setAlpha(0);
        double[] d = Cling.dimens;
		if(!Cling.isCircle){
			Paint p = new Paint();
			p.setColor(Color.rgb(65, 186, 231));
			p.setAlpha(255);
			p.setStrokeWidth(2);
			p.setStyle(Paint.Style.STROKE);
			c.drawRect((float) d[0], (float)d[1], (float) d[2], (float)d[3], mErasePaint);
			c.drawRect((float) d[0], (float)d[1], (float) d[2], (float)d[3], p);
		}else{
			int x = (int) (-50*density);
	        int y = (int) (0*density);
	        int x2 = (int) (250*density);
	        int y2 = (int) (300*density);
	        int cx = (x2-Math.abs(x))/2;
	        int cy = (y2-Math.abs(y))/2;
	        int r = (int) (cx/1.4*density);
	        c.drawCircle(cx, cy, r, mErasePaint);
			Drawable cling = getResources().getDrawable(R.drawable.cling);
			cling.setBounds(x, y, x2,y2);
			cling.draw(c);
		}
		canvas.drawBitmap(b, 0, 0, null);
		c.setBitmap(null);
		b = null;
		super.onDraw(canvas);
	}
}
