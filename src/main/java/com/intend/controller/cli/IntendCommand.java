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

    @Option(names = {"-d", "--data"}, description = "JSON payload")
    String data;

    @Option(names = "--new", description = "Force a new Idempotency Key")
    boolean newKey;

    @Option(names = "--env", defaultValue = "dev", description = "Target Environment (dev, prod)")
    String environment;

    @Override
    public Integer call() {
        try {
            RequestIntent intent = new RequestIntent(
                RequestIntent.Method.valueOf(method.toUpperCase()),
                URI.create(url),
                data,
                auth,
                newKey,
                environment
            );

            service.executeRequest(intent);

            return 0;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
