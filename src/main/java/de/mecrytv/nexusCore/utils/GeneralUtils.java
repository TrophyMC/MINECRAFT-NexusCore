package de.mecrytv.nexusCore.utils;

import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusapi.NexusAPI;
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

    public static void sendStaffNotification(String langKey, String... replacements) {
        NexusAPI.getInstance().getGlobalNotifyer()
                .sendStaffNotification("nexus.staff.reports", langKey, replacements);
    }
}