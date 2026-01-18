package de.mecrytv.nexusCore.inventorys;

import de.mecrytv.DatabaseAPI;
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

public class ReportInv {

    public void open(Player player, Player target) {
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

                Component title = TranslationUtils.sendGUITranslation(finalLang, "gui.report.title", "{target}", target.getName());

                Gui gui = Gui.gui()
                        .title(title)
                        .rows(6)
                        .disableAllInteractions()
                        .create();

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                head.editMeta(SkullMeta.class, meta -> {
                    meta.setPlayerProfile(target.getPlayerProfile());
                    meta.displayName(TranslationUtils.sendGUITranslation(finalLang, "gui.report.head.name"));
                    meta.lore(getTranslatedLore(finalLang, "gui.report.head.lore"));
                });
                gui.setItem(1, 5, ItemBuilder.from(head).asGuiItem());

                gui.setItem(3, 4, createReportItem(Material.IRON_SWORD, "client_mod", finalLang, target, gui));
                gui.setItem(3, 6, createReportItem(Material.COMMAND_BLOCK, "bug_abuse", finalLang, target, gui));
                gui.setItem(4, 2, createReportItem(Material.GOLDEN_APPLE, "teaming", finalLang, target, gui));
                gui.setItem(4, 3, createReportItem(Material.LAVA_BUCKET, "trolling", finalLang, target, gui));
                gui.setItem(4, 4, createReportItem(Material.CLOCK, "afk_farming", finalLang, target, gui));
                gui.setItem(4, 5, createReportItem(Material.DIAMOND_CHESTPLATE, "stats_pushing", finalLang, target, gui));
                gui.setItem(4, 6, createReportItem(Material.BARRIER, "ban_evasion", finalLang, target, gui));
                gui.setItem(4, 7, createReportItem(Material.PLAYER_HEAD, "skin", finalLang, target, gui));
                gui.setItem(4, 8, createReportItem(Material.NAME_TAG, "name", finalLang, target, gui));

                gui.setItem(5, 2, createReportItem(Material.PAPER, "provocation", finalLang, target, gui));
                gui.setItem(5, 3, createReportItem(Material.PAPER, "insult", finalLang, target, gui));
                gui.setItem(5, 4, createReportItem(Material.PAPER, "chat_spam", finalLang, target, gui));
                gui.setItem(5, 5, createReportItem(Material.PAPER, "server_insult", finalLang, target, gui));
                gui.setItem(5, 6, createReportItem(Material.PAPER, "threat", finalLang, target, gui));
                gui.setItem(5, 7, createReportItem(Material.PAPER, "racism", finalLang, target, gui));
                gui.setItem(5, 8, createReportItem(Material.PAPER, "death_wish", finalLang, target, gui));

                ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                pane.editMeta(meta -> meta.displayName(Component.empty()));
                gui.getFiller().fill(ItemBuilder.from(pane).asGuiItem());

                gui.open(player);
            });
        });
    }

    private GuiItem createReportItem(Material material, String reasonKey, String langCode, Player target, Gui gui) {
        String baseKey = "gui.report.reasons." + reasonKey;

        Component reasonName = TranslationUtils.sendGUITranslation(langCode, baseKey + ".name");
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

            ReportModel report = new ReportModel();
            report.setReportID(reportID);
            report.setTargetUUID(target.getUniqueId().toString());
            report.setTargetName(target.getName());
            report.setReporterUUID(clicker.getUniqueId().toString());
            report.setReporterName(clicker.getName());
            report.setReportTime(System.currentTimeMillis());
            report.setReason(reasonKey);
            report.setState("OPEN");
            report.setStaffUUID("none");
            report.setStaffName("none");

            DatabaseAPI.set("reports", report);

            String reasonStr = MiniMessage.miniMessage().serialize(reasonName);

            TranslationUtils.sendTranslation(clicker, langCode, "messages.report.report_success",
                    "{target}", target.getName(),
                    "{reason}", reasonStr
            );

            ReportCommand.setCooldown(clicker.getUniqueId(), target.getUniqueId());
            GeneralUtils.sendStaffNotification();
            gui.close(clicker);
        });
    }

    private List<Component> getTranslatedLore(String langCode, String configKey) {
        NexusCore plugin = NexusCore.getInstance();
        String message = plugin.getLanguageAPI().getTranslation(langCode, configKey);

        if ((message == null || message.isEmpty()) && !langCode.equals("en_US")) {
            message = plugin.getLanguageAPI().getTranslation("en_US", configKey);
        }

        if (message == null) message = configKey;

        List<Component> loreComponents = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();

        String[] lines = message.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                loreComponents.add(mm.deserialize(line));
            }
        }

        return loreComponents;
    }
}