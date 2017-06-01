package theandrey.regionremover;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public final class RemoveTask implements Runnable {

	private final static Set<String> adminRegions = new HashSet<>();
	private final WorldGuardPlugin wg;
	private final List<World> worlds = new ArrayList<>();
	private final BukkitTask task;
	private final String defaultWorld;

	static {
		adminRegions.add("spawn");
		adminRegions.add("spawn-area");
		adminRegions.add("road-north");
		adminRegions.add("road-south");
		adminRegions.add("road-west");
		adminRegions.add("road-east");
	}

	public RemoveTask(RegionRemoverPlugin plugin) {
		wg = WGBukkit.getPlugin();

		worlds.addAll(Bukkit.getWorlds());
		defaultWorld = Bukkit.getWorlds().get(0).getName();

		task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 1);
	}

	@Override
	public void run() {
		if(worlds.isEmpty()) {
			RegionRemoverPlugin.log.info("Удаление регионов завершено.");
			task.cancel();
			return;
		}

		boolean stopByLimit = false;
		World world = worlds.get(0);
		RegionManager mgr = wg.getRegionManager(world);

		if(mgr != null) {
			final long startTime = System.currentTimeMillis();
			for(ProtectedRegion region : new ArrayList<>(mgr.getRegions().values())) {
				if(region instanceof GlobalProtectedRegion) continue; // нельзя удалять
				String id = region.getId();
				if(world.getName().equalsIgnoreCase(defaultWorld) && adminRegions.contains(id.toLowerCase())) continue;
				mgr.removeRegion(id);
				RegionRemoverPlugin.log.info("Удалён регион: " + id);

				// Прерывание по тайм-ауту
				if((System.currentTimeMillis() - startTime) >= 1000) {
					stopByLimit = true;
					break;
				}
			}
		}

		if(!stopByLimit) worlds.remove(world);
	}

}
