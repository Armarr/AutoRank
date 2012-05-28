package me.armar.plugins.autorank;

public class AutorankSaveData implements Runnable {

	private Autorank plugin;
	
	public AutorankSaveData(Autorank plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public void run() {

		plugin.getData().save();
		plugin.debugMessage("Data saved");
		
	}

}
