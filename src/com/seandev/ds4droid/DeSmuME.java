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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;

class DeSmuME {
	
	public static Context context;
	
	static boolean loaded = false;
	
	static final int CPUTYPE_V7 = 0;
	static final int CPUTYPE_NEON = 1;
	
	static void load()
	{
		if(loaded)
			return;
		System.loadLibrary("cpudetect");
		final int cpuType = getCPUType();
		switch(cpuType) {
		case CPUTYPE_NEON:
			System.loadLibrary("desmumeneon");
			Log.i(MainActivity.TAG, "Using NEON enhanced native library.");
			break;
		default:
			System.loadLibrary("desmumev7");
			Log.i(MainActivity.TAG, "Using ARMv7 native library, hopefully this is supported.");
			break;
		}
		loaded = true;
	}
	
	static native int getCPUType();
	static native void init();
	static native void runCore();
	static native int runOther();
	static native void resize(Bitmap bitmap);
	static native void draw(Bitmap bitmapMain, Bitmap bitmapTouch, boolean rotate);
	static native void touchScreenTouch(int x, int y);
	static native void touchScreenRelease();
	static native void setButtons(int l, int r, int up, int down, int left, int right, int a, int b, int x, int y, int start, int select);
	static native boolean loadRom(String path);
	static native void setWorkingDir(String path, String temp);
	static native void saveState(int slot);
	static native void restoreState(int slot);
	static native void loadSettings();
	static native int getNativeWidth();
	static native int getNativeHeight();
	static native void setFilter(int index);
	static native void copyMasterBuffer();
	static native void change3D(int set);
	static native void changeSound(int set);
	static native void setSoundPaused(int set);
	static native void drawToSurface(Surface surface);
	static native void reloadFirmware();
	static native int getNumberOfCheats();
	static native String getCheatName(int pos);
	static native boolean getCheatEnabled(int pos);
	static native String getCheatCode(int pos);
	static native int getCheatType(int pos);
	static native void addCheat(String description, String code);
	static native void updateCheat(String description, String code, int pos);
	static native void saveCheats();
	static native void setCheatEnabled(int pos, boolean enabled);
	static native void deleteCheat(int pos);
	static native void setMicPaused(int set);
	static native void closeRom();
	
	static boolean touchScreenMode = false;
	static boolean inited = false;
	static boolean romLoaded = false;
	
	public static int getSettingInt(String name, int def)
	{
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
		if(!pm.contains(name))
			return def;
		try {
			return pm.getInt(name, def);
		}
		catch(ClassCastException e) {
		}
		try {
			String ret = pm.getString(name, String.valueOf(def));
			return Integer.valueOf(ret);
		}
		catch(ClassCastException e) {
		}
		try {
			Boolean ret = pm.getBoolean(name, def == 0 ? false : true);
			return ret.booleanValue() ? 1 : 0;
		}
		catch(ClassCastException e) {
		}
		return def;
	}

}