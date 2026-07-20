package dev.piny.easyPronouns;

public enum Flags {
    LESBIAN("\uE000"),
    GAY("\uE001"),
    BISEXUAL("\uE002"),
    TRANSGENDER("\uE003"),
    QUEER("\uE004"),
    INTERSEX("\uE005"),
    ASEXUAL("\uE006"),
    AROMANTIC("\uE007"),
    PROGRESS("\uE008"),
    NONBINARY("\uE009"),
    PANSEXUAL("\uE00A"),
    AGENDER("\uE00B"),
    POLYSEXUAL("\uE00C"),
    AROACE("\uE00D"),
    DEMIGIRL("\uE00E"),
    DEMIBOY("\uE00F"),
    GENDERQUEER("\uE010"),
    GENDERFLUID("\uE011"),
    TRANSMASC("\uE012"),
    TRANSFEM("\uE013"),
    OMNISEXUAL("\uE014");

    private final String unicode;
    Flags(String unicode) {
        this.unicode = unicode;
    }

    public String getUnicode() {
        return unicode;
    }
    public String getName() {
        return this.name().toLowerCase();
    }
}
