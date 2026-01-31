package de.mecrytv.nexusCore.manager;

import com.google.gson.JsonObject;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.config.PunishConfig;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.RecordUtils;
import de.mecrytv.nexusapi.NexusAPI;
import de.mecrytv.nexusapi.models.BanModel;
import de.mecrytv.nexusapi.models.MuteModel;
import de.mecrytv.nexusapi.models.PunishmentHistoryModel;
import de.mecrytv.nexusapi.models.WarnModel;
import org.bukkit.entity.Player;

import java.util.List;

import static de.mecrytv.nexusapi.enums.PunishTypes.*;

public class PunishManager {

    private final NexusCore plugin;

    public PunishManager(NexusCore plugin) {
        this.plugin = plugin;
    }

    public void executePunishment(String reportID, String reasonKey, Player target, Player staff) {
        String historyId = target.getUniqueId() + ":" + reasonKey;
        var repo = NexusAPI.getInstance().getRepoManager().getHistoryRepository();

        repo.getOnce(historyId).thenAccept(optHistory -> {
            PunishmentHistoryModel history = optHistory.orElse(new PunishmentHistoryModel(target.getUniqueId().toString(), reasonKey, 0));
            history.increment();

            processPunishment(history.getCount(), reportID, reasonKey, target, staff);
            repo.save(history);
        });
    }

    private void processPunishment(int level, String reportID, String reasonKey, Player target, Player staff) {
        List<RecordUtils.PunishmentStep> steps = PunishConfig.getSteps(reasonKey);
        if (steps == null || steps.isEmpty()) return;

        RecordUtils.PunishmentStep step = steps.stream()
                .filter(s -> level >= s.level())
                .reduce((first, second) -> second)
                .orElse(steps.get(0));

        executeStep(step, reportID, reasonKey, target, staff);
    }

    private void executeStep(RecordUtils.PunishmentStep step, String reportID, String reasonKey, Player target, Player staff) {
        long today = System.currentTimeMillis();
        var apiRepos = NexusAPI.getInstance().getRepoManager();

        String tUUID = target.getUniqueId().toString();
        String sUUID = staff.getUniqueId().toString();
        String sName = staff.getName();
        String reason = PunishConfig.getPlainReason(staff, reasonKey);


        switch (step.type()) {
            case WARN ->
                    apiRepos.getWarnRepository().save(new WarnModel(reportID, tUUID, reason, sUUID, sName, today));

            case TEMP_MUTE -> {
                long expiry = today + step.duration();
                apiRepos.getMuteRepository().save(new MuteModel(reportID, tUUID, reason, sUUID, sName, today, expiry));
            }

            case PERMA_MUTE ->
                    apiRepos.getMuteRepository().save(new MuteModel(reportID, tUUID, reason, sUUID, sName, today, -1L));

            case TEMP_BAN -> {
                BanModel tempBan = new BanModel(reportID, tUUID, reason, sUUID, sName, TEMP_BAN, today, today + step.duration());
                apiRepos.getBanRepository().save(tempBan);
            }

            case PERMA_BAN -> {
                String ip = target.getAddress() != null ? target.getAddress().getAddress().getHostAddress() : "0.0.0.0";
                BanModel permaBan = new BanModel(reportID, tUUID, reason, sUUID, sName, PERMA_BAN, today, ip);
                apiRepos.getBanRepository().save(permaBan);
            }
            default -> {
                plugin.getLogger().warning("Unbekannter Punishment-Typ: " + step.type() + " für Grund: " + reasonKey);
                return;
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