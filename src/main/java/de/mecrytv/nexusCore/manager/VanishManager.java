package de.mecrytv.nexusCore.manager;

import de.mecrytv.nexusCore.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VanishManager {

    private final NexusCore plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(NexusCore plugin) {
        this.plugin = plugin;
    }

    public void addVanish(@NotNull Player player){
        vanishedPlayers.add(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (!onlinePlayer.hasPermission("nexus.vanish")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        });
    }

    public void removeVanish(@NotNull Player player){
        vanishedPlayers.remove(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            onlinePlayer.showPlayer(plugin, player);
        });
    }

    public boolean isVanished(@NotNull Player player){
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedUUIDs() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    public Set<String> getVanishedPlayerNames() {
        return vanishedPlayers.stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .collect(Collectors.toSet());
    }
}
