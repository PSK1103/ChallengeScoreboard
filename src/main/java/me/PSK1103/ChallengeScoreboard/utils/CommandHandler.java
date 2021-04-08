package me.PSK1103.ChallengeScoreboard.utils;

import me.PSK1103.ChallengeScoreboard.ChallengeScoreboard;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler implements TabExecutor {

    private final ChallengeScoreboard plugin;

    public CommandHandler(ChallengeScoreboard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(label.equals("cs") || label.equals("challenge") || label.equals("challengescoreboard")) {
            if(args.length >= 3 && args[0].equals("new") && sender.hasPermission("CS.challenge")) {
                if(plugin.getScoreboardHandler().isActive()) {
                    sender.sendMessage(ChatColor.RED + "Another challenge is already active. Stop the previous one before starting a new one");
                }
                StringBuilder displayName = new StringBuilder();
                String name;
                long timeOffset = -1;
                if(args[2].equals("timed")) {
                    if(args.length == 3) {
                        sender.sendMessage(ChatColor.RED + "Incomplete command");
                        return false;
                    }
                    Pattern timePattern = Pattern.compile("(?>(\\d+)Y)?(?>(\\d+)M)?(?>(\\d+)W)?(?>(\\d+)D)?(?>(\\d+)h)?(?>(\\d+)m)?(?>(\\d+)s)?");
                    Matcher timeMatcher = timePattern.matcher(args[3]);
                    if(!timeMatcher.matches() || (timeMatcher.group(1) == null && timeMatcher.group(2) == null && timeMatcher.group(3) == null
                            && timeMatcher.group(4) == null && timeMatcher.group(5) == null && timeMatcher.group(6) == null && timeMatcher.group(7) == null)) {
                        sender.sendMessage(ChatColor.RED + "Time not specified in proper format");
                        return false;
                    }
                    timeOffset = 0;
                    if(timeMatcher.group(1) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(1))* 365L;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if(timeMatcher.group(2) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(2).substring(0,timeMatcher.group(2).length()-1))* 30L;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if(timeMatcher.group(3) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(3))* 7L;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if(timeMatcher.group(4) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(4));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        timeOffset*=24;
                    }
                    if(timeMatcher.group(5) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(5));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        timeOffset*=60;
                    }
                    if(timeMatcher.group(6) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(6));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        timeOffset*=60;
                    }
                    if(timeMatcher.group(7) != null) {
                        try {
                            timeOffset += Integer.parseInt(timeMatcher.group(7));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        timeOffset*=1000;
                    }

                    for (int i = 4; i < args.length; i++)
                        displayName.append(" ").append(args[i]);
                }
                else {
                    for (int i = 2; i < args.length; i++)
                        displayName.append(" ").append(args[i]);
                }
                name = displayName.toString().trim();
                if (name.equals(""))
                    name = "Challenge";


                boolean result = timeOffset > 0 ? plugin.getScoreboardHandler().newTimedChallenge(args[1], name, timeOffset) : plugin.getScoreboardHandler().newChallenge(args[1], name);
                if(!result) {
                    sender.sendMessage(ChatColor.RED + "Incorrect challenge name");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Challenge added");
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "A new challenge has begun!");
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + " " + ChatColor.translateAlternateColorCodes('&',displayName.toString()));
                return true;
            }
            if(args.length == 0) {
                if(sender instanceof Player)
                    plugin.getScoreboardHandler().showScoreboard((Player) sender);
                return true;
            }
            if(args.length == 1) {
                if(args[0].equals("reload") && sender.hasPermission("CS.challenge")) {
                    plugin.getCustomConfig().reloadConfig();
                    return true;
                }
                else if(args[0].equals("stop") && sender.hasPermission("CS.challenge")) {
                    if(!plugin.getScoreboardHandler().isActive()) {
                        sender.sendMessage(ChatColor.YELLOW + "No active challenge");
                        return true;
                    }
                    plugin.getScoreboardHandler().stopChallenge(sender);
                    return true;
                }
                else if(args[0].equals("help")) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "===============ChallengeScoreboard v" + plugin.getDescription().getVersion() + "===============");
                    sender.sendMessage(ChatColor.GOLD + "/challenge: " + ChatColor.GREEN + "Toggle scoreboard on/off");
                    if(sender.hasPermission("CS.challenge")) {
                        TextComponent link = new TextComponent(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "https://minecraft.fandom.com/wiki/Statistics");
                        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraft.fandom.com/wiki/Statistics"));
                        sender.sendMessage(new ComponentBuilder(ChatColor.GOLD + "/challenge new <criterion> (timed <time>) <display name>: " + ChatColor.GREEN + "Start a challenge with criterion as defined in ").append(link).append(ChatColor.GREEN + ". Time has to be in format of combination of xxY (years), xxM (months), xxW (weeks), xxD (days), xxh (hours), xxm (minutes), xxs (seconds), where x is a number. Display name can be a sequence of words with format codes").create());
//                        sender.sendMessage(ChatColor.GOLD + "/challenge new <criterion> (timed <time>) <display name>: " + ChatColor.GREEN + "Start a challenge with criterion as defined in https://minecraft.fandom.com/wiki/Statistics. Time has to be in format of combination of xxY (years), xxM (months), xxW (weeks), xxD (days), xxh (hours), xxm (minutes), xxs (seconds), where x is a number. Display name can be a sequence of words with format codes");
                        sender.sendMessage(ChatColor.GOLD + "/challenge stop: " + ChatColor.GREEN + "Ends current challenge");
                        sender.sendMessage(ChatColor.GOLD + "/challenge reload: " + ChatColor.GREEN + "Reloads the config");
                        sender.sendMessage(ChatColor.GOLD + "/challenge history : " + ChatColor.GREEN + "Shows the challenge history (max 5)");
                    }
                    return true;
                }
                else if(args[0].equals("history")) {
                    plugin.getScoreboardHandler().showHistory(sender);
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (alias.equals("cs") || alias.equals("challenge") || alias.equals("challengescoreboard")) {
            List<String> hints = new ArrayList<>();

            if (args.length == 1) {
                if (args[0].length() == 0) {
                    hints.add("help");
                    if (sender.hasPermission("CS.challenge")) {
                        hints.add("new");
                        hints.add("history");
                        hints.add("stop");
                        hints.add("reload");
                    }
                } else {
                    if ("help".startsWith(args[0]) && !args[0].equals("help")) {
                        hints.add("help");
                    }
                    if (sender.hasPermission("CS.challenge")) {
                        if ("new".startsWith(args[0]) && sender.hasPermission("CS.challenge")) {
                            hints.add("new");
                        }
                        if ("reload".startsWith(args[0]) && sender.hasPermission("CS.challenge")) {
                            hints.add("reload");
                        }
                        if ("history".startsWith(args[0]) && sender.hasPermission("CS.challenge")) {
                            hints.add("history");
                        }
                        if ("stop".startsWith(args[0]) && sender.hasPermission("CS.challenge")) {
                            hints.add("stop");
                        }
                    }
                }
            }

            if (args.length == 3) {
                if (args[0].equals("new") && sender.hasPermission("CS.challenge")) {
                    if (args[2].length() == 0) {
                        hints.add("timed");
                    } else {
                        if ("timed".startsWith(args[2])) {
                            if (!args[1].equals("timed")) {
                                hints.add("timed");
                            }
                        }
                    }
                }
            }

            if (args.length == 0) {
                hints.add("help");
                if (sender.hasPermission("CS.challenge")) {
                    hints.add("new");
                    hints.add("history");
                    hints.add("stop");
                    hints.add("reload");
                }
            }

            return hints;

        }
        return null;
    }
}
