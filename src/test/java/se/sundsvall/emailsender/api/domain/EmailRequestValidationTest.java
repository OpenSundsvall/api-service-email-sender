package se.sundsvall.emailsender.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("junit")
class EmailRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidationWithValidRequest() {
        var request = createEmailRequest();
        var constraintViolations = validator.validate(request);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void testValidationWithNullEmailAddress() {
        var request = createEmailRequest(req -> req.setEmailAddress(null));
        var constraintViolations = List.copyOf(validator.validate(request));

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.get(0).getPropertyPath().toString()).isEqualTo("emailAddress");
        assertThat(constraintViolations.get(0).getMessage()).isEqualTo("must not be blank");
    }

    @Test
    void testValidationWithBlankEmailAddress() {
        var request = createEmailRequest(req -> req.setEmailAddress(""));
        var constraintViolations = List.copyOf(validator.validate(request));

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.get(0).getPropertyPath().toString()).isEqualTo("emailAddress");
        assertThat(constraintViolations.get(0).getMessage()).isEqualTo("must not be blank");
    }

    @Test
    void testValidationWithInvalidEmailAddress() {
        var request = createEmailRequest(req -> req.setEmailAddress("kalle"));
        var constraintViolations = List.copyOf(validator.validate(request));

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.get(0).getPropertyPath().toString()).isEqualTo("emailAddress");
        assertThat(constraintViolations.get(0).getMessage()).isEqualTo("must be a well-formed email address");
    }

    @Test
    void testValidationWithNullSenderName() {
        var request = createEmailRequest(req -> req.setSenderName(null));

        var constraintViolations = validator.validate(request);

        assertThat(constraintViolations).hasSize(1);
    }

    @Test
    void testValidationWithNullSenderEmailAddress() {
        var request = createEmailRequest(req -> req.setEmailAddress(null));

        var constraintViolations = validator.validate(request);

        assertThat(constraintViolations).hasSize(1);
    }

    @Test
    void testValidationWithInvalidSenderEmailAddress() {
        var request = createEmailRequest(req -> req.setSenderEmail("not-an-email-address"));

        var constraintViolations = List.copyOf(validator.validate(request));

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.get(0).getPropertyPath().toString()).isEqualTo("senderEmail");
        assertThat(constraintViolations.get(0).getMessage()).isEqualTo("must be a well-formed email address");
    }

    private EmailRequest createEmailRequest() {
        return createEmailRequest(null);
    }

    private EmailRequest createEmailRequest(final Consumer<EmailRequest> modifier) {
        var html = Base64.getEncoder().encodeToString("<p>html</p>".getBytes(StandardCharsets.UTF_8));

        var attachment = EmailRequest.Attachment.builder()
            .withContent("aGVsbG8gd29ybGQK")
            .withContentType("text/plain")
            .withName("test.txt")
            .build();

        var request = EmailRequest.builder()
            .withEmailAddress("some.other.email@someotherhost.com")
            .withSubject("someSubject")
            .withMessage("someMessage")
            .withHtmlMessage(html)
            .withSenderName("senderName")
            .withSenderEmail("senderEmail@email.com")
            .withAttachments(List.of(attachment, attachment))
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }
}