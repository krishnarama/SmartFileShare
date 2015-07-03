package com.android.BluetoothFTP;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class MyShadowBuilder extends View.DragShadowBuilder{
	   private Drawable shadow = null;
	   public MyShadowBuilder(View view) {
		super(view);
		shadow = new ColorDrawable(Color.YELLOW);
		//shadow.setCallback(view);
		shadow.setBounds(0, 0, view.getWidth(), view.getHeight());
	}
	   @Override
       public void onDrawShadow(Canvas canvas) {

		   super.onDrawShadow(canvas);
           // Draws the ColorDrawable in the Canvas passed in from the system.
           shadow.draw(canvas);
           getView().draw(canvas);
       }   
   }
