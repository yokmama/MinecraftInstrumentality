package jp.minecraftday.minecraftinstrumentality;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Threading {

    public static abstract class Task {
        final Plugin plugin;
        public Task(Plugin plugin){
            this.plugin = plugin;
        }

        public abstract void execute();
    }

    public static void postToServerThread(Task task) {
        Bukkit.getScheduler().runTask(task.plugin, () -> task.execute());
    }
}
