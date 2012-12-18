package com.shinohane.onsao.core;


import com.shinohane.onsao.annotation.NativeReferenced;
import com.shinohane.onsao.misc.GLSurfaceView_SDL;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

@NativeReferenced
public class DemoGLSurfaceView extends GLSurfaceView_SDL {
	
	private final String currentDirectoryPath;
	
	public String getDirectoryPath(){
		return currentDirectoryPath;
	}
	
	public DemoGLSurfaceView(
			Context context, 
			String currentDirectoryPath, boolean isRenderFontOutline) {
		super(context);
		this.currentDirectoryPath = currentDirectoryPath;
		mAudioThread = new AudioThread(context);
		mRenderer = new DemoRenderer(
				context, 
				currentDirectoryPath, isRenderFontOutline);
		setRenderer(mRenderer);
		
		// View Properties
		setKeepScreenOn(true);
		setFocusableInTouchMode(true);
		setFocusable(true);
		requestFocus();
		
	}

	public void exitApp() {
		mRenderer.exitApp();
	};

	@Override
	public void onPause() {
		nativeKey( 0, 3 ); // send SDL_ACTIVEEVENT
		super.onPause();
		surfaceDestroyed(this.getHolder());
		if( mAudioThread != null )
			mAudioThread.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		nativeKey( 0, 3 ); // send SDL_ACTIVEEVENT
		if( mAudioThread != null )
			mAudioThread.onResume();
	}
	
	@Override
	public boolean onTouchEvent(final MotionEvent event) 
	{
		// TODO: add multitouch support (added in Android 2.0 SDK)
		int action = -1;
		if(event.getAction() == MotionEvent.ACTION_DOWN)
			action = MOUSE_DOWN;
		if(event.getAction() == MotionEvent.ACTION_UP)
			action = MOUSE_UP;
		if(event.getAction() == MotionEvent.ACTION_MOVE)
			action = MOUSE_MOVE;
		if(action >= 0)
			nativeMouse((int)event.getX(), (int)event.getY(), action);

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			Activity activity = (Activity)this.getContext();
			AudioManager audio = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
			int volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) + (keyCode == KeyEvent.KEYCODE_VOLUME_UP ? 1 : (-1));
			if(volume >= 0 && volume <= audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)){
				audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_MENU){
			super.onKeyDown(keyCode, event);
			return false;
		}
		
		nativeKey(keyCode, KEY_DOWN);

		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_MENU){
			super.onKeyUp(keyCode, event);
			return false;
		}
		nativeKey(keyCode, KEY_UP);
		return true;
	}

	DemoRenderer mRenderer;
	
	private AudioThread mAudioThread = null;

	public native void nativeMouse(int x, int y, int action);
	public native void nativeKey(int keyCode, int down);

	public static final int KEY_DOWN = 1;
	public static final int KEY_UP = 0;
	public static final int MOUSE_DOWN = 0;
	public static final int MOUSE_UP = 1;
	public static final int MOUSE_MOVE = 2;
	
}
