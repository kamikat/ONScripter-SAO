package org.hanenoshino.uisao;

import java.io.File;
import java.util.ArrayList;

import com.footmark.utils.cache.FileCache;
import com.footmark.utils.image.ImageManager;
import com.footmark.utils.image.ImageSetter;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {

	{
		// Set the priority
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}

	private ImageManager imgMgr;

	private ListView games;
	private ImageView cover, background;
	private TextView gametitle;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		CoverDecoder.init(getApplicationContext(), cover.getWidth(), cover.getHeight());
		initImageManager();

		items = new GameItemAdapter(this, R.layout.gamelist_item, new ArrayList<GameItem>());
		games.setAdapter(items);
		games.setOnItemClickListener(this);
	}

	public void onDestroy() {
		super.onDestroy();
		destroyImageManager();
	}

	public void onResume() {
		super.onResume();

		new Handler() {
			public void handleMessage(Message msg) {
				items.add(new GameItem() {{title="月は东に日は西に ～Operation Sanctuary～"; cover="http://www.august-soft.com/hani/event/cg_09.jpg";}});
				items.add(new GameItem() {{title="寒蝉鸣泣之时系列"; cover="http://www.forcos.com/upload/2009_07/09071414528628.jpg";}});
				items.add(new GameItem() {{title="One Way Love～ミントちゃん物语"; cover="http://ec2.images-amazon.com/images/I/61LUkVZeNTL.jpg";}});
				items.add(new GameItem() {{title="水仙~narcissu~"; cover="http://img.4gdm.com/forum/201105/06/11050623502dd4b9cef1b2e2f3.jpg";}});
				items.add(new GameItem() {{title="水色"; cover="http://i2.sinaimg.cn/gm/2010/1110/20101110214231.jpg";}});
				items.add(new GameItem() {{title="Princess Holiday ～転がるりんご亭千夜一夜～"; cover="http://image.space.rakuten.co.jp/lg01/30/0000604730/52/img7529b0fbzik3zj.jpeg";}});
				items.add(new GameItem() {{title="月姫"; cover="http://i246.photobucket.com/albums/gg97/zelda45694/Shingetsutan%20Tsukihime/Tsukihime.jpg";}});
				items.add(new GameItem() {{title="海猫鸣泣之时"; cover="http://comic.ce.cn/news/dmzx/200805/06/W020080506493361950744.jpg";}});
				items.add(new GameItem() {{title="Kcnny"; cover="/sdcard/dbkg_kcn.jpg";}});
				items.add(new GameItem() {{title="Oda Nobuna"; cover="/sdcard/dbkg_nob.jpg";}});
				items.add(new GameItem() {{title="Yuruyuri"; cover="/sdcard/dbkg_yur.jpg";}});
				items.add(new GameItem() {{title="Remilia Scarlet"; cover="/sdcard/dbkg_rem.jpg";}});
			}
		}.sendMessageDelayed(Message.obtain(), 400);

	}

	@Override
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

	}

	public class GameItemAdapter extends ArrayAdapter<GameItem> implements ListAdapter {

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

		private int viewCount = 0;

		@TargetApi(11)
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
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
					v.setAlpha(0.8f);
					if(convertView == null)
						flyInAnimation(v, 100 * ++viewCount, 0.8f);
				}else{
					icon.setImageResource(R.drawable.test_icon_1);
					caption.setTextColor(getResources().getColor(R.color.sao_white));
					v.setBackgroundColor(getResources().getColor(R.color.sao_orange));
					v.setAlpha(1.0f);
					if(convertView == null)
						flyInAnimation(v, 100 * ++viewCount, 1.0f);
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
			games.smoothScrollBy(viewY, 300);
		}
	}

	private Animation animCoverOut = coverOutAnimation(new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			animCoverOut = coverOutAnimation(this);
			if(cover.getTag() instanceof Bitmap) {
				cover.setImageBitmap((Bitmap) cover.getTag());
				cover.setBackgroundDrawable(null);
				cover.setTag(null);
				cover.startAnimation(coverInAnimation());
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
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
				if(animCoverOut.hasEnded()||!animCoverOut.hasStarted()) {
					super.act();
					cover.startAnimation(coverInAnimation());
				}else{
					cover.setTag(image().bmp());
				}
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

		@Override
		public void onAnimationEnd(Animation arg0) {
			animBackgroundOut = bkgOutAnimation(this);
			if(background.getTag() instanceof Bitmap) {
				background.setImageBitmap((Bitmap) background.getTag());
				background.setBackgroundDrawable(null);
				background.setTag(null);
				background.startAnimation(bkgInAnimation());
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		items.setSelectedPosition(position);
		scrollViewToCenter(view);

		final GameItem item = items.getItem(position);

		if(item.background != null) {
			updateBackground(item.background);
		}
		if(item.cover != null) {
			updateCover(item.cover, item.background == null);
		}else{
			cover.setImageResource(R.drawable.dbkg_und);
			if(item.background == null) {
				background.setImageResource(R.drawable.dbkg_und_blur);
			}
		}

		gametitle.setText(item.title);
	}

}
