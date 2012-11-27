package org.hanenoshino.uisao.anim;

import android.view.animation.Animation;

/**
 * Automata builder for Animation states.
 * An automata should be attatched to a View and be passed a StateRunner.
 * Then when StateRunner transfer states, animation should be correctly triggered.
 * @author trinity
 *
 */
public class AnimationAutomata implements StateIO {
	
	private final StateIO runner;
	
	// To avoid circular notify
	private int lastIssue = 0;
	
	private AnimationAutomata(StateIO sio) {
		runner = sio;
	}
	
	public static AnimationAutomata to(StateIO sio) {
		if(sio == null) sio = new StateRunner();
		return new AnimationAutomata(sio);
	}

	/**
	 * Add AutomataAction from this state to another state
	 * @param from
	 * @param to
	 * @param action
	 * @return
	 */
	public AnimationAutomata addAction(int from, int to, AutomataAction action) {
		return this;
	}
	
	/**
	 * Add Animation from this state to another state
	 * @param from
	 * @param to
	 * @param anim
	 * @return
	 */
	public AnimationAutomata addAnimation(int from, int to, Animation anim) {
		return this;
	}

	public void onStateTransferred(int before, int after, int issueId) {
		if(lastIssue == issueId) return;
		lastIssue = issueId;
		// TODO Check state transfer and decide animation and actions to be taken
	}

	// Following code behave as a wrapper to StateIO
	public StateIO gotoState(int to) {
		runner.gotoState(to);
		return this;
	}

	public int currentState() {
		return runner.currentState();
	}

	public void addSublevelStateIO(StateIO sio) {
		runner.addSublevelStateIO(sio);
	}
	
	public boolean removeSublevelStateIO(StateIO sio) {
		return runner.removeSublevelStateIO(sio);
	}

	public void clearSublevelStateIO() {
		runner.clearSublevelStateIO();
	}
	
}
