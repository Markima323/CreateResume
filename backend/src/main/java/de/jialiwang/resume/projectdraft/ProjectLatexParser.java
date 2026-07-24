package de.jialiwang.resume.projectdraft;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProjectLatexParser {
    private static final Pattern FORBIDDEN = Pattern.compile("\\\\(input|include|write|write18|openout|read|usepackage|newcommand|renewcommand|def|csname|catcode|immediate|loop|special)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMAND = Pattern.compile("\\\\([A-Za-z]+)");
    private static final Pattern TITLE = Pattern.compile("\\\\textbf\\{([^{}]+)}");
    private static final Pattern TECH = Pattern.compile("\\\\emph\\{([^{}]+)}");
    private static final Pattern HEADING_CONTEXT = Pattern.compile("\\\\resumeProjectHeading.*?\\}\\s*\\{([^{}]*)}\\s*\\\\resumeItemListStart", Pattern.DOTALL);
    private static final Pattern ITEM = Pattern.compile("\\\\resumeItem\\s*\\{((?:[^{}]|\\\\[{}])*)}", Pattern.DOTALL);

    public ParsedProject parse(String raw) {
        return parse(raw, 4);
    }

    public ParsedProject parse(String raw, int expectedItemCount) {
        List<String> errors = new ArrayList<>();
        if (raw == null || raw.isBlank()) return new ParsedProject("", "", "", List.of(), List.of("项目内容不能为空"));
        String value = raw.replace("```latex", "").replace("```tex", "").replace("```", "").trim();
        if (value.length() > 12000) errors.add("项目内容过长");
        if (FORBIDDEN.matcher(value).find()) errors.add("包含禁止使用的 LaTeX 命令");
        Matcher commands = COMMAND.matcher(value);
        List<String> allowed = List.of("resumeProjectHeading", "textbf", "emph", "resumeItemListStart", "resumeItem", "resumeItemListEnd");
        while (commands.find()) {
            if (!allowed.contains(commands.group(1))) {
                errors.add("包含不允许的 LaTeX 命令：\\" + commands.group(1));
                break;
            }
        }
        if (count(value, "\\resumeProjectHeading") != 1) errors.add("必须恰好包含一个 \\resumeProjectHeading");
        if (count(value, "\\resumeItemListStart") != 1 || count(value, "\\resumeItemListEnd") != 1) errors.add("必须包含一组项目条目列表");
        String title = first(TITLE, value);
        String tech = first(TECH, value);
        String context = first(HEADING_CONTEXT, value);
        if (title.isBlank()) errors.add("无法读取项目标题");
        if (tech.isBlank()) errors.add("无法读取技术栈");
        if (context.isBlank()) errors.add("无法读取项目类型/角色");
        List<String> items = new ArrayList<>();
        Matcher m = ITEM.matcher(value);
        while (m.find()) items.add(m.group(1).trim());
        if (items.size() != expectedItemCount) errors.add("必须恰好包含 " + expectedItemCount + " 个 \\resumeItem，当前为 " + items.size() + " 个");
        if (items.stream().anyMatch(String::isBlank)) errors.add("项目条目不能为空");
        return new ParsedProject(title, tech, context, items, errors);
    }

    private int count(String source, String needle) { int count = 0, pos = 0; while ((pos = source.indexOf(needle, pos)) >= 0) { count++; pos += needle.length(); } return count; }
    private String first(Pattern pattern, String source) { Matcher m = pattern.matcher(source); return m.find() ? m.group(1).trim() : ""; }

    public record ParsedProject(String title, String technologies, String context, List<String> items, List<String> errors) {
        public boolean valid() { return errors.isEmpty(); }
    }
}
