package me.andrew.GriefPolice;

import java.io.File;

import com.almworks.sqlite4java.SQLiteConnection;

public class GriefPoliceSqlite {

	public static GriefPolice plugin;
	
	public GriefPoliceSqlite(GriefPolice instance)
	{
		plugin = instance;
	}
	
	
	public boolean initDB() throws Exception {
		SQLiteConnection db = new SQLiteConnection(new File(plugin.getDataFolder(), "griefpolicemobile.db"));
	    db.open(true);
	    db.dispose();
	    return true;
	}
	
}
