package com.sky.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.filter.JwtAuthenticationFilter;
import com.sky.mapper.EmployeeMapper;
import com.sky.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.time.*;
import java.util.Base64;
import jakarta.servlet.ServletException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtUtil jwt=new JwtUtil(Base64.getEncoder().encodeToString(new byte[32]),Duration.ofHours(2),Clock.systemUTC());
    private final EmployeeMapper employees=mock(EmployeeMapper.class);
    private final JwtAuthenticationFilter filter=new JwtAuthenticationFilter(jwt,employees,new ObjectMapper(),true);
    @AfterEach void clear() { BaseContext.clear(); }

    private MockHttpServletRequest authenticated() {
        MockHttpServletRequest request=new MockHttpServletRequest("GET","/admin/employee/page");
        request.addHeader("Authorization","Bearer "+jwt.createToken(1L));
        Employee employee=new Employee();employee.setId(1L);employee.setStatus(1);when(employees.findById(1L)).thenReturn(employee);
        return request;
    }
    @Test void identityIsAvailableOnlyInsideRequest() throws Exception {
        filter.doFilter(authenticated(),new MockHttpServletResponse(),(request,response)->assertEquals(1L,BaseContext.getCurrentId()));
        assertNull(BaseContext.getCurrentId());
    }
    @Test void exceptionalRequestAlwaysClearsIdentity() {
        var request=authenticated();
        assertThrows(ServletException.class, () -> filter.doFilter(request,new MockHttpServletResponse(),(req,res)->{throw new ServletException("failure");}));
        assertNull(BaseContext.getCurrentId());
    }
    @Test void rejectedRequestCannotReusePreviousIdentity() throws Exception {
        BaseContext.setCurrentId(999L);
        MockHttpServletResponse response=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET","/admin/category/page"),response,(req,res)->fail("Must reject"));
        assertEquals(401,response.getStatus());assertNull(BaseContext.getCurrentId());
    }
    @Test void badTokenAndDeletedAccountAreRejected() throws Exception {
        MockHttpServletRequest bad=new MockHttpServletRequest("GET","/admin/employee/page");bad.addHeader("Authorization","Bearer malformed");
        MockHttpServletResponse response=new MockHttpServletResponse();filter.doFilter(bad,response,(req,res)->fail());assertEquals(401,response.getStatus());
        MockHttpServletRequest deleted=authenticated();when(employees.findById(1L)).thenReturn(null);
        response=new MockHttpServletResponse();filter.doFilter(deleted,response,(req,res)->fail());assertEquals(401,response.getStatus());
    }
    @Test void loginWhiteListIsExactAndMethodRestricted() throws Exception {
        var response=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET","/admin/employee/login"),response,(req,res)->fail());assertEquals(401,response.getStatus());
        response=new MockHttpServletResponse();filter.doFilter(new MockHttpServletRequest("POST","/admin/employee/login/extra"),response,(req,res)->fail());assertEquals(401,response.getStatus());
    }
}
