package de.mecrytv.nexusCore.utils;

import de.mecrytv.languageapi.profile.ILanguageProfile;
import de.mecrytv.nexusCore.NexusCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TranslationUtils {

    private static String getLang(CommandSender sender) {
        if (sender instanceof Player player) {
            return NexusCore.getInstance().getLanguageAPI()
                    .getProfile(player.getUniqueId(), "en_US").getLanguageCode();
        }
        return "en_US";
    }

    public static void sendTranslation(CommandSender sender, String configKey, String... replacements) {
        String langCode = getLang(sender);
        String message = NexusCore.getInstance().getLanguageAPI().getTranslation(langCode, configKey);

        if ((message == null || message.contains("Missing Lang")) && !langCode.equals("en_US")) {
            message = NexusCore.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
        }
        if (message == null || message.contains("Missing Lang")) message = configKey;

        Component component = MiniMessage.miniMessage().deserialize(message);
        sender.sendMessage(NexusCore.getInstance().getPrefix().append(applyReplacements(component, replacements)));
    }

    public static Component getGUITranslation(Player player, String configKey, String... replacements) {
        String langCode = getLang(player);
        String message = NexusCore.getInstance().getLanguageAPI().getTranslation(langCode, configKey);

        if ((message == null || message.contains("Missing Lang")) && !langCode.equals("en_US")) {
            message = NexusCore.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
        }
        if (message == null || message.contains("Missing Lang")) return Component.text(configKey);

        message = message.replaceFirst("(?i)(?:<[^>]*>)*Dynamic\\s*", "").trim();
        return applyReplacements(MiniMessage.miniMessage().deserialize(message), replacements);
    }

    public static List<Component> getGUILoreTranslation(Player player, String configKey, String... replacements) {
        String langCode = getLang(player);
        String message = NexusCore.getInstance().getLanguageAPI().getTranslation(langCode, configKey);

        if ((message == null || message.contains("Missing Lang")) && !langCode.equals("en_US")) {
            message = NexusCore.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
        }
        if (message == null || message.contains("Missing Lang")) return List.of(Component.text(configKey));

        message = message.replaceFirst("(?i)(?:<[^>]*>)*Dynamic\\s*", "").trim();

        List<Component> lore = new ArrayList<>();
        for (String line : message.split("\n")) {
            if (!line.isEmpty()) {
                lore.add(applyReplacements(MiniMessage.miniMessage().deserialize(line), replacements));
            }
        }
        return lore;
    }

    private static Component applyReplacements(Component component, String... replacements) {
        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    component = component.replaceText(b -> b.matchLiteral(target).replacement(value));
                }
            }
        }
        return component;
    }
}