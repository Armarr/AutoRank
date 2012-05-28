package me.armar.plugins.autorank;

import org.bukkit.command.CommandSender;

public class Leaderboard {

	private int[] scores = new int[10];
	private String[] names = new String[10];

	public Leaderboard() {
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

	public void display(CommandSender sender) {
		for (int i = 0; i < 10; i++) {
			if (names[i] != null) {
				String message = names[i];
				message += " - " + Integer.toString(scores[i]);
				sender.sendMessage(message);
			}
		}
	}

}
