// -*- Mode: jde; eval: (hs-hide-level 2) -*-

package io.ivy.grouper;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.canarymod.Canary;
import net.canarymod.plugin.PluginListener;

import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.chat.MessageReceiver;

import net.canarymod.api.scoreboard.Score;
import net.canarymod.api.scoreboard.ScoreObjective;
import net.canarymod.api.scoreboard.ScorePosition;
import net.canarymod.api.scoreboard.Scoreboard;

import net.canarymod.api.entity.living.humanoid.Player;
import redis.clients.jedis.*;
import net.canarymod.api.scoreboard.Team;

import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.*;
import net.canarymod.hook.system.*;

import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.Server;


/*

group.pending.<name> - when an invite is pending for <name>.  Value is the name of the inviter.
group.grouped.<name> - <name> is in a group.
group.group.<name> - <name> is the leader of the group. Value is list of names in that group.
group.<name>.leader - <name> is YOU, value is the leader of the group.

*/

public class GrouperListener implements PluginListener, CommandListener {

    /*
      
      Commands
      
    */
    
    @Command( aliases = {"invite"},
              description = "Invite another player to a group.",
              permissions = {""},
              toolTip = "/invite playername",
              min = 1)
    public void inviteCommand(MessageReceiver sender, String[] args) {
    
        String inviter = sender.getName();
        String invitee = args[1];
        
        List<Player> players = Canary.getServer().getPlayerList();
        players.forEach(new Consumer<Player>() {
                @Override
                public void accept(Player player) {
                    if (player.getName().equals(invitee)) {

                        Jedis j = new Jedis("192.168.0.210");
                                                
                        // Have they already been invited to a group?
                        if (j.get("group.pending." + invitee) != null) {
                            sender.message("That player is already considering a group.");
                            j.close();
                            return;
                        }
                        
                        // Are they already in a group?
                        if (j.get("group.grouped." + invitee) != null) {
                        	sender.message("That player is already in a group.");
                        	j.close();
                        	return;
                        }
                        
                        j.set("group.pending." + invitee, inviter);
                        
                        player.message(sender.getName() + " has invited you to a group.");
                        player.message("Type /accept to accept this, or /decline to decline this invite.");
                        j.close();
                    }
                }
            });
    }
            
    @Command( aliases = { "accept" },
              description = "Accept a group invitation.",
              permissions = {""},
              toolTip = "/accept")
    public void acceptCommand(MessageReceiver sender, String[] args) {
        Jedis j = new Jedis("192.168.0.210");
    	
        String inviter = j.get("group.pending." + sender.getName());

        j.del("group.pending." + sender.getName());
    	
        if (j.llen("group." + inviter) == 0) {
            // This is the first person added to this group.
            j.rpush("group.group." + inviter, inviter);
            j.set("group." + inviter + ".leader", inviter);
        }
    	
        j.rpush("group.group." + inviter, sender.getName());    
        j.set("group.grouped." + sender.getName(), "1");
        j.set("group.grouped." + inviter, "1");
    	
        j.set("group." + sender.getName() + ".leader", inviter);
    	
        List<String> members = j.lrange("group.group" + inviter, 0, -1);
    	
        members.forEach(new Consumer<String>() {
                @Override
                public void accept(String name) {
                    Player the_player = player_for_name(name);
                    if (the_player != null) {
                        the_player.notice(sender.getName() + " has joined the group.");
                    }
                }
            });
        j.close();
    }
    
    @Command( aliases = { "decline" },
              description = "Decline a group invitation from another player.",
              permissions = { "" },
              toolTip = "/decline")
    public void declineCommand(MessageReceiver sender, String[] args) {
        Jedis j = new Jedis("192.168.0.210");

        String invitee = sender.getName();
        String inviter = j.get("group.pending." + sender.getName());
        
        j.del("group.pending." + invitee);
        
        Player inviter_player = player_for_name(inviter);
        
        if (inviter_player != null) {
            inviter_player.message(invitee + " has declined your group request.");
        } else {
            sender.message("Null??");
        }
        j.close();
    }

    /*

      Utils

    */
    
    private Player player_for_name(String name) {
        return Canary
            .getServer()
            .getPlayerList()
            .stream()
            .filter(x -> x.getName().equals(name))
            .findFirst().get();
    }
}
