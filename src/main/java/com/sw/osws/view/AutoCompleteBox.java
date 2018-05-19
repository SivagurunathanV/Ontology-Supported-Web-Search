package com.sw.osws.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.ArrayList;

public class AutoCompleteBox extends JComboBox {
    private final AutoCompletable<RenderItem, String> autoCompletable;

    public AutoCompleteBox(final AutoCompletable<RenderItem, String> autoCompletable) {
        super();
        this.autoCompletable = autoCompletable;
        setEditable(true);
        Component c = getEditor().getEditorComponent();

        final JTextComponent tc = (JTextComponent)c;


        tc.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent arg0) {}

            public void insertUpdate(DocumentEvent arg0) {
                update();
            }

            public void removeUpdate(DocumentEvent arg0) {
                update();
            }

            public void update() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        List<RenderItem> items = new ArrayList<RenderItem>(autoCompletable.autoComplete(tc.getText()));
                        setEditable(false);
                        removeAllItems();

                        for (RenderItem item : items) {
                            addItem(item.getRenderText());
                            if (item.isDisplayDash()) {
                                addItem("--------------------------------------------------------");
                            }
                        }

                        setEditable(true);
                        setPopupVisible(true);
                    }
                });
            }
        });

        tc.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent arg0) {
                if (tc.getText().length() > 0) {
                    setPopupVisible(true);
                }
                tc.setCaretPosition(tc.getText().length());
            }

            public void focusLost(FocusEvent arg0) {
            }
        });
    }
}
