package com.ibasco.sourcebuddy.gui.skins;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.TaskProgressView;

public class CustomTaskProgressViewSkin<T extends Task<?>> extends SkinBase<TaskProgressView<T>> {

    public CustomTaskProgressViewSkin(TaskProgressView<T> monitor) {
        super(monitor);

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("box");

        // list view
        JFXListView<T> listView = new JFXListView<>();
        listView.setPrefSize(500, 400);
        listView.setPlaceholder(new Label("No tasks running"));
        listView.setCellFactory(param -> new TaskCell());
        listView.setFocusTraversable(false);

        Bindings.bindContent(listView.getItems(), monitor.getTasks());
        borderPane.setCenter(listView);

        getChildren().add(listView);
    }

    class TaskCell extends ListCell<T> {

        private JFXProgressBar progressBar;

        private Label titleText;

        private Label messageText;

        private Button cancelButton;

        private T task;

        private BorderPane borderPane;

        public TaskCell() {
            titleText = new Label();
            titleText.getStyleClass().add("task-title");

            messageText = new Label();
            messageText.getStyleClass().add("task-message");

            progressBar = new JFXProgressBar();
            progressBar.setSecondaryProgress(-1);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setMaxHeight(8);
            progressBar.getStyleClass().add("task-progress-bar");

            cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().add("task-cancel-button");
            cancelButton.setTooltip(new Tooltip("Cancel Task"));
            cancelButton.setOnAction(evt -> {
                if (task != null) {
                    task.cancel();
                }
            });

            VBox vbox = new VBox();
            vbox.setSpacing(4);
            vbox.getChildren().add(titleText);
            vbox.getChildren().add(progressBar);
            vbox.getChildren().add(messageText);

            BorderPane.setAlignment(cancelButton, Pos.CENTER);
            BorderPane.setMargin(cancelButton, new Insets(0, 0, 0, 4));

            borderPane = new BorderPane();
            borderPane.setCenter(vbox);
            borderPane.setRight(cancelButton);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);

            /*
             * I have no idea why this is necessary but it won't work without
             * it. Shouldn't the updateItem method be enough?
             */
            if (index == -1) {
                setGraphic(null);
                getStyleClass().setAll("task-list-cell-empty");
            }
        }

        @Override
        protected void updateItem(T task, boolean empty) {
            super.updateItem(task, empty);

            this.task = task;

            if (empty || task == null) {
                getStyleClass().setAll("task-list-cell-empty");
                setGraphic(null);
            } else if (task != null) {
                getStyleClass().setAll("task-list-cell");
                progressBar.progressProperty().bind(task.progressProperty());
                titleText.textProperty().bind(task.titleProperty());
                messageText.textProperty().bind(task.messageProperty());
                cancelButton.disableProperty().bind(
                        Bindings.not(task.runningProperty()));

                Callback<T, Node> factory = getSkinnable().getGraphicFactory();
                if (factory != null) {
                    Node graphic = factory.call(task);
                    if (graphic != null) {
                        BorderPane.setAlignment(graphic, Pos.CENTER);
                        BorderPane.setMargin(graphic, new Insets(0, 4, 0, 0));
                        borderPane.setLeft(graphic);
                    }
                } else {
                    /*
                     * Really needed. The application might have used a graphic
                     * factory before and then disabled it. In this case the border
                     * pane might still have an old graphic in the left position.
                     */
                    borderPane.setLeft(null);
                }

                setGraphic(borderPane);
            }
        }
    }
}
