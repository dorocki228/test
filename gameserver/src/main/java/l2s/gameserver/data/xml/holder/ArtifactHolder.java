package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.artifact.ArtifactTemplate;

import java.util.HashMap;
import java.util.Map;

public class ArtifactHolder extends AbstractHolder {
    private static ArtifactHolder INSTANCE = new ArtifactHolder();
    private final Map<Integer, ArtifactTemplate> templateMap = new HashMap<>();

    private ArtifactHolder() {
    }

    public static ArtifactHolder getInstance() {
        return INSTANCE;
    }

    public void add(ArtifactTemplate template) {
        templateMap.put(template.getId(), template);
    }

    public ArtifactTemplate get(int id) {
        return templateMap.get(id);
    }

    public Map<Integer, ArtifactTemplate> getTemplateMap() {
        return templateMap;
    }

    @Override
    public int size() {
        return templateMap.size();
    }

    @Override
    public void clear() {
        templateMap.clear();
    }
}
