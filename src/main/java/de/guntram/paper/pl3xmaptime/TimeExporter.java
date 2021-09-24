package de.guntram.paper.pl3xmaptime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.pl3x.map.api.MapWorld;
import net.pl3x.map.api.Pl3xMap;
import net.pl3x.map.api.Pl3xMapProvider;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class TimeExporter extends JavaPlugin implements Runnable {
    
    private static Logger LOGGER;
    private Pl3xMap api;
    private BukkitTask timer;

    @Override
    public void onEnable() {
        LOGGER = this.getLogger();
        if (!getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            LOGGER.warning("Pl3xMap Plugin not found");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        api = Pl3xMapProvider.get();
        if (api == null) {
            LOGGER.warning("Cannot get API from Pl3xMap");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Ignore the result of this; the user may have changed some of the
        // files and made them readonly. This shouldn't prevent us from working.
        WebfileExtractor.initWebDirectory(api.webDir());
        timer = getServer().getScheduler().runTaskTimer(this, this, 100, 100);
    }
    
    @Override
    public void onDisable() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void run() {
        Path dir = api.webDir().resolve("worldtimes.json");
        StringBuilder json = new StringBuilder("{\n\"times\": {\n");
        boolean first = true;

        for (World world: getServer().getWorlds()) {
            Optional<MapWorld> mapworld = api.getWorldIfEnabled(world);
            if (mapworld.isEmpty()) {
                continue;
            }
            if (!first) {
                json.append(",\n");
            }
            first = false;
            json.append("\t\t");
            json.append('"').append(mapworld.get().name()).append('"');
            json.append(": ");
                json.append("{ ");
                json.append(" \"time\": ").append(world.getFullTime());
                json.append(", \"advancing\": ").append(world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
                json.append(" }");
        }
        json.append("\n\t}\n}\n");
        
        try {
            Files.write(dir, json.toString().getBytes());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Cannot write times file "+dir.toString()+", stopping time export", ex);
            timer.cancel();
            timer = null;
        }
    }
}
