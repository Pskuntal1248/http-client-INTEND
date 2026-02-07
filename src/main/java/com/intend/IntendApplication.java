package com.intend;

import com.intend.controller.cli.IntendCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class IntendApplication implements CommandLineRunner, ExitCodeGenerator {

    private final IntendCommand command;
    private final IFactory factory;
    private int exitCode;

    public IntendApplication(IntendCommand command, IFactory factory) {
        this.command = command;
        this.factory = factory;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(IntendApplication.class, args)));
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(command, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
