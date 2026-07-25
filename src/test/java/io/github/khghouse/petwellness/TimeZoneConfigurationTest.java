package io.github.khghouse.petwellness;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeZoneConfigurationTest {

    private TimeZone originalTimeZone;

    @BeforeEach
    void setUp() {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(originalTimeZone);
    }

    @DisplayName("애플리케이션 시작 시 기본 시간대를 한국 시간대로 설정한다")
    @Test
    void configureDefaultTimeZone_setsAsiaSeoul() {
        PetWellnessApplication.configureDefaultTimeZone();

        assertThat(ZoneId.systemDefault()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
