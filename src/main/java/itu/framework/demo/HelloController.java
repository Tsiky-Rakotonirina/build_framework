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

    @Web(value = "/greet", method = "GET")
    public String greet() {
        return "Greetings from GET /greet";
    }

    @Web(value = "/greet", method = "POST")
    public String greetPost() {
        return "Greetings from POST /greet";
    }

    @Web(value = "/multiply", method = "ANY")
    public String multiply() {
        int x = 4, y = 5;
        return "multiply=" + (x * y);
    }

    @Web(value = "/info")
    public String info() {
        return "Info page - method=ANY (default)";
    }
}
