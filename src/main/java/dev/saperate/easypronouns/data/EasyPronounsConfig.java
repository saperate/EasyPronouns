package dev.saperate.easypronouns.data;

import dev.saperate.easypronouns.EasyPronouns;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = EasyPronouns.MODID)
public class EasyPronounsConfig implements ConfigData {
    private int maxPronouns = 3;
    private int maxPronounSize = 10;
    private boolean displayOnTabList = true;

    public int getMaxPronouns() {
        return maxPronouns;
    }

    public int getMaxPronounSize() {
        return maxPronouns;
    }
    
    public boolean displaysOnTabList(){
        return displayOnTabList;
    }
    
}