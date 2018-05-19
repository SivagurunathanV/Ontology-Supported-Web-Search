package com.sw.osws.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class RdfAutoComplete {
    public static void main(String[] args) throws Exception{
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run() {
                AutoCompletable<RenderItem, String> autoCompletable = new OSWSAutoComplete();
                final AutoCompleteBox combo = new AutoCompleteBox(autoCompletable);
                combo.setPreferredSize(new Dimension(1024, 20));
                JButton goButton = new JButton("Go!");
                goButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI("http://www.google.com/search?q=" +
                                    URLEncoder.encode(combo.getSelectedItem().toString(), "UTF-8")));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                goButton.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        combo.getEditor().getEditorComponent().requestFocus();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {

                    }
                });

                JFrame frame = new JFrame();
                frame.setLayout(new FlowLayout());
                frame.add(combo);
                frame.add(goButton);
                frame.pack();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}
