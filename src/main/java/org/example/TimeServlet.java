package org.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time/*")
public class TimeServlet extends HttpServlet {

    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    @Override
    public void init() {
        this.application =
                JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = buildTemplateEngine(this.application);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String reqTimezone = req.getParameter("timezone");
        ZoneId timeZone;

        if (reqTimezone != null) {
            timeZone = ZoneId.of(reqTimezone);
            Cookie cookie = new Cookie("time_zone", "");
            cookie.setMaxAge(0);
            resp.addCookie(cookie);
            resp.addCookie(new Cookie("time_zone",reqTimezone));
        } else if (req.getCookies() != null) {
            Cookie[] cookies = req.getCookies();
            timeZone = ZoneId.of(cookies[0].getValue());
        } else {
            timeZone = ZoneId.of("UTC");
        }

        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, timeZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedTime = zdt.format(formatter);
        resp.setContentType("text/html; charset=utf-8");

        final IWebExchange webExchange = this.application.buildExchange(req, resp);
        WebContext webContext = new WebContext(webExchange);
        webContext.setVariable("time", formattedTime);
        templateEngine.process("time",webContext, resp.getWriter());    }

    private static ITemplateEngine buildTemplateEngine(final IWebApplication application) {

        final WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");

        templateResolver.setCacheable(false);

        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }
}
