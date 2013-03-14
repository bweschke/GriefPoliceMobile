package me.andrew.GriefPolice;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.gson.Gson;

public class GriefPoliceSqlite {

	public static GriefPolice plugin;
	public static SQLiteQueue queue;
	private static final Logger log = Logger.getLogger("Minecraft");	
	
	public GriefPoliceSqlite(GriefPolice instance)
	{	
		plugin = instance;
		
		File gpm = new File(plugin.getDataFolder(), "griefpolicemobile.db");
		if (!gpm.exists()) {
			try {
				SQLiteConnection db = new SQLiteConnection(gpm);
			    db.open(true);
			    db.dispose();
				queue = new SQLiteQueue(gpm);
				queue.start();
				initDB();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to initialize DB!!!\n", e);
			}
		} else {
			queue = new SQLiteQueue(gpm);
			queue.start();
		}
	}	
	
	public void StopSQLQueue() {
		queue.stop(true);
	}
	

	public void blockBreak(final int x, final int y, final int z, final String breaker, final int btypeid) {
	  final String tableName = "";
	  queue.execute(new SQLiteJob<String>() {
	    protected String job(SQLiteConnection connection) throws SQLiteException {
		  log.log(Level.INFO, "Block break select: "+"SELECT owner FROM track_block_changes where x = "+x+" and y = "+y+" and z = "+z);	
	      SQLiteStatement st = connection.prepare("SELECT owner FROM track_block_changes where x = "+x+" and y = "+y+" and z = "+z);
	      try {
	        return st.step() ? st.columnString(0) : "";
	      } finally {
	        st.dispose();
	      }
	    }
	    
	    protected void jobFinished(String result) {
	      if (result != null) {
		    log.log(Level.INFO, "Block break select FOUND: "+result);
		    if (result.length() > 1 && !result.equalsIgnoreCase(breaker)) {
			    Map<String, Object> pmap = new HashMap();
	
			    pmap.put("x", x);
			    pmap.put("y", y);
			    pmap.put("z", z);
			    pmap.put("blockowner", result);
			    pmap.put("blockbreaker", breaker);
			    pmap.put("btypeid", btypeid);
			    
			    Gson gson = new Gson();
			    
			    String json = gson.toJson(pmap);
	
			    HttpPost httpPost = new HttpPost("http://secaucusll.elasticbeanstalk.com/mcapi/blockbreak");
	
			    try {
				    DefaultHttpClient httpclient = new DefaultHttpClient();
				    httpPost.setEntity(new StringEntity(json));
				    HttpResponse response2 = httpclient.execute(httpPost);
	
			        System.out.println(response2.getStatusLine());
			        HttpEntity entity2 = response2.getEntity();
			        // do something useful with the response body
			        // and ensure it is fully consumed
			        EntityUtils.consume(entity2);
			    } catch (Exception e) {
			    	log.log(Level.WARNING, "Unable to post: "+json);			    	
			    } finally {
			    	log.log(Level.INFO, "Posted: "+json);			    	
			        httpPost.releaseConnection();
			    }		    
		    }
	      } else {
	    	log.log(Level.INFO, "Block break select NOT FOUND: "+"SELECT owner FROM track_block_changes where x = "+x+" and y = "+y+" and z = "+z);	
	      }
	    }
	  });	
	}
	  
	  
	public boolean blockPlacement(final int x, final int y, final int z, final String owner, final int btypeid) {
		return queue.execute(new SQLiteJob<Boolean>() {
		     protected Boolean job(SQLiteConnection connection) throws SQLiteException {
			 Date tstamp = new Date();
			 String insertquery = "INSERT INTO track_block_changes VALUES ("+x+","+y+","+z+",'"+ owner +"',"+btypeid+","+ tstamp.getTime() +")";
		     try {
		       connection.exec("BEGIN");
		       connection.exec("DELETE FROM track_block_changes where x = "+x+" and y = "+y+" and z = "+z);
		       connection.exec(insertquery);
		       connection.exec("COMMIT");
		       log.log(Level.INFO, "Block placement success: "+"INSERT INTO track_block_changes VALUES ("+x+","+y+","+z+",'"+ owner +"',"+btypeid+","+ tstamp.getTime() +")");
		       return true;
		     } catch (Exception e) {
		    	 connection.exec("ROLLBACK");
		    	 log.log(Level.WARNING, "Block placement fail: "+insertquery + "\n", e);
		    	 return false;
		     }
		     }
		   }).complete();				
	}
	
	public boolean initDB() throws Exception {
		return queue.execute(new SQLiteJob<Boolean>() {
		     protected Boolean job(SQLiteConnection connection) throws SQLiteException {
		     try {
		       connection.exec("BEGIN");
		       connection.exec("CREATE TABLE track_block_changes (x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL, owner TEXT NOT NULL, btypeid INTEGER NOT NULL, tstamp INTEGER NOT NULL)");
		       connection.exec("CREATE INDEX track_block_changes_idx ON track_block_changes (x,y,z,owner,tstamp);");
		       connection.exec("COMMIT");
		       log.log(Level.INFO, "Table track_block_changes created and index initialized");
		       return true;
		     } catch (Exception e) {
		    	 connection.exec("ROLLBACK");
		    	 log.log(Level.SEVERE, "Unable to initialize track_block_changes table!!!\n", e);
		    	 return false;
		     }
		     }
		   }).complete();		
	}
	
}
