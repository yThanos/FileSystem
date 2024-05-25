package com.so.filesystem.test;

import javax.swing.*;

import com.so.filesystem.Archive;

import java.awt.*;
import java.awt.event.ActionListener;

public class ArchivePanel extends JPanel{
    private JTextField nameField;
    private JTextField extField;
    private JTextField posField;
    private JTextField sizeField;
    private JButton saveButton;
    private JButton editButton;
    private JButton deleteButton;

    public ArchivePanel(Archive archive, ActionListener saveListener, ActionListener editListener, ActionListener deleteListener) {
        setLayout(new GridLayout(1, 7));

        nameField = new JTextField(archive.getName());
        extField = new JTextField(archive.getExt());
        posField = new JTextField(String.valueOf(archive.getPos()));
        sizeField = new JTextField(String.valueOf(archive.getSize()));

        saveButton = new JButton("Salvar");
        editButton = new JButton("Editar");
        deleteButton = new JButton("Excluir");

        saveButton.addActionListener(saveListener);
        editButton.addActionListener(editListener);
        deleteButton.addActionListener(deleteListener);

        add(nameField);
        add(extField);
        add(posField);
        add(sizeField);
        add(saveButton);
        add(editButton);
        add(deleteButton);
    }

    public String getArchiveName() {
        return nameField.getText();
    }

    public String getArchiveExt() {
        return extField.getText();
    }

    public int getArchivePos() {
        try {
            return Integer.parseInt(posField.getText());
        } catch (NumberFormatException e) {
            return 0; // Valor padrão em caso de erro
        }
    }

    public int getArchiveSize() {
        try {
            return Integer.parseInt(sizeField.getText());
        } catch (NumberFormatException e) {
            return 0; // Valor padrão em caso de erro
        }
    }

    public void setEditable(boolean editable) {
        nameField.setEditable(editable);
        extField.setEditable(editable);
        posField.setEditable(editable);
        sizeField.setEditable(editable);
    }
}
