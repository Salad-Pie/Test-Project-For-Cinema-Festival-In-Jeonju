package com.example.springboot.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springboot.controller.ExhibitionSurveyController;
import com.example.springboot.dto.ExhibitionSurveyRequest;
import com.example.springboot.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ExhibitionSurveyIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ExhibitionSurveyService exhibitionSurveyService;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ExhibitionSurveyController controller = new ExhibitionSurveyController(exhibitionSurveyService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    @DisplayName("전시 설문 생성 API 통합 테스트 - 성공")
    void createSurvey_Success() throws Exception {
        // given
        ExhibitionSurveyRequest request = new ExhibitionSurveyRequest(
                "Integration User", "010-9999-8888", "Seoul", "123456",
                "Point", "Improvement", "Genre", "Artist", "Feedback"
        );

        // when & then
        mockMvc.perform(post("/api/exhibition-surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("전시 설문 생성 API 통합 테스트 - 유효성 검사 실패 (이름 누락)")
    void createSurvey_ValidationFail() throws Exception {
        // given
        ExhibitionSurveyRequest request = new ExhibitionSurveyRequest(
                "", "010-9999-8888", "Seoul", null,
                "Point", "Improvement", "Genre", "Artist", "Feedback"
        );

        // when & then
        mockMvc.perform(post("/api/exhibition-surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
