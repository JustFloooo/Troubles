package net.problemzone.troubles.game;

import net.problemzone.troubles.util.Language;

public enum Role {

    INNOCENT(Language.INNOCENT_ROLE, Language.INNOCENT_DESCRIPTION),
    TRAITOR(Language.TRAITOR_ROLE, Language.TRAITOR_DESCIRPTION),
    DETECTIVE(Language.DETECTIVE_ROLE, Language.DETECTIVE_DESCRIPTION);

    private final Language roleName;
    private final Language roleDescription;

    Role(Language roleName, Language roleDescription) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }

    public Language getRoleName() {
        return roleName;
    }

    public Language getRoleDescription() {
        return roleDescription;
    }
}
