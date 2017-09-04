package LobbySystem;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import java.util.HashMap;
import java.util.Map;

public class main extends PluginBase implements Listener{
    
    public Map<Player, Map> inv = new HashMap<>();
    
    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.reloadConfig();
    }
     
    
    public void putitems(Player pl){
        inv.put(pl, pl.getInventory().getContents());
        pl.getInventory().clearAll();
        for(String i : this.getConfig().getStringList("items")){
            String it[] = i.split("|");
            if(it.length<3){
                this.getLogger().error("invalid item syntax... should be \"itemid|slot|name\"");
            }
            Item item = new Item(Integer.parseInt(it[0]));
            if(!(item instanceof Item)){
               this.getLogger().error("invalid itemid... ");
            }
            int slot = Integer.parseInt(it[1]);
            if(slot<0 || slot>35){
                this.getLogger().error("invalid slot... ");
            }
            String name = it[2];
            item.setCustomName(name);     
            if(it.length==4){
                CompoundTag nbt = new CompoundTag();
                nbt.putString("cmd", it[3]);
                item.setNamedTag(nbt);
            }
            pl.getInventory().addItem(item);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onjoin(PlayerJoinEvent ev){
        Player pl = ev.getPlayer();
        if(pl.getLevel().getName().equals(this.getConfig().getString("level"))){
            this.putitems(pl);
        } 
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void ondrop(PlayerDropItemEvent ev){
        if(ev.getPlayer().getLevel().getName().equals(this.getConfig().getString("level"))){
            ev.setCancelled();
        }      
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
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
            }
        }        
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void ontab(PlayerInteractEvent ev){
        if(ev.getPlayer().getLevel().getName().equals(this.getConfig().getString("level"))){
            if(ev.getItem().getNamedTag().exist("cmd")){
                String cmd = ev.getItem().getNamedTag().getString("cmd");
                cmd = cmd.replaceAll("%p", ev.getPlayer().getName());
                if(cmd.startsWith("tpto")){
                    cmd = cmd.replaceFirst("tp", "");
                    cmd = cmd.replaceFirst(" ", "");
                    String xyz[] = cmd.split(" ");
                    ev.getPlayer().teleport(new Vector3(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[0])));
                    return;
                }
                this.getServer().dispatchCommand(this.getServer().getConsoleSender(), cmd);
            }               
        }
    }
}