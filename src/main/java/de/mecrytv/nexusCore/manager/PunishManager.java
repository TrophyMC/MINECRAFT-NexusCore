package de.mecrytv.nexusCore.manager;

import com.google.gson.JsonObject;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.config.PunishConfig;
import de.mecrytv.nexusCore.enums.PunishTypes;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.models.punish.BanModel;
import de.mecrytv.nexusCore.models.punish.MuteModel;
import de.mecrytv.nexusCore.models.punish.PunishmentHistoryModel;
import de.mecrytv.nexusCore.models.punish.WarnModel;
import de.mecrytv.nexusCore.utils.RecordUtils;
import de.mecrytv.nexusCore.utils.TimeUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

public class PunishManager {

    private final NexusCore plugin;

    public PunishManager(NexusCore plugin) {
        this.plugin = plugin;
    }

    public void executePunishment(String reportID, String reasonKey, Player target, Player staff) {
        String historyId = target.getUniqueId() + ":" + reasonKey;

        DatabaseAPI.<PunishmentHistoryModel>get("punishments", historyId).thenAccept(history -> {
            PunishmentHistoryModel finalHistory = (history != null) ? history :
                    new PunishmentHistoryModel(target.getUniqueId().toString(), reasonKey, 0);

            finalHistory.increment();
            int level = finalHistory.getCount();

            processPunishment(level, reportID, reasonKey, target, staff);
            DatabaseAPI.set("punishments", finalHistory);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    private void processPunishment(int level, String reportID, String reasonKey, Player target, Player staff) {
        List<RecordUtils.PunishmentStep> steps = PunishConfig.getSteps(reasonKey);

        if (steps == null || steps.isEmpty()) {
            plugin.getLogger().warning("Keine Strafregeln für Grund gefunden: " + reasonKey);
            return;
        }

        RecordUtils.PunishmentStep step = steps.stream()
                .filter(s -> level >= s.level())
                .reduce((first, second) -> second)
                .orElse(steps.get(0));

        executeStep(step, reportID, reasonKey, target, staff);
    }

    private void executeStep(RecordUtils.PunishmentStep step, String reportID, String reasonKey, Player target, Player staff) {
        long today = System.currentTimeMillis();
        String targetUUID = target.getUniqueId().toString();
        String staffUUID = staff.getUniqueId().toString();
        String staffName = staff.getName();

        String plainReason = PunishConfig.getPlainReason(staff, reasonKey);

        switch (step.type()) {
            case WARN ->
                    DatabaseAPI.set("warn", new WarnModel(reportID, targetUUID, plainReason, staffUUID, staffName, today));

            case TEMP_MUTE ->
                    DatabaseAPI.set("mute", new MuteModel(reportID, targetUUID, plainReason, staffUUID, staffName, today, today + step.duration()));

            case PERMA_MUTE ->
                    DatabaseAPI.set("mute", new MuteModel(reportID, targetUUID, plainReason, staffUUID, staffName, today));

            case TEMP_BAN ->
                    DatabaseAPI.set("ban", new BanModel(reportID, targetUUID, plainReason, staffUUID, staffName, PunishTypes.TEMP_BAN, today, today + step.duration()));

            case PERMA_BAN -> {
                String targetIp = target.getAddress() != null ? target.getAddress().getAddress().getHostAddress() : "unknown";
                DatabaseAPI.set("ban", new BanModel(reportID, targetUUID, plainReason, staffUUID, staffName, PunishTypes.PERMA_BAN, today, targetIp));
            }
        }
        changeReportState(reportID);
    }

    private void changeReportState(String reportID){
        if (reportID == null || reportID.isEmpty() || reportID.equalsIgnoreCase("none")) {
            return;
        }

        DatabaseAPI.<ReportModel>get("reports", reportID).thenAccept(report -> {
            if (report == null) {
                plugin.getLogger().warning("Report " + reportID + " existiert nicht in der DB. Überspringe Status-Update.");
                return;
            }

            JsonObject updates = new JsonObject();
            updates.addProperty("state", "CLOSED");

            DatabaseAPI.updateAsync("reports", reportID, updates).thenRun(() -> {
                plugin.getLogger().info("Report " + reportID + " wurde erfolgreich geschlossen.");
            });
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Fehler beim Zugriff auf Report " + reportID + ": " + ex.getMessage());
            return null;
        });
    }
}
