package fr.skyforce77.tmtwitter.commands;

import twitter4j.TwitterException;
import fr.skyforce77.tmtwitter.TMTwitter;
import fr.skyforce77.towerminer.api.commands.Argument;
import fr.skyforce77.towerminer.api.commands.Argument.ArgumentType;
import fr.skyforce77.towerminer.api.commands.Command;
import fr.skyforce77.towerminer.api.commands.CommandSender;
import fr.skyforce77.towerminer.api.commands.SenderType;

public class CommandTweet extends Command{

	@Override
	public void onInitialized(String label) {
		setArguments(new Argument("tweet", ArgumentType.String, false));
	}
	
	@Override
	public void onTyped(final CommandSender sender, final String[] args) {
		new Thread("Tweet") {
			@Override
			public void run() {
				if(!sender.getType().equals(SenderType.OTHER_PLAYER)) {
					String status = "";
					for(String s : args)
						status = status+" "+s;
					try {
						TMTwitter.twitter.updateStatus(status.replaceFirst(" ", ""));
					} catch (TwitterException e) {
						sender.sendMessage("Your tweet can't be send");
					}
				} else {
					sender.sendMessage("You can't do this");
				}
			}
		}.start();
	}
	
	@Override
	public boolean isCorrect(String[] args) {
		if(args.length >= 1 && !args[0].equals("")) {
			return true;
		}
		return false;
	}
}
