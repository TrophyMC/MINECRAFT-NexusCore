package de.mecrytv.nexusCore.listeners;

import de.mecrytv.nexusCore.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VanishListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if (!player.hasPermission("nexus.vanish")) {
            NexusCore.getInstance().getVanishManager().getVanishedUUIDs().forEach(vansihedUUID -> {
                Player vanishedPlayer = Bukkit.getPlayer(vansihedUUID);
                if (vanishedPlayer != null) {
                    player.hidePlayer(NexusCore.getInstance(), vanishedPlayer);
                }
            });
        }
    }
}
