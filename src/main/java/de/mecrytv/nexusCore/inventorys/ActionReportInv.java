package de.mecrytv.nexusCore.inventorys;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.languageapi.profile.ILanguageProfile;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.enums.ProofType;
import de.mecrytv.nexusCore.models.ProofModel;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.TimeUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActionReportInv {

    private final HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
    private static final Set<UUID> watchingProof = new HashSet<>();

    public void open(Player player, String caseID, int reportNum, PlayerProfile targetProfile) {
        DatabaseAPI.<ReportModel>get("reports", caseID).thenAccept(report -> {
            if (targetProfile != null) {
                renderGUI(player, report, reportNum, targetProfile);
            } else {
                UUID targetUUID = UUID.fromString(report.getTargetUUID());
                NexusCore.getInstance().getSkinCacheManager().getProfile(targetUUID, report.getTargetName()).thenAccept(cachedProfile -> {
                    renderGUI(player, report, reportNum, cachedProfile);
                });
            }
        });
    }

    private void renderGUI(Player player, ReportModel report, int reportNum, PlayerProfile targetProfile) {
        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
            Date dateObj = new Date(report.getReportTime());

            Component title = TranslationUtils.getGUITranslation(player, "gui.actionReport.title");

            Gui gui = Gui.gui()
                    .title(title)
                    .rows(6)
                    .disableAllInteractions()
                    .create();

            ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
            targetHead.editMeta(SkullMeta.class, meta ->{
                meta.setPlayerProfile(targetProfile);

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.heads.title",
                        "{number}", String.valueOf(reportNum),
                        "{caseID}", report.getReportID()
                ));

                Component reasonDisplay = TranslationUtils.getGUITranslation(player, "gui.report.reasons." + report.getReason() + ".name");

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.target", "{target}", report.getTargetName()));
                lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.reporter", "{reporter}", report.getReporterName()));
                lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.reason")
                        .replaceText(builder -> builder.matchLiteral("{reason}").replacement(reasonDisplay)));
                lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.time",
                        "{date}", dateFmt.format(dateObj),
                        "{time}", timeFmt.format(dateObj)
                ));

                lore.add(Component.empty());
                lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.state", "{state}", report.getState()));
                lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.staff", "{staff}", report.getStaffName()));

                meta.lore(lore);
            });
            GuiItem targetHeadItem = ItemBuilder.from(targetHead).asGuiItem();

            ItemStack unClaimItem = new ItemStack(Material.RED_DYE);
            unClaimItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.actionReport.unclaim"));
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
                        TranslationUtils.sendTranslation(player, "messages.report.unclaimed"
                                , "{target}", report.getTargetName());
                    });
                });
            });

            ItemStack historyItem = new ItemStack(Material.BOOK);
            historyItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.actionReport.history"));
            });
            GuiItem historyGuiItem = ItemBuilder.from(historyItem).asGuiItem(event -> {
                new HistoryInv().open(player, targetProfile);
            });

            ItemStack punishItem = new ItemStack(Material.ANVIL);
            punishItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.actionReport.punish"));
            });
            GuiItem punishGuiItem = ItemBuilder.from(punishItem).asGuiItem(event -> {
                Player clicker = (Player) event.getWhoClicked();
                Player target = Bukkit.getPlayer(UUID.fromString(report.getTargetUUID()));

                new PunishReportInv().open(clicker, target, report.getReportID());
            });

            DatabaseAPI.<ProofModel>get("proofs", report.getReportID()).thenAccept(proof -> {
                ItemStack proofItem;
                String baseKey;

                if (proof == null) {
                    proofItem = new ItemStack(Material.BARRIER);
                    baseKey = "gui.actionReport.no_proof";
                } else if (proof.getType() == ProofType.MESSAGE) {
                    proofItem = new ItemStack(Material.PAPER);
                    baseKey = "gui.actionReport.proof.message";
                } else {
                    try {
                        proofItem = headDatabaseAPI.getItemHead("112347");
                    } catch (Exception e) {
                        proofItem = new ItemStack(Material.PLAYER_HEAD);
                    }
                    baseKey = "gui.actionReport.proof.replay";
                }

                proofItem.editMeta(meta -> {
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.displayName(TranslationUtils.getGUITranslation(player, baseKey + ".name"));
                });

                GuiItem proofGuiItem = ItemBuilder.from(proofItem).asGuiItem(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    if (proof == null || proof.getData() == null || proof.getData().isEmpty()) {
                        TranslationUtils.sendTranslation(clicker, "messages.report.no_proof");
                        return;
                    }

                    if (proof.getType() == ProofType.MESSAGE) {
                        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                            openProofBook(clicker, report, reportNum, targetProfile, proof.getData());
                        });
                    } else {
                        clicker.closeInventory();
                        clicker.sendMessage("§aREPORT REPLAY COMING SOON!");
                    }
                });

                Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                    gui.setItem(3, 3, proofGuiItem);
                    gui.update();
                });
            });

            ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
            teleportItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.actionReport.teleport"));
            });
            GuiItem teleportGuiItem = ItemBuilder.from(teleportItem).asGuiItem(event -> {
                Player clicker = (Player) event.getWhoClicked();
                UUID targetUUID = UUID.fromString(report.getTargetUUID());

                Player localTarget = Bukkit.getPlayer(targetUUID);

                if (localTarget != null && localTarget.isOnline()) {
                    if (!NexusCore.getInstance().getVanishManager().isVanished(clicker)) {
                        NexusCore.getInstance().getVanishManager().addVanish(clicker);
                    }

                    clicker.teleport(localTarget.getLocation());
                    clicker.closeInventory();
                    TranslationUtils.sendTranslation(clicker, "listeners.report.teleport_success", "{target}", report.getTargetName());
                    return;
                } else if (localTarget != null && !localTarget.isOnline()){
                    TranslationUtils.sendTranslation(clicker, "messages.report.teleport.offline",
                            "{target}", report.getTargetName()
                    );
                    return;
                } else {
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
                }
            });

            ItemStack rejectItem = new ItemStack(Material.BUCKET);
            rejectItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.actionReport.reject"));
            });
            GuiItem rejectGuiItem = ItemBuilder.from(rejectItem).asGuiItem(event -> {
                Player clicker = (Player) event.getWhoClicked();

                DatabaseAPI.delete("reports", report.getReportID());
                DatabaseAPI.delete("proofs", report.getReportID());

                TranslationUtils.sendTranslation(player, "messages.report.rejected",
                        "{target}", report.getTargetName()
                );
                gui.close(clicker);
            });

            ItemStack closeItem = new ItemStack(Material.BARRIER);
            closeItem.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.actionReport.close"));
            });
            GuiItem closeGuiItem = ItemBuilder.from(closeItem).asGuiItem(event -> gui.close(player));

            ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            border.editMeta(meta -> meta.displayName(Component.empty()));
            gui.getFiller().fill(ItemBuilder.from(border).asGuiItem());

            gui.setItem(1, 1, targetHeadItem);
            gui.setItem(3, 5, punishGuiItem);
            gui.setItem(3, 7, unClaimGuiItem);
            gui.setItem(4, 3, historyGuiItem);
            gui.setItem(4, 5, teleportGuiItem);
            gui.setItem(4, 7, rejectGuiItem);
            gui.setItem(6, 5, closeGuiItem);

            gui.open(player);
        });
    }

    private void openProofBook(Player staff, ReportModel report, int reportNum, PlayerProfile targetProfile, List<String> chatLogs) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null) return;

        String title = PlainTextComponentSerializer.plainText().serialize(TranslationUtils.getGUITranslation(
                staff, "gui.actionReport.proof.book_title", "{target}", targetProfile.getName()));

        String author = PlainTextComponentSerializer.plainText()
                .serialize(TranslationUtils.getGUITranslation(staff, "gui.actionReport.proof.book_author"));

        meta.setTitle(title);
        meta.setAuthor(author);

        List<String> chronologicalLogs = new ArrayList<>(chatLogs);
        Collections.reverse(chronologicalLogs);

        StringBuilder page = new StringBuilder();
        int lines = 0;

        for (String rawLog : chronologicalLogs) {
            String formattedLine;
            try {
                if (rawLog.startsWith("[") && rawLog.contains("]")) {
                    int endBracket = rawLog.indexOf("]");
                    long timestamp = Long.parseLong(rawLog.substring(1, endBracket));
                    String message = rawLog.substring(endBracket + 1);

                    String timeFormatted = TimeUtils.formatTimestamp(timestamp);
                    formattedLine = "§8[" + timeFormatted + "]\n§1» §0" + message + "\n";
                } else {
                    formattedLine = "§1» §0" + rawLog + "\n";
                }
            } catch (Exception e) {
                formattedLine = "§1» §0" + rawLog + "\n";
            }

            page.append(formattedLine).append("\n");
            lines += 3;

            if (lines >= 12) {
                meta.addPage(page.toString());
                page = new StringBuilder();
                lines = 0;
            }
        }
        if (page.length() > 0) meta.addPage(page.toString());

        book.setItemMeta(meta);
        watchingProof.add(staff.getUniqueId());

        staff.openBook(book);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onBookClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
                if (!event.getPlayer().getUniqueId().equals(staff.getUniqueId())) return;

                if (watchingProof.remove(staff.getUniqueId())) {
                    Bukkit.getScheduler().runTaskLater(NexusCore.getInstance(), () -> {
                        open(staff, report.getReportID(), reportNum, targetProfile);
                    }, 3L);

                    org.bukkit.event.HandlerList.unregisterAll(this);
                }
            }
        }, NexusCore.getInstance());
    }
}
