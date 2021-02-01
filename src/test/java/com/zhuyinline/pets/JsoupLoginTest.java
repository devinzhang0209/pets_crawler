package com.zhuyinline.pets;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.beans.Encoder;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsoupLoginTest {
    public static void main(String[] args) throws IOException {

        System.out.println(URLEncoder.encode(URLEncoder.encode("宠物零食")));
        System.out.println(URLEncoder.encode("宠物零食", "utf-8").toUpperCase());

        Connection con = Jsoup.connect("https://login.taobao.com/?spm=a1z5k.7633538.0.0.249b4c38Sp3fvr&redirectURL=https%3A%2F%2Fpassport.taobao.com%2Fac%2Fpassword_reset_success.htm%3Flang%3Dzh_CN%26bizcode%3D%26fromSite%3D0%26appName%3Dtmall");
        Connection.Response rs = con.execute();
        System.out.println(rs.cookies());
        Document d1 = Jsoup.parse(rs.body());
        List<Element> et = d1.select("form[id='login-form']");
        Map<String, String> datas = new HashMap<>();
        for (Element e : et.get(0).getAllElements()) {
            if (e.attr("name").equals("fm-login-id")) {
                e.attr("value", "15094008616");// 设置用户名
            }
            if (e.attr("name").equals("fm-login-password")) {
                e.attr("value", "goodluck@123"); // 设置用户密码
            }
            if (e.attr("name").length() > 0) {// 排除空值表单属性
                datas.put(e.attr("name"), e.attr("value"));
            }
        }
        Connection con2 = Jsoup.connect("https://login.taobao.com/newlogin/login.do");
        Connection.Response login = con2.ignoreContentType(true).method(Connection.Method.POST).data(datas).cookies(rs.cookies()).execute();
        System.out.println(login.body());
        Map<String, String> map = login.cookies();
        for (String s : map.keySet()) {
            System.out.println(s + "      " + map.get(s));
        }
    }
}
