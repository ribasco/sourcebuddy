package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.controllers.FragmentController;
import com.ibasco.sourcebuddy.controllers.fragments.AppDetailsController;
import com.ibasco.sourcebuddy.gui.tableview.cells.SteamAppDetailsCell;
import com.ibasco.sourcebuddy.gui.tableview.cells.ViewFragmentCell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
class ViewManagerTest {

    private static final Logger log = LoggerFactory.getLogger(ViewManagerTest.class);

    @Test
    void test01() throws Exception {
        //Constructor<SteamAppDetailsCell> ctr = ReflectionUtils.accessibleConstructor(SteamAppDetailsCell.class, AppDetailsController.class);

        log.info("Got: {}", loadViewFragmentCell(SteamAppDetailsCell.class, "test", AppDetailsController.class));

    }

    public <A, B, C extends FragmentController, D extends ViewFragmentCell<A, B, C>> D loadViewFragmentCell(Class<D> cls, String viewFragmentName, Class<C> controllerClass) throws Exception {
        //noinspection unchecked
        Constructor<D> a = cls.getDeclaredConstructor();
        a.setAccessible(true);
        Method m = cls.getSuperclass().getDeclaredMethod("setController", FragmentController.class);

        C dc = (C) new AppDetailsController();
        D cell = a.newInstance();

        //cell.setController(dc);
        return cell;
    }
}