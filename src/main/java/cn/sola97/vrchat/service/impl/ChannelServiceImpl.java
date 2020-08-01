package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.Channel;
import cn.sola97.vrchat.entity.ChannelExample;
import cn.sola97.vrchat.mapper.ChannelMapper;
import cn.sola97.vrchat.service.ChannelService;
import cn.sola97.vrchat.service.PingService;
import cn.sola97.vrchat.service.SubscribeService;
import cn.sola97.vrchat.service.VRChatApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {
    public static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);
    @Autowired
    ChannelMapper channelMapper;
    @Autowired
    VRChatApiService vrchatApiServiceImpl;
    @Autowired
    SubscribeService subscribeServiceImpl;
    @Autowired
    PingService pingServiceImpl;
    @Override
    public Channel selChannelById(String channelId){
        return channelMapper.selectByPrimaryKey(channelId);
    }

    @Override
    public Boolean existsChannel(String channelId){
        ChannelExample channelExample = new ChannelExample();
        channelExample.createCriteria().andChannelIdEqualTo(channelId);
        return channelMapper.countByExample(channelExample)>0;
    }

    @Override
    public int insChannel(Channel channel) {
        return channelMapper.insertSelective(channel);
    }
    @Override
    public int updChannelByPrimaryKey(Channel channel) {
        return channelMapper.updateByPrimaryKeySelective(channel);
    }
    @Override
    public int insChannelElseUpd(Channel channel) {
        if(existsChannel(channel.getChannelId())){
            return updChannelByPrimaryKey(channel);
        }
        return insChannel(channel);
    }

    @Override
    public List<Channel> selAllChannel() {
        ChannelExample exapmle = new ChannelExample();
        return channelMapper.selectByExample(exapmle);
    }

    @Override
    public List<Channel> selDisabledChannel() {
        ChannelExample exapmle = new ChannelExample();
        exapmle.createCriteria().andDisabledEqualTo(true);
        return channelMapper.selectByExample(exapmle);
    }

    @Override
    public boolean disableChannelByChannelId(String channelId) {
        Channel channel = new Channel();
        channel.setChannelId(channelId);
        channel.setDisabled(true);
        channel.setUpdatedAt(new Date());
        return updChannelByPrimaryKey(channel) > 0;
    }

    @Override
    public boolean enableChannelByChannelId(String channelId) {
        Channel channel = new Channel();
        channel.setChannelId(channelId);
        channel.setDisabled(false);
        channel.setUpdatedAt(new Date());
        return updChannelByPrimaryKey(channel) > 0;
    }
}
