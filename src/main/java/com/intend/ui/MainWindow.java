package com.intend.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intend.core.MultipartPayload;
import com.intend.core.RequestIntent;
import com.intend.engine.TemplateEngine;
import com.intend.execution.ExecutionResult;
import com.intend.repository.ConfigRepository;
import com.intend.repository.HistoryRepository;
import com.intend.repository.SavedRequestRepository;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
    private VBox paramsPane;
    private VBox bodyPane;
    private Button bodyTabBtn;
    private Button paramsTabBtn;
    private final List<ParamRow> paramRows = new ArrayList<>();
    private ListView<SavedRequestRepository.SavedRequest> savedList;
    private ComboBox<String> envBox;

    private record ParamRow(CheckBox enabled, TextField key, TextField value) {}

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

        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/icons/image.png")));
        logoView.setFitHeight(30);
        logoView.setPreserveRatio(true);
        HBox logoBox = new HBox(logoView);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 0, 4, 0));

        Label historyLabel = new Label("HISTORY");
        historyLabel.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-font-weight: 600; -fx-letter-spacing: 1;");
        historyLabel.setPadding(new Insets(8, 0, 4, 0));

        // -- Saved requests list --
        Label savedLabel = new Label("SAVED");
        savedLabel.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-font-weight: 600; -fx-letter-spacing: 1;");
        savedLabel.setPadding(new Insets(12, 0, 4, 0));

        savedList = new ListView<>();
        savedList.setPrefHeight(150);
        refreshSavedRequests();

        savedList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SavedRequestRepository.SavedRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label methodLabel = new Label(item.method());
                    methodLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + getMethodColor(item.method()) + ";");
                    methodLabel.setMinWidth(42);

                    Label nameLabel = new Label(item.name());
                    nameLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 12px; -fx-font-weight: 500;");
                    nameLabel.setMaxWidth(160);

                    HBox row = new HBox(6, methodLabel, nameLabel);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(2, 0, 2, 0));
                    setGraphic(row);
                }
            }
        });

        savedList.setOnMouseClicked(e -> {
            SavedRequestRepository.SavedRequest selected = savedList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadSavedRequest(selected);
            }
        });

        ContextMenu savedContextMenu = new ContextMenu();
        MenuItem deleteSavedItem = new MenuItem("Delete");
        deleteSavedItem.setOnAction(e -> {
            SavedRequestRepository.SavedRequest selected = savedList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                intendService.getSavedRequests().delete(selected);
                refreshSavedRequests();
            }
        });
        MenuItem shareSavedItem = new MenuItem("Copy as JSON");
        shareSavedItem.setOnAction(e -> {
            SavedRequestRepository.SavedRequest selected = savedList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String json = intendService.getSavedRequests().toJson(selected);
                ClipboardContent content = new ClipboardContent();
                content.putString(json);
                Clipboard.getSystemClipboard().setContent(content);
                statusLabel.setText("Copied to clipboard");
                statusLabel.setTextFill(Color.web("#4ADE80"));
            }
        });
        savedContextMenu.getItems().addAll(shareSavedItem, deleteSavedItem);
        savedList.setContextMenu(savedContextMenu);

        sidebar = new VBox(logoBox, savedLabel, savedList, historyLabel, historyList);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(16));
        sidebar.setSpacing(8);
        VBox.setVgrow(historyList, Priority.ALWAYS);

        methodBox = new ComboBox<>();
        methodBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodBox.setValue("POST");
        methodBox.setMinWidth(90);

        envBox = new ComboBox<>();
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
                requestBody.setPromptText("Additional fields (JSON) — sent alongside the file");
                methodBox.setValue("POST");
            }
        });

        Button clearFileButton = new Button("Clear");
        clearFileButton.setOnAction(event -> {
            selectedFile = null;
            fileLabel.setText("No file selected");
            requestBody.setPromptText("Request body (JSON)");
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

        // -- Params tab: key-value table --
        VBox paramsTable = new VBox(4);
        paramsPane = new VBox(8, buildParamsHeader(), paramsTable);
        paramsPane.setVisible(false);
        paramsPane.setManaged(false);
        addParamRow(paramsTable);

        Button addParamBtn = new Button("+ Add Param");
        addParamBtn.setStyle("-fx-font-size: 12px; -fx-text-fill: #B3B3B3; -fx-background-color: transparent; -fx-border-color: #3A3A3A; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 12;");
        addParamBtn.setOnAction(e -> addParamRow(paramsTable));
        paramsPane.getChildren().add(addParamBtn);

        // -- Body tab content --
        bodyPane = new VBox(8, fileSection, requestBody);

        // -- Tab toggle buttons --
        bodyTabBtn = new Button("Body");
        paramsTabBtn = new Button("Params");
        String activeTabStyle = "-fx-background-color: transparent; -fx-text-fill: #E6E6E6; -fx-font-size: 12px; -fx-font-weight: 600; -fx-border-color: transparent transparent #FF3B3B transparent; -fx-border-width: 0 0 2 0; -fx-background-radius: 0; -fx-border-radius: 0; -fx-padding: 6 14; -fx-cursor: hand;";
        String inactiveTabStyle = "-fx-background-color: transparent; -fx-text-fill: #808080; -fx-font-size: 12px; -fx-font-weight: 500; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-background-radius: 0; -fx-border-radius: 0; -fx-padding: 6 14; -fx-cursor: hand;";

        bodyTabBtn.setStyle(activeTabStyle);
        paramsTabBtn.setStyle(inactiveTabStyle);

        bodyTabBtn.setOnAction(e -> {
            bodyPane.setVisible(true);
            bodyPane.setManaged(true);
            paramsPane.setVisible(false);
            paramsPane.setManaged(false);
            bodyTabBtn.setStyle(activeTabStyle);
            paramsTabBtn.setStyle(inactiveTabStyle);
        });

        paramsTabBtn.setOnAction(e -> {
            bodyPane.setVisible(false);
            bodyPane.setManaged(false);
            paramsPane.setVisible(true);
            paramsPane.setManaged(true);
            paramsTabBtn.setStyle(activeTabStyle);
            bodyTabBtn.setStyle(inactiveTabStyle);
        });

        HBox tabBar = new HBox(0, bodyTabBtn, paramsTabBtn);
        tabBar.setAlignment(Pos.CENTER_LEFT);
        tabBar.setPadding(new Insets(0, 0, 4, 0));

        // -- StackPane holds both panes, toggle controls visibility --
        StackPane contentStack = new StackPane(bodyPane, paramsPane);
        contentStack.setAlignment(Pos.TOP_LEFT);

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

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #252525; -fx-text-fill: #E6E6E6; -fx-font-size: 12px; -fx-font-weight: 500; -fx-border-color: #3A3A3A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> showSaveDialog());

        Button shareBtn = new Button("Share");
        shareBtn.setStyle("-fx-background-color: #252525; -fx-text-fill: #E6E6E6; -fx-font-size: 12px; -fx-font-weight: 500; -fx-border-color: #3A3A3A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        shareBtn.setOnAction(e -> shareCurrentRequest());

        HBox urlBar = new HBox(8, urlField, saveBtn, shareBtn, sendBtn);
        urlBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(urlField, Priority.ALWAYS);

        topBarIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/image.png")));
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

        VBox requestSection = new VBox(8, requestLabel, tabBar, contentStack, captureSection);
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
                    String urlWithParams = appendQueryParams(rawUrl);
                    String resolvedUrl = templateEngine.process(urlWithParams);
                    if (resolvedUrl.contains("{{")) {
                        throw new IllegalArgumentException("Unresolved URL template variables.");
                    }
                    Object payload;
                    if (selectedFile != null) {
                        String bodyText = requestBody.getText();
                        payload = new MultipartPayload(selectedFile, bodyText);
                    } else {
                        payload = requestBody.getText();
                    }
                    RequestIntent intent = new RequestIntent(
                        RequestIntent.Method.valueOf(methodBox.getValue()),
                        URI.create(resolvedUrl),
                        payload,
                        authBox.getValue(),
                        false,
                        envBox.getValue().toLowerCase()
                    );

                    Map<String, String> captures = buildCaptures(captureField.getText());
                    ExecutionResult result = intendService.executeRequestWithResult(intent, captures);

                    // Pretty-print the body if it's JSON
                    String prettyBody = prettyPrint(result.body());

                    Platform.runLater(() -> {
                        if (result.statusCode() > 0) {
                            // Build rich display: status category + time + size + body
                            StringBuilder display = new StringBuilder();
                            display.append(prettyBody);
                            responseArea.setText(display.toString());
                            updateStatusLabel(result.statusCode(), result.statusCategory(),
                                    result.timeMs(), result.sizeBytes());
                        } else {
                            // Error result (timeout, DNS, SSL, etc.)
                            responseArea.setText(result.body());
                            statusLabel.setText(result.statusCategory());
                            statusLabel.setTextFill(Color.web("#FF3B3B"));
                        }
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
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/image.png")));
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(ImageIO.read(getClass().getResourceAsStream("/icons/image.png")));
                }
            }
        } catch (Exception ignored) {}
        stage.setScene(scene);
        stage.show();
    }

    private void refreshHistory() {
        historyList.getItems().setAll(intendService.getHistory().getAll());
    }

    private String prettyPrint(String text) {
        if (text == null) return "";
        try {
            if (text.contains("{")) {
                int start = text.indexOf("{");
                Object obj = mapper.readValue(text.substring(start), Object.class);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }
        } catch (Exception ignored) {
            return text;
        }
        return text;
    }

    private void updateStatusLabel(int statusCode, String category, long timeMs, long sizeBytes) {
        String sizeStr = sizeBytes < 1024
                ? sizeBytes + " B"
                : sizeBytes < 1024 * 1024
                    ? String.format("%.1f KB", sizeBytes / 1024.0)
                    : String.format("%.1f MB", sizeBytes / (1024.0 * 1024));

        statusLabel.setText(String.format("%d %s  •  %d ms  •  %s", statusCode, category, timeMs, sizeStr));

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
        settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/image.png")));

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

    // ── Save & Share ─────────────────────────────────────────

    private void showSaveDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Request");
        dialog.setHeaderText(null);
        dialog.setContentText("Request name:");
        dialog.setGraphic(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/intend-theme.css").toExternalForm());
        pane.setStyle("-fx-background-color: #1E1E1E;");
        pane.lookup(".content").setStyle("-fx-background-color: #1E1E1E;");

        pane.lookupAll(".button").forEach(node ->
            node.setStyle("-fx-background-color: #252525; -fx-text-fill: #E6E6E6; "
                + "-fx-border-color: #3A3A3A; -fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-min-height: 32; -fx-padding: 6 16; -fx-cursor: hand;")
        );
        pane.lookupAll(".button-bar").forEach(node ->
            node.setStyle("-fx-background-color: #1E1E1E;")
        );

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                SavedRequestRepository.SavedRequest saved = new SavedRequestRepository.SavedRequest(
                    name.trim(),
                    methodBox.getValue(),
                    urlField.getText(),
                    requestBody.getText(),
                    authBox.getValue().name(),
                    envBox.getValue(),
                    buildCurrentParams()
                );
                intendService.getSavedRequests().save(saved);
                refreshSavedRequests();
                statusLabel.setText("Saved: " + name.trim());
                statusLabel.setTextFill(Color.web("#4ADE80"));
            }
        });
    }

    private void shareCurrentRequest() {
        SavedRequestRepository.SavedRequest temp = new SavedRequestRepository.SavedRequest(
            "Shared Request",
            methodBox.getValue(),
            urlField.getText(),
            requestBody.getText(),
            authBox.getValue().name(),
            envBox.getValue(),
            buildCurrentParams()
        );
        String json = intendService.getSavedRequests().toJson(temp);
        ClipboardContent content = new ClipboardContent();
        content.putString(json);
        Clipboard.getSystemClipboard().setContent(content);
        statusLabel.setText("Request copied to clipboard");
        statusLabel.setTextFill(Color.web("#4ADE80"));
    }

    private void loadSavedRequest(SavedRequestRepository.SavedRequest saved) {
        methodBox.setValue(saved.method());
        urlField.setText(saved.url());
        requestBody.setText(saved.body() != null ? saved.body() : "");
        try {
            authBox.setValue(RequestIntent.AuthStrategy.valueOf(saved.auth()));
        } catch (Exception ignored) {
            authBox.setValue(RequestIntent.AuthStrategy.NONE);
        }
        if (saved.env() != null) {
            envBox.setValue(saved.env().toUpperCase());
        }
        // Load query params into the params table
        paramRows.clear();
        if (saved.params() != null && !saved.params().isEmpty()) {
            // Find the paramsTable VBox inside paramsPane
            VBox paramsTable = (VBox) paramsPane.getChildren().get(1);
            paramsTable.getChildren().clear();
            for (Map.Entry<String, String> entry : saved.params().entrySet()) {
                addParamRow(paramsTable);
                ParamRow lastRow = paramRows.get(paramRows.size() - 1);
                lastRow.key().setText(entry.getKey());
                lastRow.value().setText(entry.getValue());
            }
        }
    }

    private void refreshSavedRequests() {
        savedList.getItems().setAll(intendService.getSavedRequests().getAll());
    }

    private Map<String, String> buildCurrentParams() {
        Map<String, String> params = new LinkedHashMap<>();
        for (ParamRow row : paramRows) {
            if (row.enabled().isSelected()) {
                String key = row.key().getText();
                String value = row.value().getText();
                if (key != null && !key.isBlank()) {
                    params.put(key.trim(), value != null ? value.trim() : "");
                }
            }
        }
        return params;
    }

    // ── Query Params ──────────────────────────────────────────

    private HBox buildParamsHeader() {
        Label keyHeader = new Label("Key");
        keyHeader.setStyle("-fx-text-fill: #808080; -fx-font-size: 11px; -fx-font-weight: 600;");
        keyHeader.setMinWidth(28);
        HBox.setHgrow(keyHeader, Priority.ALWAYS);

        Label valueHeader = new Label("Value");
        valueHeader.setStyle("-fx-text-fill: #808080; -fx-font-size: 11px; -fx-font-weight: 600;");
        HBox.setHgrow(valueHeader, Priority.ALWAYS);

        Region spacer = new Region();
        spacer.setMinWidth(46);

        HBox header = new HBox(8, spacer, keyHeader, valueHeader);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 2, 0));
        return header;
    }

    private void addParamRow(VBox paramsTable) {
        CheckBox enabled = new CheckBox();
        enabled.setSelected(true);

        TextField keyField = new TextField();
        keyField.setPromptText("Key");
        keyField.setStyle("-fx-font-size: 13px;");
        HBox.setHgrow(keyField, Priority.ALWAYS);

        TextField valueField = new TextField();
        valueField.setPromptText("Value");
        valueField.setStyle("-fx-font-size: 13px;");
        HBox.setHgrow(valueField, Priority.ALWAYS);

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #606060; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 4 8; -fx-min-width: 28;");
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF3B3B; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 4 8; -fx-min-width: 28;"));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #606060; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 4 8; -fx-min-width: 28;"));

        ParamRow row = new ParamRow(enabled, keyField, valueField);
        paramRows.add(row);

        HBox rowBox = new HBox(8, enabled, keyField, valueField, removeBtn);
        rowBox.setAlignment(Pos.CENTER_LEFT);

        removeBtn.setOnAction(e -> {
            if (paramRows.size() > 1) {
                paramRows.remove(row);
                paramsTable.getChildren().remove(rowBox);
            }
        });

        paramsTable.getChildren().add(rowBox);
    }

    private String appendQueryParams(String baseUrl) {
        StringBuilder query = new StringBuilder();
        for (ParamRow row : paramRows) {
            if (row.enabled().isSelected()) {
                String key = row.key().getText();
                String value = row.value().getText();
                if (key != null && !key.isBlank()) {
                    if (!query.isEmpty()) {
                        query.append('&');
                    }
                    query.append(URLEncoder.encode(key.trim(), StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(value != null ? value.trim() : "", StandardCharsets.UTF_8));
                }
            }
        }
        if (query.isEmpty()) {
            return baseUrl;
        }
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + query;
    }

    @Override
    public void stop() {
        applicationContext.close();
    }
}
