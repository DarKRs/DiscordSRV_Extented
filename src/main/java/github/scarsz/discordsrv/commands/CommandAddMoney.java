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

package github.scarsz.discordsrv.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.*;
import org.apache.logging.log4j.core.helpers.Assert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static github.scarsz.discordsrv.commands.CommandLinked.*;
import github.scarsz.discordsrv.hooks.VaultHook;

public class CommandAddMoney {

    @Command(commandNames = { "addmoney" },
            helpMessage = "Add money to member",
            permission = "discordsrv.addmoney",
            usageExample = "addmoney <member> <count>"
    )

    public static void execute(CommandSender sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), () -> executeAsync(sender, args));
    }

    public static void executeAsync(CommandSender sender, String[] args){
        if (!sender.hasPermission("discordsrv.addmoney")) {
            MessageUtil.sendMessage(sender, LangUtil.Message.NO_PERMISSION.toString());
            return;
        }
        if(args.length == 0 || args.length == 1){
            MessageUtil.sendMessage(sender, ChatColor.RED + "Необходимо указать пользователя, и кол-во выдаваемых денег");
            return;
        }else{
            String target = args[0];
            String sum = args[1];
            if (target.length() >= 3 && target.length() <= 16) {
                // target is probably a Minecraft player name
                OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(OfflinePlayer::hasPlayedBefore)
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(target))
                        .findFirst().orElse(null);

                if (player != null) {
                    String UserID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
                    if(UnbelievaBoatUtil.PatchUserBalance(UserID,sum)){
                        if(Integer.parseInt(args[1]) > 0){

                            VaultHook.getEconomy().depositPlayer(player, Double.parseDouble(sum));
                            MessageUtil.sendMessage(sender, ChatColor.AQUA + "Баланс игрока " + args[0] + " пополнен на " + sum);
                        }else {
                            VaultHook.getEconomy().withdrawPlayer(player, Double.parseDouble(sum));
                            MessageUtil.sendMessage(sender, ChatColor.AQUA + "С баланса игрока " + args[0] + " списано " + sum);
                        }
                    }else{
                        MessageUtil.sendMessage(sender, ChatColor.RED + "Внутреняя ошибка");
                    }

                }
            }else{
                MessageUtil.sendMessage(sender, ChatColor.RED + "Неверно указан никнейм игрока");
            }
        }
    }

}
