package theandrey.regionremover;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionRemoverPlugin extends JavaPlugin {

	public static Logger log;
	Config config;

	@Override
	public void onEnable() {
		log = getLogger();
		try {
			config = new Config(this);
			config.load();
		} catch (Exception ex) {
			throw new RuntimeException("Произошла ошибка при активации плагина", ex);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if(!sender.isOp()) throw new CommandException("Ошибка доступа");
			if(args.length == 0) throw new CommandException("Не указана подкоманда");

			if(args[0].equalsIgnoreCase("reload")) {

				try {
					config.load();
					sender.sendMessage(ChatColor.GREEN + "Конфигурация плагина перезагружена");
				} catch (IOException | InvalidConfigurationException ex) {
					sender.sendMessage(ChatColor.RED + "Произошла ошибка: " + ex.toString());
					log.log(Level.SEVERE, "Ошибка загрузки конфигурации", ex);
				}

			} else if(args[0].equalsIgnoreCase("remove")) {

				sender.sendMessage(ChatColor.YELLOW + "Начинаем удалять регионы...");
				RemoveTask task = new RemoveTask(this);
				task.start(sender);

			} else {
				throw new CommandException("Неизвестная подкоманда");
			}
		} catch (CommandException ex) {
			sender.sendMessage(ChatColor.RED + "Ошибка: " + ex.getMessage());
		}

		return true;
	}

}
