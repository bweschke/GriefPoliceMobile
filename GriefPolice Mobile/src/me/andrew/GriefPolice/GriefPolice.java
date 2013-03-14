package me.andrew.GriefPolice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.event.Event;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GriefPolice extends JavaPlugin 
{

	private static final Logger log = Logger.getLogger("Minecraft");
	private final GriefPoliceListener blockListener = new GriefPoliceListener(this);
	public final ArrayList<String> GriefPoliceUsers = new ArrayList<String>();
	
	public GriefPoliceSqlite gpolicesqlite = null;
	
	@Override
	public void onEnable()
	{
		
		try {
            final File[] libs = new File[] {
                    new File(getDataFolder(), "sqlite4java.jar"),
                    new File(getDataFolder(), "commons-logging-1.1.1.jar"),
                    new File(getDataFolder(), "httpcore-4.2.3.jar"),
                    new File(getDataFolder(), "httpcore-nio-4.2.3.jar"),
                    new File(getDataFolder(), "org.apache.httpcomponents.httpclient_4.2.3.jar"),
                    new File(getDataFolder(), "gson-2.2.2.jar")};
            for (final File lib : libs) {
                if (!lib.exists()) {
                    JarUtils.extractFromJar(lib.getName(),
                            lib.getAbsolutePath());
                }
            }
            // Extract the native Libraries for SQLite4Java
            final File[] nativelibs = new File[] {
            		new File(getDataFolder(), "libsqlite4java-linux-amd64.so"),
            		new File(getDataFolder(), "libsqlite4java-linux-i386.so"),
            		new File(getDataFolder(), "libsqlite4java-osx-10.4.jnilib"),
            		new File(getDataFolder(), "libsqlite4java-osx-ppc.jnilib"),
            		new File(getDataFolder(), "libsqlite4java-osx.jnilib"),
            		new File(getDataFolder(), "sqlite4java-win32-x64.dll"),
            		new File(getDataFolder(), "sqlite4java-win32-x86.dll")
            };
            for (final File lib : nativelibs) {
                if (!lib.exists()) {
                    JarUtils.extractFromJar(lib.getName(),
                            lib.getAbsolutePath());
                }
            }           
            for (final File lib : libs) {
                if (!lib.exists()) {
                    getLogger().warning(
                            "There was a critical error loading My plugin! Could not find lib: "
                                    + lib.getName());
                    Bukkit.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                addClassPath(JarUtils.getJarUrl(lib));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }		
		gpolicesqlite = new GriefPoliceSqlite(this);
		log.info("[GriefPolice] has been enabled!"); 
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new GriefPoliceListener(this), this);
	}
	
	private void addClassPath(final URL url) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader
                .getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL",
                    new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new IOException("Error adding " + url
                    + " to system classloader");
        }
    }	

    @Override
    public void onDisable()
    {
    	gpolicesqlite.StopSQLQueue();
    	log.info("[GriefPolice] has been disabled!");
    }
    
   
  

private void toggleGriefPolice(CommandSender sender)
{
	if( !enabled((Player) sender) )
	{
        //GriefPoliceUsers.add((Player) sender);
        ((Player) sender).sendMessage(ChatColor.GOLD + "GriefPolice Mobile has been enabled!!! :D");
	}
	
	else
	{
		//GriefPoliceUsers.remove((Player) sender);
		((Player) sender).sendMessage(ChatColor.BLUE + "GriefPolice Mobile has been disaled.:(");
	}
}

public boolean enabled(Player player)
{
	return GriefPoliceUsers.contains(player);
}

@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
{
		/*if (cmd.getName().equalsIgnoreCase("initdb")) {
			if (!(sender instanceof Player))
			{
				try {
					gpolicesqlite.initDB();
				} catch (Exception e) {
					log.log(Level.SEVERE, "Could not initialize the DB! "+e.getMessage());
				}
			} else {
				sender.sendMessage("This command can only be run from the console.");
			}
			return true;
		}
		else*/
		if (cmd.getName().equalsIgnoreCase("informme"))
         {
        	 if (!(sender instanceof Player))
        	 {
        		 sender.sendMessage("This command can only be run by a player.");
        	 }
        	 else
        	 {
        		 Player player = (Player) sender;
        		 log.info(player.getName()+ " wants to be informed");
        		 if (GriefPoliceUsers.contains(player))
        		 {
        			 player.sendMessage("Silly, you can't be informed twice!");
        		 }
        		 else
        		 {
        			 player.sendMessage("Ok, We'll let you know :)");
        			 GriefPoliceUsers.add(player.getName()); 
        		 }
        		 
        	 }
        	 return true;
         }
         else if (cmd.getName().equalsIgnoreCase("uninformme"))
        		 {
                  if (!(sender instanceof Player))
                  {
                	  sender.sendMessage("This command can only be run by a player");
                  }
                  else
                  {
                	  Player player = (Player) sender;
                	  log.info(player.getName()+ " wants to be uninformed");
                	  if (GriefPoliceUsers.contains(player.getName()))
                	  {
                		  GriefPoliceUsers.remove(player.getName());
                		  sender.sendMessage(" You have been uninformed.");
                	  }
                	  else
                	  {
                		  sender.sendMessage(" You weren't  informed in the first place.");
                	  }
                  }
                  return true;
        		 }
         return false;
}

}
