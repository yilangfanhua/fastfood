package com.sky.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.exception.BusinessException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.Result;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;
    private final EmployeeMapper employees;
    private final ObjectMapper json;
    private final boolean docsEnabled;
    public JwtAuthenticationFilter(JwtUtil jwt, EmployeeMapper employees, ObjectMapper json,
                                   @Value("${springdoc.api-docs.enabled:true}") boolean docsEnabled) {
        this.jwt = jwt; this.employees = employees; this.json = json; this.docsEnabled = docsEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BaseContext.clear();
        try {
            if (!anonymous(request)) {
                try {
                    String header = request.getHeader("Authorization");
                    if (header == null || !header.startsWith("Bearer ")) throw new BusinessException(401, "请先登录");
                    Long id = jwt.employeeId(header.substring(7));
                    Employee employee = employees.findById(id);
                    if (employee == null) throw new BusinessException(401, "账户不存在");
                    if (employee.getStatus() != 1) throw new BusinessException(403, "账户已禁用");
                    BaseContext.setCurrentId(id);
                } catch (JwtException | IllegalArgumentException e) {
                    reject(response, 401, "登录凭证无效或已过期"); return;
                } catch (BusinessException e) {
                    reject(response, e.getStatus(), e.getMessage()); return;
                } catch (org.springframework.dao.DataAccessException e) {
                    reject(response, 500, "认证服务暂不可用"); return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            BaseContext.clear();
        }
    }

    private boolean anonymous(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("POST".equals(request.getMethod()) && "/admin/employee/login".equals(path)) return true;
        return docsEnabled && "GET".equals(request.getMethod()) &&
                (path.equals("/doc.html") || path.equals("/v3/api-docs") || path.startsWith("/v3/api-docs/")
                        || path.startsWith("/swagger-ui/") || path.startsWith("/webjars/"));
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getOutputStream(), Result.error(message));
    }
}
