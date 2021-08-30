package com.podoinikovi.jmeter.grpc.gui;

import org.apache.jmeter.gui.GuiPackage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BrowseAction implements ActionListener {
    private final JTextField control;
    private boolean isDirectoryBrowse = false;
    private String lastPath = ".";

    public BrowseAction(JTextField filename) {
        this.control = filename;
    }

    public BrowseAction(JTextField filename, boolean isDirectoryBrowse) {
        this.control = filename;
        this.isDirectoryBrowse = isDirectoryBrowse;
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = this.getFileChooser();
        if (chooser != null && GuiPackage.getInstance() != null) {
            int returnVal = chooser.showOpenDialog(GuiPackage.getInstance().getMainFrame());
            if (returnVal == 0) {
                this.control.setText(chooser.getSelectedFile().getPath());
            }

            this.lastPath = chooser.getCurrentDirectory().getPath();
        }

    }

    protected JFileChooser getFileChooser() {
        JFileChooser ret = new JFileChooser(this.lastPath);
        if (this.isDirectoryBrowse) {
            ret.setFileSelectionMode(1);
        }

        return ret;
    }
}
