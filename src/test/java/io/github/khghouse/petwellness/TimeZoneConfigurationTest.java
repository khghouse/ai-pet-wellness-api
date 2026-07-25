package io.github.khghouse.petwellness;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeZoneConfigurationTest {

    @DisplayName("테스트 JVM의 기본 시간대는 한국 시간대이다")
    @Test
    void systemDefaultTimeZone_isAsiaSeoul() {
        assertThat(ZoneId.systemDefault()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
