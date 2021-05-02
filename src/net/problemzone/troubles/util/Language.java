package net.problemzone.troubles.util;

import org.bukkit.ChatColor;

public enum Language {
    GAME_START("Das Spiel startet in: " + ChatColor.WHITE),
    SERVER_CLOSE("Der Server schließt in: " + ChatColor.WHITE),
    NOT_ENOUGH_PLAYERS("Es sind zu wenig Spieler auf dem Server!"),
    ROLE_ASSIGNED("Du bist ein %s" + ChatColor.GRAY +"!"),
    ROLE_WIN("Die %s " + ChatColor.GRAY + "haben gewonnen!"),
    INNOCENT_ROLE(ChatColor.GREEN + "Innocent"),
    DETECTIVE_ROLE(ChatColor.BLUE + "Detective"),
    TRAITOR_ROLE(ChatColor.RED + "Traitor"),
    INNOCENT_DESCRIPTION("Versuche nicht zu sterben!"),
    DETECTIVE_DESCRIPTION("Finde die Mörder!"),
    TRAITOR_DESCIRPTION("Bringe deine Gegner um!");

    private static final String SYSTEM_PREFIX = ChatColor.RED + "Troubles " + ChatColor.DARK_GRAY + "» ";

    private final String text;

    Language(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getFormattedText() {
        return SYSTEM_PREFIX + ChatColor.GRAY + text;
    }
}
