package cn.sola97.vrchat.utils;

import cn.sola97.vrchat.entity.World;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorldUtil {
    public static String convertToString(World world, Map<String, String> locationMap, String instance) {
        if (StringUtils.isEmpty(locationMap.get("instanceId"))) {
            assert world != null;
            return world.getName();
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
    }

    public static String convertToString(World world, Map<String, String> locationMap) {
        return convertToString(world, locationMap, null);
    }
}
