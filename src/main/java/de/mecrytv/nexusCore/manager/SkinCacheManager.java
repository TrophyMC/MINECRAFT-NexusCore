package de.mecrytv.nexusCore.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.models.SkinCacheModel;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinCacheManager {

    public CompletableFuture<PlayerProfile> getProfile(UUID uuid, String name) {
        return DatabaseAPI.<SkinCacheModel>get("skins", uuid.toString()).thenApply(skinModel -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, name);

            if (skinModel != null) {
                profile.setProperty(new ProfileProperty("textures", skinModel.getTextureValue()));
                return profile;
            } else {
                profile.complete();
                for (ProfileProperty prop : profile.getProperties()) {
                    if (prop.getName().equals("textures")) {
                        DatabaseAPI.set("skins", new SkinCacheModel(uuid.toString(), prop.getValue()));
                        break;
                    }
                }
                return profile;
            }
        });
    }
}
