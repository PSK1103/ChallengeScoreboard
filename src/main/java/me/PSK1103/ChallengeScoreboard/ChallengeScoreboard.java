package me.PSK1103.ChallengeScoreboard;

import me.PSK1103.ChallengeScoreboard.utils.CommandHandler;
import me.PSK1103.ChallengeScoreboard.utils.Config;
import me.PSK1103.ChallengeScoreboard.utils.ScoreboardHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class ChallengeScoreboard extends JavaPlugin implements CommandExecutor {

    private ScoreboardHandler scoreboardHandler;
    private Config config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = new Config(this);
        this.scoreboardHandler = new ScoreboardHandler(this);
        getServer().getPluginManager().registerEvents(scoreboardHandler,this);
        getCommand("ChallengeScoreboard").setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        scoreboardHandler.disableTasks();
    }

    public ScoreboardHandler getScoreboardHandler() {
        return scoreboardHandler;
    }

    public Config getCustomConfig() {
        return config;
    }
}
