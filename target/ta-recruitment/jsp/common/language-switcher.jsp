<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%
    String currentLang = I18nUtil.getLanguage(request);
    String forwardUri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
    if (forwardUri == null) {
        forwardUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
    }
    String baseUrl = forwardUri != null ? forwardUri : request.getRequestURI();

    String forwardQuery = (String) request.getAttribute("jakarta.servlet.forward.query_string");
    if (forwardQuery == null) {
        forwardQuery = (String) request.getAttribute("javax.servlet.forward.query_string");
    }
    String queryString = forwardQuery != null ? forwardQuery : request.getQueryString();

    String cleanedQuery = queryString;
    if (cleanedQuery != null && !cleanedQuery.isEmpty()) {
        cleanedQuery = cleanedQuery.replaceAll("(^|&)lang=[^&]*", "$1");
        cleanedQuery = cleanedQuery.replaceAll("^&+", "");
        cleanedQuery = cleanedQuery.replaceAll("&{2,}", "&");
        cleanedQuery = cleanedQuery.replaceAll("&+$", "");
    }

    String baseWithQuery = baseUrl;
    if (cleanedQuery != null && !cleanedQuery.isEmpty()) {
        baseWithQuery = baseWithQuery + "?" + cleanedQuery;
    }

    String enUrl = baseWithQuery + (baseWithQuery.contains("?") ? "&" : "?") + "lang=en";
    String zhUrl = baseWithQuery + (baseWithQuery.contains("?") ? "&" : "?") + "lang=zh";
%>
<div class="language-switcher">
    <a href="<%= response.encodeURL(enUrl) %>" class="<%= "en".equals(currentLang) ? "active" : "" %>">EN</a>
    <span class="lang-divider">|</span>
    <a href="<%= response.encodeURL(zhUrl) %>" class="<%= "zh".equals(currentLang) ? "active" : "" %>">中文</a>
</div>
