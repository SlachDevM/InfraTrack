package com.infratrack.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final Long USER_ID = 42L;
    private static final String EMAIL = "manager@test.com";

    @Mock
    private UserAccountStatusService userAccountStatusService;

    @Mock
    private FilterChain filterChain;

    private JwtTokenProvider tokenProvider;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "MySuperSecretKeyForInfraTrackLocalDevOnlyChangeMe123456789");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3_600_000);
        tokenProvider.initializeSigningKey();

        filter = new JwtAuthenticationFilter(tokenProvider, userAccountStatusService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validToken_enabledUser_authenticatesAndContinuesChain() throws Exception {
        String token = tokenProvider.generateToken(USER_ID, EMAIL);
        when(userAccountStatusService.isEnabled(USER_ID)).thenReturn(true);

        MockHttpServletRequest request = authorizedRequest(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getUserId())
                .isEqualTo(USER_ID);
    }

    @Test
    void validToken_disabledUser_returnsUnauthorizedAndStopsChain() throws Exception {
        String token = tokenProvider.generateToken(USER_ID, EMAIL);
        when(userAccountStatusService.isEnabled(USER_ID)).thenReturn(false);

        MockHttpServletRequest request = authorizedRequest(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validToken_missingUser_returnsUnauthorizedAndStopsChain() throws Exception {
        String token = tokenProvider.generateToken(USER_ID, EMAIL);
        when(userAccountStatusService.isEnabled(USER_ID)).thenReturn(false);

        MockHttpServletRequest request = authorizedRequest(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void invalidToken_continuesChainWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = authorizedRequest("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userAccountStatusService, never()).isEnabled(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void missingToken_continuesChainWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/assets");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userAccountStatusService, never()).isEnabled(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static MockHttpServletRequest authorizedRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/assets");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
