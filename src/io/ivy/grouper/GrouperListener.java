
package io.ivy.grouper;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import net.canarymod.Canary;
import net.canarymod.plugin.PluginListener;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.chat.MessageReceiver;

import java.util.List;
import java.util.function.Consumer;

import net.canarymod.api.entity.living.humanoid.Player;

import com.whalin.MemCached.*;


public class GrouperListener implements PluginListener, CommandListener {
    private Map group_list = new HashMap();

    
    // invitee -> inviter
    private Map group_pending = new HashMap();
    
    private List<Player> grouped = new ArrayList<Player>();
    //private List<Player> group_pending = new ArrayList<Player>();
    
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
                    	MemCachedClient mcc = new MemCachedClient("192.168.0.210");
                    	mcc.add("test", "honka");
                    	
                    	Canary.log.info(mcc.get("test"));
                    	
                        // Have they already been invited to a group?
                        //if (pending_set.contains(player)) {
                        //    sender.message("That player is already considering a group.");
                        //    return;
                        //}
                        // Are they already in a group?
                        // if (grouped.contains(player)) {
                        //     sender.message("That player is already in a group.");
                        //     return;
                        // }

                        // group_pending.put(invitee, inviter);

                        //player.message(sender.getName() + " has invited you to a group.");
                        //player.message("Type /accept to accept this, or /decline to decline this invite.");
                    }
                }});
    }
            
    @Command( aliases = { "accept" },
              description = "Accept a group invitation.",
              permissions = {""},
              toolTip = "/accept")
    public void acceptCommand(MessageReceiver sender, String[] args) {
        sender.message("Accept!");
    }

    @Command( aliases = { "decline" },
              description = "Decline a group invitation from another player.",
              permissions = { "" },
              toolTip = "/decline")
    public void declineCommand(MessageReceiver sender, String[] args) {
        
    }

    @Command( aliases = { "leave" },
              description = "Leave a group.",
              permissions = { "" },
              toolTip = "/leave")
    public void leaveCommand(MessageReceiver sender, String[] args) {
        
    }

}
