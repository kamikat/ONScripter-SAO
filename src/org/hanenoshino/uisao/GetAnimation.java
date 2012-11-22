package org.hanenoshino.uisao;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;

public class GetAnimation {

	public static class General {

		public static Animation Alpha(float from, float to, long duration) {
			AlphaAnimation anim = new AlphaAnimation(from, to);
			anim.setFillAfter(true);
			anim.setDuration(duration);
			return anim;
		}

	}

	public static class For {
		public static class MainInterface {

			public static Animation ToShowCover(AnimationListener listener) {
				AnimationSet anim = new AnimationSet(false);
				ScaleAnimation animScale = new ScaleAnimation(0.5f, 1.0f, 0.5f,
						1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animScale.setInterpolator(new OvershootInterpolator());
				animScale.setDuration(300);
				anim.addAnimation(General.Alpha(0, 1, 300));
				anim.addAnimation(animScale);
				anim.setFillAfter(true);
				anim.setAnimationListener(listener);
				return anim;
			}

			public static Animation ToHideCover(AnimationListener listener) {
				AnimationSet anim = new AnimationSet(true);
				ScaleAnimation animScale = new ScaleAnimation(1.0f, 1.5f, 1.0f,
						1.5f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animScale.setDuration(300);
				anim.addAnimation(General.Alpha(1, 0, 300));
				anim.addAnimation(animScale);
				anim.setInterpolator(new AccelerateInterpolator(2));
				anim.setAnimationListener(listener);
				return anim;
			}

			public static Animation ToShowBackground(AnimationListener listener) {
				Animation anim = General.Alpha(0, 1, 1000);
				;
				anim.setInterpolator(new DecelerateInterpolator(1.5f));
				anim.setAnimationListener(listener);
				return anim;
			}

			public static Animation ToHideBackground(AnimationListener listener) {
				Animation anim = General.Alpha(1, 0, 1000);
				anim.setInterpolator(new AccelerateInterpolator(1.5f));
				anim.setAnimationListener(listener);
				return anim;
			}

			public static Animation ToShowVideoPlayerFrame(
					AnimationListener listener) {
				Animation anim = General.Alpha(0, 1, 200);
				anim.setInterpolator(new AccelerateInterpolator(1.5f));
				anim.setAnimationListener(listener);
				return anim;
			}

			public static Animation ToHideVideoPlayerFrame(
					AnimationListener listener) {
				Animation anim = General.Alpha(1, 0, 200);
				anim.setInterpolator(new AccelerateInterpolator(1.5f));
				anim.setAnimationListener(listener);
				return anim;
			}

		}

		public static class ListItem {

			public static Animation OnItemFirstDisplayed(long delay, float alpha) {
				AnimationSet anim = new AnimationSet(true);
				AlphaAnimation animAlpha = new AlphaAnimation(0, alpha);
				TranslateAnimation animTrans = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.7f,
						Animation.RELATIVE_TO_SELF, 0.0f);
				animAlpha.setDuration(500);
				animTrans.setDuration(500);
				anim.addAnimation(animAlpha);
				anim.addAnimation(animTrans);
				anim.setStartOffset(delay);
				anim.setInterpolator(new DecelerateInterpolator(4f));
				anim.setFillAfter(true);
				return anim;
			}

		}
	}
}
