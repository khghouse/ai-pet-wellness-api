package io.github.khghouse.petwellness.support;

import io.github.khghouse.common.web.global.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public abstract class ControllerTestSupport {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;
}
