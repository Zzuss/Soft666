<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.service.AIChatService" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    String lang = I18nUtil.getLanguage(request);
    List<AIChatService.ChatMessage> messages =
            (List<AIChatService.ChatMessage>) request.getAttribute("messages");
    if (messages == null) {
        messages = new ArrayList<>();
    }
    List<String> models = (List<String>) request.getAttribute("models");
    if (models == null) {
        models = new ArrayList<>();
    }
    String selectedModel = (String) request.getAttribute("selectedModel");
    Boolean aiEnabledObj = (Boolean) request.getAttribute("aiEnabled");
    boolean aiEnabled = Boolean.TRUE.equals(aiEnabledObj);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("ai.chat.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <a href="${pageContext.request.contextPath}/ai/chat"><%= I18nUtil.get("nav.askAI", lang) %></a>
                <% if (user.isTA()) { %>
                    <a href="${pageContext.request.contextPath}/applications/my"><%= I18nUtil.get("nav.myApplications", lang) %></a>
                    <a href="${pageContext.request.contextPath}/profile"><%= I18nUtil.get("nav.profile", lang) %></a>
                <% } else if (user.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/workload"><%= I18nUtil.get("nav.workload", lang) %></a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <% } %>
                <span class="navbar-user"><%= escapeHtml(user.getName()) %> (<%= escapeHtml(user.getRole()) %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("ai.chat.title", lang) %></h2>
        <jsp:include page="/jsp/common/system-warning.jsp" />

        <% if (!aiEnabled) { %>
            <div class="alert alert-error"><%= I18nUtil.get("ai.chat.notConfigured", lang) %></div>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error" id="chatError"><%= escapeHtml(String.valueOf(request.getAttribute("error"))) %></div>
        <% } else { %>
            <div class="alert alert-error" id="chatError" style="display: none;"></div>
        <% } %>

        <div class="card ai-chat-card">
            <div class="ai-chat-toolbar">
                <div>
                    <h3><%= I18nUtil.get("ai.chat.conversation", lang) %></h3>
                    <p class="muted"><%= I18nUtil.get("ai.chat.hint", lang) %></p>
                </div>
                <form action="${pageContext.request.contextPath}/ai/chat" method="post">
                    <input type="hidden" name="action" value="clear">
                    <button type="submit" class="btn btn-secondary"><%= I18nUtil.get("ai.chat.clear", lang) %></button>
                </form>
            </div>

            <div class="ai-chat-messages" id="chatMessages">
                <% if (messages.isEmpty()) { %>
                    <div class="ai-empty-state" id="emptyState">
                        <p><%= I18nUtil.get("ai.chat.empty", lang) %></p>
                    </div>
                <% } %>
                <% for (AIChatService.ChatMessage message : messages) {
                    boolean assistant = "assistant".equalsIgnoreCase(message.getRole());
                %>
                    <div class="ai-message <%= assistant ? "ai-message-assistant" : "ai-message-user" %>">
                        <div class="ai-message-meta">
                            <%= assistant ? I18nUtil.get("ai.chat.assistant", lang) : I18nUtil.get("ai.chat.you", lang) %>
                            <% if (assistant && message.getModel() != null && !message.getModel().isEmpty()) { %>
                                · <%= escapeHtml(message.getModel()) %>
                            <% } %>
                        </div>
                        <div class="ai-message-content markdown-content" data-markdown="<%= escapeHtml(message.getContent()) %>"></div>
                    </div>
                <% } %>
            </div>

            <form action="${pageContext.request.contextPath}/ai/chat" method="post" class="ai-chat-form" id="chatForm">
                <div class="form-group">
                    <label for="model"><%= I18nUtil.get("ai.chat.model", lang) %></label>
                    <select id="model" name="model">
                        <% for (String model : models) { %>
                            <option value="<%= escapeHtml(model) %>" <%= model.equals(selectedModel) ? "selected" : "" %>><%= escapeHtml(model) %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label for="message"><%= I18nUtil.get("ai.chat.message", lang) %></label>
                    <textarea id="message" name="message" rows="4" maxlength="2000" required
                              placeholder="<%= I18nUtil.get("ai.chat.placeholder", lang) %>"></textarea>
                </div>
                <button type="submit" class="btn btn-primary" id="sendButton" <%= aiEnabled ? "" : "disabled" %>><%= I18nUtil.get("ai.chat.send", lang) %></button>
            </form>
        </div>
    </div>
    <script>
        (function () {
            const chatForm = document.getElementById('chatForm');
            const messagesEl = document.getElementById('chatMessages');
            const emptyState = document.getElementById('emptyState');
            const messageInput = document.getElementById('message');
            const modelSelect = document.getElementById('model');
            const sendButton = document.getElementById('sendButton');
            const errorEl = document.getElementById('chatError');
            const endpoint = '<%= request.getContextPath() %>/ai/chat';
            let sending = false;

            function escapeHtml(value) {
                return value
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#39;');
            }

            function renderInline(value) {
                let html = escapeHtml(value);
                html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
                html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
                html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
                return html;
            }

            function renderMarkdown(markdown) {
                const lines = markdown.replace(/\r\n/g, '\n').split('\n');
                const blocks = [];
                let paragraph = [];
                let listItems = [];
                let orderedItems = [];
                let inCode = false;
                let codeLines = [];

                function flushParagraph() {
                    if (paragraph.length > 0) {
                        blocks.push('<p>' + renderInline(paragraph.join(' ')) + '</p>');
                        paragraph = [];
                    }
                }

                function flushList() {
                    if (listItems.length > 0) {
                        blocks.push('<ul>' + listItems.map(item => '<li>' + renderInline(item) + '</li>').join('') + '</ul>');
                        listItems = [];
                    }
                    if (orderedItems.length > 0) {
                        blocks.push('<ol>' + orderedItems.map(item => '<li>' + renderInline(item) + '</li>').join('') + '</ol>');
                        orderedItems = [];
                    }
                }

                for (const rawLine of lines) {
                    const line = rawLine.trimEnd();
                    const trimmed = line.trim();

                    if (trimmed.startsWith('```')) {
                        flushParagraph();
                        flushList();
                        if (inCode) {
                            blocks.push('<pre><code>' + escapeHtml(codeLines.join('\n')) + '</code></pre>');
                            codeLines = [];
                            inCode = false;
                        } else {
                            inCode = true;
                        }
                        continue;
                    }

                    if (inCode) {
                        codeLines.push(line);
                        continue;
                    }

                    if (trimmed === '') {
                        flushParagraph();
                        flushList();
                        continue;
                    }

                    const headingMatch = trimmed.match(new RegExp('^(#' + '{1,4})\\s+(.+)$'));
                    if (headingMatch) {
                        flushParagraph();
                        flushList();
                        const level = headingMatch[1].length;
                        blocks.push('<h' + level + '>' + renderInline(headingMatch[2]) + '</h' + level + '>');
                        continue;
                    }

                    const orderedMatch = trimmed.match(/^\d+\.\s+(.+)$/);
                    if (orderedMatch) {
                        flushParagraph();
                        listItems = [];
                        orderedItems.push(orderedMatch[1]);
                        continue;
                    }

                    const listMatch = trimmed.match(/^[-*]\s+(.+)$/);
                    if (listMatch) {
                        flushParagraph();
                        orderedItems = [];
                        listItems.push(listMatch[1]);
                        continue;
                    }

                    paragraph.push(trimmed);
                }

                flushParagraph();
                flushList();
                if (inCode && codeLines.length > 0) {
                    blocks.push('<pre><code>' + escapeHtml(codeLines.join('\n')) + '</code></pre>');
                }
                return blocks.join('');
            }

            function renderAllMarkdown() {
                document.querySelectorAll('.markdown-content').forEach(el => {
                    el.innerHTML = renderMarkdown(el.dataset.markdown || '');
                });
            }

            function appendMessage(role, label, model, content) {
                if (emptyState) {
                    emptyState.remove();
                }
                const wrapper = document.createElement('div');
                wrapper.className = 'ai-message ' + (role === 'assistant' ? 'ai-message-assistant' : 'ai-message-user');
                const meta = document.createElement('div');
                meta.className = 'ai-message-meta';
                meta.textContent = label + (model ? ' · ' + model : '');
                const body = document.createElement('div');
                body.className = 'ai-message-content markdown-content';
                body.dataset.markdown = content || '';
                body.innerHTML = renderMarkdown(content || '');
                wrapper.appendChild(meta);
                wrapper.appendChild(body);
                messagesEl.appendChild(wrapper);
                messagesEl.scrollTop = messagesEl.scrollHeight;
                return body;
            }

            function showError(message) {
                errorEl.textContent = message;
                errorEl.style.display = 'block';
            }

            function clearError() {
                errorEl.textContent = '';
                errorEl.style.display = 'none';
            }

            async function sendMessage() {
                if (sending) {
                    return;
                }
                const message = messageInput.value.trim();
                if (!message) {
                    return;
                }

                sending = true;
                sendButton.disabled = true;
                modelSelect.disabled = true;
                clearError();

                const model = modelSelect.value;
                appendMessage('user', '<%= I18nUtil.get("ai.chat.you", lang) %>', '', message);
                const assistantBody = appendMessage('assistant', '<%= I18nUtil.get("ai.chat.assistant", lang) %>', model, '');
                messageInput.value = '';

                try {
                    const formData = new URLSearchParams();
                    formData.set('action', 'stream');
                    formData.set('model', model);
                    formData.set('message', message);

                    const response = await fetch(endpoint, {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'},
                        body: formData.toString()
                    });

                    if (!response.ok || !response.body) {
                        throw new Error('Request failed: HTTP ' + response.status);
                    }

                    const reader = response.body.getReader();
                    const decoder = new TextDecoder('utf-8');
                    let buffer = '';
                    let markdown = '';

                    while (true) {
                        const result = await reader.read();
                        if (result.done) {
                            break;
                        }
                        buffer += decoder.decode(result.value, {stream: true});
                        const events = buffer.split('\n\n');
                        buffer = events.pop() || '';

                        for (const eventText of events) {
                            const eventName = eventText.includes('event: error') ? 'error'
                                : (eventText.includes('event: done') ? 'done' : 'message');
                            const dataLine = eventText.split('\n').find(line => line.startsWith('data: '));
                            if (!dataLine) {
                                continue;
                            }
                            const payload = JSON.parse(dataLine.substring(6));
                            if (eventName === 'error') {
                                throw new Error(payload.message || 'AI request failed');
                            }
                            if (payload.delta) {
                                markdown += payload.delta;
                                assistantBody.dataset.markdown = markdown;
                                assistantBody.innerHTML = renderMarkdown(markdown);
                                messagesEl.scrollTop = messagesEl.scrollHeight;
                            }
                        }
                    }
                } catch (error) {
                    showError(error.message || 'AI request failed');
                } finally {
                    sending = false;
                    sendButton.disabled = false;
                    modelSelect.disabled = false;
                    messageInput.focus();
                }
            }

            chatForm.addEventListener('submit', event => {
                event.preventDefault();
                sendMessage();
            });

            messageInput.addEventListener('keydown', event => {
                if (event.isComposing) {
                    return;
                }
                if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    sendMessage();
                }
            });

            renderAllMarkdown();
            messageInput.focus();
        })();
    </script>
</body>
</html>
