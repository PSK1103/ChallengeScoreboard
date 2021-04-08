package me.PSK1103.ChallengeScoreboard.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.PSK1103.ChallengeScoreboard.ChallengeScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardHandler implements Listener {
    private final ChallengeScoreboard plugin;
    private final Logger logger;
    private boolean active;
    private Map<String,Integer> stats;
    private String statName;
    private Scoreboard board;
    private Objective objective;
    private long endTime;

    public ScoreboardHandler(ChallengeScoreboard plugin) {
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        findExistingChallenge();
    }

    public boolean newChallenge(String statName, String boardName) {
        return newTimedChallenge(statName, boardName,-1);
    }

    public boolean newTimedChallenge(String statName, String boardName, long timeOffset) {
        this.active = true;
        this.statName = statName;
        if(timeOffset > 0) {
            this.endTime = System.currentTimeMillis() + timeOffset;
            checkTime();
        }
        else
            this.endTime = -1;
        String[] parts = statName.toLowerCase(Locale.ROOT).split(":");
        StringBuilder criteria = new StringBuilder();
        if(parts.length == 1) {
            criteria.append("minecraft.custom:");
            if(!parts[0].startsWith("minecraft"))
                criteria.append("minecraft.");
            criteria.append(parts[0]);
        }
        else {
            if(!parts[0].startsWith("minecraft"))
                criteria.append("minecraft.");
            criteria.append(parts[0]);
            criteria.append(":");
            if(!parts[1].startsWith("minecraft"))
                criteria.append("minecraft.");
            criteria.append(parts[1]);
        }
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = board.registerNewObjective("challenge",criteria.toString().trim(),ChatColor.translateAlternateColorCodes('&',boardName));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        saveStatsAsync();
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public void stopChallenge(CommandSender sender) {
        stopChallenge();
        sender.sendMessage(ChatColor.AQUA + "Challenge " + objective.getDisplayName() + " stopped");
    }

    public void stopChallenge() {
        active = false;
        Bukkit.getScheduler().cancelTasks(plugin);
        displayWinners();
        new File(plugin.getDataFolder(),"current-stats").delete();
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if(player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null && player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(objective.getName())) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                player.sendMessage(ChatColor.YELLOW + "The challenge has ended");
            }
        }
    }

    private void displayWinners() {
        plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + ChatColor.BOLD + objective.getDisplayName() + ChatColor.RESET + ChatColor.YELLOW + " has ended!");
        List<Score> scores = board.getEntries().stream().map(e -> objective.getScore(e)).collect(Collectors.toList());
        if(scores.size() == 0) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Unfortunately no one participated in this challenge");
            return;
        }
        Map<String,Integer> winners = new HashMap<>();
        scores.sort(Comparator.comparingInt(Score::getScore));
        Collections.reverse(scores);
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + "The winners are:");
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Winner: " + ChatColor.RESET + plugin.getCustomConfig().getFirst() + scores.get(0).getEntry());
        winners.put(scores.get(0).getEntry(),scores.get(0).getScore());
        if(scores.size() > 1) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Runner Up: " + ChatColor.RESET + plugin.getCustomConfig().getSecond() + scores.get(1).getEntry());
            winners.put(scores.get(1).getEntry(),scores.get(1).getScore());
        }
        if(scores.size() > 2) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "2nd Runner Up: " + ChatColor.RESET + plugin.getCustomConfig().getThird() + scores.get(2).getEntry());
            winners.put(scores.get(2).getEntry(),scores.get(2).getScore());
        }

        saveChallenge(winners);
    }

    public void saveChallenge(Map<String,Integer> winners) {
        File savedChallenges = new File(plugin.getDataFolder(),"saved-challenges");
        Map<String,Object> challenge = new HashMap<>();
        challenge.put("challenge",ChatColor.stripColor(objective.getDisplayName()));
        challenge.put("winners",winners);
        try {
            JSONParser parser = new JSONParser();
            saveChallenge(savedChallenges, savedChallenges.exists() ? (JSONArray) parser.parse(new FileReader(savedChallenges)) : new JSONArray(), challenge);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void saveChallenge(File savedChallenges, JSONArray data, Map<String, Object> challenge) throws IOException {
        data.add(challenge);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(data.toJSONString());
        String prettyJsonString = gson.toJson(je);
        FileWriter fw = new FileWriter(savedChallenges);
        fw.write(prettyJsonString);
        fw.flush();
        fw.close();
    }

    public void showHistory(CommandSender sender) {
        File savedChallenges = new File(plugin.getDataFolder(),"saved-challenges");
        if(!savedChallenges.exists()) {
            sender.sendMessage(ChatColor.RED + "No record found");
            return;
        }

        try {
            JSONParser parser = new JSONParser();
            JSONArray data = (JSONArray) parser.parse(new FileReader(savedChallenges));
            Collections.reverse(data);
            sender.sendMessage("Last " + Math.min(5, data.size()) + " challenges:");
            for (int i = 0; i < Math.min(5, data.size()); i++) {
                JSONObject challenge = ((JSONObject) data.get(i));
                sender.sendMessage(ChatColor.LIGHT_PURPLE + challenge.get("challenge").toString());
                Map<String,Long> entries = ((Map<String,Long>) challenge.get("winners"));
                entries.forEach((k,v) -> sender.sendMessage("  " + ChatColor.GREEN + k + ": " + ChatColor.AQUA + v));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void checkTime() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (System.currentTimeMillis() > endTime) stopChallenge();
        }, plugin.getCustomConfig().getTimeCheckInterval(), plugin.getCustomConfig().getTimeCheckInterval());
    }

    public void showScoreboard(Player player) {
        if(!active) {
            player.sendMessage(ChatColor.YELLOW + "There is no challenge active currently");
            return;
        }
        if (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null || !player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(objective.getName()))
            player.setScoreboard(board);
        else
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

    }

    private void saveStats() {
        if(!active)
            return;
        File currentStats = new File(plugin.getDataFolder(),"current-stats");
        if(!currentStats.exists()) {
            try {
                currentStats.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String,Integer> entries = new HashMap<>();
        board.getEntries().forEach(e -> {
            entries.put(e,objective.getScore(e).getScore());
        });
        JSONObject data = new JSONObject();

        data.put("displayName",objective.getDisplayName());
        data.put("name",objective.getName());
        data.put("criteria",objective.getCriteria());
        if(endTime > 0)
            data.put("endTime",endTime);
        data.put("scores",entries);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(data.toJSONString());
        String prettyJsonString = gson.toJson(je);

        try {
            FileWriter fw = new FileWriter(currentStats);
            fw.write(prettyJsonString);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveStatsAsync() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveStats,plugin.getCustomConfig().getSaveInterval(), plugin.getCustomConfig().getSaveInterval());
    }

    public void disableTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
        saveStats();
    }

    private void findExistingChallenge() {
        File currentStats = new File(plugin.getDataFolder(),"current-stats");
        if(currentStats.exists()) {
            logger.info("An active challenge exists, loading");
            JSONParser parser = new JSONParser();
            try {
                JSONObject data = (JSONObject) parser.parse(new FileReader(currentStats));
                String displayName = data.get("displayName").toString();
                String name = data.get("name").toString();
                String criteria = data.get("criteria").toString();
                Map<String,Long> entries = ((Map<String,Long>) data.get("scores"));
                if(data.containsKey("endTime"))
                    endTime = Long.parseLong(data.get("endTime").toString());
                board = Bukkit.getScoreboardManager().getNewScoreboard();
                objective = board.registerNewObjective(name,criteria,displayName);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                entries.forEach((k,v) -> objective.getScore(k).setScore(v.intValue()));
                active = true;
                saveStatsAsync();

            }
            catch (ParseException | IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @EventHandler
    public final void onPlayerJoin(PlayerJoinEvent e) {
        if(e.getPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null && e.getPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals("challenge")) {
            e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    @EventHandler
    public final void onPlayerQuit(PlayerQuitEvent e) {
        if(e.getPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null && e.getPlayer().getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals("challenge")) {
            e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

}
