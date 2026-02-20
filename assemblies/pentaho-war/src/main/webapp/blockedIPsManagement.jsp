<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.pentaho.platform.api.security.ILoginAttemptService" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Blocked IPs Management</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f4f4f4; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .section { margin-bottom: 30px; }
        .action-form { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 10px 0; }
        .button { padding: 8px 16px; margin: 5px; background-color: #007cba; color: white; border: none; border-radius: 3px; cursor: pointer; }
        .button.danger { background-color: #d32f2f; }
        .button.success { background-color: #388e3c; }
        .table { width: 100%; border-collapse: collapse; }
        .table th, .table td { padding: 10px; border: 1px solid #ddd; text-align: left; }
        .table th { background-color: #f4f4f4; }
        .input-field { padding: 5px; margin: 5px; border: 1px solid #ccc; border-radius: 3px; }
        .message { padding: 10px; margin: 10px 0; border-radius: 3px; }
        .message.success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .message.info { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Admin - Blocked IPs Management</h1>
        <p>Manage login attempt tracking and blocked IP addresses</p>
    </div>

    <%
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
        ILoginAttemptService loginAttemptService = null;
        String message = "";
        String messageType = "";

        try {
            loginAttemptService = (ILoginAttemptService) context.getBean("loginAttemptService");
        } catch (Exception e) {
            message = "Error: Unable to access Login Attempt Service - " + e.getMessage();
            messageType = "error";
        }

        // Handle form submissions
        String action = request.getParameter("action");
        if (loginAttemptService != null && action != null) {
            try {
                if ("remove".equals(action)) {
                    String keyToRemove = request.getParameter("key");
                    if (keyToRemove != null && !keyToRemove.trim().isEmpty()) {
                        loginAttemptService.removeFromCache(keyToRemove);
                        message = "Successfully removed key: " + keyToRemove;
                        messageType = "success";
                    } else {
                        message = "Error: Key cannot be empty";
                        messageType = "error";
                    }
                } else if ("clear".equals(action)) {
                    loginAttemptService.clearCache();
                    message = "Successfully cleared all entries from cache";
                    messageType = "success";
                }
            } catch (Exception e) {
                message = "Error processing request: " + e.getMessage();
                messageType = "error";
            }
        }
    %>

    <% if (!message.isEmpty()) { %>
        <div class="message <%= messageType %>"><%= message %></div>
    <% } %>

    <% if (loginAttemptService != null) { %>
        <div class="section">
            <h2>Service Information</h2>
            <div class="message info">
                <strong>Maximum Allowed Attempts:</strong> <%= loginAttemptService.getMaxAttempt() %><br>
                <strong>Total Cached Entries:</strong> <%= loginAttemptService.getAllAttempts().size() %>
            </div>
        </div>

        <div class="section">
            <h2>Current Login Attempts Cache</h2>
            <%
                Map<String, Integer> attempts = loginAttemptService.getAllAttempts();
                if (attempts.isEmpty()) {
            %>
                <div class="message info">No entries currently in the cache</div>
            <%
                } else {
            %>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Key (IP/Username)</th>
                            <th>Failed Attempts</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Map.Entry<String, Integer> entry : attempts.entrySet()) {
                                String key = entry.getKey();
                                Integer attemptCount = entry.getValue();
                                boolean isBlocked = loginAttemptService.isBlocked(key);
                        %>
                        <tr>
                            <td><%= key %></td>
                            <td><%= attemptCount %></td>
                            <td <% if (isBlocked) { %>style="color: red; font-weight: bold;"<% } else { %>style="color: green;"<% } %>>
                                <%= isBlocked ? "BLOCKED" : "ALLOWED" %>
                            </td>
                            <td>
                                <form method="post" style="display: inline;">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="key" value="<%= key %>">
                                    <button type="submit" class="button danger" 
                                            onclick="return confirm('Are you sure you want to remove this entry?')">
                                        Remove
                                    </button>
                                </form>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            <%
                }
            %>
        </div>

        <div class="section">
            <h2>Administrative Actions</h2>
            
            <div class="action-form">
                <h3>Clear All Entries</h3>
                <form method="post" onsubmit="return confirm('Are you sure you want to clear ALL entries? This action cannot be undone.')">
                    <input type="hidden" name="action" value="clear">
                    <button type="submit" class="button danger">Clear All Cache</button>
                </form>
                <p style="font-size: 12px; color: #666;">
                    This will remove all entries from the login attempts cache.
                </p>
            </div>

            <div class="action-form">
                <h3>Refresh Page</h3>
                <button onclick="location.reload()" class="button">Refresh</button>
                <p style="font-size: 12px; color: #666;">
                    Refresh to see the latest cache state.
                </p>
            </div>
        </div>

    <% } else { %>
        <div class="message error">
            <h2>Service Unavailable</h2>
            <p>The Login Attempt Service is not available. Please check:</p>
            <ul>
                <li>The loginAttemptService bean is properly configured in Spring context</li>
                <li>The application context is properly initialized</li>
                <li>Check the server logs for any configuration errors</li>
            </ul>
        </div>
    <% } %>

    <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #ccc; color: #666; font-size: 12px;">
        <p><strong>Usage Notes:</strong></p>
        <ul>
            <li>Keys typically represent IP addresses or usernames being tracked</li>
            <li>Entries are automatically removed after the configured cache timeout</li>
            <li>Blocked status is determined by comparing attempts to the maximum allowed (<%= loginAttemptService != null ? loginAttemptService.getMaxAttempt() : "N/A" %>)</li>
            <li>Successful logins automatically clear the corresponding cache entry</li>
        </ul>
    </div>
</body>
</html>