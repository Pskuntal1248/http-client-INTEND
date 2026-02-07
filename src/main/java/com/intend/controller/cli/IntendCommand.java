package com.intend.controller.cli;

import com.intend.core.RequestIntent;
import com.intend.service.IntendService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

@Component
@Command(name = "intend", mixinStandardHelpOptions = true, description = "Dynamic API Client")
public class IntendCommand implements Callable<Integer> {

    private final IntendService service;

    public IntendCommand(IntendService service) {
        this.service = service;
    }

    @Parameters(index = "0", description = "HTTP Method")
    String method;

    @Parameters(index = "1", description = "URL")
    String url;

    @Option(names = "--auth", defaultValue = "NONE")
    RequestIntent.AuthStrategy auth;

    @Override
    public Integer call() {
        try {
            RequestIntent intent = new RequestIntent(
                RequestIntent.Method.valueOf(method.toUpperCase()),
                URI.create(url),
                null,
                auth
            );

            Map<String, String> headers = service.prepareRequest(intent);

            System.out.println("\n✅ Final Request Headers:");
            headers.forEach((k, v) -> System.out.println(String.format("   %s: %s", k, v)));

            return 0;

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return 1;
        }
    }
}
