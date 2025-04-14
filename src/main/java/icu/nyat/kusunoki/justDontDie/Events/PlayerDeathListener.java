package icu.nyat.kusunoki.justDontDie.Events;

import icu.nyat.kusunoki.justDontDie.JustDontDie;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerDeathListener implements Listener {
    private final JustDontDie plugin;

    public PlayerDeathListener(JustDontDie plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (player.getHealth() - event.getFinalDamage() > 0) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) return;

        handleRevival(player, event);
    }

    @EventHandler
    public void onVanillaTotem(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (plugin.isVanillaTotemDisabled()) {
            event.setCancelled(true);
        }
    }

    private void spawnFlyingItems(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        ItemStack totemItem = new ItemStack(Material.TOTEM_OF_UNDYING);

        for (int i = 0; i < 8; i++) {
            Item item = world.dropItem(loc.clone().add(0, 1, 0), totemItem);
            item.setPickupDelay(Integer.MAX_VALUE); // 防止被捡起
            item.setInvulnerable(true); // 防止被破坏

            // 设置随机运动方向
            double x = (Math.random() - 0.5) * 0.75;
            double y = Math.random() * 0.5 + 0.5;
            double z = (Math.random() - 0.5) * 0.75;
            item.setVelocity(new Vector(x, y, z));

            // 10 ticks后移除物品（0.5秒）
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (item.isValid()) {
                    item.remove();
                    // 添加消失粒子效果
                    world.spawnParticle(Particle.CLOUD, item.getLocation(), 5, 0, 0, 0, 0.1);
                }
            }, 10);
        }
    }

    private void handleRevival(Player player, EntityDamageEvent event) {
        event.setCancelled(true);
        player.setHealth(1);

        // 药水效果
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION, 5*20, 1));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION, 40*20, 1));

        // 特效
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);

        // 消耗物品
        ItemStack handItem = player.getInventory().getItemInMainHand();
        handItem.setAmount(handItem.getAmount() - 1);
        spawnFlyingItems(player);
    }
}
