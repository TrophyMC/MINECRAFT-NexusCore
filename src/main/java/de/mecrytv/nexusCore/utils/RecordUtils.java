package de.mecrytv.nexusCore.utils;

import de.mecrytv.nexusCore.enums.PunishTypes;

public class RecordUtils {

    public record PunishmentStep(int level, PunishTypes type, long duration) {}
}
