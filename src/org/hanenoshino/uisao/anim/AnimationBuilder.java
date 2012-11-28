package org.hanenoshino.uisao.anim;

import java.util.ArrayList;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

public class AnimationBuilder {

	/*
	 * AnimationBuilder.create()
	 * .alpha(0, 1).startAt(100).to(200).serial().fillafter().fillbefore().fillenabled()
	 * .scale(tx1, x1, tx2, x2, ty1, y1, ty2, y2).overshoot().startAt(0).animateFor(200).parallel()
	 * .anim(someAnimation)
	 * .build()
	 */

	public static AnimationBuilder create() {
		return new AnimationBuilder();
	}
	
	ArrayList<Animation> animations = new ArrayList<Animation>();
	
	// Utility Functions
	
	private Animation current() {
		Animation anim = anim(-1);
		if(anim == null)
			throw new NoAnimationException();
		return anim;
	}
	
	private Animation previous() {
		return anim(-2);
	}
	
	private Animation anim(int number) {
		if(animations.size() == 0)
			return null;
		if(number < 0) number += animations.size();
		if(number >= animations.size() || number < 0) 
			return null;
		return animations.get(number);
	}
	
	private AnimationBuilder() {
		
	}
	
	// Animation Creation
	
	public AnimationBuilder alpha(float from, float to) {
		anim(new AlphaAnimation(from, to));
		return this;
	}
	
	public AnimationBuilder anim(Animation anim) {
		animations.add(anim);
		return this;
	}
	
	// Timing Tweak
	
	/**
	 * Set the start time of the animation
	 * @param timeMillis
	 * @return
	 */
	public AnimationBuilder startAt(long timeMillis) {
		current().setStartTime(timeMillis);
		return this;
	}
	
	/**
	 * Wait before the animation is started since its startTime
	 * @param timeMillis
	 * @return
	 */
	public AnimationBuilder pending(long timeMillis) {
		current().setStartOffset(timeMillis);
		return this;
	}
	
	/**
	 * Animate to what time? (relative to the parent)
	 * @param timeMillis
	 * @return
	 */
	public AnimationBuilder to(long timeMillis) {
		current().setDuration(timeMillis - current().getStartTime() - current().getStartOffset());
		return this;
	}
	
	/**
	 * Animate for how long?
	 * @param timeMillis
	 * @return
	 */
	public AnimationBuilder animateFor(long timeMillis) {
		current().setDuration(timeMillis);
		return this;
	}
	
	/**
	 * Alias for animateFor
	 */
	public AnimationBuilder duration(long timeMillis) {
		return animateFor(timeMillis);
	}
	
	// synchronize the StartOffset offend the behaviour rule of serial()
	
	/**
	 * Parallel with previous Animation
	 * @return
	 */
	public AnimationBuilder parallel() {
		if(previous() != null) {
			current().setStartTime(previous().getStartTime());
			current().setStartOffset(previous().getStartOffset());
		}else{
			current().setStartTime(0);
			current().setStartOffset(0);
		}
		return this;
	}
	
	/**
	 * Parallel with Animation for N before
	 * @return
	 */
	public AnimationBuilder parallel(int n) {
		if(anim(-n-1) != null) {
			current().setStartTime(anim(-n-1).getStartTime());
			current().setStartOffset(anim(-n-1).getStartOffset());
		}else{
			throw new NoAnimationException();
		}
		return this;
	}
	
	/**
	 * Serial to previous Animation
	 * @return
	 */
	public AnimationBuilder serial() {
		long pOffset = 0, pDuration = 0;
		if(previous() != null) {
			pOffset = previous().getStartTime() + previous().getStartOffset();
			pDuration = previous().getDuration();
		}
		current().setStartTime(pOffset + pDuration);
		return this;
	}
	
	/**
	 * Serial with Animation for N before
	 * @param n
	 * @return
	 */
	public AnimationBuilder serial(int n) {
		long pOffset = 0, pDuration = 0;
		if(anim(-n-1) != null) {
			pOffset = anim(-n-1).getStartTime() + anim(-n-1).getStartOffset();
			pDuration = anim(-n-1).getDuration();
		}
		current().setStartTime(pOffset + pDuration);
		return this;
	}
	
	// Interpolator
	
	public AnimationBuilder accelerated() {
		return interpolator(new AccelerateInterpolator());
	}
	
	public AnimationBuilder decelerated() {
		return interpolator(new DecelerateInterpolator());
	}
	
	public AnimationBuilder overshoot() {
		return interpolator(new OvershootInterpolator());
	}
	
	public AnimationBuilder accelerated(float factor) {
		return interpolator(new AccelerateInterpolator(factor));
	}
	
	public AnimationBuilder decelerated(float factor) {
		return interpolator(new DecelerateInterpolator(factor));
	}
	
	public AnimationBuilder overshoot(float factor) {
		return interpolator(new OvershootInterpolator(factor));
	}
	
	private Interpolator global_interpolator = null;
	
	public AnimationBuilder interpolator(Interpolator i) {
		try{
			current().setInterpolator(i);
		}catch(NoAnimationException e){
			global_interpolator = i;
		}
		return this;
	}

	// FillOptions

	public FillOptions Fill = new FillOptions();
	
	public class FillOptions {
		
		public FillOptions after(boolean enabled) {
			current().setFillAfter(enabled);
			return this;
		}
		
		public FillOptions before(boolean enabled) {
			current().setFillBefore(enabled);
			return this;
		}
		
		public FillOptions enabled(boolean enabled) {
			current().setFillEnabled(enabled);
			return this;
		}
		
		public AnimationBuilder upward() {
			return AnimationBuilder.this;
		}
		
	}
	
	/**
	 * Build for output of the animation
	 * @return
	 */
	public Animation build() {
		if(animations.size() <= 1) {
			return current();
		}else{
			AnimationSet set = new AnimationSet(global_interpolator != null);
			if(global_interpolator != null)
				set.setInterpolator(global_interpolator);
			for(Animation anim : animations) {
				set.addAnimation(anim);
			}
			return set;
		}
	}
	
}
