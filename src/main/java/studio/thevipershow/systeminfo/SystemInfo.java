package studio.thevipershow.systeminfo;

import org.bukkit.configuration.file.FileConfiguration;
import studio.thevipershow.systeminfo.api.SysteminfoPlaceholder;
import studio.thevipershow.systeminfo.commands.register.Manager;
import studio.thevipershow.systeminfo.csvwriter.CSVLogger;
import studio.thevipershow.systeminfo.gui.GuiClickListener;
import studio.thevipershow.systeminfo.listeners.JoinNotifyListener;
import studio.thevipershow.systeminfo.oshi.SystemValues;
import studio.thevipershow.systeminfo.updater.DefaultConsoleUpdateHandler;
import studio.thevipershow.systeminfo.updater.NotifyDataUpdateHandler;
import studio.thevipershow.systeminfo.updater.PluginUpdater;
import studio.thevipershow.systeminfo.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import org.bukkit.plugin.PluginManager;

public final class SystemInfo extends JavaPlugin {

    private final LocalDateTime startupTime = LocalDateTime.now();
    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private CSVLogger csvLogger;
    private Manager commandManager;
    private PluginUpdater pluginUpdater;
    private JoinNotifyListener joinNotifyListener;
    private static SystemInfo instance;
    private static FileConfiguration config;
    private SysteminfoPlaceholder systeminfoPlaceholder;
    private final static long EIGHT_HOURS_TICKS = 20L * 60L * 60L * 8L;

    @Override
    public final void onEnable() {
        instance = this;

        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            systeminfoPlaceholder = new SysteminfoPlaceholder();
            systeminfoPlaceholder.register();
        } else {
            getLogger().info("Could not find PlaceholderAPI, placeholders won't be available.");
        }

        try {
            commandManager = Manager.getInstance(Utils.getCommandMap());
            commandManager.registerAll();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().warning("Could not get command map!");
            getLogger().warning("Disabling plugin . . .");
            pluginManager.disablePlugin(this);
            return;
        }
        pluginManager.registerEvents(new GuiClickListener(), this);

        this.saveDefaultConfig();
        config = this.getConfig();
        if (config.getBoolean("csvlogging")) {
            csvLogger = CSVLogger.getInstance(SystemValues.getInstance(), this);
            csvLogger.startLogging();
            getLogger().info("CSV logging is enabled");
        } else {
            getLogger().info("CSV logging is NOT enabled");
        }

        joinNotifyListener = new JoinNotifyListener(this);
        pluginUpdater = PluginUpdater.getInstance(this);
        pluginUpdater.addHandler(new DefaultConsoleUpdateHandler(this));
        pluginUpdater.addHandler(new NotifyDataUpdateHandler(this));
        getServer().getScheduler().runTaskTimer(this, pluginUpdater::performVersionChecks, 0L, EIGHT_HOURS_TICKS);

        pluginManager.registerEvents(joinNotifyListener, this);
    }

    @Override
    public final void onDisable() {
        csvLogger.stopLogging();
    }

    public static SystemInfo getInstance() {
        return instance;
    }

    public final LocalDateTime getStartupTime() {
        return startupTime;
    }

    public final PluginUpdater getPluginUpdater() {
        return pluginUpdater;
    }

    public final JoinNotifyListener getJoinNotifyListener() {
        return joinNotifyListener;
    }
}



