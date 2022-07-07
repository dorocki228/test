import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Java-man
 * @since 19.11.2017
 */
public class SystemMsgParser {
    private static final Pattern COMPILE1 = Pattern.compile("a,");
    private static final Pattern COMPILE21 = Pattern.compile("u,");
    private static final Pattern COMPILE2 = Pattern.compile("\\\\0");
    private static final Pattern COMPILE3 = Pattern.compile("\\s");
    private static final Pattern COMPILE24 = Pattern.compile(" ");
    private static final Pattern COMPILE4 = Pattern.compile("\\.");
    private static final Pattern COMPILE5 = Pattern.compile("/");
    private static final Pattern COMPILE6 = Pattern.compile("@");
    private static final Pattern COMPILE7 = Pattern.compile("#");
    private static final Pattern COMPILE9 = Pattern.compile("\\*");
    private static final Pattern COMPILE10 = Pattern.compile("\\+");
    private static final Pattern COMPILE11 = Pattern.compile("\\(");
    private static final Pattern COMPILE12 = Pattern.compile("\\)");
    private static final Pattern COMPILE13 = Pattern.compile("'");
    private static final Pattern COMPILE14 = Pattern.compile("&");
    private static final Pattern COMPILE15 = Pattern.compile("<");
    private static final Pattern COMPILE16 = Pattern.compile(">");
    private static final Pattern COMPILE19 = Pattern.compile(",");
    private static final Pattern COMPILE20 = Pattern.compile("!");
    private static final Pattern COMPILE22 = Pattern.compile("’");
    private static final Pattern COMPILE23 = Pattern.compile("\\?");
    private static final Pattern COMPILE25 = Pattern.compile("\\^");
    private static final Pattern COMPILE26 = Pattern.compile("\\\\\\\\n");
    private static final Pattern COMPILE28 = Pattern.compile("\"");
    private static final Pattern COMPILE29 = Pattern.compile(";");
    private static final Pattern COMPILE31 = Pattern.compile("=");
    private static final Pattern COMPILE32 = Pattern.compile("[%©…“”~]|\\\\n|\\\\N");
    private static final Pattern COMPILE30 = Pattern.compile("_{2,10}");

    private static final Pattern COMPILE8 = Pattern.compile("\\$");
    private static final Pattern COMPILE17 = Pattern.compile("[-—]");
    private static final Pattern COMPILE18 = Pattern.compile(":");

    private static final Pattern COMPILE27 = Pattern.compile("={1,7}<(.*)>={1,7}");

    private static final Pattern S = Pattern.compile("_s");
    private static final Pattern C = Pattern.compile("_c");
    private static final Pattern COMPILE = Pattern.compile("\n+");
    private static final Pattern COMPILE40 = Pattern.compile("(s\\d)\\(s\\)");

    private static Multiset<String> usedMessages = HashMultiset.create();

