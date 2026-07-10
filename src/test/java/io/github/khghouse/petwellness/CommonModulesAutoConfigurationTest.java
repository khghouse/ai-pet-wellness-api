package io.github.khghouse.petwellness;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class CommonModulesAutoConfigurationTest {

    @Autowired private ApplicationContext applicationContext;

    @DisplayName("공통 웹과 로깅, 인증 자동 설정 Bean을 등록한다")
    @Test
    void applicationContext_commonModules_registersAutoConfigurationBeans() {
        assertThat(applicationContext.containsBean("globalExceptionHandler")).isTrue();
        assertThat(applicationContext.containsBean("mdcLoggingFilter")).isTrue();
        assertThat(applicationContext.containsBean("accessLoggingFilter")).isTrue();
        assertThat(applicationContext.containsBean("loggingFilter")).isTrue();
        assertThat(applicationContext.containsBean("authController")).isTrue();
        assertThat(applicationContext.containsBean("authService")).isTrue();
        assertThat(applicationContext.containsBean("jwtTokenProvider")).isTrue();
        assertThat(applicationContext.containsBean("jwtAuthenticationFilter")).isTrue();
        assertThat(applicationContext.containsBean("authSecurityFilterChain")).isTrue();
    }
}
