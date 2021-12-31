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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class CommandTakeDiscord {
    @Command(commandNames = { "takeDiscord" },
            helpMessage = "Take money from Discord UnbelievaBoat",
            permission = "discordsrv.takeDiscord",
            usageExample = "takeDiscord [cash | bank] <count>"
    )

    public static void execute(CommandSender sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), () -> executeAsync(sender, args));
    }

    public static void executeAsync(CommandSender sender, String[] args){
        if (!sender.hasPermission("discordsrv.takeDiscord")) {
            MessageUtil.sendMessage(sender, LangUtil.Message.NO_PERMISSION.toString());
            return;
        }
        if(args.length < 1 || args[0].contains("help")){
            MessageUtil.sendMessage(sender, ChatColor.YELLOW + "Справка по команде: " + ChatColor.WHITE +  "/takeDiscord");
            MessageUtil.sendMessage(sender, ChatColor.YELLOW + "Описание:" + ChatColor.WHITE + "Снимает деньги с вашего счёта UnbelievaBoat и перечисляет их на счёт Minecraft");
            MessageUtil.sendMessage(sender, ChatColor.YELLOW + "Использование:" + ChatColor.WHITE + "/takeDiscord [cash | bank] <сумма>");
            MessageUtil.sendMessage(sender, ChatColor.YELLOW + "Примечание:" + ChatColor.WHITE + "Аргумент cash/bank опционален. По умолчанию списывается с cash");
            MessageUtil.sendMessage(sender, ChatColor.YELLOW + "Примечание:" + ChatColor.WHITE + "Сумма должна быть больше 0");
            return;
        }
            if(args.length == 1){
                if(!args[0].matches("[-+]?\\d+") || Integer.parseInt(args[0]) < 1 ){
                    MessageUtil.sendMessage(sender, ChatColor.RED + "Кол-во снимаемых со счёта денег должно быть больше 0");
                    return;
                }else{
                    takeCash(sender,args[0],"cash");
                    return;
                }
            }if(args.length == 2){
                if(!args[0].contains("cash") && !args[0].contains("bank")){
                    MessageUtil.sendMessage(sender, ChatColor.RED + "Неверно указан аргумент. Введите /discord takeDiscord help для получения справки");
                    return;
                }
                if (!args[1].matches("[-+]?\\d+") || Integer.parseInt(args[1]) < 1 ){
                    MessageUtil.sendMessage(sender, ChatColor.RED + "Кол-во снимаемых со счёта денег должно быть больше 0");
                    return;
                }else {
                    if(args[0].contains("cash")){
                        takeCash(sender,args[1],"cash");
                        return;
                    }
                    if (args[0].contains("bank")){
                        takeCash(sender,args[1],"bank");
                        return;
                    }
                }
            }else {
                MessageUtil.sendMessage(sender, ChatColor.RED + "Неверное кол-во аргументов. Можно указать [cash | bank] и кол-во снимаемых со счёта денег");
                return;
            }

    }

    private static void takeCash(CommandSender sender, String sum, String CashOrBank){
        String UserID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(((Player) sender).getUniqueId());
        String Http = UnbelievaBoatUtil.GetUserBalance(UserID);
        JsonObject jsonObject = new JsonParser().parse(Http).getAsJsonObject();
        if(jsonObject.get(CashOrBank).getAsInt() < Integer.parseInt(sum)){
            MessageUtil.sendMessage(sender, ChatColor.RED + "У вас недостаточно денег на счету " + CashOrBank + " в UnbelievaBoat");
            return;
        }
        UnbelievaBoatUtil.PatchUserBalance(UserID,"-"+sum,CashOrBank);
        VaultHook.getEconomy().depositPlayer((OfflinePlayer) sender, Double.parseDouble(sum));
        MessageUtil.sendMessage(sender, ChatColor.GREEN + "Вы успешно перевели " + sum + " денариев себе на счёт Minecraft!");
    }


}