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

package github.scarsz.discordsrv.api.events;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.hooks.VaultHook;
import github.scarsz.discordsrv.util.UnbelievaBoatUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class GameEconomyEvenetListener extends Thread implements  Listener{

    public static Economy econ = VaultHook.getEconomy();
    public static HashMap<String, Double> balance = new HashMap<String, Double>();


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        balance.put(event.getPlayer().getName(), econ.getBalance(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if(balance.containsKey(event.getPlayer().getName()))
            balance.remove(event.getPlayer().getName());
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            //Checking if the players balance is the same as we have, if not continue
            if(econ.getBalance(p) != balance.get(p.getName())){
                //Do stuff here
                String UserID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId((p).getUniqueId());
                UnbelievaBoatUtil.PutUserBalance(UserID,"cash",Double.toString(econ.getBalance(p)));

                //Once we're done, we have to update the players money
                balance.remove(p.getName());
                balance.put(p.getName(), econ.getBalance(p));
            }
        }
    }



}

