package theandrey.regionremover;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

public final class RemoveTask implements Runnable {

	private final RegionRemoverPlugin plugin;
	private final WorldGuardPlugin wg;
	private final List<World> worlds = new ArrayList<>();
	private BukkitTask task;
	private WeakReference<CommandSender> launchedBy;
	private boolean removeAll;

	public RemoveTask(RegionRemoverPlugin plugin) {
		wg = WGBukkit.getPlugin();
		worlds.addAll(Bukkit.getWorlds());
		this.plugin = plugin;
	}

	public void start(CommandSender launchedBy, boolean removeAll) {
		this.launchedBy = new WeakReference<>(launchedBy);
		this.removeAll = removeAll;
		task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 1);
	}

	@Override
	public void run() {
		if(worlds.isEmpty()) {
			task.cancel();

			CommandSender sender = launchedBy.get();
			if(sender != null) sender.sendMessage(ChatColor.GREEN + "Удаление регионов завершено.");
			RegionRemoverPlugin.log.info("Удаление регионов завершено.");
			return;
		}

		World world = worlds.get(0);
		RegionManager mgr = wg.getRegionManager(world);
		boolean stopByLimit = false;

		if(mgr != null) {
			final long startTime = System.currentTimeMillis();
			for(ProtectedRegion region : new ArrayList<>(mgr.getRegions().values())) {
				String id = region.getId();
				if(!removeAll && plugin.config.protectedRegions.contains(id.toLowerCase())) continue;
				mgr.removeRegion(id);
				RegionRemoverPlugin.log.info("Удалён регион: " + id + " (" + world.getName() + ")");

				// Прерывание по тайм-ауту
				if((System.currentTimeMillis() - startTime) >= 1000) {
					stopByLimit = true;
					break;
				}
			}

			try {
				mgr.save();
			} catch (ProtectionDatabaseException ex) {
				RegionRemoverPlugin.log.log(Level.SEVERE, "Произошла ошибка при сохранении регионов", ex);
			}
		}

		if(!stopByLimit) worlds.remove(world);
	}

}
