package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    final FileChooser fileChooser = new FileChooser();
    private Stage stage;
    boolean mapChanged;

    enum GameObject {
        EMPTY('0'),
        WALL('1'),
        FOOD('_'),
        XFOOD('*'),
        PACMAN('s'),
        RED_GHOST('r'),
        GREEN_GHOST('g'),
        BLUE_GHOST('b'),
        PURPLE_GHOST('p'),
        ;

        char mapSymbol;

        GameObject(char mapSymbol) {
            this.mapSymbol = mapSymbol;
        }

        public char getMapSymbol() {
            return mapSymbol;
        }

        public static GameObject getObject(char symbol) {
            for (GameObject o: GameObject.values()) {
                if (o.getMapSymbol() == symbol) return o;
            }
            return GameObject.EMPTY;
        }
    }

    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private GridPane mapGrid;
    @FXML
    private ComboBox gameObject;
    @FXML
    private Spinner<Double> scaleSpinner;

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(e -> onClose(e));
    }

    private void onClose(WindowEvent e) {
        if (!checkUnsavedChanges()) e.consume();
    }

    private boolean checkUnsavedChanges() {
        if (!mapChanged) return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Действие требует подтверждения");
        alert.setHeaderText("Текущая карта содержит несохранённые изменения!");

        ButtonType buttonTypeOne = new ButtonType("Сохранить");
        ButtonType buttonTypeTwo = new ButtonType("Не сохранять");
        ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            saveMap();
            return true;
        } else if (result.get() == buttonTypeTwo) {
            return true;
        } else {
            return false;
        }
    }

    public void saveMap() {
        int mapWidth = 0;
        int mapHeight = 0;

        for (Node node : mapGrid.getChildren()) {
            mapWidth = Math.max(GridPane.getColumnIndex(node) + 1, mapWidth);
            mapHeight = Math.max(GridPane.getRowIndex(node) + 1, mapHeight);
        }

        char[][] mapData = new char[mapHeight][mapWidth];
        for (Node node : mapGrid.getChildren()) {
            GameObject o = GameObject.EMPTY;
            if (node.getId() != null) {
                o = GameObject.valueOf(node.getId().toUpperCase());
            }
            mapData[mapHeight - GridPane.getRowIndex(node) - 1][GridPane.getColumnIndex(node)] = o.mapSymbol;
        }

        if (checkMap(mapData)) return;

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < mapData.length; i++) {
                out.write(mapData[i]);
                out.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mapChanged = false;
    }

    public void openMap(ActionEvent actionEvent) {
        if (!checkUnsavedChanges()) return;

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        ArrayList<String> list = new ArrayList<>();
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                list.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (list.size() == 0) return;

        int mapWidth = list.get(0).length();
        int mapHeight = list.size();

        width.setText(String.valueOf(mapWidth));
        height.setText(String.valueOf(mapHeight));
        mapGrid.getChildren().clear();

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < list.get(y).length(); x++) {
                char currentSymb = list.get(y).charAt(x);
                Button button = new Button();
                GameObject o = GameObject.getObject(currentSymb);
                if (o != GameObject.EMPTY) button.setId(o.toString().toLowerCase());
                mapGrid.add(button, x, mapHeight - y - 1);
            }
        }
        setMapActions();
        mapChanged = false;
    }

    public void createNewMap(ActionEvent actionEvent) {
        if (!checkUnsavedChanges()) return;

        int mapWidth = 0;
        int mapHeight = 0;

        try {
            mapWidth = Integer.parseInt(width.getText());
            mapHeight = Integer.parseInt(height.getText());
        } catch (NumberFormatException e) { }

        if (checkInput(mapWidth, mapHeight)) return;

        mapGrid.getChildren().clear();
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                mapGrid.add(new Button(), i, j);
            }
        }
        setMapActions();
        mapChanged = false;
    }

    private void setMapActions() {
        mapGrid.getChildren().forEach(node -> {
            node.setOnMouseClicked(event -> onMouseEvent(event));
            node.setOnMouseDragged(event -> onMouseEvent(event));
        });
    }

    private void onMouseEvent(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;
        Node node = event.getPickResult().getIntersectedNode();
        if (node == null || node.getParent() != mapGrid) return;
        if (event.isShiftDown()) {
            node.setId("empty");
        } else {
            node.setId(gameObject.getValue().toString().toLowerCase());
        }
        mapChanged = true;
    }

    private boolean checkInput(int mapWidth, int mapHeight) {
        StringBuilder errorMessage = new StringBuilder();
        if (mapWidth < 16) {
            errorMessage.append("Минимальная ширина: 5 ячеек.\n");
        }
        if (mapWidth > 99) {
            errorMessage.append("Максимальная ширина: 99 ячеек.\n");
        }
        if (mapHeight < 9) {
            errorMessage.append("Минимальная высота: 5 ячеек.\n");
        }
        if (mapHeight > 99) {
            errorMessage.append("Максимальная высота: 99 ячеек.\n");
        }

        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка создания карты");
            alert.setHeaderText("Ширина и высота указаны не корректно!");
            alert.setContentText(errorMessage.toString());

            alert.showAndWait();
        }

        return errorMessage.length() > 0;
    }

    private boolean checkMap(char[][] mapData) {
        StringBuilder errorMessage = new StringBuilder();

        int pacmanCounter = 0;
        int redGhostCounter = 0;
        int greenGhostCounter = 0;
        int blueGhostCounter = 0;
        int purpleGhostCounter = 0;

        for (int i = 0; i < mapData.length; i++) {
            for (int j = 0; j < mapData[i].length; j++) {
                if (mapData[i][j] == GameObject.PACMAN.getMapSymbol()) pacmanCounter++;
                if (mapData[i][j] == GameObject.RED_GHOST.getMapSymbol()) redGhostCounter++;
                if (mapData[i][j] == GameObject.GREEN_GHOST.getMapSymbol()) greenGhostCounter++;
                if (mapData[i][j] == GameObject.BLUE_GHOST.getMapSymbol()) blueGhostCounter++;
                if (mapData[i][j] == GameObject.PURPLE_GHOST.getMapSymbol()) purpleGhostCounter++;
            }
        }

        if (pacmanCounter != 1) errorMessage.append("На карте должен быть один пакман\n");
        if (redGhostCounter != 1) errorMessage.append("На карте должен быть один красный призрак\n");
        if (blueGhostCounter != 1) errorMessage.append("На карте должен быть один синий призрак\n");
        if (greenGhostCounter != 1) errorMessage.append("На карте должен быть один зеленый призрак\n");
        if (purpleGhostCounter != 1) errorMessage.append("На карте должен быть один фиолетовый призрак\n");

        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Карта содержит логические ошибки");
            alert.setHeaderText("Не корректное количество игровых объектов!");
            alert.setContentText(errorMessage.toString());

            alert.showAndWait();
        }

        return errorMessage.length() > 0;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (GameObject o:GameObject.values()) {
            if (o == GameObject.EMPTY) continue;
            gameObject.getItems().add(o);
        }

        width.setText("16");
        height.setText("9");
        gameObject.getSelectionModel().selectFirst();
        scaleSpinner.valueProperty().addListener((observable, oldValue, newValue) -> scaleMap(newValue));
        scaleSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.4, 1, 1, 0.1));
    }

    private void scaleMap(Double newValue) {
        mapGrid.setScaleX(newValue);
        mapGrid.setScaleY(newValue);
    }
}
