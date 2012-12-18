package com.shinohane.onsao.widget;

import com.shinohane.onsao.core.ONScripter;

import android.app.Activity;
import android.view.KeyEvent;

public class ONScripterView extends ONScripter{
	
	public interface OnVideoPlayRequestedListener extends ONScripter.OnVideoPlayRequestedListener {
		
	}
	
	public ONScripterView(
			Activity context, 
			String currentDirectoryPath, boolean isRenderFontOutline) {
		super(context, currentDirectoryPath, isRenderFontOutline);
	}
	
	public void switchAutoMode() {
		nativeKey( KeyEvent.KEYCODE_A, 1 );
		nativeKey( KeyEvent.KEYCODE_A, 0 );
	}
	
	public void switchSkipMode() {
		nativeKey( KeyEvent.KEYCODE_S, 1 );
		nativeKey( KeyEvent.KEYCODE_S, 0 );
	}
	
	public void switchSpeed() {
		nativeKey( KeyEvent.KEYCODE_O, 1 );
		nativeKey( KeyEvent.KEYCODE_O, 0 );
	}
	
	public void quit() {
		nativeKey( KeyEvent.KEYCODE_MENU, 2 ); // Send SDL_QUIT
	}
	
}
