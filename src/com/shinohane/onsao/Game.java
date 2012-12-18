package com.shinohane.onsao;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class Game {

	public enum Validation {
		VALID, INVALID, MISS_FONT
	}
	
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

	// Optional Path/To/Video/File
	public String video;

	// Optional Path/To/Audio/File
	public String audio;
	
	// Optional Path/To/Script/File
	public String script;

	// Optional Path/To/Font/File
	public String font;
	
	// Optional preferred game engine name for the game 
	public GamePreference preference = new GamePreference();
	
	public final String basepath;
	
	public Game(String basepath) {
		this.basepath = (basepath.endsWith("/")?basepath:(basepath+"/"));
	}
	
	public Validation isItemRunnable() {
		if(script == null) return Validation.INVALID;
		if(font == null) return Validation.MISS_FONT;
		return Validation.VALID;
	}
	
	public void readJSON(JSONObject json) throws JSONException {
		readJSON(json, false);
	}
	
	public void readJSON(JSONObject json, boolean overlay) throws JSONException {
		if(json.has("preference"))
		if(overlay || preference == null)
		preference = GamePreference.fromJSON(json.getJSONObject("preference"));
		if(json.has("title"))
		if(overlay || title == null)
		title = json.getString("title");
		if(json.has("cover"))
		if(overlay || cover == null)
		cover = json.getString("cover").replace("./", basepath);
		if(json.has("description"))
		if(overlay || description == null)
		description = json.getString("description");
		if(json.has("background"))
		if(overlay || background == null)
		background = json.getString("background").replace("./", basepath);
		if(json.has("icon"))
		if(overlay || icon == null)
		icon = json.getString("icon").replace("./", basepath);
		if(json.has("video"))
		if(overlay || video == null)
		video = json.getString("video").replace("./", basepath);
		if(json.has("audio"))
		if(overlay || audio == null)
		audio = json.getString("audio").replace("./", basepath);
		if(json.has("script"))
		if(overlay || script == null)
		script = json.getString("script").replace("./", basepath);
		if(json.has("font"))
		if(overlay || font == null)
		font = json.getString("font").replace("./", basepath);
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject b = new JSONObject();
		b.put("preference", preference.toJSON());
		b.put("title", title);
		b.put("cover", cover);
		b.put("description", description);
		b.put("background", background);
		b.put("icon", icon);
		b.put("video", video);
		b.put("audio", audio);
		b.put("script", script);
		b.put("font", font);
		return b;
	}
	
	public void writeJSON() {
		File media = new File(basepath, "media.json");
		try {
			U.write(media, toJSON().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Game fromBundle(Bundle bundle) {
		Game g = new Game(bundle.getString("basepath"));
		g.title = bundle.getString("title");
		g.cover = bundle.getString("cover");
		g.description = bundle.getString("description");
		g.background = bundle.getString("background");
		g.icon = bundle.getString("icon");
		g.video = bundle.getString("video");
		g.audio = bundle.getString("audio");
		g.script = bundle.getString("script");
		g.font = bundle.getString("font");
		g.preference = GamePreference.fromBundle(bundle.getBundle("preference"));
		return g;
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString("basepath", basepath);
		b.putString("title", title);
		b.putString("cover", cover);
		b.putString("description", description);
		b.putString("background", background);
		b.putString("icon", icon);
		b.putString("video", video);
		b.putString("audio", audio);
		b.putString("script", script);
		b.putString("font", font);
		b.putBundle("preference", preference.toBundle());
		return b;
	}
	
	public static Game scanGameDir(File gamedir) {
		Game g = new Game(gamedir.getAbsolutePath());
		g.title = gamedir.getName();
		File media = new File(gamedir, "media.json");
		if(media.exists()) {
			try {
				JSONObject data = new JSONObject(U.read(media));
				g.readJSON(data, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(	g.cover != null && g.background != null && g.video != null && 
			g.icon != null && g.audio != null && g.script != null && g.font != null) return g;
		String[] files = gamedir.list();
		for(String file: files) {
			String name = file.toLowerCase();
			if(name.equals("cover.jpg") || name.equals("cover.png")) {
				if(g.cover == null) 
					g.cover = new File(gamedir, file).getAbsolutePath();
			}
			if(name.equals("background.jpg") || name.equals("background.png") ||
			   name.equals("bkg.jpg") || name.equals("bkg.png")) {
				if(g.background == null) 
					g.background = new File(gamedir, file).getAbsolutePath();
			}
			if(name.equals("preview.mp4") || name.equals("preview.avi") || name.equals("preview.mpg") ||
			   name.equals("pv.mp4") || name.equals("pv.avi") || name.equals("pv.mpg")) {
				if(g.video == null) 
					g.video = new File(gamedir, file).getAbsolutePath();
			}
			if(name.equals("theme.mp3") || name.equals("theme.flac") || name.equals("theme.ogg") ||
			   name.equals("theme.wma") ||
			   name.equals("track.mp3") || name.equals("track.flac") || name.equals("track.ogg") || 
			   name.equals("track.wma")) {
				if(g.audio == null) 
					g.audio = new File(gamedir, file).getAbsolutePath();
			}
			if(name.equals("icon.jpg") || name.equals("icon.png")) {
				if(g.icon == null) 
					g.icon = new File(gamedir, file).getAbsolutePath();
			}
			if(name.equals("0.txt") || name.equals("00.txt") || name.equals("nscr_sec.dat") || 
			   name.equals("nscript.___") || name.equals("nscript.dat")) {
				if(g.script == null) 
					g.script = new File(gamedir, file).getAbsolutePath();
			}
			if(name.equals("default.ttf")) {
				if(g.font == null) 
					g.font = new File(gamedir, file).getAbsolutePath();
			}
		}
		if(g.cover != null && g.video != null && g.audio != null) return g;
		for(String file: files) {
			String name = file.toLowerCase();
			if(name.endsWith(".jpg") || name.endsWith(".png")) {
				if(g.cover == null) 
					g.cover = new File(gamedir, file).getAbsolutePath();
			}
			if(U.supportAudioMedia(name)) {
				if(g.audio == null) 
					g.audio = new File(gamedir, file).getAbsolutePath();
			}
			if(name.startsWith("preview.") && U.supportVideoMedia(name)) {
				if(g.video == null) 
					g.video = new File(gamedir, file).getAbsolutePath();
			}
		}
		if(g.video != null) return g;
		for(String file: files) {
			String name = file.toLowerCase();
			if(U.supportVideoMedia(name)) {
				if(g.video == null) 
					g.video = new File(gamedir, file).getAbsolutePath();
			}
		}
		return g;
	}
	
}
