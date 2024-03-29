package home;

import dataParser.DataParser;
import dataParser.NGSIConverter;
import guimodel.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.*;
import modelingEntities.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;


public class HomeController implements Initializable {


    @FXML
    private Button btnPackages;


    @FXML
    private Button btnSimulate;

    @FXML
    private Button btnModeling;

    @FXML
    private Pane pnlOrders;


    @FXML
    private Pane pnlSmlSettings;
    @FXML
    private Pane pnlModeling;

    @FXML
    private Button btnaddDevice;
    @FXML
    private Button btnaddApp;
    @FXML
    private Button btnDeleteEntity;
    @FXML
    private Button btnSaveEntities;

    @FXML
    private Pane pnlDraw;


    @FXML
    private TextField commChannelLossRT;

    @FXML
    private TextField commChannelLossTS;

    @FXML
    private TextField commChannelLossVS;

    @FXML
    private TextField commChannelLossAN;

    @FXML
    private ChoiceBox bandwidthPolicy;

    @FXML
    private TextField brokerCapacity;

    @FXML
    private TextField systemBandwidth;

    @FXML
    private TextField durationField;

    @FXML
    private TextField aliasField;

    @FXML
    private TextField messageField;


    @FXML
    private TextField urlField;
    @FXML
    private Text dirPathId;

    @FXML
    private Text dataPathId;
    private final SystemSpecifications systemSpecifications = new SystemSpecifications();
    @FXML
    private ObservableList<DeviceEntity> deviceEntityList;
    @FXML
    private ObservableList<BrokerEntity> brokerEntityList;
    @FXML
    private ObservableList<ApplicationEntity> applicationEntityList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        if (!systemSpecifications.loadSystemSpecifications())
            createSystemSpecifications();
        initializeSystemSpecifications();


