package net.extrillius.sweartoggle;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SwearToggle extends JavaPlugin implements Listener {
    private List<String> wordList;
    private List<String> playerList;
    private List<String> pardonedList;

    public void onEnable() {
        getConfig().addDefault("words", new ArrayList<String>());
        wordList = getConfig().getStringList("words");

        getConfig().addDefault("players", new ArrayList<String>());
        playerList = getConfig().getStringList("players");

        getConfig().addDefault("pardoned", new ArrayList<String>());
        pardonedList = getConfig().getStringList("pardoned");

        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
    /*
    So my idea here is that I split the message and check each word individually. If that word is a
    banned word and not a pardoned word, then censor it. If it is both a banned word AND a pardoned
    word, don't do anything -- break, return, whatever. As each word is finished, put it into a
    StringBuilder, then set newMessage to sb.toString().
    */
        Set<Player> filteredPlayers = new HashSet<>();
        String newMessage; // Warning said setting to event.getMessage() is redundant. Why?
        String[] splitMessage = event.getMessage().split(" "); // Could this (" ") be causing problems?
        StringBuilder sb = new StringBuilder();
        StringBuilder mb = new StringBuilder();

        for (String split : splitMessage) {
            for (String word : wordList) {
                if (StringUtils.containsIgnoreCase(split, word)) {
                    for (String pardoned : pardonedList) {
                        if (!(word.equalsIgnoreCase(pardoned))) {
                            for (int i = 0; i < word.length(); i++) {
                                sb.append("*");
                            }
                            split = split.replace(word, sb.toString());
                        }
                    }
                }
            }
            mb.append(split);
            mb.append(" ");
        }
        newMessage = mb.toString();

        Iterator<Player> it = event.getRecipients().iterator();
        while (it.hasNext()) {
            Player p = it.next();
            if (playerList.contains(p.getName())) {
                it.remove();
                filteredPlayers.add(p);
            }
        }
        newMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), newMessage);
        for (Player p : filteredPlayers) {
            p.sendMessage(newMessage);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this plugin!");
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                if (!(playerList.contains(sender.getName()))) {
                    playerList.add(sender.getName());
                    getConfig().set("players", playerList);
                    saveConfig();
                    reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Swearing is now filtered.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Swearing is already filltered!");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("off")) {
                if (playerList.contains(sender.getName())) {
                    playerList.remove(sender.getName());
                    getConfig().set("players", playerList);
                    saveConfig();
                    reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Swearing is now unfiltered.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Swearing is already unfiltered!");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!(sender.hasPermission("swear.reload"))) {
                    sender.sendMessage(ChatColor.RED + "Reloading the plugin is an admin-only command.");
                    return false;
                } else {
                    reloadConfig();
                    saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
                }
            } else if (args[0].equalsIgnoreCase("add")) {
                sender.sendMessage(ChatColor.RED + "Specify a word to add.");
                return false;
            } else if (args[0].equalsIgnoreCase("remove")) {
                sender.sendMessage(ChatColor.RED + "Specify a word to remove.");
                return false;
            } else {
                sender.sendMessage(ChatColor.GREEN + "Commands:");
                sender.sendMessage(ChatColor.GRAY + "/swear on: " + ChatColor.DARK_GRAY + "Filters swear words.");
                sender.sendMessage(ChatColor.GRAY + "/swear off: " + ChatColor.DARK_GRAY + "Unfilters swear words.");
                sender.sendMessage(ChatColor.GRAY + "/swear add: " + ChatColor.DARK_GRAY + "Adds a word to the list.");
                sender.sendMessage(ChatColor.GRAY + "/swear remove: " + ChatColor.DARK_GRAY + "Removes a word from the list.");
                sender.sendMessage(ChatColor.GRAY + "/swear reload: " + ChatColor.DARK_GRAY + "Reloads the plugin.");
            }
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "Commands:");
            sender.sendMessage(ChatColor.GRAY + "/swear on: " + ChatColor.DARK_GRAY + "Filters swear words.");
            sender.sendMessage(ChatColor.GRAY + "/swear off: " + ChatColor.DARK_GRAY + "Unfilters swear words.");
            sender.sendMessage(ChatColor.GRAY + "/swear add: " + ChatColor.DARK_GRAY + "Adds a word to the list.");
            sender.sendMessage(ChatColor.GRAY + "/swear remove: " + ChatColor.DARK_GRAY + "Removes a word from the list.");
            sender.sendMessage(ChatColor.GRAY + "/swear reload: " + ChatColor.DARK_GRAY + "Reloads the plugin.");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (!(sender.hasPermission("swear.add"))) {
                    sender.sendMessage(ChatColor.RED + "Adding and removing words are admin-only commands!");
                    return false;
                } else {
                    if (!(wordList.contains(args[1]))) {
                        wordList.add(args[1]);
                        getConfig().set("words", wordList);
                        saveConfig();
                        reloadConfig();
                        sender.sendMessage(ChatColor.DARK_AQUA + args[1] +
                                ChatColor.GREEN + " has been added to the list of banned words.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "That word has already been added!");
                        return false;
                    }
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!(sender.hasPermission("swear.remove"))) {
                    sender.sendMessage(ChatColor.RED + "Adding and removing words are admin-only commands!");
                    return false;
                } else {
                    if (wordList.contains(args[1])) {
                        wordList.remove(args[1]);
                        getConfig().set("words", wordList);
                        saveConfig();
                        reloadConfig();
                        sender.sendMessage(ChatColor.DARK_AQUA + args[1] +
                                ChatColor.GREEN + " has been removed from the list of banned words.");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "That word is not on the list!");
                        return false;
                    }
                }
            } else if (args[0].equalsIgnoreCase("pardon")) {
                if (!(sender.hasPermission("swear.pardon"))) {
                    sender.sendMessage(ChatColor.RED + "Pardoning and unpardoningwords are admin-only commands!");
                    return false;
                } else {
                    if (!(pardonedList.contains(args[1]))) {
                        pardonedList.add(args[1]);
                        getConfig().set("pardoned", pardonedList);
                        saveConfig();
                        reloadConfig();
                        sender.sendMessage(ChatColor.DARK_AQUA + args[1] +
                                ChatColor.GREEN + " will not be censored.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "That word is already pardoned!");
                        return false;
                    }
                }
            } else if (args[0].equalsIgnoreCase("unpardon")) {
                if (!(sender.hasPermission("swear.unpardon"))) {
                    sender.sendMessage(ChatColor.RED + "Pardoning and unpardoning words are admin-only commands!");
                    return false;
                } else {
                    if (pardonedList.contains(args[1])) {
                        pardonedList.remove(args[1]);
                        getConfig().set("pardoned", pardonedList);
                        saveConfig();
                        reloadConfig();
                        sender.sendMessage(ChatColor.DARK_AQUA + args[1] +
                                ChatColor.GREEN + " will now be censored.");
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid parameters!");
                sender.sendMessage(ChatColor.GREEN + "Usage:");
                sender.sendMessage(ChatColor.GRAY + "/swear add [word]");
                sender.sendMessage(ChatColor.GRAY + "/swear remove [word]");
                return false;
            }
        }
        if (args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Invalid parameters!");
            sender.sendMessage(ChatColor.GREEN + "Usage:");
            sender.sendMessage(ChatColor.GRAY + "/swear add [word]");
            sender.sendMessage(ChatColor.GRAY + "/swear remove [word]");
            return false;
        }

        return true;
    }
}
