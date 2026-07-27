package io.github.khghouse.petwellness.support;

import io.github.khghouse.petwellness.global.config.JpaAuditingConfig;
import io.github.khghouse.petwellness.global.config.QuerydslConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public abstract class RepositoryTestSupport {}
