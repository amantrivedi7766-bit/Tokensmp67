package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /tokencinematic - runs from the clickable cinematic prompt. Plays the
 * optional camera shot for the ability the player just used.
 */
public final class CinematicCommand implements CommandExecutor {

    private final TokenSMP plugin;

    public CinematicCommand(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        plugin.cinematics().requestShot(player);
        return true;
    }
}
