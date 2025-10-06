package icu.nyat.kusunoki.justDontDie.Events;

import icu.nyat.kusunoki.justDontDie.JustDontDie;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final JustDontDie plugin;
    // 简单的冷却，避免同一死亡事件被多次触发
    private final Map<UUID, Long> recentUseMillis = new HashMap<>();

    public PlayerDeathListener(JustDontDie plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        // 仅在生存/冒险模式下生效
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (player.getHealth() - event.getFinalDamage() > 0) return;

        // 简单的 500ms 冷却，避免同一 tick 多次触发
        long now = System.currentTimeMillis();
        long last = recentUseMillis.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 500) return;

        // 优先使用主手物品，其次副手
        EquipmentSlot slotToUse = null;
        ItemStack itemToUse = null;
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() != Material.AIR) {
            slotToUse = EquipmentSlot.HAND;
            itemToUse = main.clone();
        } else {
            ItemStack off = player.getInventory().getItemInOffHand();
            if (off.getType() != Material.AIR) {
                slotToUse = EquipmentSlot.OFF_HAND;
                itemToUse = off.clone();
            }
        }
        if (slotToUse == null) return; // 手上都没拿东西

        recentUseMillis.put(player.getUniqueId(), now);
        handleRevival(player, event, slotToUse, itemToUse);
    }

    @EventHandler
    public void onVanillaTotem(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (plugin.isVanillaTotemDisabled()) {
            event.setCancelled(true);
        }
    }

    private void showItemTotemAnimation(Player player, ItemStack itemForAnimation) {
        // 如果在 Paper 上运行，尝试调用 Player#showTotem(ItemStack) 来显示任意物品的复活动画
        try {
            player.getClass().getMethod("showTotem", ItemStack.class).invoke(player, itemForAnimation);
            return;
        } catch (Throwable ignored) {
            // 非 Paper 或方法不可用，走降级方案
        }
        // 降级：播放声音 + 粒子，尽量还原动画体验
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
    }

    private void handleRevival(Player player, EntityDamageEvent event, EquipmentSlot slotUsed, ItemStack usedItemSnapshot) {
        event.setCancelled(true);

        // 设置生命值为 1（半颗心）
        player.setHealth(1.0);

        // 尽量贴近原版：再生 II 5s、伤害吸收 II 5s、抗火 40s
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0));

        // 自定义/原版动画
        showItemTotemAnimation(player, usedItemSnapshot);

        // 消耗手中物品（按使用的手）
        if (slotUsed == EquipmentSlot.HAND) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() != Material.AIR) {
                int newAmount = hand.getAmount() - 1;
                if (newAmount <= 0) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                } else {
                    hand.setAmount(newAmount);
                    player.getInventory().setItemInMainHand(hand);
                }
            }
        } else if (slotUsed == EquipmentSlot.OFF_HAND) {
            ItemStack off = player.getInventory().getItemInOffHand();
            if (off.getType() != Material.AIR) {
                int newAmount = off.getAmount() - 1;
                if (newAmount <= 0) {
                    player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                } else {
                    off.setAmount(newAmount);
                    player.getInventory().setItemInOffHand(off);
                }
            }
        }
    }
}
