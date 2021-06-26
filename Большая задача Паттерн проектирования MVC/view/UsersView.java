package com.javarush.task.task36.task3608.view;

import com.javarush.task.task36.task3608.controller.Controller;
import com.javarush.task.task36.task3608.model.ModelData;

public class UsersView implements View {

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    private Controller controller;

    public void fireEventShowAllUsers() {
        controller.onShowAllUsers();
    }

    public void fireEventShowDeletedUsers() {
        controller.onShowAllDeletedUsers();
    }

    public void fireEventOpenUserEditForm(long id) {
        controller.onOpenUserEditForm(id);
    }

    @Override
    public void refresh(ModelData modelData) {
        if (!modelData.isDisplayDeletedUserList()) {
            System.out.println("All users:");
            modelData.getUsers().forEach(e -> System.out.println("\t" + e));
        } else {
            System.out.println("All deleted users:");
            modelData.getUsers().forEach(e -> System.out.println("\t" + e));
        }
        System.out.println("===================================================");
    }
}
