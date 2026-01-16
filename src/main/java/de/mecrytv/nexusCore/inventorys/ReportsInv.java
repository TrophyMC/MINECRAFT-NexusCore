package de.mecrytv.nexusCore.inventorys;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.GeneralUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.ScrollingGui;
import dev.triumphteam.gui.components.ScrollType;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.Comparator;
import java.util.List;

public class ReportsInv {

    private final HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();

    public void open(Player player) {
        String playerUUID = player.getUniqueId().toString();

        DatabaseAPI.getInstance().getGenericAsync(
                "language", "language", "id", "data", playerUUID
        ).thenAccept(json -> {

            String langCode = "en_US";
            if (json != null && json.has("languageCode")) {
                langCode = json.get("languageCode").getAsString();
            }

            final String finalLang = langCode;

            DatabaseAPI.<ReportModel>getAll("reports").thenAccept(allReports -> {
                Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {

                    Component title = TranslationUtils.sendGUITranslation(finalLang, "gui.reports.title");

                    ScrollingGui gui = Gui.scrolling()
                            .scrollType(ScrollType.VERTICAL)
                            .title(title)
                            .rows(6)
                            .pageSize(28)
                            .disableAllInteractions()
                            .create();

                    ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                    borderPane.editMeta(meta -> meta.displayName(Component.empty()));
                    gui.getFiller().fillBorder(ItemBuilder.from(borderPane).asGuiItem());

                    allReports.sort(Comparator.comparingLong(ReportModel::getReportTime).reversed());

                    int reportCounter = 1;
                    for (ReportModel report : allReports) {
                        if (!"OPEN".equalsIgnoreCase(report.getState())) continue;

                        gui.addItem(createReportHead(report, finalLang, reportCounter));
                        reportCounter++;
                    }

                    setupStaticItems(gui, finalLang);
                    gui.open(player);
                });
            }).exceptionally(ex -> {
                TranslationUtils.sendTranslation(player, finalLang, "gui.reports.errors.loading_reports");
                ex.printStackTrace();
                return null;
            });
        }).exceptionally(ex -> {
            TranslationUtils.sendTranslation(player, "en_US", "gui.reports.errors.loading");
            ex.printStackTrace();
            return null;
        });
    }

    private void setupStaticItems(ScrollingGui gui, String langCode) {
        ItemStack workingItem = new ItemStack(Material.CHEST);
        workingItem.editMeta(meta -> meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.working")));
        gui.setItem(1, 5, ItemBuilder.from(workingItem).asGuiItem( event -> {
            Player clicker = (Player) event.getWhoClicked();
            new WorkingOnReportInv().open(clicker);
        }));

        ItemStack closedItem = new ItemStack(Material.BARRIER);
        closedItem.editMeta(meta -> meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.close")));
        gui.setItem(6, 5, ItemBuilder.from(closedItem).asGuiItem(event -> gui.close(event.getWhoClicked())));

        ItemStack prevPage = headDatabaseAPI.getItemHead("10786");
        if (prevPage != null) {
            prevPage.editMeta(meta -> meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.previous_page")));
            gui.setItem(6, 4, ItemBuilder.from(prevPage).asGuiItem(event -> gui.previous()));
        }

        ItemStack nextPage = headDatabaseAPI.getItemHead("10783");
        if (nextPage != null) {
            nextPage.editMeta(meta -> meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.next_page")));
            gui.setItem(6, 6, ItemBuilder.from(nextPage).asGuiItem(event -> gui.next()));
        }
    }

    private GuiItem createReportHead(ReportModel report, String langCode, int reportNumber) {
        UUID targetUUID = UUID.fromString(report.getTargetUUID());
        String targetName = (report.getTargetName() != null) ? report.getTargetName() : "Unknown";

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.title", "{number}", String.valueOf(reportNumber), "{caseID}", report.getReportID()));

            List<Component> lore = new ArrayList<>();
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.target", "{target}", targetName));
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.reporter", "{reporter}", report.getReporterName()));
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.time", "{date}", new SimpleDateFormat("dd.MM.yyyy").format(new Date(report.getReportTime())), "{time}", new SimpleDateFormat("HH:mm:ss").format(new Date(report.getReportTime()))));
            meta.lore(lore);
        });

        NexusCore.getInstance().getSkinCacheManager().getProfile(targetUUID, targetName).thenAccept(profile -> {
            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                head.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(profile));
            });
        });

        return ItemBuilder.from(head).asGuiItem(event -> {
            Player clicker = (Player) event.getWhoClicked();
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            new ClaimReportInv().open(clicker, report.getReportID(), reportNumber, meta.getPlayerProfile());
        });
    }
}