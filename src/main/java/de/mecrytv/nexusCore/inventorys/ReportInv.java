package de.mecrytv.nexusCore.inventorys;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.languageapi.profile.ILanguageProfile;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.commands.ReportCommand;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.utils.GeneralUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportInv {

    public void open(Player player, Player target) {
        String langCode = NexusCore.getInstance().getLanguageAPI()
                .getProfile(player.getUniqueId(), "en_US").getLanguageCode();

        Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {

            Component title = TranslationUtils.getGUITranslation(player, "gui.report.title", "{target}", target.getName());

            Gui gui = Gui.gui()
                    .title(title)
                    .rows(6)
                    .disableAllInteractions()
                    .create();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(SkullMeta.class, meta -> {
                meta.setPlayerProfile(target.getPlayerProfile());
                meta.displayName(TranslationUtils.getGUITranslation(player, "gui.report.head.name"));
                meta.lore(getTranslatedLore(langCode, "gui.report.head.lore"));
            });
            gui.setItem(1, 5, ItemBuilder.from(head).asGuiItem());

            gui.setItem(3, 4, createReportItem(Material.IRON_SWORD, "client_mod", player, target, gui));
            gui.setItem(3, 6, createReportItem(Material.COMMAND_BLOCK, "bug_abuse", player, target, gui));
            gui.setItem(4, 2, createReportItem(Material.GOLDEN_APPLE, "teaming", player, target, gui));
            gui.setItem(4, 3, createReportItem(Material.LAVA_BUCKET, "trolling", player, target, gui));
            gui.setItem(4, 4, createReportItem(Material.CLOCK, "afk_farming", player, target, gui));
            gui.setItem(4, 5, createReportItem(Material.DIAMOND_CHESTPLATE, "stats_pushing", player, target, gui));
            gui.setItem(4, 6, createReportItem(Material.BARRIER, "ban_evasion", player, target, gui));
            gui.setItem(4, 7, createReportItem(Material.PLAYER_HEAD, "skin", player, target, gui));
            gui.setItem(4, 8, createReportItem(Material.NAME_TAG, "name", player, target, gui));

            gui.setItem(5, 2, createReportItem(Material.PAPER, "provocation", player, target, gui));
            gui.setItem(5, 3, createReportItem(Material.PAPER, "insult", player, target, gui));
            gui.setItem(5, 4, createReportItem(Material.PAPER, "chat_spam", player, target, gui));
            gui.setItem(5, 5, createReportItem(Material.PAPER, "server_insult", player, target, gui));
            gui.setItem(5, 6, createReportItem(Material.PAPER, "threat", player, target, gui));
            gui.setItem(5, 7, createReportItem(Material.PAPER, "racism", player, target, gui));
            gui.setItem(5, 8, createReportItem(Material.PAPER, "death_wish", player, target, gui));

            ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            pane.editMeta(meta -> meta.displayName(Component.empty()));
            gui.getFiller().fill(ItemBuilder.from(pane).asGuiItem());

            gui.open(player);
        });
    }

    private GuiItem createReportItem(Material material, String reasonKey, Player player, Player target, Gui gui) {
        String baseKey = "report.reasons." + reasonKey;
        String langCode = NexusCore.getInstance().getLanguageAPI().getProfile(player.getUniqueId(), "en_US").getLanguageCode();

        Component reasonName = TranslationUtils.getGUITranslation(player, baseKey + ".name");
        List<Component> loreLines = getTranslatedLore(langCode, baseKey + ".lore");

        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(reasonName);
            meta.lore(loreLines);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        });

        return ItemBuilder.from(item).asGuiItem(event -> {
            Player clicker = (Player) event.getWhoClicked();
            String reportID = GeneralUtils.generateUniqueReportID();

            ReportModel report = new ReportModel(
                    target.getUniqueId().toString(),
                    target.getName(),
                    clicker.getUniqueId().toString(),
                    clicker.getName(),
                    reasonKey
            );

            report.setReportID(reportID);
            DatabaseAPI.set("reports", report);

            TranslationUtils.sendTranslation(clicker, "report.report_success",
                    "{target}", target.getName(),
                    "{reason}", MiniMessage.miniMessage().serialize(reasonName));

            ReportCommand.setCooldown(clicker.getUniqueId(), target.getUniqueId());
            GeneralUtils.sendStaffNotification();
            gui.close(clicker);
        });
    }

    private List<Component> getTranslatedLore(String langCode, String configKey) {
        String message = NexusCore.getInstance().getLanguageAPI().getTranslation(langCode, configKey);
        if (message.contains("Missing Lang") && !langCode.equals("en_US")) {
            message = NexusCore.getInstance().getLanguageAPI().getTranslation("en_US", configKey);
        }

        List<Component> loreComponents = new ArrayList<>();
        for (String line : message.split("\n")) {
            if (!line.isEmpty()) loreComponents.add(MiniMessage.miniMessage().deserialize(line));
        }
        return loreComponents;
    }
}