/*
 Copyright (C) 2012-2013 Sean Dev
 
 This file is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 2 of the License, or
 (at your option) any later version.
 
 This file is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with the this software.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.seandev.ds4droid;

import java.util.ArrayList;
import com.seandev.ds4droid.MainActivity.NDSView;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;

class Controls {
	
	float xscale = 0, yscale = 0;
	final Paint controlsPaint = new Paint();
	NDSView view;
	boolean landscape;
	Rect screen;
	float aspectRatio;
	
	Controls(NDSView view) {
		this.view = view;
	}
	
	public void setView(NDSView view) {
		this.view = view;
	}
	
	Button touchButton;
	
	final SparseIntArray keyMappings = new SparseIntArray();
	
	public static final int[] KEYS_WITH_MAPPINGS = new int[] { Button.BUTTON_UP, Button.BUTTON_DOWN, Button.BUTTON_LEFT, Button.BUTTON_RIGHT,
			Button.BUTTON_A, Button.BUTTON_B, Button.BUTTON_X, Button.BUTTON_Y, Button.BUTTON_START, Button.BUTTON_SELECT,
			Button.BUTTON_TOUCH, Button.BUTTON_L, Button.BUTTON_R };
	
	void loadMappings(Context context) {
		keyMappings.clear();
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		for(int id : KEYS_WITH_MAPPINGS) {
			int map = prefs.getInt("Controls.KeyMap." + Button.getButtonName(id), 0);
			if(map != 0)
				keyMappings.put(map, id);
		}
	}
	
	void loadControls(Context context, int screenWidth, int screenHeight, boolean is565, boolean landscape) {
		
		screen = new Rect(0, 0, screenWidth, screenHeight);
		
		xscale = (float)screen.width() / (landscape ? 512.0f : 256.0f);
		yscale = (float)screen.height() / (landscape ? 192.0f : 384.0f);
		
		aspectRatio = (float)screen.width() / (float)screen.height();
		
		for(int i = 0 ; i < buttonStates.length ; ++i)
			buttonStates[i] = 0;
		
		buttonsToDraw.clear();
		buttonsToProcess.clear();
		activeTouches.clear();
		
		this.landscape = landscape;
		
		final Rect space = landscape ? defaultLandSpace : defaultPortSpace;
		
		final Button l = Button.load(context, Button.BUTTON_L, R.drawable.l, landscape, is565, screen, space, false);
		if(l.bitmap != null) {
			buttonsToDraw.add(l);
			buttonsToProcess.add(l);
		}
		
		final Button r = Button.load(context, Button.BUTTON_R, R.drawable.r, landscape, is565, screen, space, false);
		if(r.bitmap != null) {
			buttonsToDraw.add(r);
			buttonsToProcess.add(r);
		}
		
		touchButton = Button.load(context, Button.BUTTON_TOUCH, R.drawable.touch, landscape, is565, screen, space, false);
		if(touchButton.bitmap != null) 
			buttonsToDraw.add(touchButton);
		
		
		final Button start = Button.load(context, Button.BUTTON_START, R.drawable.start, landscape, is565, screen, space, false);
		if(start.bitmap != null) {
			buttonsToDraw.add(start);
			buttonsToProcess.add(start);
		}
		
		final Button select = Button.load(context, Button.BUTTON_SELECT, R.drawable.select, landscape, is565, screen, space, false);
		if(select.bitmap != null) {
			buttonsToDraw.add(select);
			buttonsToProcess.add(select);
		}
		
		final Button dpad = Button.load(context, Button.BUTTON_DPAD, R.drawable.dpad, landscape, is565, screen, space, false);
		if(dpad.bitmap != null) {
			buttonsToDraw.add(dpad);
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.334f, 0.0f, 0.647f, 0.353f), Button.BUTTON_UP));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.631f, 0.35f, 0.973f, 0.643f), Button.BUTTON_RIGHT));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.0f, 0.35f, 0.356f, 0.643f), Button.BUTTON_LEFT));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.334f, 0.643f, 0.647f, 1.0f), Button.BUTTON_DOWN));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.026f, 0.047f, 0.344f, 0.334f), Button.BUTTON_UPLEFT));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.64f, 0.047f, 0.862f, 0.334f), Button.BUTTON_UPRIGHT));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.026f, 0.65f, 0.344f, 0.926f), Button.BUTTON_DOWNLEFT));
			buttonsToProcess.add(new Button(getRatioRect(dpad.position, 0.64f, 0.65f, 0.862f, 0.926f), Button.BUTTON_DOWNRIGHT));
		}
		
		final Button abxy = Button.load(context, Button.BUTTON_ABXY, R.drawable.abxy, landscape, is565, screen, space, false);
		if(abxy.bitmap != null) {
			buttonsToDraw.add(abxy);
			buttonsToProcess.add(new Button(getRatioRect(abxy.position, 0.317f, 0.0f, 0.676f, 0.378f), Button.BUTTON_X));
			buttonsToProcess.add(new Button(getRatioRect(abxy.position, 0.662f, 0.293f, 1.0f, 0.681f), Button.BUTTON_A));
			buttonsToProcess.add(new Button(getRatioRect(abxy.position, 0.317f, 0.611f, 0.676f, 1.0f), Button.BUTTON_B));
			buttonsToProcess.add(new Button(getRatioRect(abxy.position, 0.0f, 0.293f, 0.334f, 0.681f), Button.BUTTON_Y));
		}
		
	}
	
	static Rect getRatioRect(Rect base, float left, float top, float right, float bottom) {
		final int width = base.width();
		final int height = base.height();
		return new Rect(
			(int)(base.left + (width * left)),
			(int)(base.top + (height * top)),
			(int)(base.left + (width * right)),
			(int)(base.top + (height * bottom))
		);
	}
	
	//Screen resolution for button mapping (3x DS screen size)
	public static final Rect defaultPortSpace = new Rect(0, 0, 768, 1152);
	public static final Rect defaultLandSpace = new Rect(0, 0, 1152, 768);
	
	//On/off button values
	final int[] buttonStates = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	final SparseArray<Button> activeTouches = new SparseArray<Button>(); 
	
	final ArrayList<Button> buttonsToDraw = new ArrayList<Button>();
	final ArrayList<Button> buttonsToProcess = new ArrayList<Button>();
	
	boolean touchScreenProcess(MotionEvent event) {
		switch(event.getAction()) {
		//TODO: Fix landscape touch input
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			float x = event.getX();
			float y = event.getY();
			x /= xscale;
			y /= yscale;
			if(landscape && view.dontRotate) {
				final float newy = x / 1.33f;
				final float newx = (192 - y) * 1.33f;
				x = newx;
				y = newy;
			}
			if(landscape && !view.dontRotate && view.landscapeMode) {
				x -= (256 / 2); //Center squished landscape touch input
				if(x >= 0)
					DeSmuME.touchScreenTouch((int)x, (int)y);
			} else {
				y -= 192;
				if(y >= 0)
					DeSmuME.touchScreenTouch((int)x, (int)y);
			}
		
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			DeSmuME.touchScreenRelease();
			if(touchButton.bitmap != null && touchButton.position.contains((int)event.getX(), (int)event.getY())) {
				DeSmuME.touchScreenMode = false;
			}
			break;
		default:
			return false;
		}
		return true;		
	}
	
	boolean onTouchEvent(MotionEvent event) {
		if(xscale == 0 || yscale == 0)
			return false;
		if(DeSmuME.touchScreenMode) 
			return touchScreenProcess(event);
		else {
			switch(event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_MOVE:
			{
				int i = event.getActionIndex();
				int id = event.getPointerId(i);
				
				final Button existingTouch = activeTouches.get(id);
				if(existingTouch != null) {
					existingTouch.apply(buttonStates, false);
				}
				int x = (int) event.getX(i);
				int y = (int) event.getY(i);
				
				boolean pressedButton = false;
				for(Button process : buttonsToProcess) {
					if(process.position.contains(x, y)) {
						process.apply(buttonStates, true);
						activeTouches.put(id, process);
						if(event.getActionMasked() != MotionEvent.ACTION_MOVE)
							view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
						pressedButton = true;
						break;
					}
				}
				
				if(!pressedButton) 
					return touchScreenProcess(event);
			}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				if(touchButton.bitmap != null && touchButton.position.contains((int)event.getX(), (int)event.getY())) {
					DeSmuME.touchScreenMode = true;
					activeTouches.clear();
					break;
				}
				touchScreenProcess(event);
			case MotionEvent.ACTION_CANCEL:
			{
				int i = event.getActionIndex();
				int id = event.getPointerId(i);
				Button button = activeTouches.get(id);
				if(button == null)
					break;
				button.apply(buttonStates, false);
				activeTouches.remove(id);
			}
				break;
			default:
				return false;
			}
			sendStates();
			return true;
		}
	}
	
	boolean onKeyDown(int keyCode, KeyEvent event) {
		int button = keyMappings.get(keyCode, -1);
		if(button == -1)
			return false;
		if(button >=0 && button < buttonStates.length)
			buttonStates[button] = 1;
		sendStates();
		return true;
	}
	
	boolean onKeyUp(int keyCode, KeyEvent event) {
		int button = keyMappings.get(keyCode, -1);
		if(button == -1)
			return false;
		if(button >=0 && button < buttonStates.length)
			buttonStates[button] = 0;
		else if(button == Button.BUTTON_TOUCH) {
			DeSmuME.touchScreenMode = !DeSmuME.touchScreenMode;
		}
		sendStates();
		return true;
	}
	
	void sendStates() {
		DeSmuME.setButtons(buttonStates[Button.BUTTON_L], buttonStates[Button.BUTTON_R], buttonStates[Button.BUTTON_UP], buttonStates[Button.BUTTON_DOWN], buttonStates[Button.BUTTON_LEFT], buttonStates[Button.BUTTON_RIGHT], 
				buttonStates[Button.BUTTON_A], buttonStates[Button.BUTTON_B], buttonStates[Button.BUTTON_X], buttonStates[Button.BUTTON_Y], buttonStates[Button.BUTTON_START], buttonStates[Button.BUTTON_SELECT]);

	}
	
	void drawControls(Canvas canvas) {
		if(DeSmuME.touchScreenMode) {
			if(touchButton.bitmap != null)
				canvas.drawBitmap(touchButton.bitmap, touchButton.position.left, touchButton.position.top, controlsPaint);
		}
		else {
			for(Button button : buttonsToDraw)  {
				if(button.bitmap != null && button.position != null)
					canvas.drawBitmap(button.bitmap, button.position.left, button.position.top, controlsPaint);
			}
		}
	}
}