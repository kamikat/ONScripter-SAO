package com.shinohane.onsao;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class GamePreference {
	
	// Optional preferred game engine name for the game 
	public String engine;
	
	// Optional draw outline of text
	public boolean draw_outline;
	
	public static GamePreference fromJSON(final JSONObject json) throws JSONException {
		return new GamePreference(){{ readJSON(json); }};
	}
	
	public void readJSON(JSONObject json) throws JSONException {
		if(json.has("engine"))
			engine = json.getString("engine");
		if(json.has("draw_outline"))
			draw_outline = json.getBoolean("draw_outline");
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject b = new JSONObject();
		b.put("engine", engine);
		b.put("draw_outline", draw_outline);
		return b;
	}

	public static GamePreference fromBundle(Bundle bundle) {
		GamePreference g = new GamePreference();
		g.engine = bundle.getString("engine");
		g.draw_outline = bundle.getBoolean("draw_outline");
		return g;
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString("engine", engine);
		b.putBoolean("draw_outline", draw_outline);
		return b;
	}
	
}
