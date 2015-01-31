package fr.skyforce77.tmtwitter.listeners;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.TwitterException;
import twitter4j.User;
import fr.skyforce77.tmtwitter.TMTwitter;
import fr.skyforce77.tmtwitter.commands.CommandTweetFav;
import fr.skyforce77.tmtwitter.commands.CommandTweetReply;
import fr.skyforce77.towerminer.TowerMiner;
import fr.skyforce77.towerminer.api.EventHandler;
import fr.skyforce77.towerminer.api.TMListener;
import fr.skyforce77.towerminer.api.commands.CommandSender;
import fr.skyforce77.towerminer.api.commands.SenderType;
import fr.skyforce77.towerminer.api.events.chat.ChatMouseHoverRenderEvent;
import fr.skyforce77.towerminer.api.events.chat.ChatPluginActionEvent;
import fr.skyforce77.towerminer.menus.Menu;
import fr.skyforce77.towerminer.render.RenderRunnable;
import fr.skyforce77.towerminer.ressources.RessourcesManager;

public class PluginMessageListener extends TMListener{
	
	private HashMap<String, User> users = new HashMap<>();

	@EventHandler
	public void onChatAction(ChatPluginActionEvent e) {
		if(e.getChannel().equals("TMTwitter-Fav")) {
			new CommandTweetFav().onTyped(new CommandSender(SenderType.CHAT), e.getActions());
		} else if(e.getChannel().equals("TMTwitter-Reply")) {
			new CommandTweetReply().onTyped(new CommandSender(SenderType.CHAT), e.getActions());
		}
	}
	
	@EventHandler
	public void onMouseRender(ChatMouseHoverRenderEvent e) {
		
		String link = null;
		if(e.getMouseModel().getText().startsWith("https://twitter.com/")) {
			link = e.getMouseModel().getText();
		} else if(e.getChatModel().getText().startsWith("https://twitter.com/")) {
			link = e.getChatModel().getText();
		}
		
		if(link != null) {
			final Menu mp = TowerMiner.menu;
			final String name = link.replaceAll("https://twitter.com/", "");
			
			e.addRender(new RenderRunnable(true) {
				@Override
				public void run(Graphics2D g2d) {
					g2d.setFont(TowerMiner.getFont(16));
					
					Dimension size = new Dimension(500,300);
					
					User u = null;
					if(!users.containsKey(name)) {
						users.put(name, null);
						new Thread("TMTwitter-ShowUser-"+name) {
							public void run() {
								try {
									User u = TMTwitter.twitter.showUser(name);
									users.put(name, u);
								} catch (TwitterException e) {
									e.printStackTrace();
								}
							};
						}.start();
					} else {
						u = users.get(name);
					}
					
					if(u != null) {
						g2d.drawImage(RessourcesManager.getDistantImage(u.getProfileBannerURL(), "unknown"),
								mp.Xcursor, mp.Ycursor - (int)size.getHeight() + 2, (int) (4 + size.getWidth()), (int)size.getHeight(), null);
						g2d.setColor(new Color(0, 0, 0, 200));
						g2d.fillRect(mp.Xcursor, mp.Ycursor - (int)size.getHeight() + 12, (int) (4 + size.getWidth()), 50);
						Color c = Color.decode("#"+u.getProfileSidebarFillColor());
						g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 250));
						g2d.drawRect(mp.Xcursor, mp.Ycursor - (int)size.getHeight() + 2, (int) (4 + size.getWidth()), (int)size.getHeight());
						g2d.drawImage(RessourcesManager.getDistantImage(u.getProfileImageURLHttps(), "unknown"),
								mp.Xcursor+10, mp.Ycursor - (int)size.getHeight() + 12, 50, 50, null);
						g2d.setColor(Color.WHITE);
						g2d.drawString(u.getName(), mp.Xcursor+65, mp.Ycursor - (int)size.getHeight() + 5 + g2d.getFontMetrics(g2d.getFont()).getHeight());
						g2d.setFont(TowerMiner.getFont(11));
						g2d.drawString("@"+u.getScreenName(), mp.Xcursor+65, mp.Ycursor - (int)size.getHeight() + 50);
						
						g2d.setFont(TowerMiner.getFont(14));
						String desc = u.getDescription();
						int y = 0;
						for(String s : cutParts(g2d, desc, size)) {
							y += g2d.getFontMetrics(g2d.getFont()).getHeight() + 10;
							g2d.setColor(new Color(0,0,0,200));
							g2d.drawString(s, mp.Xcursor+12, mp.Ycursor - (int)size.getHeight() + 70 + y);
							g2d.drawString(s, mp.Xcursor+8, mp.Ycursor - (int)size.getHeight() + 70 + y);
							g2d.drawString(s, mp.Xcursor+10, mp.Ycursor - (int)size.getHeight() + 72 + y);
							g2d.drawString(s, mp.Xcursor+10, mp.Ycursor - (int)size.getHeight() + 68 + y);
							g2d.setColor(Color.WHITE);
							g2d.drawString(s, mp.Xcursor+10, mp.Ycursor - (int)size.getHeight() + 70 + y);
						}
					}
				}
			});
		}
	}
	
	public static ArrayList<String> cutParts(Graphics2D g2d, String desc, Dimension size) {
		ArrayList<String> cut = new ArrayList<>();
		while(!desc.equals("")) {
			String rest = "";
			while(g2d.getFontMetrics(g2d.getFont()).stringWidth(desc) > size.getWidth()-30) {
				int i = desc.lastIndexOf(" ");
				rest = desc.substring(i)+rest;
				desc = desc.substring(0, i);
			}
			if(desc.startsWith(" "))
				desc = desc.replaceFirst(" ", "");
			cut.add(desc);
			desc = rest;
		}
		return cut;
	}

}