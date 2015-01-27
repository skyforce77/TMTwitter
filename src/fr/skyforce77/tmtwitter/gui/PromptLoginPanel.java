package fr.skyforce77.tmtwitter.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class PromptLoginPanel extends JPanel{

	private static final long serialVersionUID = 7409461233691558103L;
	
	public GroupLayout layout;
	public JEditorPane pane;
	public JTextField field;
	
	public PromptLoginPanel(String url) {
		layout = new GroupLayout(this);
		setLayout(layout);
		setPreferredSize(new Dimension(500, 100));
		pane = new JEditorPane();
		pane.setContentType("text/html");
		pane.setBackground(new Color(0f,0f,0f,0f));
		pane.setEditable(false);
		pane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if(Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		pane.setText("Open the following URL and grant access to your account:<br/>"+
		"<a href=\""+url+"\">Link</a><br/>"+
		"Enter the PIN(if aviailable) or just hit enter.[PIN]:");
		
		field = new JTextField();
		add(field);
		
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(pane).addComponent(field));
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(pane).addComponent(field));
		add(pane);
	}

}
