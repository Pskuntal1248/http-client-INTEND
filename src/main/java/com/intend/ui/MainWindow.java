package com.intend.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intend.core.RequestIntent;
import com.intend.engine.TemplateEngine;
import com.intend.repository.ConfigRepository;
import com.intend.repository.HistoryRepository;
import com.intend.service.impl.IntendServiceImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends Application {

    private ConfigurableApplicationContext applicationContext;
    private IntendServiceImpl intendService;
    private TemplateEngine templateEngine;
    private final ObjectMapper mapper = new ObjectMapper();

    private ComboBox<String> methodBox;
    private ComboBox<RequestIntent.AuthStrategy> authBox;
    private TextField urlField;
    private TextArea requestBody;
    private TextField captureField;
    private ListView<HistoryRepository.HistoryItem> historyList;
    private Label statusLabel;
    private SplitPane splitPane;
    private VBox sidebar;
    private Button mainToggleButton;
    private Button settingsButton;
    private ImageView topBarIcon;
    private CheckBox chainToggle;
    private boolean historyCollapsed;
    private double historyDividerPosition = 0.3;
    private File selectedFile;

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
                    methodLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + getMethodColor(item.method()) + ";");
                    methodLabel.setMinWidth(52);

                    Label urlLabel = new Label(item.url());
                    urlLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
                    urlLabel.setMaxWidth(180);
                    HBox.setHgrow(urlLabel, Priority.ALWAYS);

                    Label timeLabel = new Label(item.timestamp());
                    timeLabel.setStyle("-fx-text-fill: #606060; -fx-font-size: 11px;");

                    HBox row = new HBox(8, methodLabel, urlLabel);
                    row.setAlignment(Pos.CENTER_LEFT);
                    VBox cell = new VBox(2, row, timeLabel);
                    cell.setPadding(new Insets(4, 0, 4, 0));
                    setGraphic(cell);
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

        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/icons/intend-logo.png")));
        logoView.setFitHeight(30);
        logoView.setPreserveRatio(true);
        HBox logoBox = new HBox(logoView);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 0, 4, 0));

        Label historyLabel = new Label("HISTORY");
        historyLabel.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-font-weight: 600; -fx-letter-spacing: 1;");
        historyLabel.setPadding(new Insets(8, 0, 4, 0));

        sidebar = new VBox(logoBox, historyLabel, historyList);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(16));
        sidebar.setSpacing(8);
        VBox.setVgrow(historyList, Priority.ALWAYS);

        methodBox = new ComboBox<>();
        methodBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodBox.setValue("POST");
        methodBox.setMinWidth(90);

        ComboBox<String> envBox = new ComboBox<>();
        envBox.getItems().addAll("DEV", "PROD");
        envBox.setValue("DEV");
        envBox.setMinWidth(75);

        authBox = new ComboBox<>();
        authBox.getItems().addAll(RequestIntent.AuthStrategy.values());
        authBox.setValue(RequestIntent.AuthStrategy.NONE);
        authBox.setMinWidth(90);

        urlField = new TextField();
        urlField.setPromptText("https://api.example.com/endpoint");
        urlField.setStyle("-fx-font-size: 14px;");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button sendBtn = new Button("SEND");
        sendBtn.getStyleClass().add("send-button");
        sendBtn.setMinWidth(80);

        requestBody = new TextArea();
        requestBody.setPromptText("Request body (JSON)");
        requestBody.setPrefHeight(100);
        requestBody.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace; -fx-font-size: 13px;");

        Button fileButton = new Button("Attach File");
        Label fileLabel = new Label("No file selected");
        fileLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #808080;");

        fileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Upload");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedFile = file;
                fileLabel.setText("File: " + file.getName());
                requestBody.setDisable(true);
                requestBody.setText("[File Selected: " + file.getAbsolutePath() + "]");
                methodBox.setValue("POST");
            }
        });

        Button clearFileButton = new Button("Clear");
        clearFileButton.setOnAction(event -> {
            selectedFile = null;
            fileLabel.setText("No file selected");
            requestBody.setDisable(false);
            requestBody.setText("");
        });

        HBox fileSection = new HBox(10, fileButton, clearFileButton, fileLabel);
        fileSection.setAlignment(Pos.CENTER_LEFT);

        captureField = new TextField();
        captureField.setPromptText("Capture (e.g. USER_ID=/json/id)");
        captureField.setVisible(false);
        captureField.setManaged(false);

        chainToggle = new CheckBox("Chain / Extract Variable");
        chainToggle.setStyle("-fx-font-size: 13px; -fx-text-fill: #B3B3B3; -fx-cursor: hand;");
        chainToggle.setOnAction(event -> toggleCaptureField());

        VBox captureSection = new VBox(5, chainToggle, captureField);
        captureSection.setPadding(new Insets(5, 0, 10, 0));

        TextArea responseArea = new TextArea();
        responseArea.setEditable(false);
        responseArea.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace; -fx-font-size: 13px;");
        VBox.setVgrow(responseArea, Priority.ALWAYS);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #808080; -fx-font-size: 13px;");

        mainToggleButton = new Button("◀");
        mainToggleButton.getStyleClass().add("icon-button");
        mainToggleButton.setTooltip(new Tooltip("Toggle History"));
        mainToggleButton.setOnAction(event -> toggleHistory());

        settingsButton = new Button("⚙");
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setTooltip(new Tooltip("Settings"));
        settingsButton.setOnAction(event -> openSettingsWindow());

        HBox urlBar = new HBox(8, urlField, sendBtn);
        urlBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(urlField, Priority.ALWAYS);

        topBarIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/intend-icon.png")));
        topBarIcon.setFitHeight(28);
        topBarIcon.setPreserveRatio(true);
        topBarIcon.setVisible(false);
        topBarIcon.setManaged(false);

        HBox controlsBar = new HBox(8, topBarIcon, settingsButton, mainToggleButton, methodBox, authBox, envBox);
        controlsBar.setAlignment(Pos.CENTER_LEFT);

        VBox topBar = new VBox(6, controlsBar, urlBar);
        topBar.setPadding(new Insets(4, 0, 8, 0));

        Label requestLabel = new Label("REQUEST");
        requestLabel.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-font-weight: 600; -fx-letter-spacing: 1;");
        requestLabel.setPadding(new Insets(4, 0, 4, 0));

        Label responseLabel = new Label("RESPONSE");
        responseLabel.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-font-weight: 600; -fx-letter-spacing: 1;");
        responseLabel.setPadding(new Insets(8, 0, 4, 0));

        VBox requestSection = new VBox(8, requestLabel, fileSection, requestBody, captureSection);
        requestSection.getStyleClass().add("request-section");
        requestSection.setPadding(new Insets(12));

        VBox responseSection = new VBox(8, responseLabel, responseArea, statusLabel);
        responseSection.getStyleClass().add("response-section");
        responseSection.setPadding(new Insets(12));
        VBox.setVgrow(responseSection, Priority.ALWAYS);
        VBox.setVgrow(responseArea, Priority.ALWAYS);

        VBox mainContent = new VBox(12, topBar, requestSection, responseSection);
        mainContent.setPadding(new Insets(16));
        VBox.setVgrow(responseSection, Priority.ALWAYS);

        splitPane = new SplitPane();
        splitPane.getItems().addAll(sidebar, mainContent);
        splitPane.setDividerPositions(historyDividerPosition);

        sendBtn.setOnAction(e -> {
            sendBtn.setDisable(true);
            statusLabel.setText("Sending...");
            statusLabel.setTextFill(Color.web("#808080"));

            new Thread(() -> {
                try {
                    String rawUrl = urlField.getText();
                    String resolvedUrl = templateEngine.process(rawUrl);
                    if (resolvedUrl.contains("{{")) {
                        throw new IllegalArgumentException("Unresolved URL template variables.");
                    }
                    Object payload = selectedFile != null ? selectedFile : requestBody.getText();
                    RequestIntent intent = new RequestIntent(
                        RequestIntent.Method.valueOf(methodBox.getValue()),
                        URI.create(resolvedUrl),
                        payload,
                        authBox.getValue(),
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
                        statusLabel.setTextFill(Color.web("#FF3B3B"));
                        sendBtn.setDisable(false);
                    });
                }
            }).start();
        });

        Scene scene = new Scene(splitPane, 1100, 750);
        scene.getStylesheets().add(getClass().getResource("/styles/intend-theme.css").toExternalForm());
        stage.setTitle("INTEND - API Workspace");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/intend-icon.png")));
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(ImageIO.read(getClass().getResourceAsStream("/icons/intend-icon.png")));
                }
            }
        } catch (Exception ignored) {}
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
            statusLabel.setTextFill(Color.web("#4ADE80"));
            return;
        }

        statusLabel.setText("Status: " + statusCode);
        if (statusCode >= 200 && statusCode < 300) {
            statusLabel.setTextFill(Color.web("#4ADE80"));
        } else if (statusCode >= 400) {
            statusLabel.setTextFill(Color.web("#FF3B3B"));
        } else {
            statusLabel.setTextFill(Color.web("#FBBF24"));
        }
    }

    private void toggleHistory() {
        if (historyCollapsed) {
            sidebar.setManaged(true);
            sidebar.setVisible(true);
            splitPane.setDividerPositions(historyDividerPosition);
            mainToggleButton.setText("◀");
            topBarIcon.setVisible(false);
            topBarIcon.setManaged(false);
            historyCollapsed = false;
        } else {
            historyDividerPosition = splitPane.getDividerPositions()[0];
            sidebar.setManaged(false);
            sidebar.setVisible(false);
            splitPane.setDividerPositions(0.0);
            mainToggleButton.setText("▶");
            topBarIcon.setVisible(true);
            topBarIcon.setManaged(true);
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

    private String getMethodColor(String method) {
        return switch (method) {
            case "GET" -> "#4ADE80";
            case "POST" -> "#60A5FA";
            case "PUT" -> "#FBBF24";
            case "DELETE" -> "#FF3B3B";
            case "PATCH" -> "#C084FC";
            default -> "#E6E6E6";
        };
    }

    private void openSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Environment Configuration");
        settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/intend-icon.png")));

        ConfigRepository.ConfigData current = intendService.getConfigRepository().get();

        TextField devUrlField = new TextField(current.devUrl);
        PasswordField devKeyField = new PasswordField();
        devKeyField.setText(current.devKey);

        TextField prodUrlField = new TextField(current.prodUrl);
        PasswordField prodKeyField = new PasswordField();
        prodKeyField.setText(current.prodKey);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.addRow(0, new Label("DEV URL:"), devUrlField);
        grid.addRow(1, new Label("DEV Key:"), devKeyField);
        grid.addRow(2, new Separator());
        grid.addRow(3, new Label("PROD URL:"), prodUrlField);
        grid.addRow(4, new Label("PROD Key:"), prodKeyField);

        Button saveBtn = new Button("Save Configuration");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.getStyleClass().add("save-button");
        saveBtn.setOnAction(event -> {
            intendService.getConfigRepository().save(
                devUrlField.getText(),
                devKeyField.getText(),
                prodUrlField.getText(),
                prodKeyField.getText()
            );
            settingsStage.close();
        });

        VBox root = new VBox(10, grid, saveBtn);
        root.setPadding(new Insets(10));

        Scene settingsScene = new Scene(root, 450, 280);
        settingsScene.getStylesheets().add(getClass().getResource("/styles/intend-theme.css").toExternalForm());
        settingsStage.setScene(settingsScene);
        settingsStage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
    }
}
