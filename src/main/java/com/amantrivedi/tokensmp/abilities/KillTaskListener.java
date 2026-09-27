package com.amantrivedi.tokensmp.abilities;

import com.amantrivedi.tokensmp.core.TierDefinition;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Tracks every mob kill and feeds the unlock/upgrade task grinders:
 * normal mob kills, Nether-dimension mob kills, wither kill streaks
 * (reset on death) and the barehanded no-armor Warden execution.
 */
public final class KillTaskListener implements Listener {

    private final TokenSmpPlugin plugin;

    public KillTaskListener(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        EntityType victimType = event.getEntityType();

        for (TokenDefinition token : plugin.getRegistry().all()) {
            int tier = plugin.getData().getTier(killer, token.getId());
            TierDefinition next = token.tier(tier + 1);
            if (next == null) {
                continue; // already max tier
            }
            TierDefinition.TaskSpec task = next.getTask();
            int progress = plugin.getData().getProgress(killer, token.getId());
            if (progress >= task.getCount()) {
                continue; // task already completed
            }

            boolean matched = switch (task.getType()) {
                case KILLS -> task.getMobs().contains(victimType);
                case NETHER_KILLS -> event.getEntity().getWorld().getEnvironment() == World.Environment.NETHER
                        && victimType.isAlive() && victimType != EntityType.PLAYER;
                case WITHER_STREAK -> victimType == EntityType.WITHER;
                case WARDEN_BAREHAND -> victimType == EntityType.WARDEN && isBarehanded(killer);
            };

            if (matched) {
                int updated = plugin.getData().addProgress(killer, token.getId(), 1);
                if (updated >= task.getCount()) {
                    notifyTaskComplete(killer, token, task);
                } else if (updated % 50 == 0 || task.getCount() - updated <= 5) {
                    sendBar(killer, plugin.getConfig().getString("messages.task-progress",
                            "§e{token} task: §f{current}§7/§f{required}")
                            .replace("{token}", token.getName())
                            .replace("{current}", String.valueOf(updated))
                            .replace("{required}", String.valueOf(task.getCount())));
                }
            }
        }
    }

    /** Dying resets the wither kill streak (admin hard grind). */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (TokenDefinition token : plugin.getRegistry().all()) {
            int tier = plugin.getData().getTier(player, token.getId());
            TierDefinition next = token.tier(tier + 1);
            if (next != null && next.getTask().getType() == TierDefinition.TaskType.WITHER_STREAK) {
                plugin.getData().setProgress(player, token.getId(), 0);
            }
        }
    }

    private boolean isBarehanded(Player player) {
        // Zero armor items equipped + nothing in either hand.
        for (org.bukkit.inventory.ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                return false;
            }
        }
        return player.getInventory().getItemInMainHand().getType().isAir()
                && player.getInventory().getItemInOffHand().getType().isAir();
    }

    private void notifyTaskComplete(Player player, TokenDefinition token, TierDefinition.TaskSpec task) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
        plugin.sendMessage(player, plugin.getConfig().getString("messages.task-complete",
                "§6🎯 Task complete! §e{token} §6can now be upgraded - open /tokens!")
                .replace("{token}", token.getName()));
        sendBar(player, plugin.getConfig().getString("messages.task-complete-bar",
                "§a✔ {token} task complete - upgrade it in /tokens!")
                .replace("{token}", token.getName()));
    }

    private void sendBar(Player player, String message) {
        if (message != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
}
