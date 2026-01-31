package de.mecrytv.nexusCore.inventorys;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.enums.FilterType;
import de.mecrytv.nexusCore.models.punish.BanModel;
import de.mecrytv.nexusCore.models.punish.MuteModel;
import de.mecrytv.nexusCore.models.punish.WarnModel;
import de.mecrytv.nexusCore.utils.RecordUtils;
import de.mecrytv.nexusCore.utils.TimeUtils;
import de.mecrytv.nexusCore.utils.TranslationUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.ScrollingGui;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

            bansFuture.join().forEach(ban -> allEntries.add(new RecordUtils.HistoryEntry("BAN", ban.getReason(), ban.getBanTimestamp(), ban.getStaffName(), ban.getBanExpires())));
            mutesFuture.join().forEach(mute -> allEntries.add(new RecordUtils.HistoryEntry("MUTE", mute.getReason(), mute.getMuteTimestamp(), mute.getStaffName(), mute.getMuteExpires())));
            warnsFuture.join().forEach(warn -> allEntries.add(new RecordUtils.HistoryEntry("WARN", warn.getReason(), warn.getWarnTimestamp(), warn.getStaffName(), 0)));

            allEntries.sort(Comparator.comparing(RecordUtils.HistoryEntry::timestamp).reversed());

            Bukkit.getScheduler().runTask(NexusCore.getInstance(), () -> {
                renderGUI(player, targetProfile, allEntries, FilterType.ALL);
            });
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

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        border.editMeta(meta -> meta.displayName(Component.empty()));
        gui.getFiller().fillBorder(ItemBuilder.from(border).asGuiItem());

        staticItems(gui, player, targetProfile, allEntries, activeFilter);

        for (RecordUtils.HistoryEntry entry : allEntries) {
            if (activeFilter == FilterType.ALL || entry.type().equalsIgnoreCase(activeFilter.name())) {
                gui.addItem(createHistoryItem(player, entry));
            }
        }

        gui.open(player);
    }

    private void staticItems(ScrollingGui gui, Player player, PlayerProfile targetProfile, List<RecordUtils.HistoryEntry> allEntries, FilterType activeFilter) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.setPlayerProfile(targetProfile);
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.head.name"));
            meta.lore(TranslationUtils.getGUILoreTranslation(player, "gui.history.head.lore",  "{target}", targetProfile.getName()));
        });
        gui.setItem(1, 5, ItemBuilder.from(head).asGuiItem());

        ItemStack filterItem = new ItemStack(Material.HOPPER);
        filterItem.editMeta(meta -> {
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.filter.name"));
            List<Component> lore = new ArrayList<>();
            for (FilterType type : FilterType.values()) {
                String prefix = (type == activeFilter) ? "§a§l» " : "§8  ";
                String color = (type == activeFilter) ? "§f" : "§7";
                lore.add(Component.text(prefix + color + type.name()));
            }
            meta.lore(lore);

            if (activeFilter != FilterType.ALL) meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        });

        gui.setItem(5, 5, ItemBuilder.from(filterItem).asGuiItem(e -> {
            FilterType nextFilter = FilterType.values()[(activeFilter.ordinal() + 1) % FilterType.values().length];
            renderGUI(player, targetProfile, allEntries, nextFilter);
        }));

        setupNavigationAndBorder(gui, player);
    }

    private void setupNavigationAndBorder(ScrollingGui gui, Player player) {
        ItemStack borderStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        borderStack.editMeta(meta -> meta.displayName(Component.empty()));
        GuiItem borderItem = ItemBuilder.from(borderStack).asGuiItem();

        for (int slot = 1; slot <= 9; slot++) {
            if (slot != 5) {
                gui.setItem(5, slot, borderItem);
            }
        }

        ItemStack prev = headDatabaseAPI.getItemHead("10786");
        if (prev != null) {
            prev.editMeta(meta -> meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.previous_page")));
            gui.setItem(6, 4, ItemBuilder.from(prev).asGuiItem(e -> gui.previous()));
        }

        ItemStack closedItem = new ItemStack(Material.BARRIER);
        closedItem.editMeta(meta -> meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.close")));
        gui.setItem(6, 5, ItemBuilder.from(closedItem).asGuiItem(e -> gui.close(player)));

        ItemStack next = headDatabaseAPI.getItemHead("10783");
        if (next != null) {
            next.editMeta(meta -> meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.next_page")));
            gui.setItem(6, 6, ItemBuilder.from(next).asGuiItem(e -> gui.next()));
        }

        gui.setItem(6, 1, borderItem);
        gui.setItem(6, 2, borderItem);
        gui.setItem(6, 3, borderItem);
        gui.setItem(6, 7, borderItem);
        gui.setItem(6, 8, borderItem);
        gui.setItem(6, 9, borderItem);
    }

    private GuiItem createHistoryItem(Player player, RecordUtils.HistoryEntry entry) {
        String typeColor = switch (entry.type().toUpperCase()) {
            case "BAN" -> "<red>";
            case "MUTE" -> "<blue>";
            case "WARN" -> "<gold>";
            default -> "<white>";
        };

        Material material = entry.type().equalsIgnoreCase("WARN") ? Material.BOOK : Material.WRITTEN_BOOK;

        String durationStr;
        String statusKey;
        String statusColorKey;

        if (entry.type().equalsIgnoreCase("WARN")) {
            durationStr = "---";
            statusKey = "gui.history.status.noted";
            statusColorKey = "gui.history.status.noted_color";
        } else {
            if (entry.expiry() == -1) {
                durationStr = "Permanent";
            } else {
                long diff = entry.expiry() - entry.timestamp();
                durationStr = TimeUtils.formatDuration(diff);
            }

            if (entry.expiry() == -1 || entry.expiry() > System.currentTimeMillis()) {
                statusKey = "gui.history.status.active";
                statusColorKey = "gui.history.status.active_color";
            } else {
                statusKey = "gui.history.status.expired";
                statusColorKey = "gui.history.status.expired_color";
            }
        }

        String statusText = PlainTextComponentSerializer.plainText()
                .serialize(TranslationUtils.getGUITranslation(player, statusKey));

        String statusColor = PlainTextComponentSerializer.plainText()
                .serialize(TranslationUtils.getGUITranslation(player, statusColorKey));

        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(TranslationUtils.getGUITranslation(player, "gui.history.item.displayname",
                    "{color}", typeColor));

            meta.lore(TranslationUtils.getGUILoreTranslation(player, "gui.history.item.lore",
                    "{color}", typeColor,
                    "{type}", entry.type(),
                    "{reason}", entry.reason().toUpperCase(),
                    "{staff}", entry.staff(),
                    "{time_label}", entry.type().equalsIgnoreCase("WARN") ? "Date:" : "Start:",
                    "{time}", TimeUtils.formatTimestamp(entry.timestamp()),
                    "{duration}", durationStr,
                    "{status_color}", statusColor,
                    "{status}", statusText
            ));
        });

        return ItemBuilder.from(item).asGuiItem();
    }
}