package de.mecrytv.nexusCore.utils;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ReportModel;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.security.SecureRandom;

public class GeneralUtils {

    private static final String ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateUniqueReportID() {
        String id;
        boolean exists;

        do {
            id = generateRawID();
            exists = DatabaseAPI.get("reports", id).join() != null;

        } while (exists);

        return id;
    }

    private static String generateRawID() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }

    public static void sendStaffNotification() {
        DatabaseAPI.<ReportModel>getAll("reports").thenAccept(allReports -> {
            long openCount = allReports.stream()
                    .filter(report -> "OPEN".equalsIgnoreCase(report.getState()))
                    .count();

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("nexus.staff.reports")) {

                    DatabaseAPI.getInstance().getGenericAsync(
                            "language", "language", "id", "data", staff.getUniqueId().toString()
                    ).thenAccept(json -> {

                        String langCode = "en_US";
                        if (json != null && json.has("languageCode")) {
                            langCode = json.get("languageCode").getAsString();
                        }

                        final String finalLang = langCode;

                        String rawMessage = NexusCore.getInstance().getLanguageAPI()
                                .getTranslation(finalLang, "messages.staff_report_actionbar");

                        if (rawMessage == null) rawMessage = "<red>New Report! <gray>Open: <yellow>{count}";

                        String formatted = rawMessage.replace("{count}", String.valueOf(openCount));

                        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                            staff.sendActionBar(MiniMessage.miniMessage().deserialize(formatted));
                        });
                    });
                }
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}