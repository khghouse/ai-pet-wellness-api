package io.github.khghouse.petwellness.support;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import io.github.khghouse.common.web.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected abstract Object initController();

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        RestDocumentationResultHandler document =
                org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document(
                        "{class-name}/{method-name}");

        this.mockMvc =
                MockMvcBuilders.standaloneSetup(initController())
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .addFilters(new CharacterEncodingFilter("UTF-8", true))
                        .apply(documentationConfiguration(restDocumentation))
                        .alwaysDo(document)
                        .build();
    }
}
