package de.mecrytv.nexusCore.commands;

import de.mecrytv.nexusCore.inventorys.ReportsInv;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReportsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("§cThis command can only be executed by a player!");
            return true;
        }

        Player player = (Player) commandSender;
        new ReportsInv().open(player);

        return true;
    }
}
