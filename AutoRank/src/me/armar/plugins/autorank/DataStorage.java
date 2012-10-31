package me.armar.plugins.autorank;

import java.io.File;
import java.util.Set;

public class DataStorage{

    private Config data;

    public DataStorage(Autorank plugin) {
	data = new Config(plugin,plugin.getDataFolder().getAbsolutePath() + File.separator + "data.yml", null, "data");
	//CONNECT TO SQL 
    }

    public Set<String> getKeys() {
	return data.getKeys();
    }

    public Object get(String key) {
	return data.get(key);
    }

    public void set(String key, Object value) {
	data.set(key, value);
	//SQL UPDATE ISNTRUCTION
    }

    public boolean exists(String key) {
	return data.exists(key);
    }

    public void save() {
	data.save();
    }

    
    
}
