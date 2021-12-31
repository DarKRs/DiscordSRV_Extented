/*-
 * LICENSE
 * DiscordSRV
 * -------------
 * Copyright (C) 2016 - 2021 Austin "Scarsz" Shapiro
 * -------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * END
 */

package github.scarsz.discordsrv.hooks;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.PluginUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;


public class VaultHook implements PluginHook {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;

   //@Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s"));// getDescription().getName(), getDescription().getVersion()));
    }


    public static void onEnable() {
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!"));// getDescription().getName()));
            return;
        }
        setupPermissions();
    }

    private static  boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static String getPrimaryGroup(Player player) {
        if (!PluginUtil.pluginHookIsEnabled("vault")) {
            DiscordSRV.debug("Tried looking up primary group for player " + player.getName() + " but the Vault plugin hook wasn't enabled");
            return " ";
        }

        try {
            net.milkbowl.vault.permission.Permission permissionProvider = (net.milkbowl.vault.permission.Permission) Bukkit.getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.permission.Permission")).getProvider();
            if (permissionProvider == null) {
                DiscordSRV.debug("Tried looking up group for player " + player.getName() + " but failed to get the registered service provider for Vault");
                return " ";
            }

            String primaryGroup = permissionProvider.getPrimaryGroup(player);
            if (!primaryGroup.equals("default")) {
                return primaryGroup;
            } else {
                DiscordSRV.debug("Tried looking up group for player " + player.getName() + " but the given group was \"default\"");
                return " ";
            }
        } catch (Exception e) {
            DiscordSRV.debug("Failed to look up group for player " + player.getName() + ": " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            return " ";
        }
    }

    public static String[] getPlayersGroups(OfflinePlayer player) {
        if (!PluginUtil.pluginHookIsEnabled("vault")) return new String[] {};

        try {
            RegisteredServiceProvider<?> service = Bukkit.getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.permission.Permission"));
            if (service == null) return new String[] {};

            // ((net.milkbowl.vault.permission.Permission) service.getProvider()).getPlayerGroups(worldName, OfflinePlayer)

            List<String> playerGroups = new ArrayList<>();
            Method getPlayerGroupsMethod = service.getProvider().getClass().getMethod("getPlayerGroups");
            for (World world : Bukkit.getWorlds())
                for (String group : (String[]) getPlayerGroupsMethod.invoke(service.getProvider(), world.getName(), player))
                    if (!playerGroups.contains(group)) playerGroups.add(group);
            for (String group : (String[]) getPlayerGroupsMethod.invoke(service.getProvider(), null, player))
                if (!playerGroups.contains(group)) playerGroups.add(group);

            return playerGroups.toArray(new String[0]);
        } catch (Exception ignored) { }
        return new String[] {};
    }

    public static String[] getGroups() {
        if (!PluginUtil.pluginHookIsEnabled("vault")) return new String[] {};

        try {
            RegisteredServiceProvider<?> service = Bukkit.getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.permission.Permission"));
            if (service == null) return new String[] {};

            Method getGroupsMethod = service.getProvider().getClass().getMethod("getGroups");
            return (String[]) getGroupsMethod.invoke(service.getProvider());
        } catch (Exception ignored) { }
        return new String[] {};
    }



    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if(command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player, 1.05);
            if(r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        } else if(command.getLabel().equals("test-permission")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(perms.has(player, "example.plugin.awesome")) {
                sender.sendMessage("You are awesome!");
            } else {
                sender.sendMessage("You suck!");
            }
            return true;
        } else {
            return false;
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    @Override
    public Plugin getPlugin() {
        return PluginUtil.getPlugin("Vault");
    }

}
