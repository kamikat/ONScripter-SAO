package org.hanenoshino.uisao;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
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
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {

	private static final int ACTION_LOOP_VIDEO_PREVIEW = 13;
	private static final int ACTION_GENERATE_TEST_DATA = 184;
	// obj - listview, arg1 - distance, arg2 - duration
	private static final int ACTION_SCROLL_LIST_FOR_DISTANCE_IN_ANY_MILLS = 10;
	
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

	private static <T> T $(Object o) {
		return U.$(o);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T $(int id) {
		// Black Magic
		return (T) findViewById(id);
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
							"saoui/cover")));
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
				Message.obtain(WhatDoUWantFromMe, ACTION_LOOP_VIDEO_PREVIEW, preview).sendToTarget();
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
					}
				}

			}.execute();
		}else{
			isVideoInitialized = true;
		}
	}

	@SuppressLint("SdCardPath")
	private static void fillTestData(GameAdapter items) {
		items.add(new Game() {{title="月は东に日は西に ～Operation Sanctuary～"; cover="http://www.august-soft.com/hani/event/cg_09.jpg"; video="/sdcard/test.mp4";}});
		items.add(new Game() {{title="寒蝉鸣泣之时系列"; cover="http://www.forcos.com/upload/2009_07/09071414528628.jpg"; video="/sdcard/test2.mp4";}});
		items.add(new Game() {{title="One Way Love～ミントちゃん物语"; cover="http://ec2.images-amazon.com/images/I/61LUkVZeNTL.jpg";}});
		items.add(new Game() {{title="水仙~narcissu~"; cover="http://img.4gdm.com/forum/201105/06/11050623502dd4b9cef1b2e2f3.jpg";}});
		items.add(new Game() {{title="水色"; cover="http://i2.sinaimg.cn/gm/2010/1110/20101110214231.jpg";}});
		items.add(new Game() {{title="Princess Holiday ～転がるりんご亭千夜一夜～"; cover="http://image.space.rakuten.co.jp/lg01/30/0000604730/52/img7529b0fbzik3zj.jpeg";}});
		items.add(new Game() {{title="月姫"; cover="http://i246.photobucket.com/albums/gg97/zelda45694/Shingetsutan%20Tsukihime/Tsukihime.jpg";}});
		items.add(new Game() {{title="海猫鸣泣之时"; cover="http://comic.ce.cn/news/dmzx/200805/06/W020080506493361950744.jpg";}});
		items.add(new Game() {{title="Kcnny"; cover="http://komica.byethost32.com/pix/src/1334318735221.jpg";}});
		items.add(new Game() {{title="Oda Nobuna"; cover="http://randomc.net/image/Oda%20Nobuna%20no%20Yabou/Oda%20Nobuna%20no%20Yabou%20-%20OP%20-%20Large%2002.jpg";}});
		items.add(new Game() {{title="Yuruyuri"; cover="http://www.emptyblue.it/data/wallpaper/Yuruyuri/yuruyuri_91341_thumb.jpg";}});
		items.add(new Game() {{title="Remilia Scarlet"; cover="http://konachan.com/image/5dd13f43bd3e78625a99ba49195cab50/Konachan.com%20-%2040803%20remilia_scarlet%20touhou.jpg";}});
	}

	private static Handler WhatDoUWantFromMe = new Handler() {
		
		public void handleMessage(Message msg) {
			switch(msg.what){
			case ACTION_LOOP_VIDEO_PREVIEW:
				VideoView videoview = $(msg.obj);
				if(videoview.canSeekForward()) {
					videoview.seekTo(0);
				}else{
					videoview.setVideoURI(videoview.getVideoURI());
				}
				break;
			case ACTION_SCROLL_LIST_FOR_DISTANCE_IN_ANY_MILLS:
				ListView listview = $(msg.obj);
				listview.smoothScrollBy(msg.arg1, msg.arg2);
				break;
			case ACTION_GENERATE_TEST_DATA:
				GameAdapter datalist = $(msg.obj);
				fillTestData(datalist);
				break;
			}
		}
		
	};
	
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
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(Build.VERSION.SDK_INT < 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setContentView(R.layout.activity_main);
		findViews();
		
		// Pass parameters to CoverDecoder to get better performance
		CoverDecoder.init(getApplicationContext(), cover.getWidth(), cover.getHeight());
		
		initImageManager();

		configureVideoPlayer();
		
		// Initializing data and binding to ListView
		items = new GameAdapter(this, R.layout.gamelist_item, new ArrayList<Game>());
		games.setAdapter(items);
		games.setOnItemClickListener(this);
		
	}

	public void onDestroy() {
		super.onDestroy();
		destroyImageManager();
	}
	
	public void onResume() {
		super.onResume();
		
		// Ask for test data
		WhatDoUWantFromMe.sendMessageDelayed(
				Message.obtain(WhatDoUWantFromMe, ACTION_GENERATE_TEST_DATA, items)
				, 400);
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
			Game item = items.getItem(items.getSelectedPosition());
			if(item.video != null) {
				videoframe.startAnimation(AnimationFactory.videoPlayerAnimation(new AnimationListener(){

					public void onAnimationEnd(Animation animation) {
						playPreview();
					}

					public void onAnimationRepeat(Animation animation) {}

					public void onAnimationStart(Animation animation) {
						videoframe.setVisibility(View.VISIBLE);
					}
					
				}));
			}
		}
	}
	
	private boolean playPreview() {
		Game item = items.getItem(items.getSelectedPosition());
		if(item.video != null) {
			// Load Video
			if(isVideoInitialized) {
				videoframe.setVisibility(View.VISIBLE);
				preview.setVisibility(View.VISIBLE);
				preview.setVideoPath(item.video);
			}
			return true;
		}
		return false;
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
			Message msg = Message.obtain(
					WhatDoUWantFromMe, ACTION_SCROLL_LIST_FOR_DISTANCE_IN_ANY_MILLS,
					games);
			msg.arg1 = viewY;
			msg.arg2 = 300;
			WhatDoUWantFromMe.sendMessageDelayed(msg, 100);
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		scrollViewToCenter(view);

		if(items.getSelectedPosition() != position) {
			
			// Clear Video Player
			if(preview.isPlaying()){
				preview.stopPlayback();
				preview.setVideoURI(null);
				preview.setVisibility(View.GONE);
				videoframe.startAnimation(animHideVideo);
			}
			
			// Set Selection
			items.setSelectedPosition(position);

			final Game item = items.getItem(position);

			if(item.background != null) {
				updateBackground(item.background);
			}
			if(item.cover != null) {
				updateCover(item.cover, item.background == null);
			}else{
				// If no cover but video, play video directly
				if(item.video != null) {
					playPreview();
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
