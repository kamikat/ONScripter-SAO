package com.shinohane.onsao.core;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.net.Uri;

import com.shinohane.onsao.annotation.NativeReferenced;

@NativeReferenced
public class ONScripter extends DemoGLSurfaceView 
{
	
	private final Activity parent;
	private final int game_width;
	private final int game_height;
	
	public int width(){
		return game_width;
	}
	
	public int height(){
		return game_height;
	}
	
	public ONScripter(Activity context, String currentDirectoryPath,
			boolean isRenderFontOutline) {
		super(context, currentDirectoryPath, isRenderFontOutline);
		parent = context;
		nativeInitJavaCallbacks();
		game_width  = nativeGetWidth();
		game_height = nativeGetHeight();
	}

	public interface OnVideoPlayRequestedListener {
		public void playVideo(String videoPath);
	}
	
	private OnVideoPlayRequestedListener mOnVideoPlayRequestedListener;
	
	public void setOnVideoPlayRequestedListener(OnVideoPlayRequestedListener l) {
		mOnVideoPlayRequestedListener = l;
	}
	
	private boolean inVideoPlay = false;
	
	@NativeReferenced
	public void playVideo(char[] filename){
		if(mOnVideoPlayRequestedListener != null) {
			if(!inVideoPlay) {
				mOnVideoPlayRequestedListener.playVideo(new String(filename));
				onPause();
				this.setVisibility(View.GONE);
				inVideoPlay = true;
			}
		} else {
			try{
				String filename2 = "file:/" + getDirectoryPath() + "/" + new String(filename);
				filename2 = filename2.replace('\\', '/');
				Log.v("ONS", "playVideo: " + filename2);
				Uri uri = Uri.parse(filename2);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(uri, "video/*");
				parent.startActivityForResult(i, -1);
			}catch(Exception e){
				Log.e("ONS", "playVideo error:  " + e.getClass().getName());
			}
		}
	}
	
	public void playVideoFinished() {
		if(inVideoPlay) {
			inVideoPlay = false;
			onResume();
			this.setVisibility(View.VISIBLE);
		}
	}
	
	private native int nativeInitJavaCallbacks();
	private native int nativeGetWidth();
	private native int nativeGetHeight();
    
	public static void loadLibrary(){
		loadLibrary("");
	}
	
	public static void loadLibrary(String locale){
		System.loadLibrary("mad");
		System.loadLibrary("bz2");
		System.loadLibrary("tremor");
		System.loadLibrary("sdl");
		System.loadLibrary("sdl_mixer");
		System.loadLibrary("sdl_image");
		System.loadLibrary("sdl_ttf");
		System.loadLibrary("application" + locale);
		System.loadLibrary("sdl_main");
	}
	
}
