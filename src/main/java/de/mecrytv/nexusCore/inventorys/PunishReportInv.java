package de.mecrytv.nexusCore.inventorys;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.commands.ReportCommand;
import de.mecrytv.nexusCore.models.punish.PunishmentHistoryModel;
import de.mecrytv.nexusCore.utils.GeneralUtils;
import de.mecrytv.nexusCore.utils.TimeUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PunishReportInv {

    public void open(Player player, Player target) {
        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
            Component title = TranslationUtils.getGUITranslation(player, "guid.punsihReport.title", "{target}", target.getName());

            Gui gui = Gui.gui()
                    .title(title)
                    .rows(6)
                    .disableAllInteractions()
                    .create();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(SkullMeta.class, meta -> {
                meta.setPlayerProfile(target.getPlayerProfile());
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.punishReport.head.name", "{target}", target.getName()));
                meta.lore(TranslationUtils.getGUILoreTranslation(player, "gui.punishReport.head.lore"));
            });
            gui.setItem(1, 5, ItemBuilder.from(head).asGuiItem());

            gui.setItem(3, 4, createPunishReportItem(Material.IRON_SWORD, "client_mod", player, target, gui));
            gui.setItem(3, 6, createPunishReportItem(Material.COMMAND_BLOCK, "bug_abuse", player, target, gui));
            gui.setItem(4, 2, createPunishReportItem(Material.GOLDEN_APPLE, "teaming", player, target, gui));
            gui.setItem(4, 3, createPunishReportItem(Material.LAVA_BUCKET, "trolling", player, target, gui));
            gui.setItem(4, 4, createPunishReportItem(Material.CLOCK, "afk_farming", player, target, gui));
            gui.setItem(4, 5, createPunishReportItem(Material.DIAMOND_CHESTPLATE, "stats_pushing", player, target, gui));
            gui.setItem(4, 6, createPunishReportItem(Material.BARRIER, "ban_evasion", player, target, gui));
            gui.setItem(4, 7, createPunishReportItem(Material.PLAYER_HEAD, "skin", player, target, gui));
            gui.setItem(4, 8, createPunishReportItem(Material.NAME_TAG, "name", player, target, gui));

            gui.setItem(5, 2, createPunishReportItem(Material.PAPER, "provocation", player, target, gui));
            gui.setItem(5, 3, createPunishReportItem(Material.PAPER, "insult", player, target, gui));
            gui.setItem(5, 4, createPunishReportItem(Material.PAPER, "chat_spam", player, target, gui));
            gui.setItem(5, 5, createPunishReportItem(Material.PAPER, "server_insult", player, target, gui));
            gui.setItem(5, 6, createPunishReportItem(Material.PAPER, "threat", player, target, gui));
            gui.setItem(5, 7, createPunishReportItem(Material.PAPER, "racism", player, target, gui));
            gui.setItem(5, 8, createPunishReportItem(Material.PAPER, "death_wish", player, target, gui));

            ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            pane.editMeta(meta -> meta.displayName(Component.empty()));
            gui.getFiller().fill(ItemBuilder.from(pane).asGuiItem());

            gui.open(player);
        });
    }

    private GuiItem createPunishReportItem(Material material, String reasonKey, Player player, Player target, Gui gui){
        String baseKey = "gui.report.reasons." + reasonKey;

        Component reasonName = TranslationUtils.getGUITranslation(player, baseKey + ".name");

        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(reasonName);
            meta.lore(TranslationUtils.getGUILoreTranslation(player, baseKey + ".lore"));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        });

        return ItemBuilder.from(item).asGuiItem(event -> {
            Player clicker = (Player) event.getWhoClicked();
            String reportID = GeneralUtils.generateUniqueReportID();

            NexusCore.getInstance().getPunishManager().executePunishment(reportID, reasonKey, target, clicker);

            TranslationUtils.sendTranslation(clicker, "gui.report.report_success",
                    "{target}", target.getName(),
                    "{reason}", MiniMessage.miniMessage().serialize(reasonName));

            GeneralUtils.sendStaffNotification();
            gui.close(clicker);
        });
    }
}
