package de.mecrytv.nexusCore.listeners;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.TeleportModel;
import de.mecrytv.nexusCore.utils.TranslationUtils;
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
                        if (!NexusCore.getInstance().getVanishManager().isVanished(staff)) {
                            NexusCore.getInstance().getVanishManager().addVanish(staff);
                        }

                        staff.teleport(target);
                        TranslationUtils.sendTranslation(staff, "listeners.report.teleport_success", "{target}", target.getName());
                    }
                    DatabaseAPI.delete("reportteleport", staff.getUniqueId().toString());
                }, 10L);
            }
        });
    }
}
