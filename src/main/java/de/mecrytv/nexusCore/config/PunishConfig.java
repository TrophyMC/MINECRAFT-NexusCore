package de.mecrytv.nexusCore.config;

import de.mecrytv.nexusCore.utils.RecordUtils;
import de.mecrytv.nexusCore.utils.TimeUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import de.mecrytv.nexusapi.enums.PunishTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PunishConfig {

    private static final Map<String, String> reasonCache = new HashMap<>();

    public static List<RecordUtils.PunishmentStep> getSteps(String reasonKey) {
        return RULES.get(reasonKey);
    }

    public static String getPlainReason(Player staff, String reasonKey) {
        return reasonCache.computeIfAbsent(reasonKey, k -> {
            Component comp = TranslationUtils.getGUITranslation(staff, "gui.report.reasons." + k + ".name");
            return MiniMessage.miniMessage().serialize(comp);
        });
    }

    public static void clearCache() {
        reasonCache.clear();
    }

    public static final Map<String, List<RecordUtils.PunishmentStep>> RULES = Map.ofEntries(
            Map.entry("client_mod", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("bug_abuse", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_BAN, TimeUtils.days(180)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("teaming", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.WARN, 0),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("trolling", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_MUTE, TimeUtils.hours(8)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_BAN, TimeUtils.days(30))
            )),
            Map.entry("afk_farming", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.WARN, 0),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("stats_pushing", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.WARN, 0),
                    // TODO: Stats Reset Implementation
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_BAN, TimeUtils.days(17)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("ban_evasion", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("skin", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.WARN, 0),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_BAN, TimeUtils.days(30))
            )),
            Map.entry("name", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.WARN, 0),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_BAN, TimeUtils.days(30))
            )),
            Map.entry("provocation", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_MUTE, TimeUtils.hours(8)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_BAN, TimeUtils.days(14))
            )),
            Map.entry("insult", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_MUTE, TimeUtils.hours(8)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(5, PunishTypes.PERMA_MUTE, -1)
            )),
            Map.entry("chat_spam", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_MUTE, TimeUtils.hours(1)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.hours(8)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_MUTE, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(5, PunishTypes.PERMA_MUTE, -1)
            )),
            Map.entry("server_insult", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_MUTE, TimeUtils.days(1)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.TEMP_MUTE, TimeUtils.days(7)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.TEMP_BAN, TimeUtils.days(14)),
                    new RecordUtils.PunishmentStep(4, PunishTypes.TEMP_BAN, TimeUtils.days(30))
            )),
            Map.entry("threat", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(3, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("racism", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.PERMA_BAN, -1)
            )),
            Map.entry("death_wish", List.of(
                    new RecordUtils.PunishmentStep(1, PunishTypes.TEMP_BAN, TimeUtils.days(30)),
                    new RecordUtils.PunishmentStep(2, PunishTypes.PERMA_BAN, -1)
            ))
    );
}