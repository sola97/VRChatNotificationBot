package cn.sola97.vrchat.utils;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

public class MyDateConverter extends StdConverter<String, Date> {
    @Override
    public Date convert(final String value) {
        if("none".equals(value))return null;
        try {
            return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).parse(value.replaceAll("Z$", "+0000"));
        } catch (ParseException e) {
            return null;
        }
    }
}