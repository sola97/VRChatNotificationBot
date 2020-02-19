package cn.sola97.vrchat.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandResultVO {
    int code;
    String msg;
    Object data;

    public int getCode() {
        return code;
    }

    public CommandResultVO setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public CommandResultVO setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Object getData() {
        return data;
    }

    public CommandResultVO setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "CommandResultVO{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
