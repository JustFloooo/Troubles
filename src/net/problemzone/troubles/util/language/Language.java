package net.problemzone.troubles.util.language;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class Language {

    private static final String SYSTEM_PREFIX = ChatColor.RED + "Troubles " + ChatColor.DARK_GRAY + "» ";

    private static final Map<LanguageKeyword, String> lang = new HashMap<>();

    static {
        lang.put(LanguageKeyword.TITLE_START, ChatColor.RED + "Troubles" + ChatColor.GRAY + " startet in:");
        lang.put(LanguageKeyword.ROLE_ASSIGNED, "Du bist ein %s!");
        lang.put(LanguageKeyword.INNOCENT_ROLE, ChatColor.GREEN + "Unschuldiger");
        lang.put(LanguageKeyword.TRAITOR_ROLE, ChatColor.RED + "Mörder");
        lang.put(LanguageKeyword.DETECTIVE_ROLE, ChatColor.BLUE + "Detektiv");
        lang.put(LanguageKeyword.INNOCENT_DESCRIPTION, "Versuche nicht zu sterben!");
        lang.put(LanguageKeyword.TRAITOR_DESCIRPTION, "Bringe deine Gegner um!");
        lang.put(LanguageKeyword.DETECTIVE_DESCRIPTION, "Finde die Mörder!");
    }

    public static String getStringFromKeyword(LanguageKeyword keyword){
        return format(lang.get(keyword));
    }

    public static String getUnformattedStringFromKeyword(LanguageKeyword keyword) {return lang.get(keyword);}

    private static String format(String unformattedString){
        return SYSTEM_PREFIX + ChatColor.GRAY + unformattedString;
    }

}
