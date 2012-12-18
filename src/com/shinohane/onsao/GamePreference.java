package com.shinohane.onsao;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class GamePreference {
	
	// Optional preferred game engine name for the game 
	public String engine = "";
	
	// Optional draw outline of text
	public boolean draw_outline = true;
	
	// Optional whether show side buttons during game play
	public boolean button_visible = true;
	
	// Optional whether to center screen during game play
	public boolean screen_centered = false;
	
	public static GamePreference fromJSON(final JSONObject json) throws JSONException {
		return new GamePreference(){{ readJSON(json); }};
	}
	
	public void readJSON(JSONObject json) throws JSONException {
		if(json.has("engine"))
			engine = json.getString("engine");
		if(json.has("draw_outline"))
			draw_outline = json.getBoolean("draw_outline");
		if(json.has("button_visible"))
			button_visible = json.getBoolean("button_visible");
		if(json.has("screen_centered"))
			screen_centered = json.getBoolean("screen_centered");
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject b = new JSONObject();
		b.put("engine", engine);
		b.put("draw_outline", draw_outline);
		b.put("button_visible", button_visible);
		b.put("screen_centered", screen_centered);
		return b;
	}

	public static GamePreference fromBundle(Bundle bundle) {
		GamePreference g = new GamePreference();
		g.engine = bundle.getString("engine");
		g.draw_outline = bundle.getBoolean("draw_outline");
		g.button_visible = bundle.getBoolean("button_visible");
		g.screen_centered = bundle.getBoolean("screen_centered");
		return g;
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString("engine", engine);
		b.putBoolean("draw_outline", draw_outline);
		b.putBoolean("button_visible", button_visible);
		b.putBoolean("screen_centered", screen_centered);
		return b;
	}
	
}
