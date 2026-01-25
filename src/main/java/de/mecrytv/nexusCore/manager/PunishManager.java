package de.mecrytv.nexusCore.manager;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.enums.BanType;
import de.mecrytv.nexusCore.models.punish.BanModel;
import de.mecrytv.nexusCore.models.punish.PunishmentHistoryModel;
import de.mecrytv.nexusCore.utils.TimeUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

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

    private void processPunishment(int level, String reportID, String reasonKey, Player target, Player staff){
        long today = System.currentTimeMillis();
        String targetUUID = target.getUniqueId().toString();
        String staffUUID = staff.getUniqueId().toString();
        String targetIp = target.getAddress() != null ? target.getAddress().getAddress().getHostAddress() : "unknown";

        String baseKey = "gui.report.reasons." + reasonKey;
        Component reasonComp = TranslationUtils.getGUITranslation(staff, baseKey + ".name");
        String plainReason = MiniMessage.miniMessage().serialize(reasonComp);

        switch (reasonKey) {
            case "client_mod":
                if (level == 1) {
                    long duration = TimeUtils.days(30);

                    BanModel userBan = new BanModel(
                            targetUUID,
                            plainReason,
                            staffUUID,
                            BanType.TEMPORARY,
                            today,
                            today + duration
                    );
                    DatabaseAPI.set("ban", userBan);
                } else if (level == 2) {
                    BanModel userBan = new BanModel(
                            targetUUID,
                            plainReason,
                            staffUUID,
                            BanType.PERMANENT,
                            today,
                            targetIp
                    );
                    DatabaseAPI.set("ban", userBan);
                }
                break;
        }
    }
}
