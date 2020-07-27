package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.Channel;
import cn.sola97.vrchat.entity.Ping;
import cn.sola97.vrchat.entity.PingExample;
import cn.sola97.vrchat.mapper.PingMapper;
import cn.sola97.vrchat.service.ChannelService;
import cn.sola97.vrchat.service.PingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PingServiceImpl implements PingService {
    @Autowired
    PingMapper pingMapper;
    @Autowired
    ChannelService channelServiceImpl;
    @Override
    public List<Ping> selPingByChannelIdAndUserId(String channelId,String usrId){
        PingExample example = new PingExample();
        example.createCriteria().andChannelIdEqualTo(channelId).andUsrIdEqualTo(usrId);
        return pingMapper.selectByExample(example);
    }

    @Override
    public int insPing(Ping ping) {
        return pingMapper.insertSelective(ping);
    }
    @Override
    public int updPing(Ping ping){
        return pingMapper.updateByPrimaryKeySelective(ping);
    }

    @Override
    public int updPingByChannelIdAndUsrId(Ping ping) {
        PingExample pingExample = new PingExample();
        pingExample.createCriteria()
                .andChannelIdEqualTo(ping.getChannelId())
                .andUsrIdEqualTo(ping.getUsrId());
        return pingMapper.updateByExampleSelective(ping, pingExample);
    }

    @Override
    public int delPingByPrimaryKey(Ping ping) {
        return pingMapper.deleteByPrimaryKey(ping);
    }

    @Override
    public int delPingByChannelIdAndUsrId(Ping ping) {
        PingExample pingExample = new PingExample();
        pingExample.createCriteria()
                .andChannelIdEqualTo(ping.getChannelId())
                .andUsrIdEqualTo(ping.getUsrId());
        return pingMapper.deleteByExample(pingExample);
    }

    @Override
    public int insPingElseUpd(Ping ping){
        if(!existsPing(ping))
            return insPing(ping);
        return updPing(ping);
    }

    @Override
    public Byte selMaskByChannelIdAndUsrId(Ping ping) {
        PingExample pingExample = new PingExample();
        pingExample.createCriteria()
                .andChannelIdEqualTo(ping.getChannelId())
                .andUsrIdEqualTo(ping.getUsrId());
        List<Ping> pings = pingMapper.selectByExample(pingExample);
        return pings.get(0).getMask();
    }

    @Override
    public Ping selPingByPrimaryKey(Ping ping) {
        return pingMapper.selectByPrimaryKey(ping);
    }

    @Override
    public Boolean existsPing(Ping ping){
        PingExample example  = new PingExample();
        example.createCriteria().andChannelIdEqualTo(ping.getChannelId()).andUsrIdEqualTo(ping.getUsrId()).andDiscordIdEqualTo(ping.getDiscordId());
        return pingMapper.countByExample(example) > 0;
    }

    @Override
    public List<Ping> selAllPingNotInUsrIdList(List<String> usrIds) {
        List<String> channels = channelServiceImpl.selDisabledChannel().stream().map(Channel::getChannelId).collect(Collectors.toList());
        PingExample example = new PingExample();
        example.createCriteria().andChannelIdNotIn(channels).andUsrIdNotIn(usrIds).andDisabledEqualTo(false);
        return pingMapper.selectByExample(example);
    }
}
