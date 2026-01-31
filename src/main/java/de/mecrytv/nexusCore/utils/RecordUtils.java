package de.mecrytv.nexusCore.utils;

import de.mecrytv.nexusCore.enums.PunishTypes;

public class RecordUtils {

    public record PunishmentStep(int level, PunishTypes type, long duration) {}
    public record HistoryEntry(String type, String reason, long timestamp, String staff, long expiry, String reportId) {}
}
