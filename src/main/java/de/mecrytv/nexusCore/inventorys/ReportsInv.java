package de.mecrytv.nexusCore.inventorys;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.ScrollingGui;
import dev.triumphteam.gui.components.ScrollType;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        DatabaseAPI.<ReportModel>getAll("reports").thenAccept(allReports -> {
            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                Component title = TranslationUtils.getGUITranslation(player, "gui.reports.title");

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
                    gui.addItem(createReportHead(gui, report, player, reportCounter));
                    reportCounter++;
                }

                setupStaticItems(gui, player);
                gui.open(player);
            });
        });
    }

    private void setupStaticItems(ScrollingGui gui, Player player) {
        ItemStack workingItem = new ItemStack(Material.CHEST);
        workingItem.editMeta(meta -> {
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.working"));
        });

        gui.setItem(1, 5, ItemBuilder.from(workingItem).asGuiItem(event -> {
            Player clicker = (Player) event.getWhoClicked();
            new WorkingOnReportInv().open(clicker);
        }));

        ItemStack closedItem = new ItemStack(Material.BARRIER);
        closedItem.editMeta(meta -> {
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.close"));
        });

        gui.setItem(6, 5, ItemBuilder.from(closedItem).asGuiItem(event -> gui.close(event.getWhoClicked())));

        ItemStack prev = headDatabaseAPI.getItemHead("10786");
        if (prev != null) {
            prev.editMeta(meta -> meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.previous_page")));
            gui.setItem(6, 4, ItemBuilder.from(prev).asGuiItem(e -> gui.previous()));
        }

        ItemStack next = headDatabaseAPI.getItemHead("10783");
        if (next != null) {
            next.editMeta(meta -> meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.next_page")));
            gui.setItem(6, 6, ItemBuilder.from(next).asGuiItem(e -> gui.next()));
        }
    }

    private GuiItem createReportHead(ScrollingGui gui, ReportModel report, Player player, int reportNumber) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.heads.title", "{number}", String.valueOf(reportNumber), "{caseID}", report.getReportID()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.target", "{target}", report.getTargetName()));
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.reporter", "{reporter}", report.getReporterName()));
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.time", "{date}", new SimpleDateFormat("dd.MM.yyyy").format(new Date(report.getReportTime())), "{time}", new SimpleDateFormat("HH:mm:ss").format(new Date(report.getReportTime()))));
            lore.add(Component.empty());
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.state", "{state}", report.getState()));
            meta.lore(lore);
        });

        GuiItem guiItem = ItemBuilder.from(head).asGuiItem(event -> {
            new ClaimReportInv().open(player, report.getReportID(), reportNumber, ((SkullMeta) head.getItemMeta()).getPlayerProfile());
        });

        NexusCore.getInstance().getSkinCacheManager().getProfile(UUID.fromString(report.getTargetUUID()), report.getTargetName()).thenAccept(profile -> {
            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                head.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(profile));
                guiItem.setItemStack(head);
                gui.update();
            });
        });

        return guiItem;
    }
}