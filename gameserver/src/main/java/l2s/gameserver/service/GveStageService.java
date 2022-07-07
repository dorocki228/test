package l2s.gameserver.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;
import l2s.gameserver.Config;
import l2s.gameserver.config.GveStagesConfig.GveStage;

/**
 * @author KRonst
 */
public class GveStageService {

    private static final GveStageService INSTANCE = new GveStageService();

    private GveStageService() {

    }

    public static GveStageService getInstance() {
        return INSTANCE;
    }

    public boolean isMultisellAllowed(int multisellId) {
        Entry<Integer, List<Integer>> multisellStage = getMultisellStage(multisellId);
        if (multisellStage == null) {
            return true;
        }

        GveStage currentStage = getCurrentStage();
        if (currentStage == null) {
            return false;
        }

        return currentStage.getId() >= multisellStage.getKey();
    }

    public boolean isDropItemAllowed(int itemId) {
        Entry<Integer, List<Integer>> dropStage = getDropStage(itemId);
        if (dropStage == null) {
            return true;
        }

        GveStage currentStage = getCurrentStage();
        if (currentStage == null) {
            return true;
        }

        return currentStage.getId() >= dropStage.getKey();
    }

    public GveStage getMultisellStageInfo(int multisellId) {
        Entry<Integer, List<Integer>> multisellStage = getMultisellStage(multisellId);
        if (multisellStage == null) {
            return null;
        }

        return Config.GVE_STAGES.gveStages().stream()
            .filter(s -> s.getId() == multisellStage.getKey())
            .findFirst()
            .orElse(null);
    }

    public GveStage getStageInfoById(int id) {
        return Config.GVE_STAGES.gveStages().stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    public int getCurrentStageId() {
        GveStage stage = getCurrentStage();
        if (stage == null) {
            return 0;
        } else {
            return stage.getId();
        }
    }

    private GveStage getCurrentStage() {
        LocalDateTime now = LocalDateTime.now();
        GveStage current = null;

        for (GveStage gveStage : Config.GVE_STAGES.gveStages()) {
            if (current == null) {
                if (gveStage.getStart().isBefore(now)) {
                    current = gveStage;
                }
            } else {
                if (gveStage.getId() > current.getId()
                    && gveStage.getStart().isAfter(current.getStart())
                    && gveStage.getStart().isBefore(now)) {
                    current = gveStage;
                }
            }
        }

        return current;
    }

    private Entry<Integer, List<Integer>> getMultisellStage(int multisellId) {
        return Config.GVE_STAGES.allowedMultisellsByStage().entrySet().stream()
            .filter(e -> e.getValue().contains(multisellId))
            .findFirst()
            .orElse(null);
    }

    private Entry<Integer, List<Integer>> getDropStage(int itemId) {
        return Config.GVE_STAGES.allowedDropItemsByStage().entrySet().stream()
            .filter(e -> e.getValue().contains(itemId))
            .findFirst()
            .orElse(null);
    }
}
