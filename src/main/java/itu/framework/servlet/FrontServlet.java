package itu.framework.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)

public class FrontServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter w = resp.getWriter()) {
            w.println("<html><head><meta charset='UTF-8'><title>FrontServlet</title></head><body>");
            w.println("<h2>FrontServlet</h2>");
            w.println("<p>Requested URL: <strong>" + req.getRequestURL().toString() + "</strong></p>");
            w.println("</body></html>");
        }
    }
}
