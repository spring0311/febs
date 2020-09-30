package cc.mrbird.febs.common.entity;

import org.springframework.http.HttpStatus;

import java.util.HashMap;

/**
 * @author MrBird
 */
public class FebsResponse extends HashMap<String, Object> {

    private static final long serialVersionUID = -8713837118340960775L;

    /**
     * http状态码
     *
     * @param status
     * @return
     */
    public FebsResponse code(HttpStatus status) {
        this.put("code", status.value());
        return this;
    }

    /**
     * 消息
     *
     * @param message
     * @return
     */
    public FebsResponse message(String message) {
        this.put("message", message);
        return this;
    }

    /**
     * data对象 传输到页面
     *
     * @param data
     * @return
     */
    public FebsResponse data(Object data) {
        this.put("data", data);
        return this;
    }

    /**
     * 成功200
     *
     * @return
     */
    public FebsResponse success() {
        this.code(HttpStatus.OK);
        return this;
    }

    /**
     * 失败
     *
     * @return
     */
    public FebsResponse fail() {
        this.code(HttpStatus.INTERNAL_SERVER_ERROR);
        return this;
    }

    /**
     * 键值对
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public FebsResponse put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
