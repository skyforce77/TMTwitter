package fr.skyforce77.tmtwitter.commands;

import fr.skyforce77.tmtwitter.TMTwitter;
import fr.skyforce77.towerminer.TowerMiner;
import fr.skyforce77.towerminer.api.commands.Argument;
import fr.skyforce77.towerminer.api.commands.Argument.ArgumentType;
import fr.skyforce77.towerminer.api.commands.Command;
import fr.skyforce77.towerminer.api.commands.CommandSender;
import fr.skyforce77.towerminer.api.commands.SenderType;
import fr.skyforce77.towerminer.menus.ChatContainer;
import fr.skyforce77.towerminer.protocol.chat.ChatMessage;
import fr.skyforce77.towerminer.protocol.chat.ChatModel;
import fr.skyforce77.towerminer.protocol.chat.IconModel;

public class CommandTweetFav extends Command{

	@Override
	public void onInitialized(String label) {
		setArguments(new Argument("id", ArgumentType.Integer, false), new Argument("create", ArgumentType.Boolean, false));
	}

	@Override
	public void onTyped(final CommandSender sender, final String[] args) {
		new Thread("Tweet") {
			@Override
			public void run() {
				if(!sender.getType().equals(SenderType.OTHER_PLAYER)) {
					try {
						if(Boolean.parseBoolean(args[1])) {
							TMTwitter.twitter.createFavorite(Long.parseLong(args[0]));
						} else {
							TMTwitter.twitter.destroyFavorite(Long.parseLong(args[0]));
						}
						if(TowerMiner.menu instanceof ChatContainer) {
							ChatContainer cc = (ChatContainer)TowerMiner.menu;
							for(ChatMessage msg : cc.getChat().messages) {
								for(ChatModel model : msg.getModels()) {
									if(model instanceof IconModel && model.getLink().contains("true") && model.getLink().contains("TMTwitter-Fav")
											&& model.getLink().contains(args[0])) {
											model.setLink(model.getLink().replaceAll("true", "false"));
											((IconModel)model).setImage("https://dl.dropboxusercontent.com/u/38885163/TowerMiner/plugins/TMTwitter/star_yellow.png");
									} else if(model instanceof IconModel && model.getLink().contains("false") && model.getLink().contains("TMTwitter-Fav")
											&& model.getLink().contains(args[0])) {
											model.setLink(model.getLink().replaceAll("true", "false"));
											((IconModel)model).setImage("https://dl.dropboxusercontent.com/u/38885163/TowerMiner/plugins/TMTwitter/star_white.png");
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					sender.sendMessage("You can't do this");
				}
			}
		}.start();
	}

	@Override
	public boolean isCorrect(String[] args) {
		if(args.length >= 2 && !args[0].equals("")) {
			return true;
		}
		return false;
	}
}
