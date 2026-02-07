package com.intend.engine;

import com.intend.repository.VariableRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateEngine {

    private static final Pattern PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private final Random random = new Random();
    private final VariableRepository variableRepository;

    public TemplateEngine(VariableRepository variableRepository) {
        this.variableRepository = variableRepository;
    }

    public String process(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher matcher = PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            String replacement = resolveVariable(placeholder);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String resolveVariable(String key) {
        return switch (key) {
            case "uuid" -> UUID.randomUUID().toString();
            case "timestamp" -> Instant.now().toString();
            case "randomInt" -> String.valueOf(random.nextInt(1000));
            case "randomEmail" -> "user_" + random.nextInt(9999) + "@example.com";
            case "randomUser" -> "User" + random.nextInt(100);
            default -> resolveStoredVariable(key);
        };
    }

    private String resolveStoredVariable(String key) {
        String storedValue = variableRepository.get(key);
        if (storedValue != null) {
            return storedValue;
        }

        return "{{" + key + "}}";
    }
}
