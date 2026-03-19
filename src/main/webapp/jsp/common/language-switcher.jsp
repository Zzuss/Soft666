<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%
    String currentLang = I18nUtil.getLanguage(request);
    String queryString = request.getQueryString();
    String baseUrl = request.getRequestURI();

    String enUrl = baseUrl + (queryString != null ? "?" + queryString.replaceAll("lang=[^&]*&?", "") : "");
    if (enUrl.endsWith("?")) enUrl = enUrl.substring(0, enUrl.length() - 1);
    if (enUrl.contains("?lang")) {
        enUrl = enUrl.replaceAll("\\?lang=[^&]*", "");
    } else if (enUrl.contains("&lang")) {
        enUrl = enUrl.replaceAll("&lang=[^&]*", "");
    }
    enUrl = enUrl + (enUrl.contains("?") ? "&" : "?") + "lang=en";

    String zhUrl = baseUrl + (queryString != null ? "?" + queryString.replaceAll("lang=[^&]*&?", "") : "");
    if (zhUrl.endsWith("?")) zhUrl = zhUrl.substring(0, zhUrl.length() - 1);
    if (zhUrl.contains("?lang")) {
        zhUrl = zhUrl.replaceAll("\\?lang=[^&]*", "");
    } else if (zhUrl.contains("&lang")) {
        zhUrl = zhUrl.replaceAll("&lang=[^&]*", "");
    }
    zhUrl = zhUrl + (zhUrl.contains("?") ? "&" : "?") + "lang=zh";
%>
<div class="language-switcher">
    <a href="<%= enUrl %>" class="<%= "en".equals(currentLang) ? "active" : "" %>">EN</a>
    <span class="lang-divider">|</span>
    <a href="<%= zhUrl %>" class="<%= "zh".equals(currentLang) ? "active" : "" %>">中文</a>
</div>