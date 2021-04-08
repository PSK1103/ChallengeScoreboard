package me.PSK1103.ChallengeScoreboard.utils;

import me.PSK1103.ChallengeScoreboard.ChallengeScoreboard;

public class Config {
    private int saveInterval;
    private int timeCheckInterval;
    private String first,second,third;
    private ChallengeScoreboard plugin;

    public Config(ChallengeScoreboard plugin) {
        this.plugin = plugin;
        getParams();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        getParams();
    }

    private void getParams() {
        saveInterval = plugin.getConfig().getInt("save-interval",12000);
        timeCheckInterval = plugin.getConfig().getInt("time-check-interval",600);
        first = plugin.getConfig().getString("first-display-code","§6");
        second = plugin.getConfig().getString("second-display-code","§a");
        third = plugin.getConfig().getString("third-display-code","§b");
    }

    public int getSaveInterval() {
        return saveInterval;
    }

    public int getTimeCheckInterval() {
        return timeCheckInterval;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }
}
