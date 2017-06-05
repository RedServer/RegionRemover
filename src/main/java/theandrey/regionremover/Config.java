package theandrey.regionremover;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Конфигурация плагина
 * @author TheAndrey
 */
public final class Config {

	private final File file;
	// Опции
	public final Set<String> protectedRegions = new HashSet<>();

	public Config(RegionRemoverPlugin plugin) {
		File dir = plugin.getDataFolder();
		dir.mkdir();
		file = new File(dir, "config.yml");
	}

	public void load() throws IOException, InvalidConfigurationException {
		YamlConfiguration config = new YamlConfiguration();
		config.addDefault("protected-regions", Arrays.asList("spawn", "spawn-area", "example1"));
		config.options().copyDefaults(true);
		if(file.exists()) config.load(file);

		protectedRegions.clear();
		for(String regionId : config.getStringList("protected-regions")) {
			protectedRegions.add(regionId.toLowerCase());
		}

		config.save(file);
	}

}
