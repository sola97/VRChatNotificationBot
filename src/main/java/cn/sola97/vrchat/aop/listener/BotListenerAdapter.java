package cn.sola97.vrchat.aop.listener;

import cn.sola97.vrchat.aop.handler.WsHandler;
import cn.sola97.vrchat.pojo.MessageDTO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class BotListenerAdapter extends ListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(BotListenerAdapter.class);
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${bot.messageQueueKey}")
    String messageKey;
    @Autowired
    WsHandler wsHandler;
    @Value("${bot.ownerId}")
    String ownerId;
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        JDA jda = event.getJDA();
        logger.info("-------------------Discord Bot Connected-------------------");
        this.startedMessage(jda);
        new Thread(() -> {
            while (jda.getStatus().equals(JDA.Status.CONNECTED)) {
                try {
                    MessageDTO message = (MessageDTO) redisTemplate.opsForList().leftPop(messageKey, 86400, TimeUnit.SECONDS);
                    MessageEmbed embed = Optional.ofNullable(message.getEmbedBuilder()).map(EmbedBuilder::build).orElse(null);
                    String content = message.getContent();
                    String channelId = message.getChannelId();
                    String callback = message.getCallback();
                    if (message.getType() == MessageDTO.typeEnums.SEND_TO_CHANNEL) {
                        this.sendToChannel(jda, message, embed, content, channelId);
                    } else if (message.getType() == MessageDTO.typeEnums.SEND_TO_OWNER) {
                        this.sendToOwner(jda, message, embed, content);
                    } else if (message.getType() == MessageDTO.typeEnums.EDIT_MESSAGE) {
                        this.editMessage(jda, message, embed, content, channelId, callback);
                    } else {
                        logger.warn("unknown MessageDTO type, message:" + message.toString());
                    }
                } catch (Exception e) {
                    logger.error("message 发送失败" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        logger.warn("Bot已断开连接 status:" + event.getJDA().getStatus().name());
    }

    @Override
    public void onResume(@Nonnull ResumedEvent event) {
        logger.info("Bot重新连接成功 status:" + event.getJDA().getStatus().name());
    }

    @Override
    public void onReconnect(@Nonnull ReconnectedEvent event) {
        logger.warn("Bot已重新连接 status:" + event.getJDA().getStatus().name());
    }

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        logger.info("bot is shutting down now");
    }

    private void sendToChannel(JDA jda, MessageDTO message, MessageEmbed embed, String content, String channelId) {
        //好友通知
        new MessageBuilder()
                .setContent(Optional.ofNullable(content).map(t -> t + "\n").orElse("") + String.join("\n", message.getPings()))
                .setEmbed(embed).sendTo(jda.getTextChannelById(channelId)).queue(
                suc -> logger.info(message.getChannelId() + "发送成功：" + embed.getAuthor().getName() + " " + message.getEmbedBuilder().getDescriptionBuilder().toString()
                        + Optional.ofNullable(message.getEmbedBuilder()).map(EmbedBuilder::getFields).filter(fields -> fields.size() > 0).map(m -> m.get(0).getName() + " ").orElse("")
                        + Optional.ofNullable(message.getEmbedBuilder()).map(EmbedBuilder::getFields).filter(fields -> fields.size() > 0).map(m -> m.get(0).getValue().replaceAll("\n", " ")).orElse("")),
                fail -> {
                    logger.warn(message.getChannelId() + "发送失败：" + embed.getAuthor().getName() + " " + message.getEmbedBuilder().getDescriptionBuilder().toString()
                            + Optional.ofNullable(message.getEmbedBuilder()).map(EmbedBuilder::getFields).filter(fields -> fields.size() > 0).map(m -> m.get(0).getName() + " ").orElse("")
                            + Optional.ofNullable(message.getEmbedBuilder()).map(EmbedBuilder::getFields).filter(fields -> fields.size() > 0).map(m -> m.get(0).getValue().replaceAll("\n", " ")).orElse(""));
                    try {
                        redisTemplate.opsForList().leftPush(messageKey, message);
                    } catch (Exception e) {
                        logger.error("重新放入Message出错 message:{} content:{} channelId:{}", message.toString(), content, channelId, e);
                    }
                });
    }

    private void sendToOwner(JDA jda, MessageDTO message, MessageEmbed embed, String content) {
        logger.debug("send to owner：" + message.toString());
        Optional.ofNullable(jda.getUserById(ownerId)).map(User::openPrivateChannel).ifPresent((opened) -> {
            opened.queue((channel) ->
            {
                new MessageBuilder()
                        .setContent(content)
                        .setEmbed(embed).sendTo(channel).queue();
            });
        });
    }

    private void editMessage(JDA jda, MessageDTO message, MessageEmbed embed, String content, String channelId, String callback) {
        if (callback == null) return;
        logger.debug("准备编辑：" + channelId + "  messageId:" + message.getCallback());
        jda.getTextChannelById(channelId).editMessageById(callback, new MessageBuilder()
                .setEmbed(embed)
                .setContent(content)
                .build()).queue(succ -> {
            logger.debug("编辑成功：" + channelId + "  messageId:" + message.getCallback());
        }, fail -> {
            logger.warn("编辑失败：" + channelId + "  messageId:" + message.getCallback());
        });
    }

    private void startedMessage(JDA jda) {
        Optional.ofNullable(jda.getUserById(ownerId)).map(User::openPrivateChannel).ifPresent((opened) -> {
            opened.queue((channel) ->
            {
                channel.sendMessage("Bot Started").queue();
            });
        });
    }
}

