import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Java-man
 * @since 03.06.2018
 */
public class RoutePointZChanger
{
    private static final Path dir = Paths.get("P:\\pvp-game\\gameserver\\dist\\data\\npc");
    private static final Path dir2 = Paths.get("P:\\pvp-game\\gameserver\\dist\\data\\npc2");

    private static final Pattern pattern = Pattern.compile("(\\s*<route_point x=\".*\" y=\".*\" z=\")(-?\\d*)(\" .*/>)");

    public static void main(String[] args) throws Exception
    {
        try(Stream<Path> pathStream = Files.list(dir))
        {
            pathStream.filter(file -> !Files.isDirectory(file))
                    .forEach(file ->
                    {
                        try
                        {
                            AtomicBoolean changed = new AtomicBoolean(false);
                            List<String> newContent;
                            try(Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8))
                            {
                                newContent = lines
                                        .map(Line::new)
                                        .map(line ->
                                        {
                                            if(!line.find())
                                                return line.line;

                                            changed.set(true);
                                            String z = line.group(2);
                                            int zzz = Integer.parseInt(z);
                                            return line.matcher.replaceFirst("$1" + (zzz - 16) + "$3");
                                        })
                                        .collect(Collectors.toUnmodifiableList());
                            }
                            if(newContent.isEmpty() || !changed.get())
                                return;
                            Path path = dir2.resolve(file.getFileName());
                            Files.write(path, newContent);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
            );
        }
    }

    static class Line
    {
        String line;
        Matcher matcher;

        public Line(String line)
        {
            this.line = line;
            matcher = pattern.matcher(line);
        }

        boolean find()
        {
            return matcher.find();
        }

        String group(int group)
        {
            return matcher.group(group);
        }
    }
}
