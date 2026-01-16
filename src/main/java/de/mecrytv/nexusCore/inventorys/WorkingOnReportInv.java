package de.mecrytv.nexusCore.inventorys;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.ScrollingGui;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorkingOnReportInv {

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

                    Component title = TranslationUtils.sendGUITranslation(finalLang, "gui.reports.working");

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
                        if (!"WORKING_ON".equalsIgnoreCase(report.getState())) continue;

                        gui.addItem(createReportHead(report, finalLang, reportCounter));
                        reportCounter++;
                    }

                    gui.open(player);
                });
            });
        });
    }

    private GuiItem createReportHead(ReportModel report, String langCode, int reportNumber){
        String targetName = (report.getTargetName() != null) ? report.getTargetName() : "Unknown";
        String reporterName = (report.getReporterName() != null) ? report.getReporterName() : "Unknown";
        UUID targetUUID = UUID.fromString(report.getTargetUUID());

        Component reasonComp = TranslationUtils.sendGUITranslation(langCode, "gui.report.reasons." + report.getReason() + ".name");

        SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
        Date dateObj = new Date(report.getReportTime());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES);

            meta.displayName(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.title",
                    "{number}", String.valueOf(reportNumber),
                    "{caseID}", report.getReportID()
            ));

            List<Component> lore = new ArrayList<>();
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.target", "{target}", targetName));
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.reporter", "{reporter}", reporterName));
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.reason")
                    .replaceText(builder -> builder.matchLiteral("{reason}").replacement(reasonComp)));
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.time",
                    "{date}", dateFmt.format(dateObj),
                    "{time}", timeFmt.format(dateObj)
            ));
            lore.add(Component.empty());
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.state", "{state}", report.getState()));
            lore.add(TranslationUtils.sendGUITranslation(langCode, "gui.reports.heads.staff", "{staff}", report.getStaffName()));

            meta.lore(lore);
        });

        NexusCore.getInstance().getSkinCacheManager().getProfile(targetUUID, targetName).thenAccept(profile -> {
            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                head.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(profile));
            });
        });

        return ItemBuilder.from(head).asGuiItem(event -> {
            Player clicker = (Player) event.getWhoClicked();
            String clickerUUID = clicker.getUniqueId().toString();
            String staffUUID = (report.getStaffUUID() != null) ? report.getStaffUUID() : "";

            boolean isOwner = clickerUUID.equals(staffUUID);
            boolean hasBypassPerms = false;

            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(clicker.getUniqueId());
            if (user != null) {
                int maxWeight = user.getNodes().stream()
                        .filter(node -> node instanceof InheritanceNode)
                        .map(node -> (InheritanceNode) node)
                        .map(node -> luckPerms.getGroupManager().getGroup(node.getGroupName()))
                        .filter(Objects::nonNull)
                        .mapToInt(group -> group.getWeight().orElse(0))
                        .max()
                        .orElse(0);

                if (maxWeight >= 960 || maxWeight == 920) {
                    hasBypassPerms = true;
                }
            }

            if (!isOwner && !hasBypassPerms) {
                clicker.closeInventory();
                TranslationUtils.sendTranslation(clicker, langCode, "messages.reports.not_your_report");
                return;
            }

            // TODO: Open Detail Inventory
            clicker.closeInventory();
        });
    }
}
