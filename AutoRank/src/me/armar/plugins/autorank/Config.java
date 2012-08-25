package me.armar.plugins.autorank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

	private File file;
	private YamlConfiguration config;
	private Autorank plugin;
	private LinkedHashMap<String, Object> configDefaults;

	public Config(Autorank plugin, String filePath,
			LinkedHashMap<String, Object> configDefaults, String name) {
		// accepts null as configDefaults -> empty by default
		this.file = new File(filePath);
		this.plugin = plugin;
		this.config = new YamlConfiguration();
		this.configDefaults = configDefaults;

		if (file.exists() == false) {
			if (configDefaults != null) {
				for (String key : this.configDefaults.keySet()) {
					this.config.set(key, this.configDefaults.get(key));
				}
			}
			try {
				this.config.save(file);
				this.plugin.logMessage("new " + name + " file created");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.config.load(file);
				plugin.logMessage(name + " file loaded");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void save() {
		try {
			this.config.save(file);
		} catch (ConcurrentModificationException e){
			save();
		} catch (IOException e) {
			e.printStackTrace();
		    }

	}

	public void set(String key, Object value) {
		this.config.set(key, value);
	}

	public Object get(String key) {
		return this.config.get(key);
	}
	
	public ConfigurationSection getSection(String key){
	    return config.getConfigurationSection(key);
	}

	public Set<String> getKeys() {
		return config.getKeys(false);
	}

	public boolean exists(String key) {
		boolean res = false;
		if (get(key) != null) {
			res = true;
		}
		return res;
	}

	public void load() {
			try {
				this.config.load(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
	}

}
