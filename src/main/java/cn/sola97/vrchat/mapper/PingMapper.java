package cn.sola97.vrchat.mapper;

import cn.sola97.vrchat.entity.Ping;
import cn.sola97.vrchat.entity.PingExample;
import cn.sola97.vrchat.entity.PingKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PingMapper {
    long countByExample(PingExample example);

    int deleteByExample(PingExample example);

    int deleteByPrimaryKey(PingKey key);

    int insert(Ping record);

    int insertSelective(Ping record);

    List<Ping> selectByExample(PingExample example);

    Ping selectByPrimaryKey(PingKey key);

    int updateByExampleSelective(@Param("record") Ping record, @Param("example") PingExample example);

    int updateByExample(@Param("record") Ping record, @Param("example") PingExample example);

    int updateByPrimaryKeySelective(Ping record);

    int updateByPrimaryKey(Ping record);
}