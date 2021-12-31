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
import github.scarsz.discordsrv.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CommandGetBalance {

    private static List<ChatColor> disallowedChatColorCharacters = new ArrayList<ChatColor>() {{
        add(ChatColor.BLACK);
        add(ChatColor.DARK_BLUE);
        add(ChatColor.GRAY);
        add(ChatColor.DARK_GRAY);
        add(ChatColor.WHITE);
        add(ChatColor.MAGIC);
        add(ChatColor.BOLD);
        add(ChatColor.STRIKETHROUGH);
        add(ChatColor.UNDERLINE);
        add(ChatColor.ITALIC);
        add(ChatColor.RESET);
    }};

    @Command(commandNames = { "bal", "balance", "money" },
            helpMessage = "Get your balance on server",
            permission = "discordsrv.balance",
            usageExample = "balance"
    )

    public static void execute(CommandSender sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), () -> executeAsync(sender, args));
    }

    public static void executeAsync(CommandSender sender, String[] args){
        if (!sender.hasPermission("discordsrv.balance")) {
            MessageUtil.sendMessage(sender, LangUtil.Message.NO_PERMISSION.toString());
            return;
        }
        ChatColor titleColor = ChatColor.RESET, cashColor = ChatColor.RESET, bankColor = ChatColor.RESET, totalColor = ChatColor.RESET;
        String UserID = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(((Player) sender).getUniqueId());
        String Http = UnbelievaBoatUtil.GetUserBalance(UserID);
        JsonObject jsonObject = new JsonParser().parse(Http).getAsJsonObject();
        titleColor = ChatColor.values()[ThreadLocalRandom.current().nextInt(ChatColor.values().length)];
        MessageUtil.sendMessage(sender, ChatColor.DARK_GRAY + "================[ " + titleColor + "Ваш баланс" + ChatColor.DARK_GRAY + " ]================");
        MessageUtil.sendMessage(sender, ChatColor.DARK_GREEN + "На руках :" + jsonObject.get("cash").getAsString()+ "денариев");
        MessageUtil.sendMessage(sender, ChatColor.AQUA + "В банке :" + jsonObject.get("bank").getAsString()+ "денариев");
        MessageUtil.sendMessage(sender, ChatColor.YELLOW + "Всего :" + jsonObject.get("total").getAsString() + "денариев");

    }

}
