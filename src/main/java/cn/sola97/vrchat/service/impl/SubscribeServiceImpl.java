package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.Channel;
import cn.sola97.vrchat.entity.Subscribe;
import cn.sola97.vrchat.entity.SubscribeExample;
import cn.sola97.vrchat.mapper.SubscribeMapper;
import cn.sola97.vrchat.service.ChannelService;
import cn.sola97.vrchat.service.PingService;
import cn.sola97.vrchat.service.SubscribeService;
import cn.sola97.vrchat.service.VRChatApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscribeServiceImpl implements SubscribeService {
    @Autowired
    SubscribeMapper subscribeMapper;
    @Autowired
    ChannelService channelServiceImpl;
    @Autowired
    VRChatApiService vrchatApiServiceImpl;
    @Autowired
    PingService pingServiceImpl;

    @Override
    public int insSubscribe(Subscribe subscribe){
        return subscribeMapper.insertSelective(subscribe);
    }

    @Override
    public int updSubscribe(Subscribe subscribe){
        return subscribeMapper.updateByPrimaryKeySelective(subscribe);
    }
    @Override
    public Boolean existsSubscribe(Subscribe subscribe){
        SubscribeExample example = new SubscribeExample();
        example.createCriteria().andChannelIdEqualTo(subscribe.getChannelId()).andUsrIdEqualTo(subscribe.getUsrId());
        return subscribeMapper.countByExample(example)>0;
    }
    @Override
    public int insSubscribeElseUpd(Subscribe subscribe){
        if(!existsSubscribe(subscribe)){
            return insSubscribe(subscribe);
        }
        return updSubscribe(subscribe);
    }

    @Override
    public int insSubscribeIfNotExists(Subscribe subscribe) {
        if (!existsSubscribe(subscribe)) {
            return insSubscribe(subscribe);
        }
        return 1;
    }

    @Override
    public int delSubscribe(Subscribe subscribe){
        return subscribeMapper.deleteByPrimaryKey(subscribe);
    }
    @Override
   public List<Subscribe> selSubscribesByUsrId(String usrId){
        SubscribeExample example = new SubscribeExample();
        example.createCriteria().andUsrIdEqualTo(usrId);
        List<Subscribe> subscribes = subscribeMapper.selectByExample(example);
        return subscribes;
    }

    @Override
    public Subscribe selSubscribesByPrimaryKey(Subscribe subscribe) {
        return subscribeMapper.selectByPrimaryKey(subscribe);
    }

    @Override
    public Boolean existsSubscribe(String channelId,String usr_id){
        SubscribeExample example = new SubscribeExample();
        example.createCriteria().andChannelIdEqualTo(channelId).andUsrIdEqualTo(usr_id);
        return subscribeMapper.countByExample(example)>0;
    }

    @Override
    public List<Subscribe> selSubscribesByChannelId(String channelId){
        SubscribeExample example = new SubscribeExample();
        example.createCriteria().andChannelIdEqualTo(channelId);
        return subscribeMapper.selectByExample(example);
    }


    @Override
    public List<Subscribe> selSubscribesByUsrIdNotInChannels(String usrId, List<String> channelIds){
        if(channelIds.isEmpty()){
            return selSubscribesByUsrId(usrId);
        }
        SubscribeExample example = new SubscribeExample();
        example.createCriteria().andUsrIdEqualTo(usrId).andChannelIdNotIn(channelIds);
        return subscribeMapper.selectByExample(example);
    }

    @Override
    public List<Subscribe> selAllSubscribesNotInUsrIdList(List<String> usrIds) {
        List<String> channels = channelServiceImpl.selDisabledChannel().stream().map(Channel::getChannelId).collect(Collectors.toList());
        SubscribeExample example = new SubscribeExample();
        example.createCriteria().andChannelIdNotIn(channels).andUsrIdNotIn(usrIds).andDisabledEqualTo(false);
        return subscribeMapper.selectByExample(example);
    }


}
