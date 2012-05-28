package me.armar.plugins.autorank;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.command.CommandSender;

public class Leaderboard {

	private TreeMap<String, Integer> scores =  new TreeMap<String, Integer>();
	private Autorank plugin;
	private Config config;
	private String layout;
	private String text;

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
		scores.put(name, score);
	}
	

	public void display(CommandSender sender, String prefix) {
	    String[] messages = text.split("%split%");
	    for(String msg:messages){
		sender.sendMessage(prefix + msg);
	    }
	}
	
	public void updateText(){
	    text = "";
	    
	    text += ("---Leaderboard---" + "%split%");
		Iterator<Entry<String, Integer>> it = scores.entrySet().iterator();
		for(int i = 0; i<10 && it.hasNext(); i++){
		    Entry<String, Integer> entry = it.next();
		    String name = entry.getKey();
		    int time = entry.getValue();
		    
		    String message = layout.replaceAll("&n", name);
		    
		    message = message.replaceAll("&p", Integer.toString(i+1));
		    
		    message = message.replaceAll("&tm", Integer.toString(time));
		    message = message.replaceAll("&th", Integer.toString(time/60));
		    
		    message = message.replaceAll("&d", Integer.toString(time/1440));
		    time = time - ((time/1440)*1440);
		    message = message.replaceAll("&h", Integer.toString(time/60));
		    time = time - ((time/60)*60);
		    message = message.replaceAll("&m", Integer.toString(time));

		   
		    text += (message + "%split%");
		}
		text += ("-----------------");
	}

}
