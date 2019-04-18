package com.ibasco.sourcebuddy.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model for tree based data structure
 *
 * @param <T>
 *         The underlying type of the tree item
 */
public class TreeDataModel<T> {

    private ReadOnlyListWrapper<TreeDataModel<T>> children = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    private ObjectProperty<T> item = new SimpleObjectProperty<>();

    public TreeDataModel() {
        this(null);
    }

    public TreeDataModel(T item) {
        this.item.set(item);
    }

    public T getItem() {
        return item.get();
    }

    public ObjectProperty<T> itemProperty() {
        return item;
    }

    public void setItem(T item) {
        this.item.set(item);
    }

    public ObservableList<TreeDataModel<T>> getChildren() {
        return children.get();
    }

    public ReadOnlyListProperty<TreeDataModel<T>> childrenProperty() {
        return children.getReadOnlyProperty();
    }

    public ObservableList<T> toList() {
        ObservableList<T> lst = FXCollections.observableArrayList();
        lst.add(getItem());
        buildFlatItemListFromTree(lst, this);
        return lst;
    }

    private void buildFlatItemListFromTree(ObservableList<T> dataList, TreeDataModel<T> root) {
        for (TreeDataModel<T> child : root.getChildren()) {
            dataList.add(child.getItem());
            if (child.getChildren().size() > 0) {
                buildFlatItemListFromTree(dataList, child);
            }
        }
    }
}
