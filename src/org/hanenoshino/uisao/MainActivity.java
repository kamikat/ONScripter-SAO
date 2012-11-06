package org.hanenoshino.uisao;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.Vitamio;

import java.io.File;
import java.util.ArrayList;

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
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {

	{
		// Set the priority
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}
	
	private static volatile boolean isVideoInitialized = false;

	private ImageManager imgMgr;

	private ListView games;
	private ImageView cover, background;
	private TextView gametitle;
	private VideoView preview;
	private RelativeLayout videoframe;

	private GameItemAdapter items;

	@SuppressWarnings("unchecked")
	public <T> T $(View v, int id) {
		// Black Magic
		return (T) v.findViewById(id);
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
	
	private static Handler startMediaPlayer = new Handler() {
		
		public void handleMessage(Message msg) {
			if(msg.obj instanceof VideoView) {
				VideoView v = ((VideoView) msg.obj);
				if(v.canSeekForward()) {
					v.seekTo(0);
				}else{
					v.setVideoURI(v.getVideoURI());
				}
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(Build.VERSION.SDK_INT < 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setContentView(R.layout.activity_main);
		findViews();
		
		CoverDecoder.init(getApplicationContext(), cover.getWidth(), cover.getHeight());
		initImageManager();

		preview.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
		preview.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer player) {
				Message.obtain(startMediaPlayer, 0, preview).sendToTarget();
			}

		});
		preview.setMediaController(new MediaController(this));

		items = new GameItemAdapter(this, R.layout.gamelist_item, new ArrayList<GameItem>());
		games.setAdapter(items);
		games.setOnItemClickListener(this);
		
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

	public void onDestroy() {
		super.onDestroy();
		destroyImageManager();
	}

	public void onResume() {
		super.onResume();

		new Handler() {
			public void handleMessage(Message msg) {
				items.add(new GameItem() {{title="月は东に日は西に ～Operation Sanctuary～"; cover="http://www.august-soft.com/hani/event/cg_09.jpg"; video="/sdcard/test.mp4";}});
				items.add(new GameItem() {{title="寒蝉鸣泣之时系列"; cover="http://www.forcos.com/upload/2009_07/09071414528628.jpg"; video="/sdcard/test2.mp4";}});
				items.add(new GameItem() {{title="One Way Love～ミントちゃん物语"; cover="http://ec2.images-amazon.com/images/I/61LUkVZeNTL.jpg";}});
				items.add(new GameItem() {{title="水仙~narcissu~"; cover="http://img.4gdm.com/forum/201105/06/11050623502dd4b9cef1b2e2f3.jpg";}});
				items.add(new GameItem() {{title="水色"; cover="http://i2.sinaimg.cn/gm/2010/1110/20101110214231.jpg";}});
				items.add(new GameItem() {{title="Princess Holiday ～転がるりんご亭千夜一夜～"; cover="http://image.space.rakuten.co.jp/lg01/30/0000604730/52/img7529b0fbzik3zj.jpeg";}});
				items.add(new GameItem() {{title="月姫"; cover="http://i246.photobucket.com/albums/gg97/zelda45694/Shingetsutan%20Tsukihime/Tsukihime.jpg";}});
				items.add(new GameItem() {{title="海猫鸣泣之时"; cover="http://comic.ce.cn/news/dmzx/200805/06/W020080506493361950744.jpg";}});
				items.add(new GameItem() {{title="Kcnny"; cover="http://komica.byethost32.com/pix/src/1334318735221.jpg";}});
				items.add(new GameItem() {{title="Oda Nobuna"; cover="http://randomc.net/image/Oda%20Nobuna%20no%20Yabou/Oda%20Nobuna%20no%20Yabou%20-%20OP%20-%20Large%2002.jpg";}});
				items.add(new GameItem() {{title="Yuruyuri"; cover="http://www.emptyblue.it/data/wallpaper/Yuruyuri/yuruyuri_91341_thumb.jpg";}});
				items.add(new GameItem() {{title="Remilia Scarlet"; cover="http://konachan.com/image/5dd13f43bd3e78625a99ba49195cab50/Konachan.com%20-%2040803%20remilia_scarlet%20touhou.jpg";}});
			}
		}.sendMessageDelayed(Message.obtain(), 400);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public class GameItem {

		// Game Title
		public String title;

		// Path/To/Cover/File
		public String cover;

		// Description of the game
		public String description;

		// Optional Path/To/Background/File || blur from cover
		public String background;

		// Optional Path/To/Icon/File
		public String icon;
		
		public String video;

	}

	public class GameItemAdapter extends ArrayAdapter<GameItem> implements ListAdapter {
		
		public class ItemViewLoad {
			GameItem item;
			boolean selected;
		}
		
		private int textViewResourceId;

		public GameItemAdapter(Context context, int textViewResourceId, ArrayList<GameItem> items) {
			super(context, textViewResourceId, items);
			this.textViewResourceId=textViewResourceId;
		}

		private int selectedPos = -1;

		public void setSelectedPosition(int position) {
			selectedPos = position;
			notifyDataSetChanged();
		}

		public int getSelectedPosition() {
			return selectedPos;
		}
		
		public ItemViewLoad load(View v) {
			Object o = v.getTag();
			return (o instanceof ItemViewLoad)?(ItemViewLoad) o:null;
		}

		private int viewCount = 0;

		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
				v.setTag(new ItemViewLoad() {{
					item = getItem(position); 
					selected = false;
					}});
			}
			GameItem o = getItem(position);
			if (o != null) {
				ImageView icon = $(v, R.id.icon);
				TextView caption = $(v, R.id.caption);
				caption.setText(o.title);
				if(selectedPos != position) {
					icon.setImageResource(R.drawable.test_icon_0);
					caption.setTextColor(getResources().getColor(R.color.sao_grey));
					v.setBackgroundColor(getResources().getColor(R.color.sao_transparent_white));
					// Following code implements v.setAlpha(0.8f);
					if(load(v).selected) {
						leaveSelected(v);
						load(v).selected = false;
					}
					if(convertView == null)
						flyInAnimation(v, 30 * ++viewCount, 0.8f);
				}else{
					icon.setImageResource(R.drawable.test_icon_1);
					caption.setTextColor(getResources().getColor(R.color.sao_white));
					v.setBackgroundColor(getResources().getColor(R.color.sao_orange));
					// Following code implements v.setAlpha(1.0f);
					if(!load(v).selected) {
						goSelected(v);
						load(v).selected = true;
					}
					if(convertView == null)
						flyInAnimation(v, 30 * ++viewCount, 1.0f);
				}
			}
			return v;
		}

		/**
		 * List Item Animation Generator
		 * @param v
		 * @param delay
		 * @param alpha
		 */
		private void flyInAnimation(View v, long delay, float alpha) {
			AnimationSet set = new AnimationSet(true);
			AlphaAnimation animAlpha = new AlphaAnimation(0, alpha);
			TranslateAnimation animTrans = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.7f, Animation.RELATIVE_TO_PARENT, 0f, 
					Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
			animAlpha.setDuration(200);
			animTrans.setDuration(200);
			set.addAnimation(animAlpha);
			set.addAnimation(animTrans);
			set.setStartOffset(delay);
			set.setInterpolator(new DecelerateInterpolator(1.5f));
			set.setFillAfter(true);
			v.startAnimation(set);
		}
		
		private void goSelected(View v) {
			AlphaAnimation animAlpha = new AlphaAnimation(0.8f, 1.0f);
			animAlpha.setDuration(200);
			animAlpha.setFillAfter(true);
			v.startAnimation(animAlpha);
		}
		
		private void leaveSelected(View v) {
			AlphaAnimation animAlpha = new AlphaAnimation(0.8f, 0.8f);
			animAlpha.setFillAfter(true);
			v.startAnimation(animAlpha);
		}

	}

	private Animation coverInAnimation() {
		AnimationSet set = new AnimationSet(false);
		AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
		ScaleAnimation animScale = new ScaleAnimation(
				0.5f, 1.0f, 0.5f, 1.0f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animScale.setInterpolator(new OvershootInterpolator());
		animAlpha.setDuration(300);
		animScale.setDuration(300);
		set.addAnimation(animAlpha);
		set.addAnimation(animScale);
		set.setFillAfter(true);
		return set;
	}

	private Animation coverOutAnimation(AnimationListener listener) {
		AnimationSet set = new AnimationSet(true);
		AlphaAnimation animAlpha = new AlphaAnimation(1, 0);
		ScaleAnimation animScale = new ScaleAnimation(
				1.0f, 1.2f, 1.0f, 1.2f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animAlpha.setDuration(100);
		animScale.setDuration(100);
		set.addAnimation(animAlpha);
		set.addAnimation(animScale);
		set.setAnimationListener(listener);
		return set;
	}

	private Animation bkgInAnimation() {
		AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
		animAlpha.setDuration(1000);
		animAlpha.setInterpolator(new DecelerateInterpolator(1.5f));
		animAlpha.setFillAfter(true);
		return animAlpha;
	}

	private Animation bkgOutAnimation(AnimationListener listener) {
		AlphaAnimation animAlpha = new AlphaAnimation(1, 0);
		animAlpha.setDuration(1000);
		animAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
		animAlpha.setAnimationListener(listener);
		return animAlpha;
	}
	
	private Animation videoPlayerAnimation(AnimationListener listener) {
		AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
		animAlpha.setDuration(200);
		animAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
		animAlpha.setAnimationListener(listener);
		return animAlpha;
	}
	
	private Animation hideVideoPlayerAnimation(AnimationListener listener) {
		AlphaAnimation animAlpha = new AlphaAnimation(1, 0);
		animAlpha.setDuration(200);
		animAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
		animAlpha.setAnimationListener(listener);
		return animAlpha;
	}

	private void displayCover() {
		Object o = cover.getTag();
		if(o instanceof Bitmap) {
			cover.setTag(null);
			cover.setImageBitmap((Bitmap) o);
			cover.setBackgroundDrawable(null);
			cover.startAnimation(coverInAnimation());
			GameItem item = items.getItem(items.getSelectedPosition());
			if(item.video != null) {
				videoframe.startAnimation(videoPlayerAnimation(new AnimationListener(){

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
		GameItem item = items.getItem(items.getSelectedPosition());
		if(item.video != null) {
			// Load Video
			if(isVideoInitialized) {
				preview.setVisibility(View.VISIBLE);
				preview.setVideoPath(item.video);
			}
			return true;
		}
		return false;
	}
	
	private Animation animCoverOut = coverOutAnimation(new AnimationListener() {

		public void onAnimationEnd(Animation animation) {
			animCoverOut = coverOutAnimation(this);
			displayCover();
		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

	});

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

	private Animation animBackgroundOut = bkgOutAnimation(new AnimationListener() {

		public void onAnimationEnd(Animation arg0) {
			animBackgroundOut = bkgOutAnimation(this);
			if(background.getTag() instanceof Bitmap) {
				background.setImageBitmap((Bitmap) background.getTag());
				background.setBackgroundDrawable(null);
				background.setTag(null);
				background.startAnimation(bkgInAnimation());
			}
		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

	});

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
					background.startAnimation(bkgInAnimation());
				}else{
					background.setTag(image().bmp());
				}
			}

		},
		new BackgroundDecoder());

	}

	private Handler hdlListScroller = new Handler() {
		
		public void handleMessage(Message msg) {
			games.smoothScrollBy(msg.what, 300);
		}
		
	};
	
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
			Message msg = Message.obtain(hdlListScroller, viewY);
			hdlListScroller.sendMessageDelayed(msg, 100);
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
				videoframe.startAnimation(hideVideoPlayerAnimation(new AnimationListener(){

					public void onAnimationEnd(Animation animation) {
						videoframe.setVisibility(View.GONE);
					}

					public void onAnimationRepeat(Animation animation) {}

					public void onAnimationStart(Animation animation) {}
					
				}));
			}
			
			// Set Selection
			items.setSelectedPosition(position);

			final GameItem item = items.getItem(position);

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
