package de.mecrytv.nexusCore.commands;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.inventorys.ReportInv;
import de.mecrytv.nexusCore.inventorys.ReportsInv;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReportCommand implements CommandExecutor {

    private static final Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be used by players.");
            return true;
        }

        String playerUUID = player.getUniqueId().toString();

        DatabaseAPI.getInstance().getGenericAsync(
                "language", "language", "id", "data", playerUUID
        ).thenAccept(json -> {

            String langCode = "en_US";
            if (json != null && json.has("languageCode")) {
                langCode = json.get("languageCode").getAsString();
            }

            final String finalLang = langCode;


            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {

                if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
                    if (!player.hasPermission("nexus.development")) {
                        TranslationUtils.sendTranslation(player, "commands.command_no_permission");
                        return;
                    }

                    String reportId = args[1];

                    DatabaseAPI.get("reports", reportId).thenAccept(report -> {
                        if (report == null) {
                            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () ->
                                    TranslationUtils.sendTranslation(player, "commands.report.not_found", "{id}", reportId));
                            return;
                        }

                        DatabaseAPI.delete("reports", reportId);

                        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () ->
                                TranslationUtils.sendTranslation(player, "commands.report.deleted_success", "{id}", reportId));
                    });
                    return;
                }

                if (!player.hasPermission("nexus.report")) {
                    TranslationUtils.sendTranslation(player, "commands.command_no_permission");
                    return;
                }

                if (args.length != 1) {
                    TranslationUtils.sendTranslation(player, "commands.report.usage");
                    return;
                }

                Player target = Bukkit.getPlayer(args[0]);

                if (target == null) {
                    TranslationUtils.sendTranslation(player, "commands.report.player_not_found");
                    return;
                }

                if (target.equals(player)) {
                    TranslationUtils.sendTranslation(player, "commands.report.self_report");
                    return;
                }

                String cooldownKey = player.getUniqueId() + ":" + target.getUniqueId();
                if (cooldowns.containsKey(cooldownKey)) {
                    long lastReport = cooldowns.get(cooldownKey);
                    long diff = System.currentTimeMillis() - lastReport;
                    long cooldownMillis = TimeUnit.MINUTES.toMillis(2);

                    if (diff < cooldownMillis) {
                        long millisLeft = cooldownMillis - diff;
                        long totalSeconds = millisLeft / 1000;

                        if (totalSeconds >= 60) {
                            long minutes = totalSeconds / 60;
                            long seconds = totalSeconds % 60;

                            TranslationUtils.sendTranslation(player, "commands.report.cooldown_minutes",
                                    "{minutes}", String.valueOf(minutes),
                                    "{seconds}", String.valueOf(seconds));
                        } else {
                            TranslationUtils.sendTranslation(player, "commands.report.cooldown_seconds",
                                    "{seconds}", String.valueOf(totalSeconds));
                        }
                        return;
                    }
                }

                new ReportInv().open(player, target);
            });
        }).exceptionally(ex -> {
            player.sendMessage("Bitte versuche es später erneut.");
            ex.printStackTrace();
            return null;
        });

        return true;
    }

    public static void setCooldown(UUID reporter, UUID target) {
        cooldowns.put(reporter + ":" + target, System.currentTimeMillis());
    }
}