package pl.xcrafters.xcrauth;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuthPlugin extends JavaPlugin implements Listener, PluginMessageListener, InvocationHandler {

    public HashMap<String, String> captchas = new HashMap();
    public List<String> notRendered = new ArrayList();

    MapView view;
    CaptchaRenderer renderer;

    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        try {
            String ver = Bukkit.getServer().getClass().getName().split("\\.")[3];

            Class<?> clazz = Class.forName("net.minecraft.server." + ver + ".IPlayerFileData");
            Object proxyIPlayerFileData = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);

            Class<?> minecraftServer = Class.forName("net.minecraft.server." + ver + ".MinecraftServer");
            Object server = minecraftServer.getMethod("getServer").invoke(null);
            Object playerList = minecraftServer.getMethod("getPlayerList").invoke(server);
            Field f = playerList.getClass().getField("playerFileData");

            original = f.get(playerList);
            f.set(playerList, proxyIPlayerFileData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        renderer = new CaptchaRenderer(this);
        view = Bukkit.createMap(Bukkit.getWorlds().get(0));
        for (MapRenderer renderer : view.getRenderers()) {
            view.removeRenderer(renderer);
        }
        view.addRenderer(renderer);
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event){
        Location loc = new Location(event.getPlayer().getWorld(), 0.5, 10, 0.5);
        event.getPlayer().teleport(loc);
        event.getPlayer().getInventory().clear();
        event.getPlayer().updateInventory();
        event.getPlayer().setHealth(20.0);
        event.getPlayer().setFoodLevel(20);
        event.setJoinMessage(null);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getPlayer().isOp()){ return; }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        if(event.getPlayer().isOp()){ return; }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getPlayer().isOp()){ return; }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.getPlayer().isOp()){ return; }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(event.getPlayer().isOp()){ return; }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        event.setQuitMessage(null);
    }
    
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event){
        event.setLeaveMessage(null);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    public void onPluginMessageReceived(String channel, Player p, byte[] bytes) {
        if(!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String sub = in.readUTF();
        if(sub.equals("Captcha")) {
            String nick = in.readUTF();
            String captcha = in.readUTF();
            Player player = Bukkit.getPlayerExact(nick);
            if(player != null) {
                captchas.put(player.getName(), captcha);
                notRendered.add(player.getName());

                ItemStack map = new ItemStack(Material.MAP, 1, view.getId());
                ItemMeta meta = map.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "Kod captcha");
                map.setItemMeta(meta);
                player.getInventory().clear();
                player.getInventory().setItem(0, map);
                player.getInventory().setHeldItemSlot(0);
                player.updateInventory();
            }
        }
    }

    private Object original;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("load")) {
            return method.invoke(original, args);
        }
        if (method.getName().equals("getSeenPlayers")) {
            return method.invoke(original, args);
        }

        return null;
    }

}
