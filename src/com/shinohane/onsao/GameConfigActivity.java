package com.shinohane.onsao;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GameConfigActivity extends Activity {

	private String original_config;
	private Game mGame;
	
	private ListView config_list;
	
	private <T> T $(int id) {
		return U.$(findViewById(id));
	}
	
	private void findViews() {
		config_list = $(R.id.config_list);
	}
	
	private void loadGameContent() {
		
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT < 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setContentView(R.layout.game_config);
		findViews();
		
		Intent intent = getIntent();
		
		String path = intent.getStringExtra(Constant.EXTRA_GAME_PATH);
		
		mGame = Game.scanGameDir(new File(path));
		
		try {
			original_config = mGame.toJSON().toString();
		} catch (Exception e) {
			original_config = null;
		}
		
		loadGameContent();
	}
	
	private boolean save() {
		String config;
		try {
			config = mGame.toJSON().toString();
		} catch (Exception e) {
			config = null;
		}
		if(config == null) return false;
		if(original_config == config)
			setResult(RESULT_CANCELED);
		else 
			setResult(RESULT_OK);
		mGame.writeJSON();
		return original_config != config;
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
	}
	

	private void askToCreateShortcut(final Game g){
		switch(g.isItemRunnable()) {
		case VALID:
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.information))
			.setMessage(getString(R.string.create_shortcut, g.title))
			.setPositiveButton(getString(R.string.create_shortcut_granted), new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton) {
					createShortcut(g);
				}
			})
			.setNegativeButton(getString(R.string.create_shortcut_cancel), new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton) {
					// Nothing to do
				}
			})
			.create()
			.show();
			break;
		case MISS_FONT:
			U.showFontAlertDialog(this, g, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if(whichButton == DialogInterface.BUTTON_POSITIVE)
						askToCreateShortcut(g);
				}
			});
			break;
		case INVALID:
            Toast.makeText(
            		this, 
            		R.string.toast_cant_create_shortcut_for_invalid, Toast.LENGTH_LONG
            		).show();
			break;
		}
	}
	
	private void createShortcut(Game g) {
		Intent shortcut = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, g.title);
		shortcut.putExtra("duplicate", false);
		ComponentName comp = new ComponentName(
				getPackageName(), "." + this.getLocalClassName());
		shortcut.putExtra(
				Intent.EXTRA_SHORTCUT_INTENT, new Intent(
				Intent.ACTION_MAIN).setComponent(comp));
		Intent shortcutIntent = new Intent(Intent.ACTION_RUN);
		shortcutIntent.setClass(this, ONScripterActivity.class);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.putExtra(Constant.EXTRA_GAME_PATH, g.basepath);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

		Drawable d = Drawable.createFromPath(g.icon);
		if (d != null) {
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, ((BitmapDrawable) d).getBitmap());
		} else {
			ShortcutIconResource iconRes = Intent.ShortcutIconResource
					.fromContext(this, R.drawable.ic_launcher);
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		}
		sendBroadcast(shortcut);
	}
	
}
