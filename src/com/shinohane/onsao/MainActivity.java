package com.shinohane.onsao;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.Vitamio;

import java.io.File;
import java.util.ArrayList;

import com.shinohane.onsao.R;

import com.footmark.utils.cache.FileCache;
import com.footmark.utils.image.ImageManager;
import com.footmark.utils.image.ImageSetter;
import com.shinohane.onsao.anim.AnimationAutomata;
import com.shinohane.onsao.anim.AnimationBuilder;
import com.shinohane.onsao.anim.AutomataAction;
import com.shinohane.onsao.anim.StateIO;
import com.shinohane.onsao.anim.StateRunner;
import com.shinohane.onsao.command.Command;
import com.shinohane.onsao.command.CommandHandler;
import com.shinohane.onsao.decoder.BackgroundDecoder;
import com.shinohane.onsao.decoder.CoverDecoder;
import com.shinohane.onsao.widget.AudioPlayer;
import com.shinohane.onsao.widget.MediaController;
import com.shinohane.onsao.widget.VideoView;
import com.shinohane.onsao.widget.VideoViewContainer;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener {

	{
		// Set the priority, trick useful for some CPU
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}
	
	// Controls Initialization Block {{{
	private ListView games;
	private ImageView cover, background;
	private TextView gametitle;
	private VideoViewContainer videoframe;
	private VideoView preview;
	private ImageView btn_settings, btn_about;
	
	private int mPlaybackErrCounter = 0;
	private static final int PLAYBACK_ERR_TOLERANCE = 3;
	private static final int PLAYBACK_ERR_IGNORED_TOLERANCE = 5;

	private <T> T $(int id) {
		return U.$(findViewById(id));
	}
	
	private void findViews() {
		games = $(R.id.games);
		cover = $(R.id.cover);
		background = $(R.id.background);
		gametitle = $(R.id.gametitle);
		videoframe = $(R.id.videoframe);
		preview = videoframe.getVideoView();
		btn_settings = $(R.id.btn_settings);
		btn_about = $(R.id.btn_about);
	}

	// }}}
	
	// ImageManager Block {{{
	private ImageManager imgMgr;
	
	private void initImageManager() {
		destroyImageManager();
		if(Environment.MEDIA_MOUNTED.equals(
				Environment.getExternalStorageState())){
			imgMgr = new ImageManager(new FileCache(
					new File(
							Environment.getExternalStorageDirectory(),
							Constant.DIRECTORY + "/.cover")));
		}else{
			imgMgr = new ImageManager(new FileCache(
					new File(
							getCacheDir(),
							"cover")));
		}
	}

	private void destroyImageManager() {
		if(imgMgr != null)
			imgMgr.shutdown();
	}

	// }}}
	
	// VideoPlayer Block {{{
	private static volatile boolean isVideoInitialized = false;

	private AudioPlayer mAudioPlayer = null;
	
	private void configureVideoPlayer() {
		preview.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
		preview.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer player) {
				mPlaybackErrCounter = 0;
				Command.invoke(LOOP_VIDEO_PREVIEW).args(preview).send();
			}

		});
		preview.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer player, int framework_err, int impl_err) {
				mStatePreview.gotoState(STATE_COVER_VISIBLE);
				mPlaybackErrCounter++;
				if(mPlaybackErrCounter < PLAYBACK_ERR_TOLERANCE) {
					Toast.makeText(getApplicationContext(), 
							R.string.error_play_video, Toast.LENGTH_LONG).show();
				} else {
					if(mPlaybackErrCounter < PLAYBACK_ERR_IGNORED_TOLERANCE) {
						Toast.makeText(getApplicationContext(), 
								R.string.error_repeated_playfail, Toast.LENGTH_LONG).show();
					}
				}
				return true;
			}

		});
		preview.setMediaController(new MediaController(this));

		mAudioPlayer = new AudioPlayer(this);
		mAudioPlayer.setMediaController(new MediaController(this), preview.getRootView());
		
		mAudioPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer player) {
				mPlaybackErrCounter = 0;
				Command.invoke(LOOP_AUDIO_PLAY).args(mAudioPlayer).send();
			}

		});
		
		mAudioPlayer.setOnErrorListener(new OnErrorListener() {

			public boolean onError(MediaPlayer player, int framework_err, int impl_err) {
				mPlaybackErrCounter++;
				if(mPlaybackErrCounter < PLAYBACK_ERR_TOLERANCE) {
					Toast.makeText(getApplicationContext(), 
							R.string.error_play_audio, Toast.LENGTH_LONG).show();
				} else {
					if(mPlaybackErrCounter < PLAYBACK_ERR_IGNORED_TOLERANCE) {
						Toast.makeText(getApplicationContext(), 
								R.string.error_repeated_playfail, Toast.LENGTH_LONG).show();
					}
				}
				return true;
			}

		});
		
		cover.setOnClickListener(new OnClickListener() {

			long lastClick = 0;
			
			public void onClick(View v) {
				if(mAudioPlayer.isInPlaybackState()) {
					mAudioPlayer.toggleMediaControlsVisiblity();
				} else {
					long time = System.currentTimeMillis();
					if(time - lastClick < 500) {
						Command.invoke(ACTION_AFTER_DISPLAY_COVER)
						.args(MainActivity.this).only().send();
						lastClick = 0;
					}else{
						lastClick = time;
					}
				}
			}
			
		});
		
		// Initialize the Vitamio codecs
		if(!Vitamio.isInitialized(this)) {
			new AsyncTask<Object, Object, Boolean>() {

				protected void onPreExecute() {
					isVideoInitialized = false;
				}

				protected Boolean doInBackground(Object... params) {
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					boolean inited = Vitamio.initialize(MainActivity.this);
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
					return inited;
				}

				protected void onPostExecute(Boolean inited) {
					if (inited) {
						isVideoInitialized = true;
						
						// Play video if exists
						Command.invoke(ACTION_AFTER_DISPLAY_COVER).args(MainActivity.this).only().sendDelayed(3000);
					}
				}

			}.execute();
		}else{
			isVideoInitialized = true;
		}
	}
	
	// }}}

	// Game Entries Block {{{
	private GameAdapter items;
	
	private boolean environmentCheck() {
		File mCurrentDirectory = new File(
				Environment.getExternalStorageDirectory() + "/" + Constant.DIRECTORY
				);
		if (!mCurrentDirectory.exists()) {
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.error))
			.setMessage(getString(R.string.no_sdcard_dir))
			.setPositiveButton(getString(R.string.known), 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			})
			.create()
			.show();
			return false;
		}
		return true;
	}
	
	private void scanGames () {
		items.clear();
		items.notifyDataSetChanged();
		
		new Thread() {
			
			public void run() {
				File root = new File(
						Environment.getExternalStorageDirectory() + "/" + Constant.DIRECTORY
						);
				File[] mDirectoryFiles = root.listFiles();
				for(File file: mDirectoryFiles) {
					if(!file.isHidden() && file.isDirectory()) {
						Game g = Game.scanGameDir(file);
						if(g != null) {
							// Add Game to Game List
							Command.invoke(ADD_ITEM_TO_LISTADAPTER)
							.args(items, g).send();
						}
					}
				}
			}
			
		}.start();
	}

	// }}}
	
	// Background Image Animation & Action Block {{{
	private StateRunner mStateBackground = new StateRunner(STATE_BKG_VISIBLE);;

	private static final int STATE_BKG_HIDDEN = 3000;
	private static final int STATE_BKG_VISIBLE = 3001;
	
	private void setupBackgroundAutomata() {
		AnimationAutomata.refer(mStateBackground).target(background)
		
		.edit(STATE_BKG_VISIBLE, STATE_BKG_HIDDEN)
		.setAnimation(AnimationBuilder.create()
				.alpha(1, 0).animateFor(1000).accelerated(1.5f)
				.build())
		.addAction(new AutomataAction() {
			public void onAnimationEnd(Animation animation) {
				Command.invoke(TRY_DISPLAY_BKG)
				.args(MainActivity.this).send();
			}
		})
		
		.edit(STATE_BKG_HIDDEN, STATE_BKG_VISIBLE)
		.setAnimation(AnimationBuilder.create()
				.alpha(0, 1).animateFor(1000).decelerated(1.5f)
				.build())
		;
	}

	public void tryDisplayBackground() {
		if(background.getTag() instanceof Bitmap && !mStateBackground.isAnyAnimatingAutomata()) {
			background.setImageBitmap((Bitmap) background.getTag());
			background.setBackgroundDrawable(null);
			background.setTag(null);
			mStateBackground.gotoState(STATE_BKG_HIDDEN, STATE_BKG_VISIBLE);
		}
		if(background.getTag() instanceof Drawable) {
			background.setImageDrawable((Drawable) background.getTag());
			background.setBackgroundDrawable(null);
			background.setTag(null);
			mStateBackground.gotoState(STATE_BKG_HIDDEN, STATE_BKG_VISIBLE);
		}
	}

	private void updateBackground(String url) {
		Object o = background.getTag();
		if(o instanceof ImageSetter) {
			((ImageSetter) o).cancel();
		}
		mStateBackground.gotoState(STATE_BKG_HIDDEN);
		imgMgr.requestImageAsync(url, new ImageSetter(background) {

			protected void act() {
				background.setTag(image().bmp());
				Command.invoke(TRY_DISPLAY_BKG)
				.args(MainActivity.this).send();
			}

		}, new BackgroundDecoder());
	}
	// }}}
	
	// Cover/VideoPlayer/AudioPlayer Animation & Action Block {{{
	private StateRunner mStatePreview = new StateRunner(STATE_COVER_VISIBLE);

	private static final int STATE_COVER_HIDDEN = 2000;
	private static final int STATE_COVER_VISIBLE = 2001;
	private static final int STATE_AUDIO_PLAY = 2002;
	private static final int STATE_VIDEO_PLAY = 2003;
	
	private void setupCoverPreviewAutomata() {
		AnimationAutomata.refer(mStatePreview).target(cover)
		
		.edit(STATE_COVER_VISIBLE, STATE_COVER_HIDDEN)
		.setAnimation(AnimationBuilder.create()
				// Set the valtype of the value to be inturrpted
				.valtype(Animation.RELATIVE_TO_SELF)
				// Add a Scale Animation
				.scale(1.0f, 0.6f, 1.0f, 0.6f, 0.5f, 0.5f).animateFor(100)
				// Add an Alpha Animation
				.alpha(1, 0).animateFor(100)
				// Build Animation
				.build())
		.addAction(new AutomataAction() {
			public void Before(Animation animation) {
				getAutomata().target().setVisibility(View.VISIBLE);
			}
			public void After(Animation animation) {
				getAutomata().target().setVisibility(View.GONE);
				Command.invoke(TRY_DISPLAY_COVER).only()
				.args(MainActivity.this).send();
			}
		})
		
		.edit(STATE_COVER_HIDDEN, STATE_COVER_VISIBLE)
		.setAnimation(AnimationBuilder.create()
				// Set the valtype of the value to be inturrpted
				.valtype(Animation.RELATIVE_TO_SELF)
				// Add a Scale Animation
				.scale(0.5f, 1.0f, 0.5f, 1.0f, 0.5f, 0.5f).overshoot()
				.animateFor(300)
				// Add an Alpha Animation
				.alpha(0, 1).animateFor(300)
				.build())
		.addAction(new AutomataAction() {
			public void Before(Animation animation) {
				getAutomata().target().setVisibility(View.VISIBLE);
			}
		})
		
		.edit(STATE_COVER_VISIBLE, STATE_AUDIO_PLAY)
		.addAction(new AutomataAction() {
			public void onStateChanged(int from, int to) {
				startAudioPlay();
			}
			private void startAudioPlay() {
				Game item = items.getSelectedItem();
				if(item.audio != null && isVideoInitialized) {
					mAudioPlayer.setAudioURI(null);
					mAudioPlayer.setAudioPath(item.audio);
				}
			}
		})
		
		.edit(STATE_AUDIO_PLAY, STATE_COVER_VISIBLE)
		.addAction(new AutomataAction() {
			public void onStateChanged(int from, int to) {
				releaseAudioPlay();
			}
			private void releaseAudioPlay() {
				mAudioPlayer.stopPlayback();
				mAudioPlayer.setAudioURI(null);
				mAudioPlayer.setMediaControlsVisibility(false);
			}
		})
		
		.edit(STATE_AUDIO_PLAY, STATE_COVER_HIDDEN)
		.setAnimation(STATE_COVER_VISIBLE, STATE_COVER_HIDDEN)
		.addAction(STATE_COVER_VISIBLE, STATE_COVER_HIDDEN)
		.addAction(STATE_AUDIO_PLAY, STATE_COVER_VISIBLE)
		
		.edit(STATE_COVER_VISIBLE, STATE_VIDEO_PLAY)
		// Code Control Video Play/Stop cannot dependent on the animation of cover
		.setAnimation(AnimationBuilder.create()
				.alpha(1, 0).animateFor(300)
				.build())
		.addAction(new AutomataAction() {
			public void Before(Animation animation) {
				getAutomata().target().setVisibility(View.VISIBLE);
			}
			public void After(Animation animation) {
				getAutomata().target().setVisibility(View.GONE);
			}
		})
		
		.edit(STATE_VIDEO_PLAY, STATE_COVER_VISIBLE)
		.setAnimation(AnimationBuilder.create()
				.alpha(0, 1).animateFor(300)
				.build())
		.setAction(STATE_COVER_HIDDEN, STATE_COVER_VISIBLE)
				
		.edit(STATE_VIDEO_PLAY, STATE_COVER_HIDDEN)
		.setAction(STATE_COVER_VISIBLE, STATE_COVER_HIDDEN)
		
		;
		
		AnimationAutomata.refer(mStatePreview).target(videoframe)
		
		.edit(STATE_COVER_VISIBLE, STATE_VIDEO_PLAY)
		.setAnimation(AnimationBuilder.create()
				.alpha(0, 1).animateFor(300)
				.build())
		.addAction(new AutomataAction() {
			public void Before(Animation animation) {
				getAutomata().target().setVisibility(View.VISIBLE);
			}
			public void After(Animation animation) {
				startVideoPlay();
			}
			private void startVideoPlay() {
				Game item = items.getSelectedItem();
				if(item.video != null && isVideoInitialized) {
					videoframe.setVisibility(View.VISIBLE);
					Command.revoke(RELEASE_VIDEO_PREVIEW);
					preview.setVideoURI(null);
					preview.setVideoPath(item.video);
				}
			}
		})
		
		.edit(STATE_COVER_HIDDEN, STATE_VIDEO_PLAY)
		.setAnimation(STATE_COVER_VISIBLE, STATE_VIDEO_PLAY)
		.setAction(STATE_COVER_VISIBLE, STATE_VIDEO_PLAY)
		
		.edit(STATE_VIDEO_PLAY, STATE_COVER_VISIBLE)
		.setAnimation(AnimationBuilder.create()
				.alpha(1, 0).animateFor(300)
				.build())
		.addAction(new AutomataAction() {
			public void onStateChanged(int from, int to) {
				releaseVideoPlay();
			}
			public void After(Animation animation) {
				getAutomata().target().setVisibility(View.GONE);
			}
			public void releaseVideoPlay() {
				// Clear Video Player
				if(preview.isInPlaybackState()){
					preview.stopPlayback();
				}
				preview.setVisibility(View.GONE);
				Command.invoke(RELEASE_VIDEO_PREVIEW)
				.args(preview).sendDelayed(2000);
			}
		})
		
		.edit(STATE_VIDEO_PLAY, STATE_COVER_HIDDEN)
		.setAnimation(STATE_VIDEO_PLAY, STATE_COVER_VISIBLE)
		.setAction(STATE_VIDEO_PLAY, STATE_COVER_VISIBLE)
		;
		
	}
	
	public void tryDisplayCover() {
		if(cover.getTag() instanceof Bitmap && !mStatePreview.isAnyAnimatingAutomata(cover)) {
			cover.setImageBitmap((Bitmap) cover.getTag());
			cover.setBackgroundDrawable(null);
			cover.setTag(null);
			mStatePreview.gotoState(STATE_COVER_HIDDEN, STATE_COVER_VISIBLE);
		}
		if(cover.getTag() instanceof Drawable) {
			cover.setImageDrawable((Drawable) cover.getTag());
			cover.setBackgroundDrawable(null);
			cover.setTag(null);
			mStatePreview.gotoState(STATE_COVER_HIDDEN, STATE_COVER_VISIBLE);
		}
	}
	
	public void checkActionAfterDisplayCover() {
		if(items == null) return;
		Game item = items.getSelectedItem();
		if(item == null) return;
		if(isVideoInitialized) {
			if(item.video != null) {
				mStatePreview.gotoState(STATE_VIDEO_PLAY);
			}else if(item.audio != null) {
				mStatePreview.gotoState(STATE_AUDIO_PLAY);
			}
		}
	}

	private void updateCover(final String url, final boolean coverToBkg) {
		Object o = cover.getTag();
		if(o instanceof ImageSetter) {
			((ImageSetter) o).cancel();
		}
		
		imgMgr.requestImageAsync(url,
				new ImageSetter(cover) {

			protected void act() {
				cover.setTag(image().bmp());
				Command.invoke(TRY_DISPLAY_COVER).args(MainActivity.this).only().send();
				if(coverToBkg) {
					String background = CoverDecoder.getThumbernailCache(url);
					// Exception for Web Images
					if(background == null)
						background = CoverDecoder.getThumbernailCache(image().file().getAbsolutePath());
					if(background != null) {
						updateBackground(background);
					}
				}
			}

		}, new CoverDecoder(cover.getWidth(), cover.getHeight()));

		mStatePreview.gotoState(STATE_COVER_HIDDEN);
	}
	
	// }}}
	
	public void loadGameItem(Game item) {
		
		gametitle.setText(item.title);

		if(item.background != null) {
			updateBackground(item.background);
		}

		if(item.cover != null) {
			updateCover(item.cover, item.background == null);
		}else{
			cover.setTag(getResources().getDrawable(R.drawable.dbkg_und));
			mStatePreview.gotoState(STATE_COVER_HIDDEN);
			if(item.background == null) {
				background.setTag(getResources().getDrawable(R.drawable.dbkg_und_blur));
				mStateBackground.gotoState(STATE_BKG_HIDDEN);
			}
		}
		
		// Perform Action After Display Cover in a Time-out way
		Command.invoke(ACTION_AFTER_DISPLAY_COVER).args(MainActivity.this).only().sendDelayed(4000);
		
	}
	
	private void setupUIAutomata() {
		setupBackgroundAutomata();
		setupCoverPreviewAutomata();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT < 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setContentView(R.layout.activity_main);
		findViews();
		
		if(!environmentCheck()) return;

		// Pass parameters to CoverDecoder to get better performance
		CoverDecoder.init(getApplicationContext(), cover.getWidth(), cover.getHeight());

		initImageManager();

		configureVideoPlayer();
		
		setupUIAutomata();

		// Initializing data and binding to ListView
		items = new GameAdapter(this, R.layout.gamelist_item, new ArrayList<Game>());
		games.setAdapter(items);
		games.setOnItemClickListener(this);

		Command.invoke(
				new Runnable() { public void run() {scanGames();}}
		).sendDelayed(500);
		
		btn_settings.setOnClickListener(this);
		btn_about.setOnClickListener(this);
		items.setOnConfigClickListener(this);
		items.setOnPlayClickListener(this);
		
	}

	public void onDestroy() {
		super.onDestroy();
		destroyImageManager();
	}

	public void onResume() {
		super.onResume();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_settings:
			
			break;
		case R.id.menu_end:
			finish();
			System.exit(0);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		U.scrollViewToCenter(view, games);

		if(items.getSelectedPosition() != position) {

			// Set Selection
			items.setSelectedPosition(position);

			loadGameItem(items.getSelectedItem());
			
		}
		
		Command.invoke(GameAdapter.SHOW_PANEL).args(items).sendDelayed(100);
	}

	public void onClick(View v) {
		final Game g;
		switch(v.getId()) {
		case R.id.btn_settings:
			
			break;
		case R.id.btn_about:
			
			break;
		case R.id.btn_config:
			g = items.getSelectedItem();
			switch(g.isItemRunnable()) {
			case MISS_FONT:
				U.showFontAlertDialog(this, g, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						config(g);
					}
				});
				break;
			case VALID:
				config(g);
				break;
			case INVALID:
	            Toast.makeText(
	            		this, 
	            		R.string.toast_cant_config_for_invalid, Toast.LENGTH_LONG
	            		).show();
				break;
			}
			break;
		case R.id.btn_play:
			g = items.getSelectedItem();
			switch(g.isItemRunnable()) {
			case VALID:
				run(g); // Directly run game if all green
				break;
			case MISS_FONT:
				U.showFontAlertDialog(this, g, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(whichButton == DialogInterface.BUTTON_POSITIVE)
							run(items.getSelectedItem());
					}
				});
				break;
			case INVALID:
	            Toast.makeText(
	            		this, 
	            		R.string.toast_cant_run_for_invalid, Toast.LENGTH_LONG
	            		).show();
				break;
			}
			break;
		}
	}
	
	private boolean onBackKeyPressed() {
    	if(videoframe.isVideoFullscreen()) {
    		videoframe.toggleFullscreen();
    		return true;
    	}
    	if(mStatePreview.currentState() == STATE_VIDEO_PLAY || 
    			mStatePreview.currentState() == STATE_AUDIO_PLAY) {
    		mStatePreview.gotoState(STATE_COVER_VISIBLE);
    		return true;
    	}
    	return false;
	}

	private long last_backkey_pressed = 0;
	
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(onBackKeyPressed()) {
        		return true;
        	}
            if (msg.getEventTime()-last_backkey_pressed<2000) {
                finish();
            } else {
                Toast.makeText(
                		this, 
                		R.string.notify_exit, Toast.LENGTH_SHORT
                		).show();
                last_backkey_pressed=msg.getEventTime();
            }
            return true;
        }
		return super.onKeyUp(keyCode, msg);
	}

	private void run(Game g) {
    	if(mStatePreview.currentState() == STATE_VIDEO_PLAY || 
    			mStatePreview.currentState() == STATE_AUDIO_PLAY) {
    		mStatePreview.gotoState(STATE_COVER_VISIBLE);
    	}
    	Command.revoke(ACTION_AFTER_DISPLAY_COVER);
		Intent intent = new Intent(this, ONScripterActivity.class);
		intent.putExtra(Constant.EXTRA_GAME_PATH, g.basepath);
		startActivity(intent);
	}
	
	private void config(Game g) {
		Intent intent = new Intent(this, GameConfigActivity.class);
		intent.putExtra(Constant.EXTRA_GAME_PATH, g.basepath);
		startActivityForResult(intent, Constant.REQUEST_CODE_CONFIG_GAME);
	}
	
	// Async Operation Block {{{
	
	static {
		// Register Async Operation
		com.shinohane.onsao.command.Command.register(MainActivity.class);
	}

	public static final int LOOP_VIDEO_PREVIEW = 13;
	
	public static final int RELEASE_VIDEO_PREVIEW = 14;

	public static final int LOOP_AUDIO_PLAY = 21;

	public static final int TRY_DISPLAY_COVER = 35;
	
	public static final int TRY_DISPLAY_BKG = 36;
	
	public static final int ACTION_AFTER_DISPLAY_COVER = 38;
	
	public static final int ADD_ITEM_TO_LISTADAPTER = 102;

	public static final int DATASET_CHANGED_LISTADAPTER = 103;
	
	@CommandHandler(id = LOOP_VIDEO_PREVIEW)
	public static void LOOP_VIDEO_PREVIEW(VideoView player) {
		player.seekTo(0);
		player.start();
	}
	
	@CommandHandler(id = RELEASE_VIDEO_PREVIEW)
	public static void RELEASE_VIDEO_PREVIEW(VideoView player) {
		player.setVideoURI(null);
	}
	
	@CommandHandler(id = LOOP_AUDIO_PLAY)
	public static void LOOP_AUDIO_PLAY(AudioPlayer player) {
		player.seekTo(0);
		player.start();
	}
	
	@CommandHandler(id = TRY_DISPLAY_COVER)
	public static void TRY_DISPLAY_COVER(MainActivity activity) {
		activity.tryDisplayCover();
	}

	@CommandHandler(id = TRY_DISPLAY_BKG)
	public static void TRY_DISPLAY_BKG(MainActivity activity) {
		activity.tryDisplayBackground();
	}

	@CommandHandler(id = ACTION_AFTER_DISPLAY_COVER)
	public static void ACTION_AFTER_DISPLAY_COVER(MainActivity activity) {
		activity.checkActionAfterDisplayCover();
	}
	
	@CommandHandler(id = ADD_ITEM_TO_LISTADAPTER)
	public static void ADD_ITEM_TO_LISTADAPTER(GameAdapter adapter, Game item) {
		adapter.add(item);
		Command.invoke(DATASET_CHANGED_LISTADAPTER).args(adapter).only().sendDelayed(200);
	}

	@CommandHandler(id = DATASET_CHANGED_LISTADAPTER)
	public static void DATASET_CHANGED_LISTADAPTER(GameAdapter adapter) {
		adapter.notifyDataSetChanged();
	}
	// }}}

}
