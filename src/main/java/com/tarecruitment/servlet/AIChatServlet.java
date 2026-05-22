package com.tarecruitment.servlet;

import com.tarecruitment.model.User;
import com.tarecruitment.service.AIChatService;
import com.tarecruitment.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/ai/chat")
public class AIChatServlet extends HttpServlet {
    private static final String SESSION_MESSAGES_KEY = "aiChatMessages";
    private static final String SESSION_MODEL_KEY = "aiChatModel";

    private AuthService authService;
    private AIChatService aiChatService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
        this.aiChatService = new AIChatService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        bindChatAttributes(request, session);
        request.getRequestDispatcher("/jsp/ai/chat.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String action = request.getParameter("action");
        if ("stream".equalsIgnoreCase(action)) {
            streamChatResponse(request, response, session);
            return;
        }
        if ("clear".equalsIgnoreCase(action)) {
            session.removeAttribute(SESSION_MESSAGES_KEY);
            response.sendRedirect(request.getContextPath() + "/ai/chat");
            return;
        }

        User user = (User) session.getAttribute("user");
        String model = aiChatService.normalizeModel(request.getParameter("model"));
        session.setAttribute(SESSION_MODEL_KEY, model);
        List<AIChatService.ChatMessage> messages = getMessages(session);

        try {
            String question = request.getParameter("message");
            AIChatService.ChatMessage userMessage = new AIChatService.ChatMessage("user", question, model);
            AIChatService.ChatMessage assistantMessage = aiChatService.ask(user, messages, question, model);
            messages.add(userMessage);
            messages.add(assistantMessage);
            session.setAttribute(SESSION_MESSAGES_KEY, messages);
            response.sendRedirect(request.getContextPath() + "/ai/chat");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage() != null ? e.getMessage() : "AI request failed");
            bindChatAttributes(request, session);
            request.getRequestDispatcher("/jsp/ai/chat.jsp").forward(request, response);
        }
    }

    private void streamChatResponse(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        User user = (User) session.getAttribute("user");
        String model = aiChatService.normalizeModel(request.getParameter("model"));
        String question = request.getParameter("message");
        List<AIChatService.ChatMessage> history = new ArrayList<>(getMessages(session));

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        PrintWriter writer = response.getWriter();

        try {
            session.setAttribute(SESSION_MODEL_KEY, model);
            String answer = aiChatService.streamAnswer(user, history, question, model, chunk -> {
                writer.print("data: " + new JSONObject().put("delta", chunk).toString() + "\n\n");
                writer.flush();
            });

            synchronized (session) {
                List<AIChatService.ChatMessage> messages = getMessages(session);
                messages.add(new AIChatService.ChatMessage("user", question, model));
                messages.add(new AIChatService.ChatMessage("assistant", answer, model));
                session.setAttribute(SESSION_MESSAGES_KEY, messages);
            }
            writer.print("event: done\n");
            writer.print("data: " + new JSONObject().put("ok", true).toString() + "\n\n");
            writer.flush();
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "AI request failed";
            writer.print("event: error\n");
            writer.print("data: " + new JSONObject().put("message", message).toString() + "\n\n");
            writer.flush();
        }
    }

    @SuppressWarnings("unchecked")
    private List<AIChatService.ChatMessage> getMessages(HttpSession session) {
        Object value = session.getAttribute(SESSION_MESSAGES_KEY);
        if (value instanceof List<?>) {
            return (List<AIChatService.ChatMessage>) value;
        }
        return new ArrayList<>();
    }

    private void bindChatAttributes(HttpServletRequest request, HttpSession session) {
        String selectedModel = (String) session.getAttribute(SESSION_MODEL_KEY);
        request.setAttribute("messages", getMessages(session));
        request.setAttribute("models", aiChatService.getSupportedModels());
        request.setAttribute("selectedModel", aiChatService.normalizeModel(selectedModel));
        request.setAttribute("aiEnabled", aiChatService.isEnabled());
    }
}
