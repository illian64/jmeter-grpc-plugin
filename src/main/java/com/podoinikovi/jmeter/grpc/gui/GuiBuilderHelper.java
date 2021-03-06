package com.podoinikovi.jmeter.grpc.gui;

import lombok.experimental.UtilityClass;

import javax.swing.*;
import java.awt.*;

@UtilityClass
public class GuiBuilderHelper {
    public static JScrollPane getTextAreaScrollPaneContainer(JTextArea textArea, int nbLines) {
        JScrollPane ret = new JScrollPane();
        textArea.setRows(nbLines);
        textArea.setColumns(20);
        ret.setViewportView(textArea);
        return ret;
    }

    public static void strechItemToComponent(JComponent component, JComponent item) {
        int iWidth = (int)item.getPreferredSize().getWidth();
        int iHeight = (int)component.getPreferredSize().getHeight();
        item.setPreferredSize(new Dimension(iWidth, iHeight));
    }

    public static JPanel getComponentWithMargin(Component component, int top, int left, int bottom, int right) {
        JPanel ret = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0D;
        constraints.weighty = 1.0D;
        constraints.fill = 1;
        constraints.insets = new Insets(top, left, bottom, right);
        ret.add(component, constraints);
        return ret;
    }
}
