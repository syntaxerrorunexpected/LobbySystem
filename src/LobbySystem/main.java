package LobbySystem;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import java.util.HashMap;
import java.util.Map;

public class main extends PluginBase implements Listener{
    
    public Map<Player, Map> inv = new HashMap<>();
    
    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(this, this);
    }
     
    
    public void putitems(Player pl){
        inv.put(pl, pl.getInventory().getContents());
        pl.getInventory().clearAll();
        
        //get items from config and add them to players inv
    }
    
    @EventHandler
    public void onjoin(PlayerJoinEvent ev){
        Player pl = ev.getPlayer();
        if(pl.getLevel().getName().equals(this.getConfig().getString("level"))){
            this.putitems(pl);
        }
    }
    
    @EventHandler
    public void ondrop(PlayerDropItemEvent ev){
        if(ev.getPlayer().getLevel().getName().equals(this.getConfig().getString("level"))){
            ev.setCancelled();
        }      
    }
    
    public void ontp(PlayerTeleportEvent ev){
        Player pl = ev.getPlayer();
        if(ev.getFrom().getLevel() != ev.getTo().getLevel()){
            if(ev.getFrom().getLevel().getName().equals(this.getConfig().getString("level"))){
                pl.getInventory().clearAll();
                pl.getInventory().setContents(inv.get(ev.getPlayer()));
                return;
            }
            if(ev.getTo().getLevel().getName().equals(this.getConfig().getString("level"))){
                this.putitems(pl);
                return;
            }
        }
        
            
    }
    
}