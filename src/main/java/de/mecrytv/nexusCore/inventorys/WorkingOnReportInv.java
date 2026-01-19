package de.mecrytv.nexusCore.inventorys;

import com.destroystokyo.paper.profile.PlayerProfile;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorkingOnReportInv {

    public void open(Player player) {
        DatabaseAPI.<ReportModel>getAll("reports").thenAccept(allReports -> {
            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                Component title = TranslationUtils.getGUITranslation(player, "gui.reports.working");

                ScrollingGui gui = Gui.scrolling()
                        .scrollType(ScrollType.VERTICAL)
                        .title(title)
                        .rows(6)
                        .pageSize(28)
                        .disableAllInteractions()
                        .create();

                ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                border.editMeta(meta -> meta.displayName(Component.empty()));
                gui.getFiller().fillBorder(ItemBuilder.from(border).asGuiItem());

                allReports.sort(Comparator.comparingLong(ReportModel::getReportTime).reversed());

                int reportCounter = 1;
                for (ReportModel report : allReports) {
                    if (!"WORKING_ON".equalsIgnoreCase(report.getState())) continue;
                    gui.addItem(createReportHead(gui, report, player, reportCounter));
                    reportCounter++;
                }

                ItemStack closedItem = new ItemStack(Material.BARRIER);
                closedItem.editMeta(meta -> meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.close")));

                gui.setItem(6, 5, ItemBuilder.from(closedItem).asGuiItem(e -> gui.close(e.getWhoClicked())));
                gui.open(player);
            });
        });
    }

    private GuiItem createReportHead(ScrollingGui gui, ReportModel report, Player player, int reportNumber){
        Component reasonComp = TranslationUtils.getGUITranslation(player, "gui.report.reasons." + report.getReason() + ".name");

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES);
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.reports.heads.title", "{number}", String.valueOf(reportNumber), "{caseID}", report.getReportID()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.target", "{target}", report.getTargetName()));
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.reporter", "{reporter}", report.getReporterName()));
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.reason").replaceText(b -> b.matchLiteral("{reason}").replacement(reasonComp)));
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.time", "{date}", new SimpleDateFormat("dd.MM.yyyy").format(new Date(report.getReportTime())), "{time}", new SimpleDateFormat("HH:mm:ss").format(new Date(report.getReportTime()))));
            lore.add(Component.empty());
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.state", "{state}", report.getState()));
            lore.add(TranslationUtils.getGUITranslation(player, "gui.reports.heads.staff", "{staff}", report.getStaffName()));
            meta.lore(lore);
        });

        GuiItem guiItem = ItemBuilder.from(head).asGuiItem(event -> {
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
                TranslationUtils.sendTranslation(clicker, "messages.report.reports.not_your_report");
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            PlayerProfile profileToPass = null;
            if (clickedItem != null && clickedItem.getItemMeta() instanceof SkullMeta skullMeta) {
                profileToPass = skullMeta.getPlayerProfile();
            }

            clicker.closeInventory();
            new ActionReportInv().open(clicker, report.getReportID(), reportNumber, profileToPass);
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