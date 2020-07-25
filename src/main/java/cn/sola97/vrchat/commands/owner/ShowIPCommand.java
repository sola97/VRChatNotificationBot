package cn.sola97.vrchat.commands.owner;

import cn.sola97.vrchat.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShowIPCommand extends OwnerCommand {
    private static final Logger logger = LoggerFactory.getLogger(ShowIPCommand.class);
    public ShowIPCommand() {
        this.name = "showip";
        this.aliases = new String[]{};
        this.help = "显示当前服务器的IP地址";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        OkHttpClient client = new OkHttpClient();
        event.getChannel().sendMessage("正在获取IP...").queue(message -> {
            logger.info("ShowIP Channel:{} -{}", event.getChannel().getId(), event.getChannel().getName());
            Request request = new Request.Builder().url("https://myip.ipip.net/").build();
            try (Response response = client.newCall(request).execute()) {
                String ipString = response.body().string();
                message.editMessage(ipString).queue();
            } catch (Exception e) {
                message.editMessage("获取IP失败 error:" + e.getMessage()).queue();
            }
        });

    }
}