package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
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

/**
 * Kill-task tracking: feeds every mob death into the unlock/upgrade grinds
 * (normal kills, Nether-dimension kills, wither kill streaks and the
 * barehanded no-armor Warden execution), with periodic action-bar updates
 * and a completion fanfare.
 */
public final class EntityDeathListener implements Listener {

    private final TokenSMP plugin;

    public EntityDeathListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        EntityType victim = event.getEntityType();

        for (Token token : plugin.registry().all()) {
            int tier = plugin.data().getTier(killer, token.getId());
            TokenTier next = token.tier(tier + 1);
            if (next == null) {
                continue; // already max tier
            }
            TokenTier.TaskSpec task = next.getTask();
            int progress = plugin.data().getProgress(killer, token.getId());
            if (progress >= task.count()) {
                continue; // task already complete
            }

            boolean matched = switch (task.type()) {
                case KILLS -> task.mobs().contains(victim);
                case NETHER_KILLS -> event.getEntity().getWorld().getEnvironment() == World.Environment.NETHER
                        && victim.isAlive() && victim != EntityType.PLAYER;
                case WITHER_STREAK -> victim == EntityType.WITHER;
                case WARDEN_BAREHAND -> victim == EntityType.WARDEN && isBarehanded(killer);
            };

            if (matched) {
                int updated = plugin.data().addProgress(killer, token.getId(), 1);
                if (updated >= task.count()) {
                    notifyComplete(killer, token, task);
                } else if (updated % 50 == 0 || task.count() - updated <= 5) {
                    bar(killer, plugin.config().getString("messages.task-progress",
                                    "&e{token} task: &f{current}&7/&f{required}")
                            .replace("{token}", token.getDisplayName())
                            .replace("{current}", String.valueOf(updated))
                            .replace("{required}", String.valueOf(task.count())));
                }
            }
        }
    }

    private boolean isBarehanded(Player player) {
        for (org.bukkit.inventory.ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                return false;
            }
        }
        return player.getInventory().getItemInMainHand().getType().isAir()
                && player.getInventory().getItemInOffHand().getType().isAir();
    }

    private void notifyComplete(Player player, Token token, TokenTier.TaskSpec task) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
        plugin.messages().send(player, "messages.task-complete",
                "&6🎯 Task complete! &e{token} &6can now be upgraded - open /tokens!",
                "{token}", token.getDisplayName());
        bar(player, plugin.config().getString("messages.task-complete-bar",
                        "&a✔ {token} task complete - upgrade it in /tokens!")
                .replace("{token}", token.getDisplayName()));
    }

    /** Sends a colored action bar line. */
    private void bar(Player player, String text) {
        if (player != null && text != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(com.tokensmp.util.ColorUtil.color(text)));
        }
    }
}
