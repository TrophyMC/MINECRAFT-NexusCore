package de.mecrytv.nexusCore.listeners;

import de.mecrytv.nexusCore.NexusCore;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatLogListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        NexusCore.getInstance().getMessageLogManager().log(event.getPlayer().getUniqueId(), message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        NexusCore.getInstance().getMessageLogManager().remove(event.getPlayer().getUniqueId());
    }
}
