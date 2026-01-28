package com.intend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.intend.cli.IntendCommand;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class IntendApplication implements CommandLineRunner, ExitCodeGenerator {
    private final IntendCommand myCommand;
    private final IFactory factory;
    private int exitCode;
    public IntendApplication(IntendCommand myCommand, ApplicationContext ctx) {
        this.myCommand = myCommand;
        this.factory = new SpringFactory(ctx);
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(IntendApplication.class, args)));
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(myCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
    private static class SpringFactory implements IFactory {
        private final ApplicationContext ctx;

        SpringFactory(ApplicationContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public <K> K create(Class<K> cls) throws Exception {
            try {
                return ctx.getBean(cls);
            } catch (Exception e) {
                return CommandLine.defaultFactory().create(cls);
            }
        }
    }
}
