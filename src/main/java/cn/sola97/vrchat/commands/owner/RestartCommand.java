package cn.sola97.vrchat.commands.owner;

import cn.sola97.vrchat.commands.OwnerCommand;
import cn.sola97.vrchat.pojo.CommandResultVO;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.web.client.RestTemplate;

public class RestartCommand extends OwnerCommand {
    private final RestTemplate restTemplate;

    public RestartCommand(RestTemplate restTemplate) {
        this.name = "restart";
        this.aliases = new String[]{""};
        this.help = "重启Bot";
        this.guildOnly = false;
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendMessage("正在重启" + event.getArgs().trim()).queue(msg -> {
            String uri = "/system/restart";
            CommandResultVO commandResult = restTemplate.getForObject(uri, CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() != 200) {
                msg.editMessage(commandResult.getMsg()).queue();
            } else if (commandResult != null) {
                msg.editMessage(commandResult.getMsg()).queue();
            }
        });
    }
}