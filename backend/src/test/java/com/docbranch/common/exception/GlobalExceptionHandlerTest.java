package com.docbranch.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @Test
    void businessExceptionReturnsErrorResponse() throws Exception {
        TestService testService = mock(TestService.class);
        when(testService.call()).thenThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        MockMvc mockMvc = mockMvc(testService);

        mockMvc.perform(get("/test/error/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    void unexpectedExceptionReturnsInternalServerErrorResponse() throws Exception {
        TestService testService = mock(TestService.class);
        when(testService.call()).thenThrow(new IllegalStateException("boom"));
        MockMvc mockMvc = mockMvc(testService);

        mockMvc.perform(get("/test/error/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    private MockMvc mockMvc(TestService testService) {
        return MockMvcBuilders
                .standaloneSetup(new TestController(testService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {

        private final TestService testService;

        TestController(TestService testService) {
            this.testService = testService;
        }

        @GetMapping("/test/error/business")
        String businessError() {
            return testService.call();
        }

        @GetMapping("/test/error/unexpected")
        String unexpectedError() {
            return testService.call();
        }
    }

    interface TestService {

        String call();
    }
}
