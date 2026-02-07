package com.intend.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intend.core.RequestIntent;
import com.intend.engine.TemplateEngine;
import com.intend.repository.HistoryRepository;
import com.intend.service.impl.IntendServiceImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends Application {

    private ConfigurableApplicationContext applicationContext;
    private IntendServiceImpl intendService;
    private TemplateEngine templateEngine;
    private final ObjectMapper mapper = new ObjectMapper();

    private ComboBox<String> methodBox;
    private TextField urlField;
    private TextArea requestBody;
    private TextField captureField;
    private ListView<HistoryRepository.HistoryItem> historyList;
    private Label statusLabel;
    private SplitPane splitPane;
    private VBox sidebar;
    private Button sidebarToggleButton;
    private Button mainToggleButton;
    private CheckBox chainToggle;
    private boolean historyCollapsed;
    private double historyDividerPosition = 0.3;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(com.intend.IntendApplication.class).run();
        intendService = applicationContext.getBean(IntendServiceImpl.class);
        templateEngine = applicationContext.getBean(TemplateEngine.class);
    }

    @Override
    public void start(Stage stage) {
        historyList = new ListView<>();
        refreshHistory();

        historyList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(HistoryRepository.HistoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label methodLabel = new Label(item.method());
                    methodLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
                    methodLabel.setMinWidth(45);

                    Label urlLabel = new Label(item.url());
                    urlLabel.setMaxWidth(150);

                    Label timeLabel = new Label(item.timestamp());
                    timeLabel.setTextFill(Color.GRAY);
                    timeLabel.setFont(Font.font(10));

                    HBox root = new HBox(5, methodLabel, urlLabel, timeLabel);
                    root.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(root);
                }
            }
        });

        historyList.setOnMouseClicked(e -> {
            HistoryRepository.HistoryItem selected = historyList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                methodBox.setValue(selected.method());
                urlField.setText(selected.url());
                requestBody.setText(selected.body());
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            HistoryRepository.HistoryItem selected = historyList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                intendService.getHistory().delete(selected);
                refreshHistory();
            }
        });
        contextMenu.getItems().add(deleteItem);
        historyList.setContextMenu(contextMenu);

        Label historyLabel = new Label("History");
        sidebarToggleButton = new Button("Hide");
        sidebarToggleButton.setOnAction(event -> toggleHistory());

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox historyHeader = new HBox(8, historyLabel, headerSpacer, sidebarToggleButton);
        historyHeader.setAlignment(Pos.CENTER_LEFT);

        sidebar = new VBox(historyHeader, historyList);
        sidebar.setPadding(new Insets(10));
        sidebar.setSpacing(5);
        VBox.setVgrow(historyList, Priority.ALWAYS);

        methodBox = new ComboBox<>();
        methodBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodBox.setValue("POST");

        ComboBox<String> envBox = new ComboBox<>();
        envBox.getItems().addAll("DEV", "PROD");
        envBox.setValue("DEV");

        urlField = new TextField("https://httpbin.org/post");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button sendBtn = new Button("SEND");
        sendBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");

        requestBody = new TextArea("{\"hello\": \"history\"}");
        requestBody.setPrefHeight(100);
        requestBody.setFont(Font.font("Monospaced"));

        captureField = new TextField();
        captureField.setPromptText("Capture (e.g. USER_ID=/json/id)");
        captureField.setVisible(false);
        captureField.setManaged(false);

        chainToggle = new CheckBox("Chain / Extract Variable");
        chainToggle.setStyle("-fx-font-size: 11px; -fx-text-fill: gray; -fx-cursor: hand;");
        chainToggle.setOnAction(event -> toggleCaptureField());

        VBox captureSection = new VBox(5, chainToggle, captureField);
        captureSection.setPadding(new Insets(5, 0, 10, 0));

        TextArea responseArea = new TextArea();
        responseArea.setEditable(false);
        responseArea.setFont(Font.font("Monospaced"));
        VBox.setVgrow(responseArea, Priority.ALWAYS);

        statusLabel = new Label("Ready");
        statusLabel.setTextFill(Color.GRAY);

        mainToggleButton = new Button("Hide History");
        mainToggleButton.setOnAction(event -> toggleHistory());

        HBox topBar = new HBox(10, mainToggleButton, methodBox, envBox, urlField, sendBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox mainContent = new VBox(
            10,
            topBar,
            new Label("Request Body"),
            requestBody,
            captureSection,
            new Label("Response"),
            responseArea,
            statusLabel
        );
        mainContent.setPadding(new Insets(10));

        splitPane = new SplitPane();
        splitPane.getItems().addAll(sidebar, mainContent);
        splitPane.setDividerPositions(historyDividerPosition);

        sendBtn.setOnAction(e -> {
            sendBtn.setDisable(true);
            statusLabel.setText("Sending...");
            statusLabel.setTextFill(Color.GRAY);

            new Thread(() -> {
                try {
                    String rawUrl = urlField.getText();
                    String resolvedUrl = templateEngine.process(rawUrl);
                    if (resolvedUrl.contains("{{")) {
                        throw new IllegalArgumentException("Unresolved URL template variables.");
                    }
                    RequestIntent intent = new RequestIntent(
                        RequestIntent.Method.valueOf(methodBox.getValue()),
                        URI.create(resolvedUrl),
                        requestBody.getText(),
                        RequestIntent.AuthStrategy.API_KEY,
                        false,
                        envBox.getValue().toLowerCase()
                    );

                    Map<String, String> captures = buildCaptures(captureField.getText());
                    String rawResponse = intendService.executeRequestAsString(intent, captures);
                    String prettyJson = prettyPrint(rawResponse);
                    Integer statusCode = extractStatusCode(rawResponse);

                    Platform.runLater(() -> {
                        responseArea.setText(prettyJson);
                        updateStatusLabel(statusCode);
                        sendBtn.setDisable(false);
                        refreshHistory();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        responseArea.setText("Error: " + ex.getMessage());
                        statusLabel.setText("Failed");
                        statusLabel.setTextFill(Color.RED);
                        sendBtn.setDisable(false);
                    });
                }
            }).start();
        });

        Scene scene = new Scene(splitPane, 1000, 700);
        stage.setTitle("Intend - API Workspace");
        stage.setScene(scene);
        stage.show();
    }

    private void refreshHistory() {
        historyList.getItems().setAll(intendService.getHistory().getAll());
    }

    private String prettyPrint(String json) {
        try {
            if (json.contains("{")) {
                int start = json.indexOf("{");
                Object obj = mapper.readValue(json.substring(start), Object.class);
                return json.substring(0, start)
                    + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }
        } catch (Exception ignored) {
            return json;
        }
        return json;
    }

    private Integer extractStatusCode(String rawResponse) {
        if (rawResponse == null) {
            return null;
        }

        String prefix = "Status:";
        int prefixIndex = rawResponse.indexOf(prefix);
        if (prefixIndex == -1) {
            return null;
        }

        int numberStart = prefixIndex + prefix.length();
        int numberEnd = rawResponse.indexOf('\n', numberStart);
        String numberText = numberEnd == -1
            ? rawResponse.substring(numberStart)
            : rawResponse.substring(numberStart, numberEnd);

        String trimmed = numberText.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void updateStatusLabel(Integer statusCode) {
        if (statusCode == null) {
            statusLabel.setText("Success");
            statusLabel.setTextFill(Color.GREEN);
            return;
        }

        statusLabel.setText("Status: " + statusCode);
        if (statusCode >= 200 && statusCode < 300) {
            statusLabel.setTextFill(Color.GREEN);
        } else if (statusCode >= 400) {
            statusLabel.setTextFill(Color.RED);
        } else {
            statusLabel.setTextFill(Color.ORANGE);
        }
    }

    private void toggleHistory() {
        if (historyCollapsed) {
            sidebar.setManaged(true);
            sidebar.setVisible(true);
            splitPane.setDividerPositions(historyDividerPosition);
            sidebarToggleButton.setText("Hide");
            mainToggleButton.setText("Hide History");
            historyCollapsed = false;
        } else {
            historyDividerPosition = splitPane.getDividerPositions()[0];
            sidebar.setManaged(false);
            sidebar.setVisible(false);
            splitPane.setDividerPositions(0.0);
            sidebarToggleButton.setText("Show");
            mainToggleButton.setText("Show History");
            historyCollapsed = true;
        }
    }

    private void toggleCaptureField() {
        boolean isSelected = chainToggle.isSelected();
        captureField.setVisible(isSelected);
        captureField.setManaged(isSelected);
    }

    private Map<String, String> buildCaptures(String captureText) {
        Map<String, String> captures = new HashMap<>();
        if (captureText == null || captureText.isBlank()) {
            return captures;
        }

        String[] parts = captureText.split("=", 2);
        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                captures.put(key, value);
            }
        }

        return captures;
    }

    @Override
    public void stop() {
        applicationContext.close();
    }
}
