
package io.ivy.grouper;


import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;
import net.canarymod.commandsys.CommandDependencyException;

public class Grouper extends Plugin {

    @Override
    public boolean enable() {
        Canary.hooks().registerListener(new GrouperListener(), this);
        try {
            Canary.commands().registerCommands(new GrouperListener(), this, false);
        } catch(CommandDependencyException e) {
            Canary.log.info(e);
        }
        return true;
    }

    @Override
    public void disable() {
    }
}
