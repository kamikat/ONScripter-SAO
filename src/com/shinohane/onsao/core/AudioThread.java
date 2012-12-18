package com.shinohane.onsao.core;

import android.content.Context;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;
import java.lang.Thread;

import com.shinohane.onsao.annotation.NativeReferenced;

@NativeReferenced
public class AudioThread {

	private Context mParent;
	private AudioTrack mAudio;
	private byte[] mAudioBuffer;

	public AudioThread(Context parent)
	{
		mParent = parent;
		mAudio = null;
		mAudioBuffer = null;
		nativeAudioInitJavaCallbacks();
	}
	
	@NativeReferenced
	public int fillBuffer()
	{
		mAudio.write( mAudioBuffer, 0, mAudioBuffer.length );
		return 1;
	}

	@NativeReferenced
	public int initAudio(int rate, int channels, int encoding, int bufSize)
	{
		if( mAudio == null )
		{
			channels = ( channels == 1 ) ? AudioFormat.CHANNEL_CONFIGURATION_MONO : 
											AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			encoding = ( encoding == 1 ) ? AudioFormat.ENCODING_PCM_16BIT :
											AudioFormat.ENCODING_PCM_8BIT;

			if( AudioTrack.getMinBufferSize( rate, channels, encoding ) > bufSize )
				bufSize = AudioTrack.getMinBufferSize( rate, channels, encoding );

			mAudioBuffer = new byte[bufSize];

			mAudio = new AudioTrack(AudioManager.STREAM_MUSIC, 
									rate,
									channels,
									encoding,
									bufSize,
									AudioTrack.MODE_STREAM );
			mAudio.play();
		}
		return mAudioBuffer.length;
	}

	@NativeReferenced
	public byte[] getBuffer()
	{
		return mAudioBuffer;
	}

	@NativeReferenced
	public int deinitAudio()
	{
		if( mAudio != null )
		{
			mAudio.stop();
			mAudio.release();
			mAudio = null;
		}
		mAudioBuffer = null;
		return 1;
	}

	@NativeReferenced
	public int initAudioThread()
	{
		// Make audio thread priority higher so audio thread won't get underrun
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		return 1;
	}
	
	public void onPause() {
		if( mAudio != null )
			mAudio.setStereoVolume(0.0f, 0.0f);
	}

	public void onResume() {
		if( mAudio != null )
			mAudio.setStereoVolume(1.0f, 1.0f);
	}

	private native int nativeAudioInitJavaCallbacks();
}

