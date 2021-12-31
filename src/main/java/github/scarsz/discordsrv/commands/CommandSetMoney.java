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

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.LangUtil;
import github.scarsz.discordsrv.util.MessageUtil;
import github.scarsz.discordsrv.util.UnbelievaBoatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandSetMoney {

    @Command(commandNames = { "setmoney" },
            helpMessage = "Set money to member",
            permission = "discordsrv.setmoney",
            usageExample = "addmoney <member> <cash | bank> <count>"
    )

    public static void execute(CommandSender sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), () -> executeAsync(sender, args));
    }

    public static void executeAsync(CommandSender sender, String[] args){
        if (!sender.hasPermission("discordsrv.setmoney")) {
            MessageUtil.sendMessage(sender, LangUtil.Message.NO_PERMISSION.toString());
            return;
        }
        if(args.length < 3){
            MessageUtil.sendMessage(sender, ChatColor.RED + "Необходимо указать пользователя, cash или bank и кол-во устанавливаемых денег");
            return;
        }
        else{
            String target = args[0];
            String CashOrBank = args[1];
            if(!CashOrBank.contains("cash") && !CashOrBank.contains("bank")){
                MessageUtil.sendMessage(sender, ChatColor.RED + "Необходимо указать изменяется наличка или банк пользователя. <cash | bank>");
                return;
            }
            String sum = args[2];
            if (target.length() >= 3 && target.length() <= 16) {
                // target is probably a Minecraft player name
                OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(OfflinePlayer::hasPlayedBefore)
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(target))
                        .findFirst().orElse(null);
                if (player != null) {
                    String UserID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
                    if(UnbelievaBoatUtil.PutUserBalance(UserID, CashOrBank, sum)){
                        if(CashOrBank.equals("bank")){
                            MessageUtil.sendMessage(sender, ChatColor.AQUA + "Банк игрока " + args[0] + " установлен на " + sum);
                        }if(CashOrBank.equals("cash")){
                            MessageUtil.sendMessage(sender, ChatColor.AQUA + "Наличка игрока " + args[0] + " установлена на " + sum);
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
