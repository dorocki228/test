package l2s.gameserver.model.base;

import com.google.common.base.Enums;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Language;

import java.util.Arrays;

public enum Fraction {
    NONE(Config.NORMAL_NAME_COLOUR, "ffffff") {
        @Override
        public boolean canAttack(Fraction target) {
            return target != NONE;
        }

        @Override
        public Fraction revert() {
            return NONE;
        }

        @Override
        public String toString() {
            return "None";
        }
    },
    FIRE(Integer.decode("0x0076EE"), "ff2a0e") {
        @Override
        public boolean canAttack(Fraction target) {
            return target != FIRE;
        }

        @Override
        public Fraction revert() {
            return WATER;
        }

        @Override
        public String toString() {
            return "Fire";
        }
    },
    WATER(Integer.decode("0xFF901E"), "3d59ff") {
        @Override
        public boolean canAttack(Fraction target) {
            return target != WATER;
        }

        @Override
        public Fraction revert() {
            return FIRE;
        }

        @Override
        public String toString() {
            return "Water";
        }
    };

    private final int nameColor;
    private final String buttonColor;

    Fraction(int nameColor, String buttonColor) {
        this.nameColor = nameColor;
        this.buttonColor = buttonColor;
    }

    public int getNameColor() {
        return nameColor;
    }

    public String getButtonColor() {
        return buttonColor;
    }

    public static Fraction[] VALUES_WITH_NONE = values();

    public static Fraction[] VALUES = Arrays.copyOfRange(values(), 1, 3);

    public abstract boolean canAttack(Fraction target);
    
    public boolean canAttack(Creature target) {
        return canAttack(target.getFraction());
    }

    public abstract Fraction revert();

    public static Fraction getIfPresent(int ordinal) {
        return Arrays.stream(Fraction.values()).filter(f -> f.ordinal() == ordinal).findFirst().orElse(null);
    }

    public static Fraction getIfPresent(String name) {
        return Enums.getIfPresent(Fraction.class, name).orNull();
    }
    
    public static String getTown(Fraction f, Language lang)
    {
    	switch(lang)
    	{
  			case CHINESE:
  				return f == Fraction.FIRE ? "奥伦镇" : "精灵村";
  			case ENGLISH:
  				return f == Fraction.FIRE ? "Oren Town" : "Elven Village";
  			case RUSSIAN:
  				return f == Fraction.FIRE ? "Город Орен" : "Эльфийская деревня";
  			default:
  				return f == Fraction.FIRE ? "Oren Town" : "Elven Village";
    	}
    }
}
