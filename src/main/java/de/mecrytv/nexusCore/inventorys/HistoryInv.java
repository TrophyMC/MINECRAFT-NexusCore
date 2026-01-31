package de.mecrytv.nexusCore.inventorys;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.enums.FilterType;
import de.mecrytv.nexusCore.utils.*;
import de.mecrytv.nexusapi.models.BanModel;
import de.mecrytv.nexusapi.models.MuteModel;
import de.mecrytv.nexusapi.models.WarnModel;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.*;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HistoryInv {
    private final HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();

    public void open(Player player, PlayerProfile targetProfile) {
        String targetUUID = targetProfile.getId().toString();

        CompletableFuture<List<BanModel>> bansFuture = DatabaseAPI.<BanModel>getList("ban","targetUUID", targetUUID);
        CompletableFuture<List<MuteModel>> mutesFuture = DatabaseAPI.<MuteModel>getList("mute","targetUUID", targetUUID);
        CompletableFuture<List<WarnModel>> warnsFuture = DatabaseAPI.<WarnModel>getList("warn","targetUUID", targetUUID);

        CompletableFuture.allOf(bansFuture, mutesFuture, warnsFuture).thenAccept(v -> {
            List<RecordUtils.HistoryEntry> allEntries = new ArrayList<>();
            bansFuture.join().forEach(m -> allEntries.add(new RecordUtils.HistoryEntry("ban", m.getReason(), m.getBanTimestamp(), m.getStaffName(), m.getBanExpires(), m.getReportID())));
            mutesFuture.join().forEach(m -> allEntries.add(new RecordUtils.HistoryEntry("mute", m.getReason(), m.getMuteTimestamp(), m.getStaffName(), m.getMuteExpires(), m.getReportID())));
            warnsFuture.join().forEach(m -> allEntries.add(new RecordUtils.HistoryEntry("warn", m.getReason(), m.getWarnTimestamp(), m.getStaffName(), 0, m.getReportID())));

            allEntries.sort(Comparator.comparing(RecordUtils.HistoryEntry::timestamp).reversed());

            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> renderGUI(player, targetProfile, allEntries, FilterType.ALL));
        });
    }

    private void renderGUI(Player player, PlayerProfile targetProfile, List<RecordUtils.HistoryEntry> allEntries, FilterType activeFilter){
        ScrollingGui gui = Gui.scrolling()
                .scrollType(ScrollType.VERTICAL)
                .title(TranslationUtils.getGUITranslation(player, "gui.history.title"))
                .rows(6)
                .pageSize(21)
                .disableAllInteractions()
                .create();

        ItemStack borderStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        borderStack.editMeta(m -> m.displayName(Component.empty()));
        gui.getFiller().fillBorder(ItemBuilder.from(borderStack).asGuiItem());

        staticItems(gui, player, targetProfile, allEntries, activeFilter);

        int index = allEntries.size();
        for (RecordUtils.HistoryEntry entry : allEntries) {
            String filterName = activeFilter.name().toLowerCase();
            String entryType = entry.type().toLowerCase();

            if (activeFilter == FilterType.ALL || filterName.startsWith(entryType) || entryType.startsWith(filterName)) {
                gui.addItem(createHistoryItem(player, entry, index));
            }
            index--;
        }
        gui.open(player);
    }

    private GuiItem createHistoryItem(Player player, RecordUtils.HistoryEntry entry, int index) {
        String type = entry.type().toLowerCase();

        String typeColor = MiniMessage.miniMessage().serialize(TranslationUtils.getGUITranslation(player, "gui.history.item.types." + type + ".color"));
        String typeName = MiniMessage.miniMessage().serialize(TranslationUtils.getGUITranslation(player, "gui.history.item.types." + type + ".name"));

        String statusKey = type.equals("warn") ? "gui.history.item.status.noted" :
                (entry.expiry() == -1 || entry.expiry() > System.currentTimeMillis() ? "gui.history.item.status.active" : "gui.history.item.status.expired");

        String statusText = MiniMessage.miniMessage().serialize(TranslationUtils.getGUITranslation(player, statusKey));
        String duration = type.equals("warn") ? "---" : (entry.expiry() == -1 ? "Permanent" : TimeUtils.formatDuration(entry.expiry() - entry.timestamp()));
        String timeLabel = type.equals("warn") ? "Datum:" : "Beginn:";

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        item.editMeta(meta -> {
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.item.displayname",
                    "{type_color}", typeColor,
                    "{number}", String.valueOf(index),
                    "{caseID}", entry.reportId()));

            List<Component> finalLore = new ArrayList<>();
            finalLore.add(Component.empty());

            List<Component> configLore = TranslationUtils.getGUILoreTranslation(player, "gui.history.item.lore",
                    "{type_color}", typeColor,
                    "{type_name}", typeName,
                    "{reason}", entry.reason().toUpperCase(),
                    "{staff}", entry.staff(),
                    "{time_label}", timeLabel,
                    "{time}", TimeUtils.formatTimestamp(entry.timestamp()),
                    "{duration}", duration,
                    "{status_text}", statusText);

            for (Component line : configLore) {
                String plain = MiniMessage.miniMessage().serialize(line);
                if (plain.toLowerCase().contains("original") || (finalLore.size() == 1 && plain.isEmpty())) continue;
                finalLore.add(line);
            }

            meta.lore(finalLore);
        });
        return ItemBuilder.from(item).asGuiItem();
    }

    private void staticItems(ScrollingGui gui, Player player, PlayerProfile targetProfile, List<RecordUtils.HistoryEntry> allEntries, FilterType activeFilter) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, m -> {
            m.setPlayerProfile(targetProfile);
            m.displayName(TranslationUtils.getGUITranslation(player, "gui.history.head.name"));
            m.lore(TranslationUtils.getGUILoreTranslation(player, "gui.history.head.lore", "{target}", targetProfile.getName()));
        });
        gui.setItem(1, 5, ItemBuilder.from(head).asGuiItem());

        setupNavigation(gui, player, targetProfile, allEntries, activeFilter);
    }

    private void setupNavigation(ScrollingGui gui, Player player, PlayerProfile targetProfile, List<RecordUtils.HistoryEntry> allEntries, FilterType activeFilter) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        glass.editMeta(m -> m.displayName(Component.empty()));
        GuiItem glassItem = ItemBuilder.from(glass).asGuiItem();

        for (int i = 36; i <= 44; i++) {
            gui.setItem(i, glassItem);
        }

        ItemStack filter = new ItemStack(Material.HOPPER);
        filter.editMeta(m -> {
            m.displayName(TranslationUtils.getGUITranslation(player, "gui.history.filter.name"));
            List<Component> lore = new ArrayList<>();
            for (FilterType type : FilterType.values()) {
                String p = (type == activeFilter) ? "§a§l» " : "§8  ";
                lore.add(Component.text(p + (type == activeFilter ? "§f" : "§7") + type.name()));
            }
            m.lore(lore);
        });

        gui.setItem(40, ItemBuilder.from(filter).asGuiItem(e -> {
            FilterType next = FilterType.values()[(activeFilter.ordinal() + 1) % FilterType.values().length];
            renderGUI(player, targetProfile, allEntries, next);
        }));

        ItemStack prev = headDatabaseAPI.getItemHead("10786");
        if (prev != null) {
            prev.editMeta(m -> m.displayName(TranslationUtils.getGUITranslation(player, "gui.history.previous_page")));
            gui.setItem(48, ItemBuilder.from(prev).asGuiItem(e -> gui.previous()));
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        close.editMeta(m -> m.displayName(TranslationUtils.getGUITranslation(player, "gui.history.close")));
        gui.setItem(49, ItemBuilder.from(close).asGuiItem(e -> gui.close(player)));

        ItemStack next = headDatabaseAPI.getItemHead("10783");
        if (next != null) {
            next.editMeta(m -> m.displayName(TranslationUtils.getGUITranslation(player, "gui.history.next_page")));
            gui.setItem(50, ItemBuilder.from(next).asGuiItem(e -> gui.next()));
        }
    }
}