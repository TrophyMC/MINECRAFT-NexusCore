package de.mecrytv.nexusCore.utils;

import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.nexusCore.NexusCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class TranslationUtils {

    public static void sendTranslation(CommandSender sender, String langCode, String configKey, String... replacements) {
        NexusCore plugin = NexusCore.getInstance();
        String message = plugin.getLanguageAPI().getTranslation(langCode, configKey);

        if ((message == null || message.isEmpty()) && !langCode.equals("en_US")) {
            message = plugin.getLanguageAPI().getTranslation("en_US", configKey);
        }
        if (message == null) message = configKey;

        Component messageComp = MiniMessage.miniMessage().deserialize(message);

        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    messageComp = messageComp.replaceText(builder -> builder.matchLiteral(target).replacement(value));
                }
            }
        }

        sender.sendMessage(plugin.getPrefix().append(messageComp));
    }

    public static Component sendGUITranslation(String langCode, String configKey, String... replacements) {
        NexusCore plugin = NexusCore.getInstance();
        String message = plugin.getLanguageAPI().getTranslation(langCode, configKey);

        if ((message == null || message.isEmpty()) && !langCode.equals("en_US")) {
            message = plugin.getLanguageAPI().getTranslation("en_US", configKey);
        }
        if (message == null) message = configKey;

        message = message.replaceFirst("(?i)(?:<[^>]*>)*Dynamic\\s*", "");

        message = message.trim();

        Component component = MiniMessage.miniMessage().deserialize(message);

        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    component = component.replaceText(builder -> builder.matchLiteral(target).replacement(value));
                }
            }
        }
        return component;
    }
}
