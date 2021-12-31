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
import github.scarsz.discordsrv.hooks.VaultHook;
import github.scarsz.discordsrv.util.LangUtil;
import github.scarsz.discordsrv.util.MessageUtil;
import github.scarsz.discordsrv.util.UnbelievaBoatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandGiveMoney {

    @Command(commandNames = { "givemoney"},
            helpMessage = "give your money to member",
            permission = "discordsrv.givemoney",
            usageExample = "givemoney [user] [sum]"
    )

    public static void execute(CommandSender sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), () -> executeAsync(sender, args));
    }

    public static void executeAsync(CommandSender sender, String[] args){
        if (!sender.hasPermission("discordsrv.givemoney")) {
            MessageUtil.sendMessage(sender, LangUtil.Message.NO_PERMISSION.toString());
            return;
        }
        if(args.length < 2){
            MessageUtil.sendMessage(sender, ChatColor.RED + "Необходимо указать пользователя, и кол-во передаваемых денег");
            return;
        }else {
            String target = args[0];
            String sum = args[1];
            if(Integer.parseInt(args[1]) <= 1){
                MessageUtil.sendMessage(sender, ChatColor.RED + "Необходимо указать число больше еденицы");
                return;
            }
            OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(target))
                    .findFirst().orElse(null);
            if (player != null) {
                String TargetID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
                String SenderID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(((Player) sender).getUniqueId());
                if(VaultHook.getEconomy().getBalance((Player) sender) < Double.parseDouble(sum)) {
                    UnbelievaBoatUtil.PatchUserBalance(TargetID, sum, "cash");
                    UnbelievaBoatUtil.PatchUserBalance(SenderID, "-" + sum, "cash");
                    VaultHook.getEconomy().depositPlayer(player, Double.parseDouble(sum));
                    VaultHook.getEconomy().withdrawPlayer((Player) sender, -Double.parseDouble(sum));
                    MessageUtil.sendMessage(sender, ChatColor.AQUA + "Вы передали " + args[0] + " " + sum + " денариев");
                    MessageUtil.sendMessage((CommandSender) player, sender.getName() + " передал вам " + sum + " денариев");
                }else{
                    MessageUtil.sendMessage(sender, ChatColor.RED + "У вас не достаточно денариев!");
                    return;
                }
            }else{
                MessageUtil.sendMessage(sender, ChatColor.RED + "Неверно указан никнейм игрока");
                return;
            }
        }
    }
}
