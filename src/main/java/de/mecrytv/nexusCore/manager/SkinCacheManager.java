package de.mecrytv.nexusCore.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.nexusCore.models.SkinCacheModel;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SkinCacheManager {

    private static final long CACHE_EXPIRATION_MS = TimeUnit.HOURS.toMillis(24);

    public CompletableFuture<PlayerProfile> getProfile(UUID uuid, String name) {
        return DatabaseAPI.<SkinCacheModel>get("skins", uuid.toString()).thenApplyAsync(skinModel -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, name);

            if (skinModel != null && (System.currentTimeMillis() - skinModel.getLastUpdated() < CACHE_EXPIRATION_MS)) {
                profile.setProperty(new ProfileProperty("textures", skinModel.getTextureValue()));
                return profile;
            }

            profile.complete();

            for (ProfileProperty prop : profile.getProperties()) {
                if (prop.getName().equals("textures")) {
                    DatabaseAPI.set("skins", new SkinCacheModel(uuid.toString(), prop.getValue()));
                    break;
                }
            }
            return profile;
        });
    }
}
