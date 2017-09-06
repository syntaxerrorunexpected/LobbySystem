package LobbySystem;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main extends PluginBase implements Listener{
    
    public Map<Player, Map> inv = new HashMap<>();
    
    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.reloadConfig();
        Level l = this.getServer().getLevelByName(this.getConfig().getString("level"));
        Player[] player = l.getPlayers().values().toArray(new Player[0]);
        for(Player pl : player){
            inv.put(pl, pl.getInventory().getContents());
            this.putitems(pl, "items");        
        }
    }
    
    @Override
    public void onDisable(){
        Level l = this.getServer().getLevelByName(this.getConfig().getString("level"));
        Player[] player = l.getPlayers().values().toArray(new Player[0]);
        for(Player pl : player){
            if(inv.containsKey(pl)){
                pl.getInventory().clearAll();
                pl.getInventory().setContents(inv.get(pl));
            }
        }
    }
      
    public void putitems(Player pl, String ln){
        pl.getInventory().clearAll();
        if(!this.getConfig().exists(ln)){
            this.getLogger().error("Error list " + ln + " not found");
            return;
        }
        List<String> l = this.getConfig().getStringList(ln);
        for(String i : l){
            int cs = l.indexOf(i) + 9;
            String it[] = i.split("\\|");
            if(it.length<3){
                this.getLogger().error("invalid item syntax in list " + ln + " ... should be \"itemid|slot|name(|cmd)\"");
                continue;
            } 
            if(!isInteger(it[0]) || !isInteger(it[1])){
                this.getLogger().error("invalid itemid or slot in list " + ln + " ... " + it[0] + " or " + it[1] + " is not a number!");
                continue;
            }
            Item item = new Item(Integer.parseInt(it[0]));
            if(!(item instanceof Item)){
               this.getLogger().error("invalid itemid in list " + ln + " ... ");
            }
            int slot = Integer.parseInt(it[1]); 
            if(slot<1 || slot>9){
                this.getLogger().error("invalid slot in list " + ln + " ... ");
            }
            if(it.length>3){
                CompoundTag nbt = new CompoundTag();
                nbt.putString("cmd", it[3]);
                item.setNamedTag(nbt);
            }
            String name = it[2];
            name = name.replaceAll("%n", "\n");
            item = item.setCustomName(name);
            pl.getInventory().addItem(item);
            pl.getInventory().setHotbarSlotIndex(cs, slot - 1);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onjoin(PlayerJoinEvent ev){
        Player pl = ev.getPlayer();
        if(pl.getLevel().getName().equals(this.getConfig().getString("level"))){
            inv.put(pl, pl.getInventory().getContents());
            this.putitems(pl, "items");
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
                inv.remove(pl);
                return;
            }
            if(ev.getTo().getLevel().getName().equals(this.getConfig().getString("level"))){
                inv.put(pl, pl.getInventory().getContents());
                this.putitems(pl, "items");
            }
        }        
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onquit(PlayerQuitEvent ev){
        if(inv.containsKey(ev.getPlayer())){
            ev.getPlayer().getInventory().setContents(inv.get(ev.getPlayer()));
        }    
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onrip(PlayerRespawnEvent ev){
        Player pl = ev.getPlayer();       
        if(pl.getLevel().getName().equals(this.getConfig().getString("level"))){
            this.putitems(pl, "items");
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void ontab(PlayerInteractEvent ev){
        Player pl = ev.getPlayer();
        if(pl.getLevel().getName().equals(this.getConfig().getString("level"))){
                Item i = ev.getItem();          
                if(!i.hasCompoundTag()){
                    return;
                }
                String cmd = i.getNamedTag().getString("cmd");
                if(cmd.isEmpty()){
                    return;
                }
                cmd = cmd.replaceAll("%p", pl.getName());
                if(cmd.startsWith("tpto")){
                    cmd = cmd.replaceFirst("tpto", "");
                    cmd = cmd.replaceFirst(" ", "");
                    String xyz[] = cmd.split(" ");
                    pl.teleport(new Vector3(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[0])));
                    return;
                }
                if(cmd.startsWith("ilist")){
                    cmd = cmd.replaceFirst("ilist", "");
                    String ln = cmd.replaceFirst(" ", "");
                    this.putitems(pl, ln);
                    return;
                }
                this.getServer().dispatchCommand(this.getServer().getConsoleSender(), cmd);
        }
    }   
    public static boolean isInteger(String s){ boolean isValidInteger = false;try{Integer.parseInt(s);isValidInteger = true;}catch (NumberFormatException ex){ }return isValidInteger;}
}