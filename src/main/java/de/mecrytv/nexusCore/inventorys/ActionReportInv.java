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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ActionReportInv {

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
                if (targetProfile != null) {
                    renderGUI(player, report, reportNum, finalLang, targetProfile);
                } else {
                    UUID targetUUID = UUID.fromString(report.getTargetUUID());
                    NexusCore.getInstance().getSkinCacheManager().getProfile(targetUUID, report.getTargetName()).thenAccept(cachedProfile -> {
                        renderGUI(player, report, reportNum, finalLang, cachedProfile);
                    });
                }
            });
        });
    }

    private void renderGUI(Player player, ReportModel report, int reportNum, String langCode, PlayerProfile targetProfile) {
        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
            Date dateObj = new Date(report.getReportTime());

            Component title = TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.title");

            Gui gui = Gui.gui()
                    .title(title)
                    .rows(6)
                    .disableAllInteractions()
                    .create();

            ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
            targetHead.editMeta(SkullMeta.class, meta ->{
                meta.setPlayerProfile(targetProfile);

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.title",
                        "{number}", String.valueOf(reportNum),
                        "{caseID}", report.getReportID()
                ));

                Component reasonDisplay = TranslationUtils.sendGUITranslation(langCode, "gui.report.reasons." + report.getReason() + ".name");

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.target", "{target}", report.getTargetName()));
                lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.reporter", "{reporter}", report.getReporterName()));
                lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.reason")
                        .replaceText(builder -> builder.matchLiteral("{reason}").replacement(reasonDisplay)));
                lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.time",
                        "{date}", dateFmt.format(dateObj),
                        "{time}", timeFmt.format(dateObj)
                ));

                lore.add(Component.empty());
                lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.state", "{state}", report.getState()));
                lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.staff", "{staff}", report.getStaffName()));

                meta.lore(lore);
            });
            GuiItem targetHeadItem = ItemBuilder.from(targetHead).asGuiItem();

            ItemStack unClaimItem = new ItemStack(Material.RED_DYE);
            unClaimItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.unclaim"));
            });
            GuiItem unClaimGuiItem = ItemBuilder.from(unClaimItem).asGuiItem(event -> {
                Player clicker = (Player) event.getWhoClicked();

                JsonObject updates = new JsonObject();
                updates.addProperty("state", "OPEN");
                updates.addProperty("staffUUID", "");
                updates.addProperty("staffName", "");

                DatabaseAPI.updateAsync("reports", report.getReportID(), updates).thenRun(() -> {
                    Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                        gui.close(clicker);
                        clicker.sendMessage(TranslationUtils.sendChatTranslation(langCode, "messages.report.unclaimed"
                                , "{target}", report.getTargetName()));
                    });
                });
            });

            ItemStack historyItem = new ItemStack(Material.BOOK);
            historyItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.history"));
            });
            GuiItem historyGuiItem = ItemBuilder.from(historyItem).asGuiItem(event -> {
                // TODO: Open History GUI
            });

            ItemStack punishItem = new ItemStack(Material.ANVIL);
            punishItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.punish"));
            });
            GuiItem punishGuiItem = ItemBuilder.from(punishItem).asGuiItem(event -> {
                // TODO: Open Punish GUI
            });

            ItemStack proofItem = new ItemStack(Material.PAPER);
            proofItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.proof"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("§cCOMING SOON"));
                meta.lore(lore);
            });
            GuiItem proofGuiItem = ItemBuilder.from(proofItem).asGuiItem();

            ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
            teleportItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.teleport"));
            });
            GuiItem teleportGuiItem = ItemBuilder.from(teleportItem).asGuiItem(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.sendMessage(Component.text("§cTeleport feature is currently disabled."));

                /*
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(byteOut);

                try {
                    out.writeUTF("TeleportRequest");
                    out.writeUTF(clicker.getUniqueId().toString());
                    out.writeUTF(report.getTargetUUID());
                    out.writeUTF(report.getTargetName());

                    clicker.sendPluginMessage(NexusCore.getInstance(), "nexus:bridge", byteOut.toByteArray());
                    clicker.closeInventory();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 */
            });

            ItemStack closeItem = new ItemStack(Material.BARRIER);
            closeItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.actionReport.close"));
            });
            GuiItem closeGuiItem = ItemBuilder.from(closeItem).asGuiItem(event -> gui.close(player));

            gui.getFiller().fill(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).asGuiItem());

            gui.setItem(1, 1, targetHeadItem);
            gui.setItem(2, 5, unClaimGuiItem);
            gui.setItem(3, 4, historyGuiItem);
            gui.setItem(3, 5, punishGuiItem);
            gui.setItem(3, 6, proofGuiItem);
            gui.setItem(4, 5, teleportGuiItem);
            gui.setItem(6, 5, closeGuiItem);

            gui.open(player);
        });
    }
}
