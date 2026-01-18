package de.mecrytv.nexusCore.listeners;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.TeleportModel;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class ReportTeleportListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player staff = event.getPlayer();

        DatabaseAPI.<TeleportModel>get("reportteleport", staff.getUniqueId().toString()).thenAccept(tpModel -> {
            if (tpModel != null) {
                UUID targetUUID = UUID.fromString(tpModel.getTargetUUID());

                Bukkit.getScheduler().runTaskLater(NexusCore.getInstance(), () -> {
                    Player target = Bukkit.getPlayer(targetUUID);

                    if (target != null && target.isOnline()){
                        staff.teleport(target);
                        staff.sendMessage(NexusCore.getInstance().getPrefix().append(
                                Component.text("§7Du wurdest zum Ziel teleportiert.")
                        ));
                    }
                    DatabaseAPI.delete("reportteleport", staff.getUniqueId().toString());
                }, 10L);
            }
        });
    }
}