    public static void main(String[] args) throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource("SystemMsg_Classic-eu.txt").toURI());
        List<Line> lines = Files.lines(path, Charset.forName("windows-1252"))
                .map(line -> new Line(line.split("\t")))
                .filter(line -> !line.isFiltred())
                .map(line -> line.replaceFirst(COMPILE1, ""))
                .map(line -> line.replaceFirst(COMPILE21, ""))
                .map(line -> line.replaceFirst(COMPILE2, ""))
                .map(line -> line.replaceAll(COMPILE8, "_"))
                .map(Line::saveOriginalMessage)
                .map(line -> line.replaceGroup(COMPILE27))
                .map(line -> line.replaceAll(COMPILE3, "_"))
                .map(line -> line.replaceAll(COMPILE24, "_"))
                .map(line -> line.replaceAll(COMPILE4, ""))
                .filter(Line::nameNotEmpty)
                .map(line -> line.replaceAll(COMPILE5, ""))
                .map(line -> line.replaceAll(COMPILE6, ""))
                .map(line -> line.replaceAll(COMPILE7, ""))
                .map(line -> line.replaceAll(COMPILE9, ""))
                .map(line -> line.replaceAll(COMPILE10, ""))
                .map(line -> line.replaceAll(COMPILE40, "$1"))
                .map(line -> line.replaceAll(COMPILE11, ""))
                .map(line -> line.replaceAll(COMPILE12, ""))
                .map(line -> line.replaceAll(COMPILE14, ""))
                .map(line -> line.replaceAll(COMPILE15, ""))
                .map(line -> line.replaceAll(COMPILE16, ""))
                .map(line -> line.replaceAll(COMPILE19, ""))
                .map(line -> line.replaceAll(COMPILE20, ""))
                .map(line -> line.replaceAll(COMPILE13, ""))
                .map(line -> line.replaceAll(COMPILE22, ""))
                .map(line -> line.replaceAll(COMPILE23, ""))
                .map(line -> line.replaceAll(COMPILE25, ""))
                .map(line -> line.replaceAll(COMPILE26, ""))
                .map(line -> line.replaceAll(COMPILE29, ""))
                .map(line -> line.replaceAll(COMPILE31, ""))
                .map(line -> line.replaceAll(COMPILE32, ""))
                .map(line -> line.replaceAll(COMPILE17, "_"))
                .map(line -> line.replaceAll(COMPILE18, "_"))
                .map(line -> line.replaceAll(COMPILE30, "_"))
                .map(line -> line.replaceAll(COMPILE28, ""))
                .map(Line::removeUnderscoreInBegining)
                .map(Line::removeUnderscoreInEnding)
                .map(Line::removeDiggitsInBegginig)
                .filter(line -> !line.isFiltred2())
                .map(Line::toUpperCase)
                .map(Line::checkExistence)
                .sorted(Comparator.comparingInt(Line::getId))
                .collect(Collectors.toList());

        TypeSpec typeSpec = writeClass(lines);
        String classString = typeSpec.toString();
        classString = COMPILE.matcher(classString).replaceAll("\n");
        classString = S.matcher(classString).replaceAll("\\$s");
        classString = C.matcher(classString).replaceAll("\\$c");

        Path savePath = Paths.get("P:\\pvp-game\\parser\\src\\main\\resources\\SystemMsg.java");
        Files.deleteIfExists(savePath);
        Files.write(savePath, classString.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private static TypeSpec writeClass(List<Line> lines) {
        TypeSpec.Builder builder = TypeSpec.enumBuilder("SystemMsg")
                .addModifiers(Modifier.PUBLIC)
                .addField(int.class, "id", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(int.class, "id")
                        .addStatement("this.$N = $N", "id", "id")
                        .build());

        lines.forEach(line -> {
            try {
                builder.addEnumConstant(line.getMessage(),
                        TypeSpec.anonymousClassBuilder("$L", line.getId())
                                .addJavadoc("Message: " + line.getOriginalMessage() + '\n')
                                .build());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("not a valid enum constant: "
                        + line.getId() + ' ' + line.getMessage() + ' ' + line.getOriginalMessage(), e);
            }
        });

        return builder.build();
    }

    private static class Line {
        private final int id;
        private String originalMessage;
        private String message;

        Line(String[] message) {
            this.id = Integer.parseInt(message[1].split("=")[1]);
            this.message = message[3].split("=")[1]
                    .replace("[", "").replace("]", "");
        }

        public int getId() {
            return id;
        }

        public String getOriginalMessage() {
            return originalMessage;
        }

        public String getMessage() {
            return message;
        }

        Line replaceFirst(Pattern pattern, String replacement) {
            message = pattern.matcher(message).replaceFirst(replacement);
            return this;
        }

        Line replaceAll(Pattern pattern, String replacement) {
            message = pattern.matcher(message).replaceAll(replacement);
            return this;
        }

        Line replaceGroup(Pattern pattern) {
            Matcher m = pattern.matcher(message);
            if (m.find()) {
                message = m.group(1);
            }
            return this;
        }

        Line saveOriginalMessage() {
            originalMessage = message;
            return this;
        }

        Line removeDiggitsInBegginig() {
            try {
                if (Character.isDigit(message.charAt(0)))
                    message = '_' + message;
            } catch (StringIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("can't replace: " + id + ' ' + message);
            }

            return this;
        }

        Line toUpperCase() {
            message = message.toUpperCase();
            return this;
        }

        boolean nameNotEmpty() {
            return !message.isEmpty();
        }

        boolean isFiltred() {
            return Arrays.asList(431, 490, 501, 1720).contains(id) || "a,".equals(message);
        }

        boolean isFiltred2() {
            return Objects.equals(message, "_");
        }

        Line checkExistence() {
            int count = usedMessages.add(message, 1);

            if (count > 0) {
                System.out.println(message + " already exist");
                message += count + 1;
            }
            return this;
        }

        Line removeUnderscoreInBegining() {
            if (message.startsWith("_"))
                message = message.replaceFirst("_", "");

            return this;
        }

        Line removeUnderscoreInEnding() {
            if (message.endsWith("_"))
                message = replaceLast(message,"_", "");

            return this;
        }

        String replaceLast(String string, String from, String to) {
            int lastIndex = string.lastIndexOf(from);
            if (lastIndex < 0) return string;
            String tail = string.substring(lastIndex).replaceFirst(from, to);
            return string.substring(0, lastIndex) + tail;
        }
    }
}
