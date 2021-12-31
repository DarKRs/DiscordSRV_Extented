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

package github.scarsz.discordsrv.objects.threads;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.scarsz.discordsrv.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.events.GameEconomyEvenetListener;
import github.scarsz.discordsrv.hooks.VaultHook;
import github.scarsz.discordsrv.util.PlayerUtil;
import github.scarsz.discordsrv.util.UnbelievaBoatUtil;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class EconomyWatchdog extends Thread{

    private long lastTick = System.currentTimeMillis();
    private int timeout = DiscordSRV.config().getInt("EconomyWatchdogTimeout");

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(timeout));
                MoneySync();

            } catch (InterruptedException e) {
                DiscordSRV.debug(Debug.WATCHDOG, "Broke from Server Economy thread: sleep interrupted");
                return;
            }
        }
    }


        private void MoneySync(){
            for (Player onlinePlayer : PlayerUtil.getOnlinePlayers()) {

                String userId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(onlinePlayer.getUniqueId());
                if (userId == null) continue;

                SetCash(onlinePlayer);
            }
        }

        private void SetCash(Player onlinePlayer){
            String UserID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId((onlinePlayer).getUniqueId());
            String Http = UnbelievaBoatUtil.GetUserBalance(UserID);
            //
            JsonObject jsonObject = new JsonParser().parse(Http).getAsJsonObject();
            double bal = VaultHook.getEconomy().getBalance(onlinePlayer);
            double balanceUB = Double.parseDouble(jsonObject.get("cash").getAsString());
            VaultHook.getEconomy().withdrawPlayer(onlinePlayer,bal);
            VaultHook.getEconomy().depositPlayer(onlinePlayer,balanceUB);
            GameEconomyEvenetListener.balance.remove(onlinePlayer.getName());
            GameEconomyEvenetListener.balance.put(onlinePlayer.getName(), GameEconomyEvenetListener.econ.getBalance(onlinePlayer));
        }

}
