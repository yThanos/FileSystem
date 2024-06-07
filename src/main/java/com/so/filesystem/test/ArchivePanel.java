package com.so.filesystem.test;

import javax.swing.*;

import com.so.filesystem.model.Archive;

import java.awt.*;
import java.awt.event.ActionListener;

public class ArchivePanel extends JPanel{

    public ArchivePanel(Archive archive, ActionListener saveListener, ActionListener editListener, ActionListener deleteListener) {
        setLayout(new GridLayout(1, 7));

        JTextArea nameField = new JTextArea(archive.getName());
        JTextArea extField = new JTextArea(archive.getExt());
        JTextArea posField = new JTextArea(String.valueOf(archive.getPos()));
        JTextArea sizeField = new JTextArea(String.valueOf(archive.getSize()));

        JButton saveButton = new JButton("Salvar local");
        JButton editButton = new JButton("Append");
        JButton deleteButton = new JButton("Excluir");

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
}
