package org.hanenoshino.uisao;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.Vitamio;

import java.io.File;
import java.util.ArrayList;
import org.hanenoshino.uisao.decoder.BackgroundDecoder;
import org.hanenoshino.uisao.decoder.CoverDecoder;
import org.hanenoshino.uisao.widget.MediaController;
import org.hanenoshino.uisao.widget.VideoView;
import com.footmark.utils.cache.FileCache;
import com.footmark.utils.image.ImageManager;
import com.footmark.utils.image.ImageSetter;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {

	{
		// Set the priority, trick useful for some CPU
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}

	private static volatile boolean isVideoInitialized = false;

	private ImageManager imgMgr;

	private ListView games;
	private ImageView cover, background;
	private TextView gametitle;
	private VideoView preview;
	private RelativeLayout videoframe;

	private GameAdapter items;

	private <T> T $(int id) {
		return U.$(findViewById(id));
	}

	private void findViews() {
		games = $(R.id.games);
		cover = $(R.id.cover);
		background = $(R.id.background);
		gametitle = $(R.id.gametitle);
		preview = $(R.id.surface_view);
		videoframe = $(R.id.videoframe);
	}

	private void initImageManager() {
		destroyImageManager();
		if(Environment.MEDIA_MOUNTED.equals(
				Environment.getExternalStorageState())){
			imgMgr = new ImageManager(new FileCache(
					new File(
							Environment.getExternalStorageDirectory(),
							"saoui/.cover")));
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

	private void configureVideoPlayer() {
		preview.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
		preview.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer player) {
				Command.invoke(Command.LOOP_VIDEO_PREVIEW).of(preview).send();
			}

		});
		preview.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer player, int framework_err, int impl_err) {
				releaseVideoPlay();
				return true;
			}

		});
		preview.setMediaController(new MediaController(this));

		// Initialize the Vitamio codecs
		if(!Vitamio.isInitialized(this)) {
			new AsyncTask<Object, Object, Boolean>() {
				@Override
				protected void onPreExecute() {
					isVideoInitialized = false;
				}

				@Override
				protected Boolean doInBackground(Object... params) {
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					boolean inited = Vitamio.initialize(MainActivity.this);
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
					return inited;
				}

				@Override
				protected void onPostExecute(Boolean inited) {
					if (inited) {
						isVideoInitialized = true;
						
						// Play video if exists
						Command.invoke(Command.MAINACTIVITY_PLAY_VIDEO).of(this).only().sendDelayed(2000);
					}
				}

			}.execute();
		}else{
			isVideoInitialized = true;
		}
	}
	
	private boolean environmentCheck() {
		File mCurrentDirectory = new File(
				Environment.getExternalStorageDirectory() + "/saoui"
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
						Environment.getExternalStorageDirectory() + "/saoui"
						);
				File[] mDirectoryFiles = root.listFiles();
				for(File file: mDirectoryFiles) {
					if(!file.isHidden() && file.isDirectory()) {
						Game g = Game.scanGameDir(file);
						if(g != null) {
							// Add Game to Game List
							Command.invoke(Command.ADD_ITEM_TO_LISTADAPTER)
							.of(items).args(g.toBundle()).send();
						}
					}
				}
			}
			
		}.start();
	}

	private Animation animCoverOut = AnimationFactory.coverOutAnimation(new AnimationListener() {

		public void onAnimationEnd(Animation animation) {
			animCoverOut = AnimationFactory.coverOutAnimation(this);
			displayCover();
		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

	});

	private Animation animBackgroundOut = AnimationFactory.bkgOutAnimation(new AnimationListener() {

		public void onAnimationEnd(Animation arg0) {
			animBackgroundOut = AnimationFactory.bkgOutAnimation(this);
			if(background.getTag() instanceof Bitmap) {
				background.setImageBitmap((Bitmap) background.getTag());
				background.setBackgroundDrawable(null);
				background.setTag(null);
				background.startAnimation(AnimationFactory.bkgInAnimation());
			}
		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

	});

	private Animation animHideVideo = AnimationFactory.hideVideoPlayerAnimation(new AnimationListener(){

		public void onAnimationEnd(Animation animation) {
			videoframe.setVisibility(View.GONE);
		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

	});

	private Animation animPlayVideo = AnimationFactory.videoPlayerAnimation(new AnimationListener(){

		public void onAnimationEnd(Animation animation) {
			startVideoPlay();
		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {
			videoframe.setVisibility(View.VISIBLE);
		}
		
		private void startVideoPlay() {
			Game item = items.getItem(items.getSelectedPosition());
			if(item.video != null && isVideoInitialized) {
					videoframe.setVisibility(View.VISIBLE);
					preview.setVisibility(View.VISIBLE);
					Command.revoke(Command.RELEASE_VIDEO_PREVIEW, preview);
					preview.setVideoURI(null);
					preview.setVideoPath(item.video);
			}
		}

	});


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

		// Initializing data and binding to ListView
		items = new GameAdapter(this, R.layout.gamelist_item, new ArrayList<Game>());
		games.setAdapter(items);
		games.setOnItemClickListener(this);

		Command.invoke(Command.RUN).of(
				new Runnable() { public void run() {scanGames();}}
		).sendDelayed(500);
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

	private void displayCover() {
		Object o = cover.getTag();
		if(o instanceof Bitmap) {
			cover.setTag(null);
			cover.setImageBitmap((Bitmap) o);
			cover.setBackgroundDrawable(null);
			cover.startAnimation(AnimationFactory.coverInAnimation());
		}
	}

	public void playVideo() {
		Game item = items.getItem(items.getSelectedPosition());
		if(item.video != null && isVideoInitialized) {
			videoframe.clearAnimation();
			animPlayVideo.reset();
			videoframe.startAnimation(animPlayVideo);
		}
	}

	public void releaseVideoPlay() {
		videoframe.clearAnimation();

		// Clear Video Player
		if(preview.isPlaying()){
			preview.stopPlayback();
		}
		
		preview.setVisibility(View.GONE);

		if(videoframe.getVisibility() == View.VISIBLE) {
			animHideVideo.reset();
			videoframe.startAnimation(animHideVideo);
		}
		
		Command.invoke(Command.RELEASE_VIDEO_PREVIEW).exclude(Command.MAINACTIVITY_PLAY_VIDEO)
		.of(preview).sendDelayed(2000);
	}

	private void updateCover(final String url, final boolean coverToBkg) {
		cover.setVisibility(View.INVISIBLE);
		Object o = cover.getTag();
		if(o instanceof ImageSetter) {
			((ImageSetter) o).cancel();
		}

		if(!animCoverOut.hasStarted()) {
			cover.startAnimation(animCoverOut);
		}

		imgMgr.requestImageAsync(url,
				new ImageSetter(cover) {

			protected void act() {
				cover.setTag(image().bmp());
				displayCover();
				String background = CoverDecoder.getThumbernailCache(url);
				// Exception for Web Images
				if(background == null)
					background = CoverDecoder.getThumbernailCache(image().file().getAbsolutePath());
				if(coverToBkg && background != null) {
					updateBackground(background);
				}
			}

		},
		new CoverDecoder(cover.getWidth(), cover.getHeight()));
	}

	private void updateBackground(String url) {
		background.setVisibility(View.INVISIBLE);
		Object o = background.getTag();
		if(o instanceof ImageSetter) {
			((ImageSetter) o).cancel();
		}

		if(!animBackgroundOut.hasStarted())
			background.startAnimation(animBackgroundOut);

		imgMgr.requestImageAsync(url, new ImageSetter(background) {

			protected void act() {
				if(animBackgroundOut.hasEnded()||!animBackgroundOut.hasStarted()) {
					super.act();
					background.startAnimation(AnimationFactory.bkgInAnimation());
				}else{
					background.setTag(image().bmp());
				}
			}

		},
		new BackgroundDecoder());

	}

	/**
	 * Scroll view to the center of the game list
	 * @param view
	 * child view of game list
	 */
	private void scrollViewToCenter(View view) {
		int viewY = view.getTop() + view.getHeight() / 2 - games.getHeight() / 2;
		if(viewY < 0 && games.getFirstVisiblePosition() == 0){
			games.smoothScrollToPosition(0);
		}else if(viewY > 0 && games.getLastVisiblePosition() == items.getCount() - 1){
			games.smoothScrollToPosition(items.getCount() - 1);
		}else{
			Command.invoke(Command.SCROLL_LIST_FOR_DISTANCE_IN_ANY_MILLIS)
			.of(games).only().args(viewY, 300).sendDelayed(100);
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		scrollViewToCenter(view);

		if(items.getSelectedPosition() != position) {

			releaseVideoPlay();

			// Set Selection
			items.setSelectedPosition(position);

			final Game item = items.getItem(position);

			if(item.background != null) {
				updateBackground(item.background);
			}
			if(item.cover != null) {
				updateCover(item.cover, item.background == null);
				Command.invoke(Command.MAINACTIVITY_PLAY_VIDEO).of(this).only().sendDelayed(3000);
			}else{
				// If no cover but video, play video directly
				if(item.video != null) {
					playVideo();
				}else{
					// With no multimedia information
					cover.setImageResource(R.drawable.dbkg_und);
				}
				if(item.background == null) {
					background.setImageResource(R.drawable.dbkg_und_blur);
				}
			}

			gametitle.setText(item.title);
		}
	}

}
