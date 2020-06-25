package com.noahverkaik.playerwarps;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.Collator;
import java.util.*;

public final class PlayerWarps extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveResource("config.yml", false);
        getServer().getPluginManager().registerEvents(this, this);

        /*File file = new File(getDataFolder().getAbsolutePath() + "\\pwarpsResult.yml");
        YamlConfiguration resultConfig = YamlConfiguration.loadConfiguration(file);

        for (String playerName : getConfig().getConfigurationSection("playerwarps").getKeys(false)) {
            try {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

                boolean errored = true;
                try {
                    UUID uuid = UUID.fromString(offlinePlayer.getName());
                    errored = false;
                } catch (Exception ex) {

                }

                if (!errored) continue;

                resultConfig.set("playerdata." + offlinePlayer.getUniqueId().toString() + ".lastknownname", playerName);
                String name = getConfig().getString("playerwarps." + playerName + ".name");
                Location loc = (Location) getConfig().get("playerwarps." + playerName + ".location");
                UUID uuid = UUID.randomUUID();

                List<String> owned = new ArrayList<>();
                owned.add(uuid.toString());

                resultConfig.set("playerdata." + offlinePlayer.getUniqueId() + ".warpsowned", owned);
                resultConfig.set("playerwarps." + uuid.toString() + ".owner", offlinePlayer.getUniqueId().toString());
                resultConfig.set("playerwarps." + uuid.toString() + ".name", name);
                resultConfig.set("playerwarps." + uuid.toString() + ".location", loc);
                resultConfig.save(file);
            } catch (Exception ex) {
                System.out.println("Failed to convert " + playerName + ".");
                resultConfig = YamlConfiguration.loadConfiguration(file);
            }
        }*/

        for (int i = 1; i <= 1000; i++) {
            int finalI = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
               createWarpsInventory(finalI);
            });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getLabel().equalsIgnoreCase("playerwarps")) {
            Player player = (Player) sender;
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("create")) {
                    List<String> argsL = new ArrayList<>(Arrays.asList(args));
                    argsL.remove(0);
                    createPlayerWarp(player, String.join(" ", argsL), player.getLocation());
                } else if (args[0].equalsIgnoreCase("delete")) {
                    try {
                        deletePlayerWarp(player, UUID.fromString(args[1]));
                    } catch (Exception ignored) {
                        player.sendMessage("§eChoose one to delete:");

                        Integer id = 1;
                        for (String uuidString : getConfig().getStringList("playerdata." + player.getUniqueId() + ".warpsowned")) {
                            String name = getConfig().getString("playerwarps." + uuidString + ".name");
                            player.spigot().sendMessage(new ComponentBuilder(" §6" + id++ + ". §f" + name).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aWarp: " + name + "\n§eUUID: §f" + uuidString).create())).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pw delete " + uuidString)).create());
                        }

                        player.sendMessage("§7§oClick one or type /pwarp delete <uuid>");
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    player.sendMessage("§eYour active player warps:");

                    Integer id = 1;
                    for (String uuidString : getConfig().getStringList("playerdata." + player.getUniqueId() + ".warpsowned")) {
                        String name = getConfig().getString("playerwarps." + uuidString + ".name");
                        player.spigot().sendMessage(new ComponentBuilder(" §6" + id++ + ". §f" + name).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aWarp: " + name + "\n§eUUID: §f" + uuidString).create())).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pw delete " + uuidString)).create());
                    }
                    player.sendMessage("§7§oClick on a name to delete the warp.");
                } else if (args[0].equalsIgnoreCase("addallowed")) {
                    if (!sender.hasPermission("pwarps.admin")) return true;
                    try {
                        Player onlinePlayer = Bukkit.getPlayer(args[1]);
                        if (onlinePlayer != null) {
                            increaseAllowed(onlinePlayer.getUniqueId(), Integer.parseInt(args[2]));
                            player.sendMessage("§aIncreased their maximum by " + args[2] + ".");
                        } else {
                            player.sendMessage("§cThat player is offline.");
                        }
                    } catch (Exception ex) {
                        player.sendMessage("§cUsage: /pw addallowed <player> <amount>");
                    }
                } else if (args[0].equalsIgnoreCase("removeallowed")) {
                    if (!sender.hasPermission("pwarps.admin")) return true;
                    try {
                        Player onlinePlayer = Bukkit.getPlayer(args[1]);
                        if (onlinePlayer != null) {
                            decreaseAllowed(onlinePlayer.getUniqueId(), Integer.parseInt(args[2]));
                            player.sendMessage("§aDecreased their maximum by " + args[2] + ".");
                        } else {
                            player.sendMessage("§cThat player is offline.");
                        }
                    } catch (Exception ex) {
                        player.sendMessage("§cUsage: /pw addallowed <player> <amount>");
                    }
                } else if (args[0].equalsIgnoreCase("setallowed")) {
                    if (!sender.hasPermission("pwarps.admin")) return true;
                    try {
                        Player onlinePlayer = Bukkit.getPlayer(args[1]);
                        if (onlinePlayer != null) {
                            setAllowed(onlinePlayer.getUniqueId(), Integer.parseInt(args[2]));
                            player.sendMessage("§aSet their maximum to " + args[2] + ".");
                        } else {
                            player.sendMessage("§cThat player is offline.");
                        }
                    } catch (Exception ex) {
                        player.sendMessage("§cUsage: /pw addallowed <player> <amount>");
                    }
                } else {
                    UUID uuid;
                    String name;

                    Player onlinePlayer = Bukkit.getPlayer(args[0]);
                    if (onlinePlayer != null) {
                        uuid = onlinePlayer.getUniqueId();
                        name = onlinePlayer.getName();
                    } else {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

                        uuid = offlinePlayer.getUniqueId();
                        name = offlinePlayer.getName();
                    }

                    if (getConfig().isSet("playerdata." + uuid.toString())) {
                        player.openInventory(createWarpsInventoryPlayer(name, uuid,1));
                    } else {
                        player.sendMessage("§cThat player could not be found.");
                    }
                }
            } else {
                player.openInventory(createWarpsInventory(1));
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    HashMap<Integer, Inventory> cachedPages = new HashMap<>();

    Inventory createWarpsInventory(Integer page) {
        if (cachedPages.containsKey(page)) {
            return cachedPages.get(page);
        }

        Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54,  "Player Warps - Page " + page);
        for (int i = 0; i < 9; ++i) {
            inventory.setItem(i, new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName("§awww.tribewars.net").build());
        }
        for (int i = 45; i < 54; ++i) {
            inventory.setItem(i, new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName("§awww.tribewars.net").build());
        }

        Integer offset = 19;
        Integer index = 0;
        Boolean nextPageExists = false;
        if (getConfig().isSet("playerwarps")) {
            LinkedList<String> playersWithWarps = new LinkedList<>(getConfig().getConfigurationSection("playerdata").getKeys(false));
            LinkedList<String> playersAmount = new LinkedList<>();


            for (String value : playersWithWarps) {
                playersAmount.add(getConfig().getStringList("playerdata." + value + ".warpsowned").size() + "_" + value);
            }

            Collections.sort(playersAmount, Collator.getInstance());
            Collections.reverse(playersAmount);

            for (String dataChunk : playersAmount) {
                if (index < (page - 1) * 14) {
                    ++index;
                }
                else if (offset >= 35) {
                    nextPageExists = true;
                }
                else {
                    if (offset == 26) {
                        offset = 28;
                    }

                    UUID uuid = UUID.fromString(dataChunk.split("_")[1]);
                    List<String> playerwarps = getConfig().getStringList("playerdata." + uuid.toString() + ".warpsowned");
                    if (playerwarps.size() < 1) continue;

                    String name = getConfig().getString("playerdata." + uuid.toString() + ".lastknownname");

                    List<String> lore = new ArrayList<>();
                    lore.add("§eClick to view");

                    for (int i = 0; i < 3; i++) {
                        if (i < playerwarps.size()) {
                            lore.add(" §8- §7§o" + getConfig().getString("playerwarps." + playerwarps.get(i) + ".name"));
                        }
                    }

                    if (playerwarps.size() > 3) {
                        lore.add(" §7§o...and " + (playerwarps.size() - 3) + " more");
                    }

                    ItemStack item = new ItemStackBuilder(Material.PLAYER_HEAD)
                            .lore(lore.toArray(new String[0]))
                            .displayName("§3%player%'s Warps".replace("%player%", name))
                            .skullOwner(name)
                            .build();

                    net.minecraft.server.v1_13_R2.ItemStack stack = CraftItemStack.asNMSCopy(item);
                    NBTTagCompound tag = stack.getTag() != null ? stack.getTag() : new NBTTagCompound();
                    tag.setString("ownerid", uuid.toString());
                    stack.setTag(tag);
                    item = CraftItemStack.asBukkitCopy(stack);

                    inventory.setItem(offset, item);
                    ++offset;
                }
            }
        }
        if (page > 1) {
            inventory.setItem(45, new ItemStackBuilder(Material.LIME_STAINED_GLASS_PANE).displayName("§a< Previous page").build());
        }
        inventory.setItem(49, new ItemStackBuilder(Material.RED_STAINED_GLASS_PANE).displayName("§cClose").build());
        if (nextPageExists) {
            inventory.setItem(53, new ItemStackBuilder(Material.LIME_STAINED_GLASS_PANE).displayName("§aNext page >").build());
        }

        cachedPages.put(page, inventory);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            cachedPages.remove(page);
            createWarpsInventory(page);
        }, 20 * 60 * 5);

        return inventory;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        getConfig().set("playerdata." + e.getPlayer().getUniqueId() + ".lastknownname", e.getPlayer().getName());
        saveConfig();
    }

    Inventory createWarpsInventoryPlayer(String playerName, UUID playerUUID, Integer page) {
        Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54,  playerName + "'s Warps - Page " + page);
        for (int i = 0; i < 9; ++i) {
            inventory.setItem(i, new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName("§awww.tribewars.net").build());
        }
        for (int i = 45; i < 54; ++i) {
            inventory.setItem(i, new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName("§awww.tribewars.net").build());
        }

        Integer offset = 19;
        Integer index = 0;
        Boolean nextPageExists = false;
        if (getConfig().isSet("playerdata")) {
            List<String> warps = new ArrayList<String>(getConfig().getStringList("playerdata." + playerUUID.toString() + ".warpsowned"));
            Collections.reverse(warps);
            for (String warpID : warps) {
                if (index < (page - 1) * 14) {
                    ++index;
                }
                else if (offset >= 35) {
                    nextPageExists = true;
                }
                else {
                    if (offset == 26) {
                        offset = 28;
                    }

                    UUID uuid = UUID.fromString(getConfig().getString("playerwarps." + warpID + ".owner"));
                    String name = getConfig().getString("playerdata." + uuid.toString() + ".lastknownname");
                    String warpName = getConfig().getString("playerwarps." + warpID + ".name");

                    ItemStack item = new ItemStackBuilder(Material.PLAYER_HEAD).lore(new String[] {"§eClick to teleport to this warp."}).displayName("§3Warp: §f" + warpName).skullOwner(name).build();
                    net.minecraft.server.v1_13_R2.ItemStack stack = CraftItemStack.asNMSCopy(item);
                    NBTTagCompound tag = stack.getTag() != null ? stack.getTag() : new NBTTagCompound();
                    tag.setString("warpuuid", warpID);
                    stack.setTag(tag);
                    item = CraftItemStack.asBukkitCopy(stack);

                    inventory.setItem(offset, item);
                    ++offset;
                }
            }
        }
        if (page > 1) {
            inventory.setItem(45, new ItemStackBuilder(Material.LIME_STAINED_GLASS_PANE).displayName("§a< Previous page").build());
        }
        inventory.setItem(49, new ItemStackBuilder(Material.ORANGE_STAINED_GLASS_PANE).displayName("§6Back").build());
        if (nextPageExists) {
            inventory.setItem(53, new ItemStackBuilder(Material.LIME_STAINED_GLASS_PANE).displayName("§aNext page >").build());
        }
        return inventory;
    }

    void createPlayerWarp(Player player, String name, Location location) {
        Integer permittedAmount = getConfig().getInt("playerdata." + player.getUniqueId() + ".allowed", 1);

        int bonus = 0;
        if (player.hasPermission("essentials.kits.rancher")) {
            bonus++;
        }
        if (player.hasPermission("essentials.kits.titan")) {
            bonus++;
        }
        if (player.hasPermission("nte.helper") || player.hasPermission("nte.mod") || player.hasPermission("nte.srmod") || player.hasPermission("nte.admin")) {
            bonus++;
        }

        permittedAmount += bonus;

        List<String> owned = getConfig().getStringList("playerdata." + player.getUniqueId() + ".warpsowned");
        Integer hasAmount = owned.size();
        if (hasAmount < permittedAmount) {
            UUID warpUUID = UUID.randomUUID();
            String path = "playerwarps." + warpUUID + ".";
            getConfig().set(path + "owner", player.getUniqueId().toString());
            getConfig().set(path + "name", name);
            getConfig().set(path + "location", location);

            owned.add(warpUUID.toString());
            getConfig().set("playerdata." + player.getUniqueId() + ".warpsowned", owned);

            saveConfig();

            player.sendMessage("§aSuccessfully created warp §f" + name + "§a!");
        } else {
            player.sendMessage("§cYou can't create more player warps.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().toLowerCase().contains("warps - page")) {
            e.setCancelled(true);
            if (e.getClickedInventory() != null) {
                if (e.getCurrentItem() != null) {
                    ItemStack item = e.getCurrentItem();
                    if (item.getType().equals(Material.PLAYER_HEAD)) {
                        if (e.getView().getTitle().toLowerCase().contains("player warps")) {
                            try {
                                net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
                                UUID uuid = UUID.fromString(nmsStack.getTag().getString("ownerid"));
                                String name = getConfig().getString("playerdata." + uuid.toString() + ".lastknownname");
                                if (name == null) {
                                    e.getWhoClicked().sendMessage("§cThat player could not be found.");
                                    return;
                                }

                                e.getWhoClicked().openInventory(createWarpsInventoryPlayer(name, uuid, 1));
                                e.getWhoClicked().sendMessage("§eViewing §f" + name + "§e's player warps.");
                            } catch (Exception ex) {
                                e.getWhoClicked().sendMessage("§cThat player could not be found.");
                            }
                        } else {
                            try {
                                net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
                                UUID uuid = UUID.fromString(nmsStack.getTag().getString("warpuuid"));
                                String name = getConfig().getString("playerwarps." + uuid.toString() + ".name");
                                Location loc = (Location) getConfig().get("playerwarps." + uuid.toString() + ".location");

                                e.getWhoClicked().teleport(loc);

                                e.getWhoClicked().sendMessage("§eTeleporting to §f" + name + "§e...");
                            } catch (Exception ex) {
                                e.getWhoClicked().sendMessage("§cThat warp could not be found.");
                            }
                        }
                    } else {
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (item.getItemMeta().getDisplayName().contains("Close")) {
                                    e.getWhoClicked().closeInventory();
                                } else if (item.getItemMeta().getDisplayName().contains("Back")) {
                                    e.getWhoClicked().openInventory(createWarpsInventory(1));
                                }  else if (item.getItemMeta().getDisplayName().toLowerCase().contains("next")) {
                                    Integer nextPage = Integer.parseInt(e.getView().getTitle().split(" - Page ")[1]) + 1;
                                    if (e.getView().getTitle().toLowerCase().contains("player warps")) {
                                        e.getWhoClicked().openInventory(createWarpsInventory(nextPage));
                                    } else {
                                        OfflinePlayer player = Bukkit.getOfflinePlayer(e.getView().getTitle().toLowerCase().split("'s ")[0]);
                                        e.getWhoClicked().openInventory(createWarpsInventoryPlayer(player.getName(), player.getUniqueId(), nextPage));
                                    }
                                } else if (item.getItemMeta().getDisplayName().toLowerCase().contains("previous")) {
                                    Integer nextPage = Integer.parseInt(e.getView().getTitle().split(" - Page ")[1]) - 1;
                                    if (e.getView().getTitle().toLowerCase().contains("player warps")) {
                                        e.getWhoClicked().openInventory(createWarpsInventory(nextPage));
                                    } else {
                                        OfflinePlayer player = Bukkit.getOfflinePlayer(e.getView().getTitle().toLowerCase().split("'s ")[0]);
                                        e.getWhoClicked().openInventory(createWarpsInventoryPlayer(player.getName(), player.getUniqueId(), nextPage));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void deletePlayerWarp(Player player, UUID id) {
        String path = "playerwarps." + id;
        if (getConfig().isSet(path)) {
            List<String> owned = getConfig().getStringList("playerdata." + player.getUniqueId() + ".warpsowned");
            if (owned.contains(id.toString())) {
                owned.remove(id.toString());

                String name = getConfig().getString(path + ".name", "");
                getConfig().set(path, null);
                getConfig().set("playerdata." + player.getUniqueId() + ".warpsowned", owned);
                saveConfig();

                player.sendMessage("§aSuccessfully deleted warp §f" + name + "§a!");
            } else {
                player.sendMessage("§cThat's not your warp.");
            }
        } else {
            player.sendMessage("§cThere's no warp with ID " + id + ".");
        }
    }

    void setAllowed(UUID player, int to) {
        if (to < 1) {
            to = 1;
        }

        getConfig().set("playerdata." + player.toString() + ".allowed", to);
        saveConfig();
    }

    void increaseAllowed(UUID player, int by) {
        setAllowed(player, getConfig().getInt("playerdata." + player.toString() + ".allowed", 1) + by);
    }

    void decreaseAllowed(UUID player, int by) {
        increaseAllowed(player, -by);
    }

}
