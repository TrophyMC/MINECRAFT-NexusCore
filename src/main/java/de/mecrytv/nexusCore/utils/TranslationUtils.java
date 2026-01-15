package de.mecrytv.nexusCore.utils;

import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.nexusCore.NexusCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class TranslationUtils {

    public static void sendTranslation(CommandSender sender, String langCode, String configKey, String... replacements) {
        NexusCore plugin = NexusCore.getInstance();
        LanguageAPI languageAPI = plugin.getLanguageAPI();

        String message = languageAPI.getTranslation(langCode, configKey);
        if ((message == null || message.isEmpty()) && !langCode.equals("en_US")) {
            message = languageAPI.getTranslation("en_US", configKey);
        }
        if (message == null) message = configKey;

        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    message = message.replace(target, value);
                }
            }
        }

        Component finalMsg = plugin.getPrefix().append(MiniMessage.miniMessage().deserialize(message));
        sender.sendMessage(finalMsg);
    }

    public static Component sendGUITranslation(String langCode, String configKey, String... replacements) {
        NexusCore plugin = NexusCore.getInstance();
        LanguageAPI languageAPI = plugin.getLanguageAPI();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String message = languageAPI.getTranslation(langCode, configKey);
        if ((message == null || message.isEmpty()) && !langCode.equals("en_US")) {
            message = languageAPI.getTranslation("en_US", configKey);
        }

        if (message == null) message = configKey;

        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    message = message.replace(target, value);
                }
            }
        }

        return miniMessage.deserialize(message);
    }
}
