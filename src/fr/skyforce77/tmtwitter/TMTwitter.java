package fr.skyforce77.tmtwitter;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import fr.skyforce77.tmtwitter.commands.CommandTweet;
import fr.skyforce77.tmtwitter.commands.CommandTweetFav;
import fr.skyforce77.tmtwitter.commands.CommandTweetReply;
import fr.skyforce77.tmtwitter.gui.PromptLoginPanel;
import fr.skyforce77.tmtwitter.listeners.PluginMessageListener;
import fr.skyforce77.towerminer.TowerMiner;
import fr.skyforce77.towerminer.api.Plugin;
import fr.skyforce77.towerminer.api.PluginManager;
import fr.skyforce77.towerminer.api.PluginStatus;
import fr.skyforce77.towerminer.api.commands.CommandManager;
import fr.skyforce77.towerminer.menus.ChatContainer;
import fr.skyforce77.towerminer.protocol.chat.ChatMessage;
import fr.skyforce77.towerminer.protocol.chat.ChatModel;
import fr.skyforce77.towerminer.protocol.chat.IconModel;
import fr.skyforce77.towerminer.protocol.chat.MessageModel;

public class TMTwitter extends Plugin{

	public static String CONSUMERKEY = "VUxLEUstsYDcwjYzBCugla4nU";
	public static String CONSUMERSECRET = "M1YDE3pdiw9se3MdogyoxOcnLwVj8DwvYTESlVIxgyxNqAvwP6";
	public static String ACCESSTOKEN;
	public static String ACCESSTOKENSECRET;
	public static Twitter twitter;

	public static void main(String[] args) {}

	@Override
	public PluginStatus onEnable() {
		CommandManager.register("tweet", new CommandTweet());
		CommandManager.register("tweet-reply", new CommandTweetReply());
		CommandManager.register("tweet-fav", new CommandTweetFav());
		PluginManager.registerListener(new PluginMessageListener());
		try {
			if(getTokens().exists()) {
				read();
			} else {
				createTokens();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return PluginStatus.ERROR;
		}
		return PluginStatus.OK;
	}

	public void createTokens() throws Exception {
		Twitter twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(CONSUMERKEY,CONSUMERSECRET);
		RequestToken requestToken = twitter.getOAuthRequestToken();
		AccessToken accessToken = null;
		while (accessToken == null) {
			PromptLoginPanel loginpanel = new PromptLoginPanel(requestToken.getAuthorizationURL());
			JOptionPane.showMessageDialog(null, loginpanel, "Twitter configuration", JOptionPane.INFORMATION_MESSAGE);
			String pin = loginpanel.field.getText();
			try{
				if(pin.length() > 0){
					accessToken = twitter.getOAuthAccessToken(requestToken, pin);
				}else{
					accessToken = twitter.getOAuthAccessToken();
				}
			} catch (TwitterException te) {
				if(401 == te.getStatusCode()){
					JOptionPane.showMessageDialog(null, "Unable to get the access token.", "Twitter configuration", JOptionPane.ERROR_MESSAGE);
				}else{
					te.printStackTrace();
				}
			}
		}

		getTokens().createNewFile();

		BufferedWriter fw = new BufferedWriter(new FileWriter(getTokens()));
		fw.write(accessToken.getToken().split("-")[0]);
		fw.newLine();
		fw.write(accessToken.getToken().split("-")[1]);
		fw.newLine();
		fw.write(accessToken.getTokenSecret());
		fw.close();
		read();
	}

	public void read() throws Exception
	{
		BufferedReader fr = new BufferedReader(new FileReader(getTokens()));
		ACCESSTOKEN = fr.readLine()+"-"+fr.readLine();
		ACCESSTOKENSECRET = fr.readLine();
		fr.close();

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMERKEY)
		.setOAuthConsumerSecret(CONSUMERSECRET)
		.setOAuthAccessToken(ACCESSTOKEN)
		.setOAuthAccessTokenSecret(ACCESSTOKENSECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();

		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMERKEY)
		.setOAuthConsumerSecret(CONSUMERSECRET)
		.setOAuthAccessToken(ACCESSTOKEN)
		.setOAuthAccessTokenSecret(ACCESSTOKENSECRET);
		final TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status) {
				if(TowerMiner.menu instanceof ChatContainer) {
					ChatContainer cc = (ChatContainer)TowerMiner.menu;
					User u = status.getUser();

					IconModel favorite = new IconModel("https://dl.dropboxusercontent.com/u/38885163/TowerMiner/plugins/TMTwitter/star_white.png");
					favorite.setPluginInteract("TMTwitter-Fav", ""+status.getId(), "true");
					favorite.setMouseModel(new MessageModel("Favorites: Add or Remove"));
					if(status.isFavorited()) {
						favorite.setImage("https://dl.dropboxusercontent.com/u/38885163/TowerMiner/plugins/TMTwitter/star_yellow.png");
						favorite.setPluginInteract("TMTwitter-Fav", ""+status.getId(), "false");
					}
					
					ChatModel response = new ChatModel(u.getScreenName()+":");
					response.setForegroundColor(new Color(168, 204, 240));
					String mentions = " @"+u.getScreenName();
					for(UserMentionEntity me : status.getUserMentionEntities()) {
						if(!mentions.contains("@"+me.getScreenName()))
							mentions = mentions+" @"+me.getScreenName();
					}
					response.setCommandAutoComplete("/tweet-reply "+status.getId()+mentions);
					response.setMouseModel(new MessageModel("Click here to respond"));

					IconModel user = new IconModel(u.getProfileImageURLHttps());
					if(status.isRetweet()) {
						user.setForegroundColor(new Color(60, 200, 60));
					} else {
						user.setForegroundColor(new Color(168, 204, 240));
					}
					user.setLink("https://twitter.com/"+u.getScreenName());
					user.setMouseModel(new MessageModel("https://twitter.com/"+u.getScreenName()));
					
					ChatMessage message = cc.getChat().getText(status.getText());

					ChatMessage complete = new ChatMessage(favorite, user, response);
					complete.add(message);
					cc.getChat().onMessageReceived(complete);
				} else {
					TowerMiner.print(status.getUser().getScreenName()+": "+status.getText(), "TWITTER");
				}
			}
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice){}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses){
				System.out.println("[Limitation: "+numberOfLimitedStatuses+"]");
			}

			public void onException(Exception ex){}
			public void onScrubGeo(long arg0, long arg1){}
			public void onStallWarning(StallWarning arg0){}
		};
		twitterStream.addListener(listener);
		twitterStream.user();
	}

	public File getTokens() {
		return new File(getDirectory(), "/tokens");
	}
}
