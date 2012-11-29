package org.hanenoshino.uisao;

import java.util.ArrayList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class GameAdapter extends ArrayAdapter<Game> implements ListAdapter {

	private static <T> T $(View v, int id) {
		return U.$(v, id);
	}

	// Class for storage state in Tag of correspoinding View
	public class ItemViewLoad {
		Game item;
		boolean selected;
	}

	private int textViewResourceId;

	public GameAdapter(Context context, int textViewResourceId, ArrayList<Game> items) {
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
	
	private OnClickListener mOnConfigClickListener, mOnPlayClickListener;
	
	public void setOnConfigClickListener(OnClickListener listener) {
		mOnConfigClickListener = listener;
	}
	
	public void setOnPlayClickListener(OnClickListener listener) {
		mOnPlayClickListener = listener;
	}

	public ItemViewLoad load(View v) {
		Object o = v.getTag();
		return (o instanceof ItemViewLoad)?(ItemViewLoad) o:null;
	}

	private int maxPosition = 0;
	private int viewCount = 0;

	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(textViewResourceId, null);
			v.setTag(new ItemViewLoad() {{
				item = getItem(position);
				selected = false;
				}});
		}
		Game o = getItem(position);
		if (o != null) {
			ImageView icon = $(v, R.id.icon);
			TextView caption = $(v, R.id.caption);
			caption.setText(o.title);
			float alpha;
			if(selectedPos != position) {
				icon.setImageResource(R.drawable.test_icon_0);
				caption.setTextColor(getContext().getResources().getColor(R.color.sao_grey));
				v.setBackgroundColor(getContext().getResources().getColor(R.color.sao_transparent_white));
				// Following code implements v.setAlpha(0.8f);
				if(load(v).selected) {
					leaveSelected(v);
					hidePanel(v, true);
					load(v).selected = false;
				}else{
					hidePanel(v, false);
				}
				alpha = 0.8f;
			}else{
				icon.setImageResource(R.drawable.test_icon_1);
				caption.setTextColor(getContext().getResources().getColor(R.color.sao_white));
				v.setBackgroundColor(getContext().getResources().getColor(R.color.sao_orange));
				// Following code implements v.setAlpha(1.0f);
				if(!load(v).selected) {
					goSelected(v);
					load(v).selected = true;
				}
				alpha = 1.0f;
			}
			// animation on item first displayed TODO
			if(convertView == null || position > maxPosition) {
				if(viewCount == parent.getChildCount())
					v.startAnimation(
							GetAnimation.For.ListItem.OnItemFirstDisplayed(++viewCount * 50, alpha)
							);
				else
					v.startAnimation(
							GetAnimation.For.ListItem.OnItemFirstDisplayed(0, alpha)
							);
			}
			if(position > maxPosition) maxPosition = position;
		}
		return v;
	}

	private void goSelected(View v) {
		AlphaAnimation animAlpha = new AlphaAnimation(0.8f, 1.0f);
		animAlpha.setDuration(200);
		animAlpha.setFillAfter(true);
		v.startAnimation(animAlpha);
	}

	private void leaveSelected(View v) {
		U.setAlpha(v, 0.8f);
	}
	
	public void showPanel(View v) {
		final View start_panel = $(v, R.id.start_panel);
		if(start_panel.getVisibility()==View.VISIBLE)
			return;
		View title = $(v, R.id.caption);
		View btn_play = $(v, R.id.btn_play);
		View btn_config = $(v, R.id.btn_config);
		btn_play.setClickable(true);
		btn_config.setClickable(true);
		start_panel.setClickable(true);
		btn_play.setOnClickListener(mOnPlayClickListener);
		btn_config.setOnClickListener(mOnConfigClickListener);
		AlphaAnimation animAlpha = new AlphaAnimation(0.0f, 1.0f);
		animAlpha.setFillAfter(true);
		animAlpha.setDuration(500);
		animAlpha.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {}

			public void onAnimationRepeat(Animation animation) {}

			public void onAnimationStart(Animation animation) {
				start_panel.setVisibility(View.VISIBLE);
			}
			
		});
		start_panel.startAnimation(animAlpha);
		animAlpha = new AlphaAnimation(1.0f, 0.5f);
		animAlpha.setFillAfter(true);
		animAlpha.setDuration(500);
		title.startAnimation(animAlpha);

		final View p = v;
		Command.invoke(Command.RUN).of(new Runnable() {
			 public void run() {
				 hidePanel(p, true);
			 }
		}).sendDelayed(5000);
	}
	
	public void hidePanel(View v, boolean animation) {
		final View start_panel = $(v, R.id.start_panel);
		if(start_panel.getVisibility()==View.GONE || 
		   (start_panel.getAnimation() != null && !start_panel.getAnimation().hasEnded()))
			return;
		View title = $(v, R.id.caption);
		View btn_play = $(v, R.id.btn_play);
		View btn_config = $(v, R.id.btn_config);
		btn_play.setClickable(false);
		btn_config.setClickable(false);
		start_panel.setClickable(false);
		if(animation) {
			AlphaAnimation animAlpha = new AlphaAnimation(1.0f, 0.0f);
			animAlpha.setFillAfter(true);
			animAlpha.setDuration(300);
			animAlpha.setAnimationListener(new AnimationListener() {
	
				public void onAnimationEnd(Animation animation) {
					start_panel.setVisibility(View.GONE);
				}
	
				public void onAnimationRepeat(Animation animation) {}
	
				public void onAnimationStart(Animation animation) {start_panel.setVisibility(View.VISIBLE);}
				
			});
			start_panel.startAnimation(animAlpha);
			animAlpha = new AlphaAnimation(0.5f, 1.0f);
			animAlpha.setFillAfter(true);
			animAlpha.setDuration(300);
			title.startAnimation(animAlpha);
		}else{
			start_panel.setVisibility(View.GONE);
			Animation animAlpha = new AlphaAnimation(0.5f, 1.0f);
			animAlpha.setFillAfter(true);
			animAlpha.setDuration(100);
			title.startAnimation(animAlpha);
		}
	}

}