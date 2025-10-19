package itu.framework.demo;

import itu.framework.annotation.Web;

public class HelloController {
    @Web("/hello")
    public String hello() {
        return "Hello from @Web /hello";
    }

    @Web(value = "/sum", method = "GET")
    public String sum() {
        int a = 2, b = 3;
        return "sum=" + (a + b);
    }
}
