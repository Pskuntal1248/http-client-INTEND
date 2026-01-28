package com.intend.cli;

import java.net.URI;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import com.intend.core.IntentValidator;
import com.intend.core.RequestIntent;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "intend", mixinStandardHelpOptions = true, version = "1.0",
         description = "Intent-based HTTP client")
public class IntendCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "HTTP Method")
    String method;
    @Parameters(index = "1", description = "Request URL")
    String url;
    @Option(names = "--auth", defaultValue = "none", description = "Auth strategy: jwt, oauth, apikey, none")
    String auth;
    @Option(names = "--trace", description = "Enable tracing")
    boolean trace;
    @Option(names = "--explain", description = "Explain request plan")
    boolean explain;
    @Option(names = "--apply", description = "Execute request")
    boolean apply;
    @Override
    public Integer call(){
        try {
            if (explain && apply) {
                throw new IllegalArgumentException("Error: Cannot use both explain & apply.");
            }
         RequestIntent.Mode mode;
if (apply) {
    mode = RequestIntent.Mode.APPLY;
} else {
    mode = RequestIntent.Mode.EXPLAIN;
}
            URI parsedUri = URI.create(url);
            RequestIntent intent = new RequestIntent(
                method.toUpperCase(), 
                parsedUri, 
                auth, 
                trace, 
                mode
            );
            IntentValidator.validate(intent);
            System.out.println("\nSUCCESS: Parsed Intent");
            System.out.println(intent.toString());
            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println("error =" + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Critical Error: " + e.getMessage());
            return 2;
        }
    }
}
