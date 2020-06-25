package com.noahverkaik.playerwarps;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder implements Listener {

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemStackBuilder(Material material)
    {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemStackBuilder amount(int amount)
    {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemStackBuilder durability(short durability)
    {
        itemStack.setDurability(durability);
        return this;
    }

    public ItemStackBuilder displayName(String displayName)
    {
        itemMeta.setDisplayName(displayName);
        return this;
    }



    public ItemStackBuilder lore(String[] lore)
    {
        List<String> loreArray = new ArrayList<String>();

        for (String loreBit : lore)
        {
            // Might want to replace this with ChatColor.WHITE in your code.
            loreArray.add(ChatColor.WHITE + loreBit);
        }

        itemMeta.setLore(loreArray);
        return this;
    }

    public ItemStackBuilder enchant(Enchantment enchanement, int level, boolean ignoreLevelRestriction)
    {
        itemMeta.addEnchant(enchanement, level, ignoreLevelRestriction);
        return this;
    }

    public ItemStackBuilder itemFlags(ItemFlag... flags)
    {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemStackBuilder unbreakable(boolean unbreakable)
    {
        itemMeta.spigot().setUnbreakable(unbreakable);
        return this;
    }

    public ItemStackBuilder skullOwner(String playerName) {
        SkullMeta playerheadmeta = (SkullMeta) itemMeta;
        playerheadmeta.setOwner(playerName);
        return this;
    }

    public ItemStackBuilder skullOwner(Player player) {
        SkullMeta playerheadmeta = (SkullMeta) itemMeta;
        playerheadmeta.setOwner(player.getName());
        return this;
    }

    public ItemStack build()
    {
        ItemStack clonedStack = itemStack.clone();
        clonedStack.setItemMeta(itemMeta.clone());
        return clonedStack;
    }
}

