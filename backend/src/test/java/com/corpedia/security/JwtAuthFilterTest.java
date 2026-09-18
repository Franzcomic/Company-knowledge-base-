package com.corpedia.security;
import com.corpedia.entity.*;
import com.corpedia.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {
    private final JwtUtil jwt = mock(JwtUtil.class);
    private final UserMapper users = mock(UserMapper.class);
    private final RoleMapper roles = mock(RoleMapper.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwt,users,roles);
    @AfterEach void clear() { UserContextHolder.clear(); SecurityContextHolder.clearContext(); }
    private MockHttpServletRequest request() {
        var r = new MockHttpServletRequest(); r.addHeader("Authorization","Bearer token"); return r;
    }
    @Test void disabledUserCannotReuseAnUnexpiredToken() throws Exception {
        when(jwt.parse("token")).thenReturn(new UserContext(1L,"admin",1L,1L,"SYS_ADMIN"));
        User u = new User(); u.setId(1L); u.setStatus(0); when(users.selectById(1L)).thenReturn(u);
        filter.doFilter(request(),new MockHttpServletResponse(), (req,res) -> {
            assertNull(UserContextHolder.get());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        });
    }
    @Test void roleAndDepartmentChangesTakeEffectOnNextRequest() throws Exception {
        when(jwt.parse("token")).thenReturn(new UserContext(1L,"admin",1L,1L,"SYS_ADMIN"));
        User u = new User(); u.setId(1L); u.setUsername("employee"); u.setStatus(1); u.setDepartmentId(2L); u.setRoleId(3L);
        Role role = new Role(); role.setCode("EMPLOYEE");
        when(users.selectById(1L)).thenReturn(u); when(roles.selectById(3L)).thenReturn(role);
        filter.doFilter(request(),new MockHttpServletResponse(), (req,res) -> {
            assertEquals("EMPLOYEE",UserContextHolder.get().roleCode());
            assertEquals(2L,UserContextHolder.get().deptId());
        });
        assertNull(UserContextHolder.get());
    }
}
