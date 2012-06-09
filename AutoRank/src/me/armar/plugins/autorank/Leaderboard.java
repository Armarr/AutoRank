package me.armar.plugins.autorank;

import org.bukkit.command.CommandSender;

public class Leaderboard {

	private int[] scores = new int[10];
	private String[] names = new String[10];
	private Autorank plugin;
	private Config config;
	private String layout;
	private String[] messages;

	public Leaderboard(Autorank plugin) {
	    this.plugin = plugin;
	    config = this.plugin.getConf();
	    
	    if(config.get("Leaderboard layout") == null){
		layout = "&n - &tm";
	    }else{
		layout = (String) config.get("Leaderboard layout");
	    }
	}

	public void addScore(int score, String name) {
		int number = 0;
		boolean inTop10 = false;
		boolean end = false;
		for(int i = 9; i >= 0 && !end; i--)
		{
			if(score > scores[i])
			{
				inTop10 = true;
				number = i;
			}else{
				end = true;
			}
		}
		if (inTop10) {
			for (int j = 9; number < j; j--) {
				scores[j] = scores[j - 1];
				names[j] = names[j - 1];
			}
			scores[number] = score;
			names[number] = name;
		}
	}
	

	public void display(CommandSender sender, String prefix) {
	    for(String msg:messages){
		sender.sendMessage(prefix + msg);
	    }
	}
	
	public void updateText(){
	    String text = "";
	    
	    text += ("---Leaderboard---" + "%split%");
		for (int i = 0; i < 10; i++) {
			if (names[i] != null) {
			    String name = names[i];
			    int time = scores[i];
			    
		    String message = layout.replaceAll("&n", name);
		    
		    message = message.replaceAll("&p", Integer.toString(i+1));
		    
		    message = message.replaceAll("&tm", Integer.toString(time));
		    message = message.replaceAll("&th", Integer.toString(time/60));
		    
		    message = message.replaceAll("&d", Integer.toString(time/1440));
		    time = time - ((time/1440)*1440);
		    message = message.replaceAll("&h", Integer.toString(time/60));
		    time = time - ((time/60)*60);
		    message = message.replaceAll("&m", Integer.toString(time));
		    
		    message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");
		   
		    text += (message + "%split%");
		}}
		text += ("-----------------");
		
		messages = text.split("%split%");
	}

	public void clear() {
		scores = new int[10];
		names = new String[10];
	}

}
