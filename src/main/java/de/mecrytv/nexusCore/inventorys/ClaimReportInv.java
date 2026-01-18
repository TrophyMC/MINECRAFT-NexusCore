package de.mecrytv.nexusCore.inventorys;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimReportInv {

    private final HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();

    public void open(Player player, String caseID, int reportNum, PlayerProfile targetProfile) {
        String playerUUID = player.getUniqueId().toString();

        DatabaseAPI.getInstance().getGenericAsync(
                "language", "language", "id", "data", playerUUID
        ).thenAccept(json -> {
            String langCode = "en_US";
            if (json != null && json.has("languageCode")) {
                langCode = json.get("languageCode").getAsString();
            }

            final String finalLang = langCode;

            DatabaseAPI.<ReportModel>get("reports", caseID).thenAccept(report -> {
                UUID targetUUID = UUID.fromString(report.getTargetUUID());

                NexusCore.getInstance().getSkinCacheManager().getProfile(targetUUID, report.getTargetName()).thenAccept(cachedProfile -> {
                    Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {

                        Component title = TranslationUtils.sendGUITranslation(finalLang, "gui.claimReport.title");

                        Gui gui = Gui.gui()
                                .title(title)
                                .rows(3)
                                .disableAllInteractions()
                                .create();

                        ItemStack playerReport = new ItemStack(Material.PLAYER_HEAD);
                        playerReport.editMeta(SkullMeta.class, meta -> {
                            if (targetProfile != null) {
                                meta.setPlayerProfile(targetProfile);
                            } else {
                                meta.setPlayerProfile(cachedProfile);
                            }

                            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

                            meta.displayName(TranslationUtils.sendGUITranslation(finalLang, "gui.reports.heads.title",
                                    "{number}", String.valueOf(reportNum),
                                    "{caseID}", report.getReportID()
                            ));

                            Component reasonDisplay = TranslationUtils.sendGUITranslation(finalLang, "gui.report.reasons." + report.getReason() + ".name");

                            List<Component> lore = new ArrayList<>();
                            lore.add(Component.empty());
                            lore.add(TranslationUtils.sendGUITranslation(finalLang, "gui.reports.heads.target", "{target}", report.getTargetName()));
                            lore.add(TranslationUtils.sendGUITranslation(finalLang, "gui.reports.heads.reporter", "{reporter}", report.getReporterName()));
                            lore.add(TranslationUtils.sendGUITranslation(finalLang, "gui.reports.heads.reason")
                                    .replaceText(builder -> builder.matchLiteral("{reason}").replacement(reasonDisplay)));

                            meta.lore(lore);
                        });

                        ItemStack acceptReport = headDatabaseAPI.getItemHead("10209");
                        if (acceptReport == null) acceptReport = new ItemStack(Material.LIME_DYE);

                        acceptReport.editMeta(meta -> {
                            meta.displayName(TranslationUtils.sendGUITranslation(finalLang, "gui.claimReport.accept"));
                        });

                        GuiItem acceptItem = ItemBuilder.from(acceptReport).asGuiItem(event -> {
                            Player clicker = (Player) event.getWhoClicked();

                            JsonObject updates = new JsonObject();
                            updates.addProperty("state", "WORKING_ON");
                            updates.addProperty("staffUUID", clicker.getUniqueId().toString());
                            updates.addProperty("staffName", clicker.getName());

                            DatabaseAPI.updateAsync("reports", caseID, updates).thenRun(() -> {
                                Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                                    new WorkingOnReportInv().open(clicker);
                                });
                            }).exceptionally(ex -> {
                                TranslationUtils.sendTranslation(clicker, "en_US", "gui.reports.errors.claiming");
                                ex.printStackTrace();
                                return null;
                            });
                        });

                        ItemStack denyReport = headDatabaseAPI.getItemHead("9351");
                        if (denyReport == null) denyReport = new ItemStack(Material.RED_DYE);

                        denyReport.editMeta(meta -> {
                            meta.displayName(TranslationUtils.sendGUITranslation(finalLang, "gui.claimReport.deny"));
                        });

                        GuiItem denyItem = ItemBuilder.from(denyReport).asGuiItem(event -> {
                            new ReportsInv().open(player);
                        });

                        gui.setItem(2, 3, denyItem);
                        gui.setItem(2, 5, ItemBuilder.from(playerReport).asGuiItem());
                        gui.setItem(2, 7, acceptItem);

                        gui.open(player);
                    });
                });
            }).exceptionally(ex -> {
                TranslationUtils.sendTranslation(player, finalLang, "gui.reports.errors.loading_report");
                ex.printStackTrace();
                return null;
            });
        }).exceptionally(ex -> {
            TranslationUtils.sendTranslation(player, "en_US", "gui.reports.errors.loading");
            ex.printStackTrace();
            return null;
        });
    }
}