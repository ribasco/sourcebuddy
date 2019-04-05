package com.ibasco.sourcebuddy.controllers;

import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.net.URL;
import java.util.ResourceBundle;

abstract public class FragmentController implements Initializable {

    private Parent rootNode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public Parent getRootNode() {
        return rootNode;
    }

    public void setRootNode(Parent rootNode) {
        this.rootNode = rootNode;
    }
}
