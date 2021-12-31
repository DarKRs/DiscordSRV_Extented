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
import github.scarsz.discordsrv.api.events.WatchdogMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.WatchdogMessagePreProcessEvent;
import github.scarsz.discordsrv.util.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import github.scarsz.discordsrv.hooks.VaultHook;

public class ServerWatchdog extends Thread {

    public ServerWatchdog() {
        super("DiscordSRV - Server Watchdog");
    }

    private long lastTick = System.currentTimeMillis();
    private boolean hasBeenTriggered = true;

    private void tick() {
        lastTick = System.currentTimeMillis();
        hasBeenTriggered = false;
    }

    @Override
    public void run() {
        int taskNumber = Bukkit.getScheduler().scheduleSyncRepeatingTask(DiscordSRV.getPlugin(), this::tick, 0, 20);
        if (taskNumber == -1) {
            DiscordSRV.debug(Debug.WATCHDOG, "Failed to schedule repeating task for server watchdog; returning");
            return;
        }

        while (true) {
            try {
                int timeout = DiscordSRV.config().getInt("ServerWatchdogTimeout");
                if (timeout < 10) timeout = 10; // minimum value
                if (hasBeenTriggered || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTick) < timeout) {
                    Thread.sleep(1000);
                } else {
                    hasBeenTriggered = true;

                    if (!DiscordSRV.config().getBoolean("ServerWatchdogEnabled")) {
                        DiscordSRV.debug(Debug.WATCHDOG, "The Server Watchdog would have triggered right now but it was disabled in the config");
                        continue;
                    }

                    String channelName = DiscordSRV.getPlugin().getOptionalChannel("watchdog");
                    String message = PlaceholderUtil.replacePlaceholdersToDiscord(LangUtil.Message.SERVER_WATCHDOG.toString());
                    int count = DiscordSRV.config().getInt("ServerWatchdogMessageCount");

                    WatchdogMessagePreProcessEvent preEvent = DiscordSRV.api.callEvent(new WatchdogMessagePreProcessEvent(channelName, message, count, false));
                    if (preEvent.isCancelled()) {
                        DiscordSRV.debug(Debug.WATCHDOG, "WatchdogMessagePreProcessEvent was cancelled, message send aborted");
                        continue;
                    }
                    // Update from event in case any listeners modified parameters
                    count = preEvent.getCount();
                    channelName = preEvent.getChannel();
                    message = preEvent.getMessage();

                    String discordMessage = message
                            .replaceAll("%time%|%date%", TimeUtil.timeStamp())
                            .replace("%guildowner%", DiscordSRV.getPlugin().getMainGuild().getOwner().getAsMention());

                    WatchdogMessagePostProcessEvent postEvent = DiscordSRV.api.callEvent(new WatchdogMessagePostProcessEvent(channelName, discordMessage, count, false));
                    if (postEvent.isCancelled()) {
                        DiscordSRV.debug(Debug.WATCHDOG, "WatchdogMessagePostProcessEvent was cancelled, message send aborted");
                        continue;
                    }
                    // Update from event in case any listeners modified parameters
                    count = postEvent.getCount();
                    channelName = postEvent.getChannel();
                    discordMessage = postEvent.getProcessedMessage();

                    TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);

                    for (int i = 0; i < count; i++) {
                        DiscordUtil.queueMessage(channel, discordMessage, true);
                    }
                }
            } catch (InterruptedException e) {
                DiscordSRV.debug(Debug.WATCHDOG, "Broke from Server Watchdog thread: sleep interrupted");
                return;
            }
        }
    }



}