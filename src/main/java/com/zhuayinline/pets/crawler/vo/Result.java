package com.zhuayinline.pets.crawler.vo;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;

import java.util.List;


/**
 * @author devin
 * @description 前端返回结果集合
 * @date 2019-9-25 13:54:22
 */
public class Result {
    /**
     * 调用成功
     */
    public static final int SUCCESS = 200;

    /**
     * 调用失败
     */
    private static final int FAILURE = 500;

    /**
     * token不正确
     */
    private static final int FAILURE_UNTOOKEN = 301;

    /**
     * 没有权限
     */
    private static final int FAILURE_UNAUTH = 302;

    /**
     * 成功的verify code
     */
    private static final int VERIFY_SUCC_CODE = 0;

    /**
     * 失败的verify code
     */
    private static final int VERIFY_FAIL_CODE = 1;
    /**
     * 操作成功
     */
    private static final String SUCCESS_MSG = "操作成功";

    /**
     * 操作失败
     */
    private static final String FAILURE_MSG = "操作失败";


    /**
     * @param state  state
     * @param code code
     * @param msg msg
     * @param data data
     * @return String
     */
    private static String result(int state, String stateMsg, int code, String msg, Object data) {
        JSONObject result = new JSONObject();
        result.put("state", state);
        result.put("msg", stateMsg);

        JSONObject obj = new JSONObject();
        obj.put("code", code);
        obj.put("msg", msg);
        obj.put("obj", data == null ? new JSONObject() : JSONObject.toJSON(data));

        result.put("data", obj);
        String resultString =null;
        try{
            resultString=result.toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        return resultString;
    }

    /**
     * 成功时接口数据模型
     *
     * @param data data
     * @return 成功时返回的数据
     */
    public static String succResult(Object data) {
        return result(SUCCESS, SUCCESS_MSG, VERIFY_SUCC_CODE, SUCCESS_MSG, data);
    }
    /**
     * 成功时接口分页数据模型
     *
     * @param pageDate pageDate
     * @return 成功时返回的数据
     */
    public static String succPageResult(List pageDate) {
        PageInfo pageInfo=new PageInfo(pageDate);
        return result(SUCCESS, SUCCESS_MSG, VERIFY_SUCC_CODE, SUCCESS_MSG, pageInfo);
    }

    /**
     * 校验失败时返回的数据模型
     *
     * @param msg msg
     * @return String
     */
    public static String failResult(String msg) {
        return result(SUCCESS, SUCCESS_MSG, VERIFY_FAIL_CODE, msg, null);
    }

    /**
     * 异常时返回的数据模型
     *
     * @param e e
     * @return String
     */
    public static String exceptionResult(Exception e) {
        return result(FAILURE, FAILURE_MSG, VERIFY_FAIL_CODE, e.getMessage(), null);
    }

    /**
     * token不正确时返回的模型
     *
     * @return String
     */
    public static String tokenResult() {
        return result(FAILURE_UNTOOKEN, SUCCESS_MSG, VERIFY_FAIL_CODE, "token不正确", null);
    }

    /**
     * 没有权限时返回的模型
     *
     * @return String
     */
    public static String authResult() {
        return result(FAILURE_UNAUTH, SUCCESS_MSG, VERIFY_FAIL_CODE, "登录已失效或者没有权限，请退出重新登录!", null);
    }


}
