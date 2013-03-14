package me.andrew.GriefPolice;

import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class GriefPoliceListener implements Listener
{
	private static final Logger log = Logger.getLogger("Minecraft");
	public static GriefPolice plugin;
	
	public GriefPoliceListener(GriefPolice instance)
	{
		plugin = instance;
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event)
	{
        if(plugin.isEnabled())
        	log.info(event.getPlayer()+" did damage to a block");
	}
	
	@EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block blk = event.getBlockPlaced();
        Location blkloc = blk.getLocation();
        
        plugin.gpolicesqlite.blockPlacement(blkloc.getBlockX(), blkloc.getBlockY(), blkloc.getBlockZ(), player.getName(), blk.getTypeId()); 
    }	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (plugin.isEnabled()) {
			log.info(event.getPlayer()+" broke a block!!!");
			for(Iterator<String> p = plugin.GriefPoliceUsers.iterator(); p.hasNext(); )
			{
				String playertonotify = p.next();
				Player item = plugin.getServer().getPlayer(playertonotify);
				if (item != null) {
					item.sendMessage(event.getPlayer().getDisplayName()+ " broke a block!!!");
				} else {
					log.info(playertonotify + " wanted to be informed, but they're no longer online.");
					plugin.GriefPoliceUsers.remove(playertonotify);
				}
			}
	        Player player = event.getPlayer();
	        Block blk = event.getBlock();
	        Location blkloc = blk.getLocation();
			plugin.gpolicesqlite.blockBreak(blkloc.getBlockX(), blkloc.getBlockY(), blkloc.getBlockZ(), player.getName(), blk.getTypeId());
		}
	}
	
	
	
}