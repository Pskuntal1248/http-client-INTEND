package com.intend.engine;

import com.intend.repository.VariableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateEngineTest {

    private VariableRepository variableRepository;
    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        variableRepository = new VariableRepository();
        engine = new TemplateEngine(variableRepository);
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(engine.process(null));
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {
        assertEquals("", engine.process(""));
    }

    @Test
    void shouldReturnPlainTextUnchanged() {
        assertEquals("hello world", engine.process("hello world"));
    }

    @Test
    void shouldResolveUuid() {
        String result = engine.process("id={{uuid}}");
        assertTrue(result.startsWith("id="));
        assertFalse(result.contains("{{"));
        assertEquals(39, result.length()); // "id=" (3) + UUID (36)
    }

    @Test
    void shouldResolveTimestamp() {
        String result = engine.process("time={{timestamp}}");
        assertTrue(result.startsWith("time="));
        assertFalse(result.contains("{{"));
    }

    @Test
    void shouldResolveRandomInt() {
        String result = engine.process("num={{randomInt}}");
        assertTrue(result.startsWith("num="));
        String numPart = result.substring(4);
        int value = Integer.parseInt(numPart);
        assertTrue(value >= 0 && value < 1000);
    }

    @Test
    void shouldResolveRandomEmail() {
        String result = engine.process("email={{randomEmail}}");
        assertTrue(result.contains("@example.com"));
        assertTrue(result.startsWith("email=user_"));
    }

    @Test
    void shouldResolveRandomUser() {
        String result = engine.process("user={{randomUser}}");
        assertTrue(result.startsWith("user=User"));
    }

    @Test
    void shouldResolveStoredVariable() {
        variableRepository.put("USER_ID", "42");
        String result = engine.process("https://api.com/users/{{USER_ID}}");
        assertEquals("https://api.com/users/42", result);
    }

    @Test
    void shouldKeepUnresolvedPlaceholder() {
        String result = engine.process("https://api.com/users/{{UNKNOWN_VAR}}");
        assertEquals("https://api.com/users/{{UNKNOWN_VAR}}", result);
    }

    @Test
    void shouldResolveMultiplePlaceholders() {
        variableRepository.put("HOST", "api.example.com");
        variableRepository.put("VERSION", "v2");
        String result = engine.process("https://{{HOST}}/{{VERSION}}/users");
        assertEquals("https://api.example.com/v2/users", result);
    }

    @Test
    void shouldHandleMixedPlaceholdersAndLiterals() {
        variableRepository.put("TOKEN", "abc123");
        String result = engine.process("Bearer {{TOKEN}}");
        assertEquals("Bearer abc123", result);
    }

    @Test
    void shouldTrimPlaceholderWhitespace() {
        variableRepository.put("ID", "99");
        String result = engine.process("{{  ID  }}");
        assertEquals("99", result);
    }

    @Test
    void shouldHandleJsonBodyWithPlaceholders() {
        variableRepository.put("name", "Alice");
        String input = "{\"user\": \"{{name}}\", \"id\": \"{{uuid}}\"}";
        String result = engine.process(input);
        assertTrue(result.contains("\"user\": \"Alice\""));
        assertFalse(result.contains("{{name}}"));
        assertFalse(result.contains("{{uuid}}"));
    }
}
