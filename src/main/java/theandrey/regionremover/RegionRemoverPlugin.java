package theandrey.regionremover;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionRemoverPlugin extends JavaPlugin {

	public static Logger log;

	@Override
	public void onEnable() {
		log = getLogger();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.isOp()) {
			sender.sendMessage(ChatColor.YELLOW + "Начинаем удалять регионы...");
			RemoveTask task = new RemoveTask(sender);
			task.start(this);
		} else {
			sender.sendMessage(ChatColor.RED + "Ошибка доступа");
		}
		return true;
	}

}
