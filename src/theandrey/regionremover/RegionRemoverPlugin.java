package theandrey.regionremover;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionRemoverPlugin extends JavaPlugin {

	private final Set<String> adminRegions = new HashSet<>();
	private WorldGuardPlugin wg;
	public static Logger log;

	@Override
	public void onEnable() {
		log = getLogger();
		wg = WGBukkit.getPlugin();
		adminRegions.add("__global__");
		adminRegions.add("spawn");
		adminRegions.add("spawn-area");
		adminRegions.add("spawn_area");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.isOp()) {
			int removedRegions = 0;
			sender.sendMessage("Начинаем удалять регионы...");

			for(World world : Bukkit.getWorlds()) {
				RegionManager mgr = wg.getRegionManager(world);
				if(mgr == null) continue;
				sender.sendMessage("Удаляем регионы в мире " + world.getName() + "...");
				for(ProtectedRegion region : new ArrayList<>(mgr.getRegions().values())) {
					String id = region.getId();
					if(adminRegions.contains(id.toLowerCase())) continue;
					mgr.removeRegion(id);
					log.info("Удалён регион: " + id);
					removedRegions++;
				}
				try {
					mgr.save();
				} catch (ProtectionDatabaseException ex) {
					log.log(Level.SEVERE, "Save fail", ex);
				}
			}

			sender.sendMessage(String.format("Завершено. Удалено %,d регионов.", removedRegions));
		} else sender.sendMessage(ChatColor.RED + "Ошибка доступа");
		return true;
	}

}
