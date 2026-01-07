package com.example.backend.api.exception;

import com.example.ejb.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerWebMvcTest.ThrowingController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerWebMvcTest.ThrowingController.class})
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @RestController
    static class ThrowingController {

        @GetMapping("/not-found")
        void notFound() {
            throw new EntityNotFoundException();
        }

        @GetMapping("/business")
        Object business() {
            throw new BusinessException("regra X quebrou");
        }

        @GetMapping("/constraint")
        Object constraint() {
            throw new ConstraintViolationException("mensagem", Set.of());
        }

        @GetMapping("/tx-constraint")
        Object txConstraint() {
            var cve = new ConstraintViolationException("mensagem", Set.of());
            return throwTxWithCause(cve);
        }

        @GetMapping("/tx-generic")
        Object txGeneric() {
            return throwTxWithCause(new RuntimeException("boom"));
        }

        private static Object throwTxWithCause(Throwable cause) {
            throw new TransactionSystemException("tx failed", cause);
        }

        private static Set<ConstraintViolation<?>> mockViolations(String path, String msg) {
            ConstraintViolation<?> v = mock(ConstraintViolation.class);
            Path p = mock(Path.class);
            when(p.toString()).thenReturn(path);
            when(v.getPropertyPath()).thenReturn(p);
            when(v.getMessage()).thenReturn(msg);
            return Set.of(v);
        }
    }

    @Test
    void entityNotFound__shouldReturn404WithoutBody() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void businessException__shouldReturn422WithBody() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_EXCEPTION"))
                .andExpect(jsonPath("$.message").value("regra X quebrou"));
    }

    @Test
    void constraintViolation__shouldReturn400WithErrors() throws Exception {
        mockMvc.perform(get("/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Constraint violation"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void transactionSystem__withConstraintViolationRoot__shouldReturn400ValidationError() throws Exception {
        mockMvc.perform(get("/tx-constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Constraint violation"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void transactionSystem__generic__shouldReturn400TransactionError() throws Exception {
        mockMvc.perform(get("/tx-generic"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TRANSACTION_ERROR"))
                .andExpect(jsonPath("$.message").value("Transaction failed"));
    }
}