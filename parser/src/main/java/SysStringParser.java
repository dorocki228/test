import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Java-man
 * @since 19.11.2017
 */
public class SysStringParser {
    private static final Pattern COMPILE1 = Pattern.compile("a,");
    private static final Pattern COMPILE21 = Pattern.compile("u,");
    private static final Pattern COMPILE2 = Pattern.compile("\\\\0");
    private static final Pattern COMPILE3 = Pattern.compile(" ");
    private static final Pattern COMPILE4 = Pattern.compile("\\.");
    private static final Pattern COMPILE5 = Pattern.compile("/");
    private static final Pattern COMPILE6 = Pattern.compile("@");
    private static final Pattern COMPILE7 = Pattern.compile("#");
    private static final Pattern COMPILE8 = Pattern.compile("\\$");
    private static final Pattern COMPILE9 = Pattern.compile("\\*");
    private static final Pattern COMPILE10 = Pattern.compile("\\+");
    private static final Pattern COMPILE11 = Pattern.compile("\\(");
    private static final Pattern COMPILE12 = Pattern.compile("\\)");
    private static final Pattern COMPILE13 = Pattern.compile("'");
    private static final Pattern COMPILE14 = Pattern.compile("&");
    private static final Pattern COMPILE15 = Pattern.compile("<");
    private static final Pattern COMPILE16 = Pattern.compile(">");
    private static final Pattern COMPILE17 = Pattern.compile("-");
    private static final Pattern COMPILE18 = Pattern.compile(":");
    private static final Pattern COMPILE19 = Pattern.compile(",");
    private static final Pattern COMPILE20 = Pattern.compile("!");
    private static final Pattern COMPILE22 = Pattern.compile("’");

    private static Multiset<String> names = HashMultiset.create();

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("P:/sorin/interlude/trunk/parse/sysstring-e.txt");
        List<String> strings = Files.readAllLines(path);
        List<Line> lines = strings.stream().skip(1)
                .map(line -> new Line(line.split("\t")))
                .filter(line -> !line.isFiltred())
                .map(line -> line.replaceFirst(COMPILE1, ""))
                .map(line -> line.replaceFirst(COMPILE21, ""))
                .map(line -> line.replaceFirst(COMPILE2, ""))
                .map(line -> line.replaceAll(COMPILE8, "_"))
                .map(Line::saveOriginalName)
                .map(line -> line.replaceAll(COMPILE3, "_"))
                .map(line -> line.replaceAll(COMPILE4, ""))
                .filter(Line::nameNotEmpty)
                .map(Line::removeDiggitsInBegginig)
                .map(line -> line.replaceAll(COMPILE5, "_"))
                .map(line -> line.replaceAll(COMPILE6, "_"))
                .map(line -> line.replaceAll(COMPILE7, "_"))
                .map(line -> line.replaceAll(COMPILE9, "_"))
                .map(line -> line.replaceAll(COMPILE10, "_"))
                .map(line -> line.replaceAll(COMPILE11, "_"))
                .map(line -> line.replaceAll(COMPILE12, "_"))
                .map(line -> line.replaceAll(COMPILE14, "_"))
                .map(line -> line.replaceAll(COMPILE15, "_"))
                .map(line -> line.replaceAll(COMPILE16, "_"))
                .map(line -> line.replaceAll(COMPILE17, "_"))
                .map(line -> line.replaceAll(COMPILE18, "_"))
                .map(line -> line.replaceAll(COMPILE19, "_"))
                .map(line -> line.replaceAll(COMPILE20, "_"))
                .map(line -> line.replaceAll(COMPILE13, ""))
                .map(line -> line.replaceAll(COMPILE22, ""))
                .filter(line -> !line.isFiltred2())
                .map(Line::toUpperCase)
                .map(Line::checkExistence)
                .sorted(Comparator.comparingInt(Line::getId))
                .collect(Collectors.toList());

        TypeSpec typeSpec = writeClass(lines);

        Path savePath = Paths.get("P:/sorin/interlude/trunk/parse/SysString.java");
        Files.delete(savePath);
        Files.write(savePath, typeSpec.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private static TypeSpec writeClass(List<Line> lines) {
        TypeSpec.Builder builder = TypeSpec.enumBuilder("SysString")
                .addModifiers(Modifier.PUBLIC)
                .addField(int.class, "id", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(int.class, "id")
                        .addStatement("this.$N = $N", "id", "id")
                        .build());

        lines.forEach(line -> {
            try {
                builder.addEnumConstant(line.getName(),
                        TypeSpec.anonymousClassBuilder("$L", line.getId())
                                .addJavadoc("Text: " + line.getOriginalName() + '\n')
                                .build());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("not a valid enum constant: "
                        + line.getId() + ' ' + line.getName() + ' ' + line.getOriginalName(), e);
            }
        });

        return builder.build();
    }

    private static class Line {
        private final int id;
        private String originalName;
        private String name;

        Line(String[] name) {
            this.id = Integer.parseInt(name[0]);
            this.name = name[1];
        }

        public int getId() {
            return id;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getName() {
            return name;
        }

        Line replaceFirst(Pattern pattern, String replacement) {
            name = pattern.matcher(name).replaceFirst(replacement);
            return this;
        }


        Line replaceAll(Pattern pattern, String replacement) {
            name = pattern.matcher(name).replaceAll(replacement);
            return this;
        }

        Line saveOriginalName() {
            originalName = name;
            return this;
        }

        Line removeDiggitsInBegginig() {
            try {
                if (Character.isDigit(name.charAt(0)))
                    name = '_' + name;
            } catch (StringIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("can't replace: " + id + ' ' + name);
            }

            return this;
        }

        Line toUpperCase() {
            name = name.toUpperCase();
            return this;
        }

        boolean nameNotEmpty() {
            return !name.isEmpty();
        }

        boolean isFiltred() {
            return Arrays.asList(740, 859, 1416).contains(id) || "a,".equals(name);
        }

        boolean isFiltred2() {
            return Objects.equals(name, "_");
        }

        Line checkExistence() {
            int count = names.add(name, 1);

            if (count > 0) {
                System.out.println(name + " already exist");
                name += count + 1;
            }
            return this;
        }
    }
}
