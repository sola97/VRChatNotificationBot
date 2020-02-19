package cn.sola97.vrchat.commands;

import com.jagrosh.jdautilities.command.Command;

public abstract class ChannelCommand extends Command {
    public ChannelCommand() {
        this.category = new Category("Channel");
        this.ownerCommand = true;
    }
}
