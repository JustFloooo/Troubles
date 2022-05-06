package net.problemzone.troubles.util;

import org.bukkit.ChatColor;

public enum Language {

    GAME_START("Das Spiel startet in: " + ChatColor.YELLOW + "%d" + ChatColor.GRAY + " Sekunden"),
    GAME_START_TITLE(ChatColor.RED + "Troubles " + ChatColor.GRAY + "startet in: " + ChatColor.WHITE),
    GAME_START_CANCEL("Der Spielstart wurde manuell " + ChatColor.RED + "abgebrochen"),
    WARM_UP("Die Schutzzeit endet in " + ChatColor.YELLOW + "%d" + ChatColor.GRAY + " Sekunden"),
    SERVER_CLOSE(ChatColor.RED + "Der Server schließt in: " + ChatColor.YELLOW + "%d" + ChatColor.RED + " Sekunden"),
    NOT_ENOUGH_PLAYERS(ChatColor.RED + "Troubles benötigt mehr Spieler!"),
    PLAYER_JOIN(ChatColor.GREEN + "» " + ChatColor.WHITE),
    PLAYER_LEAVE(ChatColor.RED + "« " + ChatColor.WHITE),
    PLAYERS_NEEDED("Es werden noch " + ChatColor.RED + "%d" + ChatColor.GRAY + " weitere Spieler benötigt!"),
    PLAYERS_NEEDED_ONE("Es wird noch " + ChatColor.RED + "1" + ChatColor.GRAY + " weiterer Spieler benötigt!"),
    ROLE_ASSIGNED("Du bist ein %s" + ChatColor.GRAY + "!"),
    ROLE_WIN("Die %s " + ChatColor.GRAY + "haben gewonnen!"),
    INNOCENT_ROLE(ChatColor.GREEN + "Innocent"),
    DETECTIVE_ROLE(ChatColor.BLUE + "Detective"),
    TRAITOR_ROLE(ChatColor.RED + "Traitor"),
    INNOCENT_DESCRIPTION("Versuche nicht zu sterben!"),
    DETECTIVE_DESCRIPTION("Finde die Mörder!"),
    TRAITOR_DESCIRPTION("Bringe deine Gegner um!"),
    TESTER(ChatColor.WHITE + "%s" + ChatColor.GRAY + " hat den Tester betreten!"),
    KILLER_SLAIN("Du hast " + ChatColor.WHITE + "%s" + ChatColor.GRAY + " abgeschlachtet"),
    KILLER_SHOT("Du hast " + ChatColor.WHITE + "%s" + ChatColor.GRAY + " erschossen"),
    KILLER_FIST("Du hast " + ChatColor.WHITE + "%s" + ChatColor.GRAY + " rektal bis zum Tode gefistet"),
    VICTIM_SLAIN("Du wurdest von " + ChatColor.WHITE + "%s" + ChatColor.GRAY + " umgebracht"),
    VICTIM_SHOT("Du wurdest von " + ChatColor.WHITE + "%s" + ChatColor.GRAY + " erschossen"),
    IDENTIFY_CORPSE("Du hast die Leiche von " + ChatColor.WHITE + "%s" + ChatColor.GRAY + " identifiziert."),
    IDENTIFY_ROLE(ChatColor.WHITE + "%s" + ChatColor.GRAY + " war ein " + ChatColor.YELLOW + "%s"),
    SPECTATOR_MESSAGE("%s : %s");

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
