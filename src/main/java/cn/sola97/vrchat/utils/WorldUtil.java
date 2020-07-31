package cn.sola97.vrchat.utils;

import cn.sola97.vrchat.entity.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorldUtil {
    private static final Logger logger = LoggerFactory.getLogger(WorldUtil.class);
    public static String convertToString(World world, Map<String, String> locationMap, String instance) {
        try {
            if (StringUtils.isEmpty(locationMap.get("instanceId"))) {
                if (world != null)
                    return world.getName();
                else
                    return "";
            }
            String num = "?";
            if (instance != null) {
                List<List> lists = world.getInstances().stream().filter(Objects::nonNull).filter(pair -> instance.equals(pair.get(0))).collect(Collectors.toList());
                num = lists.isEmpty() ? "?" : lists.get(0).get(1).toString();
            }

            StringBuilder value = new StringBuilder()
                    .append(world.getName()).append(":").append(locationMap.get("instanceId")).append("\n")
                    .append(locationMap.get("username")).append(" ").append(locationMap.get("status")).append(" ").append(num).append("/").append(world.getCapacity());
            return value.toString();
        } catch (Exception e) {
            logger.error("convertToString error", e);
        }
        return "";
    }

    public static String convertToStringOneLine(World world, Map<String, String> locationMap) {
        return convertToString(world, locationMap, null).replaceAll("\n", "");
    }
}
