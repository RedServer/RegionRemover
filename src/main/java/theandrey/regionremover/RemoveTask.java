package theandrey.regionremover;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public final class RemoveTask implements Runnable {

	private final static Set<String> adminRegions = new HashSet<>();
	private final WorldGuardPlugin wg;
	private final List<RegionManager> manages = new ArrayList<>();
	private final BukkitTask task;

	static {
		adminRegions.add("__global__");
		adminRegions.add("spawn");
		adminRegions.add("spawn-area");
		adminRegions.add("spawn_area");
	}

	public RemoveTask(RegionRemoverPlugin plugin) {
		wg = WGBukkit.getPlugin();

		for(World world : Bukkit.getWorlds()) {
			RegionManager mgr = wg.getRegionManager(world);
			if(mgr == null) continue;
			manages.add(mgr);
		}

		task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 1);
	}

	@Override
	public void run() {
		if(manages.isEmpty()) {
			RegionRemoverPlugin.log.info("Удаление регионов завершено.");
			task.cancel();
			return;
		}

		RegionManager mgr = manages.get(0);

		boolean stopByLimit = false;
		final long startTime = System.currentTimeMillis();

		for(ProtectedRegion region : new ArrayList<>(mgr.getRegions().values())) {
			String id = region.getId();
			if(adminRegions.contains(id.toLowerCase())) continue;
			mgr.removeRegion(id);
			RegionRemoverPlugin.log.info("Удалён регион: " + id);

			// Прерывание по тайм-ауту
			if((System.currentTimeMillis() - startTime) >= 1000) {
				stopByLimit = true;
				break;
			}
		}

		try {
			mgr.save();
		} catch (ProtectionDatabaseException ex) {
			RegionRemoverPlugin.log.log(Level.SEVERE, "Save fail", ex);
		}

		if(!stopByLimit) manages.remove(mgr);
	}

}
