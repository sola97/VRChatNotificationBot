package cn.sola97.vrchat.commands.owner;

import cn.sola97.vrchat.commands.OwnerCommand;
import cn.sola97.vrchat.pojo.CommandResultVO;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class RestartCommand extends OwnerCommand {
    private static final Logger logger = LoggerFactory.getLogger(OwnerCommand.class);
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
        logger.info("重启Bot by Channel:{} -{}", event.getChannel().getId(), event.getChannel().getName());
        event.getChannel().sendMessage("正在重启" + event.getArgs().trim()).queue(msg -> {
            String uri = "/system/restart";
            CommandResultVO commandResult = restTemplate.getForObject(uri, CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() != 200) {
                logger.error("RestartCommand URL:{} return:{}", uri.toString(), commandResult);
                msg.editMessage(commandResult.getMsg()).queue();
            } else if (commandResult != null) {
                logger.info("RestartCommand {}", commandResult.getMsg());
                msg.editMessage(commandResult.getMsg()).queue();
            }
        });
    }
}