        initializeModelingPane();

    }

    private void loadEntities() {

        pnlDraw.getChildren().clear();
        int NUM_ROWS = 80;
        int NUM_COLS = 80;
        int CELL_SIZE = 20;

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                Rectangle cell = new Rectangle(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                cell.setStroke(Color.GRAY);
                cell.setFill(Color.WHITE);
                pnlDraw.getChildren().add(cell);
            }
        }
        BrokerEntity entity = new BrokerEntity();
        pnlDraw.getChildren().add(entity);

        deviceEntityList = FXCollections.observableArrayList();
        deviceEntityList.addAll(DataParser.readEntityFromCsv("devices", DeviceEntity.class));
        applicationEntityList = FXCollections.observableArrayList();
        applicationEntityList.addAll(DataParser.readEntityFromCsv("applications", ApplicationEntity.class));
        ObservableList<ApplicationCategory> applicationCategories = FXCollections.observableArrayList();
        applicationCategories.addAll(DataParser.readModelFromCSv("applicationCategories", ApplicationCategory.class));
        List<String> colors = Arrays.asList("#e6194b", "#3cb44b", "#ffe119", "#0882c8", "#558231", "#911eb4", "#46f0f0", "#f032e6", "#d2f53c", "#fabebe");
        HashMap<String, String> categoryColorHashMap = new HashMap<>();
        for (int i = 0; i < applicationCategories.size(); i++) {
            categoryColorHashMap.put(applicationCategories.get(i).getId(), colors.get(i % 10));
        }

        HashMap<String, String> applicationCategoryHashMap = new HashMap<>();
        for (ApplicationCategory applicationCategory : applicationCategories) {
            applicationCategoryHashMap.put(applicationCategory.getId(), applicationCategory.getCode());
        }
        ArrayList<Observation> observations = DataParser.readModelFromCSv("observations", Observation.class);
        HashMap<String, String> observationHashMap = new HashMap<>();
        for (Observation observation : observations) {
            observationHashMap.put(observation.getId(), observation.getName());
        }
        for (DeviceEntity deviceEntity : deviceEntityList) {
            deviceEntity.setEntityName(deviceEntity.getDevice().getName());
            if (deviceEntity.getArrow() != null) {

                StringBuilder Names = new StringBuilder();
                deviceEntity.getDevice().getCapturesObservation()
                        .forEach(observation -> Names.append(observationHashMap.get(observation)).append(" "));
                deviceEntity.getArrow().getLabel().setText(String.valueOf(Names));
                deviceEntity.splitArrow();
            }
            for (Node node : deviceEntity.getChildren()) {
                node.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        AddDeviceController controller = showPanel("AddDevice.fxml", "Device").getController();
                        controller.initData(deviceEntity.getDevice(), deviceEntity.getTranslateX(), deviceEntity.getTranslateY());
                    }

                });
            }

            pnlDraw.getChildren().add(deviceEntity);

        }
        for (ApplicationEntity applicationEntity : applicationEntityList) {
            applicationEntity.setEntityName(applicationEntity.getApplication().getName());
            applicationEntity.getApplicationCategoryLabel().setText(applicationCategoryHashMap.get(applicationEntity.getApplication().getApplicationCategory()));

            if (applicationEntity.getArrow() != null) {
                StringBuilder Names = new StringBuilder();
                applicationEntity.getApplication().getReceivesObservation()
                        .forEach(observation -> Names.append(observationHashMap.get(observation)).append(" "));
                applicationEntity.getArrow().getLabel().setText(String.valueOf(Names));
                applicationEntity.getRectangle().setStyle("-fx-fill: " + categoryColorHashMap.get(applicationEntity.getApplication().getApplicationCategory()) + ";");
                applicationEntity.getApplicationCategoryLabel().setPrefWidth(22);
                applicationEntity.getApplicationCategoryLabel().setPrefHeight(22);
                applicationEntity.getApplicationCategoryLabel().setTranslateX(applicationEntity.getRectangle().getWidth() * 0.7);

                applicationEntity.splitArrow();
            }
            for (Node node : applicationEntity.getChildren()) {
                if (node instanceof Arrow)
                    continue;
                node.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        AddAppController controller = showPanel("AddApp.fxml", "Application").getController();
                        controller.initData(applicationEntity.getApplication(), applicationEntity.getTranslateX(), applicationEntity.getTranslateY());

                    }
                });
            }
            pnlDraw.getChildren().add(applicationEntity);
        }

    }

    private void initializeModelingPane() {

        btnaddDevice.setOnAction(e -> openAddDevice());
        btnaddApp.setOnAction(e -> openAddApp());

        btnDeleteEntity.setOnAction(e -> {
            Thread backgroundThread = new Thread(() -> {
                for (Node node : pnlDraw.getChildren()) {
                    if (node instanceof BaseEntity && !((BaseEntity) node).isSelected) {
                        continue;
                    }
                    if (node instanceof DeviceEntity) {
                        DeviceEntity deviceEntity = (DeviceEntity) node;
                        DataParser.deleteFromCsv("devices", deviceEntity.getDevice().getId());
                        for (Object observation : DataParser.readModelFromCSv("observations", Observation.class)) {
                            Observation obs = (Observation) observation;
                            obs.getIsCapturedBy().remove(deviceEntity.getDevice().getId());
                            DataParser.addToCsv("observations", obs.toString());
                        }
                    } else if (node instanceof ApplicationEntity) {
                        ApplicationEntity applicationEntity = (ApplicationEntity) node;

                        for (Object observation : DataParser.readModelFromCSv("observations", Observation.class)) {
                            Observation obs = (Observation) observation;
                            obs.getIsReceivedBy().remove(applicationEntity.getApplication().getId());
                            DataParser.addToCsv("observations", obs.toString());
                        }


                        DataParser.deleteFromCsv("applications", applicationEntity.getApplication().getId());
                    }
                }
                Platform.runLater(this::loadEntities);
            });

            backgroundThread.start();
        });
        btnSaveEntities.setOnAction(e -> {
            Thread backgroundThread = new Thread(() -> {
                for (Node node : pnlDraw.getChildren()) {
                    if (node instanceof DeviceEntity) {
                        DeviceEntity deviceEntity = (DeviceEntity) node;
                        DataParser.addToCsv("devices", deviceEntity.toString());
                    } else if (node instanceof ApplicationEntity) {
                        ApplicationEntity applicationEntity = (ApplicationEntity) node;
                        DataParser.addToCsv("applications", applicationEntity.toString());
                    }
                }
                Platform.runLater(this::loadEntities);
            });
            backgroundThread.start();
        });
        loadEntities();

    }


    public void initializeSystemSpecifications() {
        String[] bandwidthPolicies = {"none", "shared", "max_min"};
        commChannelLossRT.setText(String.valueOf(systemSpecifications.getCommChannelLossRT()));
        commChannelLossTS.setText(String.valueOf(systemSpecifications.getCommChannelLossTS()));
        commChannelLossVS.setText(String.valueOf(systemSpecifications.getCommChannelLossVS()));
        commChannelLossAN.setText(String.valueOf(systemSpecifications.getCommChannelLossAN()));
        bandwidthPolicy.getItems().addAll(bandwidthPolicies);
        bandwidthPolicy.setValue(systemSpecifications.getBandwidthPolicy());
        brokerCapacity.setText(String.valueOf(systemSpecifications.getBrokerCapacity()));
        systemBandwidth.setText(String.valueOf(systemSpecifications.getSystemBandwidth()));
        durationField.setText(String.valueOf(systemSpecifications.getSimulationDuration()));
        aliasField.setText(systemSpecifications.getAlias());
        messageField.setText(String.valueOf(systemSpecifications.getGlobalMessageSize()));


    }

    public void createSystemSpecifications() {
        systemSpecifications.setCommChannelLossRT(0);
        systemSpecifications.setCommChannelLossTS(0);
        systemSpecifications.setCommChannelLossVS(0);
        systemSpecifications.setCommChannelLossAN(0);
        systemSpecifications.setBandwidthPolicy("none");
        systemSpecifications.setBrokerCapacity(Integer.MAX_VALUE);
        systemSpecifications.setSystemBandwidth(10);
        systemSpecifications.setSimulationDuration(10);
        systemSpecifications.setAlias("default");
        systemSpecifications.setGlobalMessageSize(1);
        systemSpecifications.saveSystemSpecifications();

    }

    public void handleClicks(ActionEvent actionEvent) {
        if (actionEvent.getSource() == btnSimulate) {
            pnlSmlSettings.toFront();
        }
        if (actionEvent.getSource() == btnPackages) {
            pnlOrders.toFront();
        }
        if (actionEvent.getSource() == btnModeling) {
            pnlModeling.toFront();
            for (DeviceEntity deviceEntity : deviceEntityList) {
                deviceEntity.getArrow().updateLabelPosition();
            }
        }
    }

    public void saveSystemSpecifications() {
        systemSpecifications.setSystemBandwidth(Double.parseDouble(systemBandwidth.getText()));
        systemSpecifications.setBandwidthPolicy((String) bandwidthPolicy.getValue());
        systemSpecifications.setBrokerCapacity(Integer.parseInt(brokerCapacity.getText()));
        systemSpecifications.setCommChannelLossAN(Double.parseDouble(commChannelLossAN.getText()));
        systemSpecifications.setCommChannelLossRT(Double.parseDouble(commChannelLossRT.getText()));
        systemSpecifications.setCommChannelLossTS(Double.parseDouble(commChannelLossTS.getText()));
        systemSpecifications.setCommChannelLossVS(Double.parseDouble(commChannelLossVS.getText()));
        systemSpecifications.setSimulationDuration(Integer.parseInt(durationField.getText()));
        systemSpecifications.setAlias(aliasField.getText());
        systemSpecifications.setGlobalMessageSize( Double.parseDouble(messageField.getText()));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (systemSpecifications.saveSystemSpecifications())
            alert.setContentText("Settings saved successfully");
        else
            alert.setContentText("Settings not saved ");
        alert.showAndWait();

    }

    public String openFileChooser() {
        String path;
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose location");
        File defaultDirectory = new File(System.getProperty("user.dir"));
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(new Stage());
        if (selectedDirectory == null) {
            System.out.println("No Directory selected");
            return null;
        }
        path = selectedDirectory.getPath();
        System.out.println("path: " + path);
        return path;
    }

    public String openCsvChooser() {
        String path;
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose output Location");
        File defaultDirectory = new File(System.getProperty("user.dir"));
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(new Stage());
        path = selectedDirectory.getPath();
        return path;
    }


    public void chooseDatamodelFolder() {
        String path = openFileChooser();
        if (path != null && !path.isEmpty())
            dataPathId.setText(path);

    }

    public void chooseDestinationFile() {
        String path = openCsvChooser();
        if (path != null && !path.isEmpty())
            dirPathId.setText(path);

    }

    public String getData(String baseUrl) throws IOException {
        String[] entityTypes = {"Device", "ApplicationCategory", "Observation", "Application"};
        Path tempDir = Files.createTempDirectory("tempData");
        for (String type : entityTypes) {
            URL url = new URL(baseUrl + "?type=" + type);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Link", "<https://raw.githubusercontent.com/SAMSGBLab/edict--datamodels/main/context.jsonld>");

            if (conn.getResponseCode() == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONArray entities = new JSONArray(response);

                for (int i = 0; i < entities.length(); i++) {
                    JSONObject entity = entities.getJSONObject(i);

                    String entityId = entity.getString("id");
                    entityId = entityId.replace(":", "_");
                    switch (type) {
                        case "Observation":
                            type = "observations";
                            break;
                        case "ApplicationCategory":
                            type = "applicationCategories";
                            break;
                        case "Application":
                            type = "applications";
                            break;
                        case "Device":
                            type = "devices";
                            break;
                    }
                    Path dirPath = tempDir.resolve(type);

                    if (!Files.exists(dirPath)) {
                        Files.createDirectory(dirPath);
                    }

                    Path filePath = dirPath.resolve(entityId + ".jsonld");

                    Files.write(filePath, entity.toString().getBytes());
                }
            }
            else {
                throw new IOException("Failed to get data from url");
            }

            conn.disconnect();
        }
        return tempDir.toAbsolutePath().toString();

    }

    public void simulate() {
        Alert alert = new Alert(AlertType.INFORMATION);
        Alert error = new Alert(AlertType.ERROR);

        if ((dataPathId.getText().isEmpty() && urlField.getText().trim().isEmpty()) || dirPathId.getText().isEmpty()) {
            error.setContentText("Please choose a valid path");
            error.showAndWait();
            return;
        }
        String dataPath = dataPathId.getText();
        if (!urlField.getText().trim().isEmpty()) {
            try {
                Platform.runLater(() -> {
                    alert.setContentText("getting data from url");
                    alert.showAndWait();
                });
                dataPath = getData(urlField.getText().trim());

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    error.setContentText("Failed to get data from url");
                    error.showAndWait();
                });
                return;

            }
        }
        String jarPath = "iotsimulator.jar";
        int simulationDuration = durationField.getText().isEmpty() ? 0 : Integer.parseInt(durationField.getText());
        String alias = aliasField.getText();
        double globalMessageSize = messageField.getText().isEmpty() ? 0 : Double.parseDouble(messageField.getText());
        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(jarPath);
            command.add(dataPath);
            command.add(dirPathId.getText());
            command.add(String.valueOf(simulationDuration));
            command.add(alias);
            command.add(String.valueOf(globalMessageSize));
            Platform.runLater(() -> {
                alert.setContentText("Simulation started");
                if (!alert.isShowing())
                    alert.showAndWait();
            });
            Thread t = new Thread(() -> {
                ProcessBuilder pb = new ProcessBuilder(command);
                try {
                    Process process = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String currentLine = line;
                        Platform.runLater(() -> {
                            System.out.println(currentLine);
                            if (!alert.isShowing())
                                alert.showAndWait();
                            alert.setContentText(currentLine);

                        });
                    }
                    while ((line = errorReader.readLine()) != null) {
                        final String currentLine = line;
                        Platform.runLater(() -> {
                            System.out.println(currentLine);
                            if (!alert.isShowing())
                                alert.showAndWait();
                            alert.setContentText(currentLine);

                        });
                    }

                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        Platform.runLater(() -> {
                            alert.setContentText("Simulation finished successfully");
                            if (!alert.isShowing())
                                alert.showAndWait();

                        });
                    } else {
                        Platform.runLater(() -> {

                            Alert alertError = new Alert(AlertType.ERROR);
                            alertError.setContentText("Simulation failed");
                            alertError.showAndWait();
                            alert.close();
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    String chooseNGSIOutput() {
        String path = openFileChooser();
        if (path != null && !path.isEmpty())
            return path;
        return null;
    }

    @FXML
    void generateNGSI(ActionEvent event) {
        String path = chooseNGSIOutput();
        if (path == null || path.isEmpty()) {
            File dir = new File("output");
            dir.mkdirs();
            path = "output";
        }
        NGSIConverter.generateNGSIfromCsv(path);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText("NGSI files generated successfully in :" + path);
        alert.showAndWait();

    }

    FXMLLoader showPanel(String resource, String type) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            List<Window> windows = Stage.getWindows().stream().filter(Window::isShowing).collect(Collectors.toList());
            fxmlLoader.setLocation((getClass().getResource("/fxml/" + resource)));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Add " + type);
            stage.setScene(new Scene(root));
            stage.initOwner(windows.get(0));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> loadEntities());
            stage.setOnHidden(paramT -> loadEntities());
            stage.show();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return fxmlLoader;
    }


    @FXML
    public void openAddApp() {
        showPanel("AddApp.fxml", "Application").getController();
    }


    @FXML
    public void openAddDevice() {
        showPanel("AddDevice.fxml", "Device").getController();
    }

    @FXML
    public void ImportModel() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            try {
                Path source = selectedDirectory.toPath();
                Path destination = Paths.get(System.getProperty("user.dir"), "data");
                moveDirectory(source, destination);
                loadEntities();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    public void ExportModel() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            try {
                Path source = Paths.get(System.getProperty("user.dir"), "data");
                Path destination = selectedDirectory.toPath().resolve("data");
                Files.createDirectories(destination);
                moveDirectory(source, destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveDirectory(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = destination.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @FXML
    public void DeleteModel() {
        try {
            Path dataDirectory = Paths.get(System.getProperty("user.dir"), "data");
            Files.walkFileTree(dataDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            loadEntities();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
