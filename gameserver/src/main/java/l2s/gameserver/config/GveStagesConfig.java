package l2s.gameserver.config;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Converter;
import org.aeonbits.owner.Reloadable;

/**
 * @author KRonst
 */
@Sources("file:config/gve_stages.properties")
public interface GveStagesConfig extends Reloadable {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Key("GveStages")
    @Separator(";")
    @ConverterClass(StageConverter.class)
    List<GveStage> gveStages();

    @Key("AllowedMultisellsByStage")
    @ConverterClass(IdsByStageConverter.class)
    Map<Integer, List<Integer>> allowedMultisellsByStage();

    @Key("AllowedDropItemsByStage")
    @ConverterClass(IdsByStageConverter.class)
    Map<Integer, List<Integer>> allowedDropItemsByStage();

    class StageConverter implements Converter<GveStage> {

        @Override
        public GveStage convert(Method method, String input) {
            String[] data = input.split(",");
            int id = Integer.parseInt(data[0]);
            LocalDateTime start = LocalDateTime.parse(data[1], FORMATTER);
            return new GveStage(id, start);
        }
    }

    class IdsByStageConverter implements Converter<Map<Integer, List<Integer>>> {

        @Override
        public Map<Integer, List<Integer>> convert(Method method, String input) {
            Map<Integer, List<Integer>> map = new HashMap<>();

            final String[] stages = input.split(";");
            for (String stage : stages) {
                final String[] stageData = stage.split(":");
                int id = Integer.parseInt(stageData[0]);
                final String[] arr = stageData[1].split(",");

                List<Integer> ids = new ArrayList<>();
                for (String s : arr) {
                    ids.add(Integer.parseInt(s));
                }
                map.put(id, ids);
            }

            return map;
        }
    }

    class GveStage {
        private final int id;
        private final LocalDateTime start;
        private final String startDate;

        public GveStage(int id, LocalDateTime start) {
            this.id = id;
            this.start = start;
            this.startDate = start.format(FORMATTER);
        }

        public int getId() {
            return id;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public String getStartDate() {
            return startDate;
        }
    }
}
