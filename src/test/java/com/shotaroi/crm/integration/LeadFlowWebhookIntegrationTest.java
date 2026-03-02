package com.shotaroi.crm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.crm.api.auth.AuthResponse;
import com.shotaroi.crm.api.auth.LoginRequest;
import com.shotaroi.crm.api.auth.RegisterRequest;
import com.shotaroi.crm.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeadFlowWebhookIntegrationTest {

    private static final UUID DEMO_TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        var registerResult = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest(
                        DEMO_TENANT_ID,
                        "admin@test.com",
                        "password123",
                        UserRole.ADMIN
                ))));
        if (registerResult.andReturn().getResponse().getStatus() == 409) {
            // Already registered
        }
        MvcResult loginResult = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(
                        DEMO_TENANT_ID,
                        "admin@test.com",
                        "password123"
                )))).andExpect(status().isOk()).andReturn();
        AuthResponse auth = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class);
        adminToken = auth.token();
    }

    @Test
    void createLeadWithIdempotencyKey_returnsSameLeadOnDuplicate() throws Exception {
        String idempotencyKey = "lead-create-" + UUID.randomUUID();
        String leadJson = """
                {"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"+1-555-123-4567","source":"website"}
                """;

        MvcResult first = mvc.perform(post("/api/leads")
                .header("Authorization", "Bearer " + adminToken)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(leadJson))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult second = mvc.perform(post("/api/leads")
                .header("Authorization", "Bearer " + adminToken)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(leadJson))
                .andExpect(status().isCreated())
                .andReturn();

        String id1 = objectMapper.readTree(first.getResponse().getContentAsString()).get("id").asText();
        String id2 = objectMapper.readTree(second.getResponse().getContentAsString()).get("id").asText();
        assertThat(id1).isEqualTo(id2);

        var lead = objectMapper.readTree(first.getResponse().getContentAsString());
        assertThat(lead.get("email").asText()).isEqualTo("john@example.com");
        assertThat(lead.get("source").asText()).isEqualTo("WEBSITE");
        assertThat(lead.get("ownerUserId")).isNotNull();
    }

    @Test
    void createFlow_setScoreAndTask_onLeadCreated() throws Exception {
        String flowJson = objectMapper.writeValueAsString(Map.of(
                "eventType", "LEAD.CREATED",
                "name", "Score and Task Flow",
                "jsonDefinition", Map.of(
                        "conditions", List.of(Map.of("field", "source", "operator", "equals", "value", "WEBSITE")),
                        "actions", List.of(
                                Map.of("type", "setField", "field", "score", "value", 10),
                                Map.of("type", "createTask", "title", "Follow up on lead", "relatedType", "Lead")
                        )
                )
        ));

        MvcResult createFlow = mvc.perform(post("/api/flows")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flowJson))
                .andExpect(status().isCreated())
                .andReturn();

        UUID flowId = UUID.fromString(objectMapper.readTree(createFlow.getResponse().getContentAsString()).get("id").asText());
        mvc.perform(post("/api/flows/" + flowId + "/activate")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(post("/api/leads")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Jane","lastName":"Smith","email":"jane@test.com","source":"WEBSITE"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(10));
    }

    @Test
    void registerWebhook_createsDeliveryOnLeadCreated() throws Exception {
        mvc.perform(post("/api/webhooks")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://httpbin.org/post\",\"secret\":\"whsec\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/leads")
                .header("Authorization", "Bearer " + adminToken)
                .header("Idempotency-Key", "webhook-test-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Webhook\",\"lastName\":\"Test\",\"email\":\"webhook@test.com\"}"))
                .andExpect(status().isCreated());

        MvcResult deliveries = mvc.perform(get("/api/webhook-deliveries")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        var list = objectMapper.readValue(deliveries.getResponse().getContentAsString(), List.class);
        assertThat(list).isNotEmpty();
    }
}
