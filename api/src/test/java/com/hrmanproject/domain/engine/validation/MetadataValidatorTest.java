package com.hrmanproject.domain.engine.validation;

import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetadataValidatorTest {

    @Mock
    private MetadataCatalogService catalogService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SafeSqlCompiler sqlCompiler;

    private MetadataValidator metadataValidator;

    @BeforeEach
    void setUp() {
        metadataValidator = new MetadataValidator(catalogService, jdbcTemplate, sqlCompiler);
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldIsNull() {
        TableMetadata meta = TableMetadata.builder()
                .tableName("test_users")
                .columns(List.of(
                        ColumnMetadata.builder().name("username").dataType("VARCHAR").nullable(false).build()
                ))
                .build();

        when(catalogService.getTableMetadata("test_users")).thenReturn(Optional.of(meta));

        Map<String, Object> record = new HashMap<>();
        record.put("username", "   "); // blank

        assertThatThrownBy(() -> metadataValidator.validate("test_users", record))
                .isInstanceOf(MetadataValidationException.class)
                .satisfies(ex -> {
                    Map<String, String> errors = ((MetadataValidationException) ex).getValidationErrors();
                    assertThat(errors).containsKey("username");
                });
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        TableMetadata meta = TableMetadata.builder()
                .tableName("test_users")
                .columns(List.of(
                        ColumnMetadata.builder().name("email").dataType("VARCHAR").nullable(false).validationType("EMAIL").build()
                ))
                .build();

        when(catalogService.getTableMetadata("test_users")).thenReturn(Optional.of(meta));

        Map<String, Object> record = Map.of("email", "not-an-email");

        assertThatThrownBy(() -> metadataValidator.validate("test_users", record))
                .isInstanceOf(MetadataValidationException.class)
                .satisfies(ex -> {
                    Map<String, String> errors = ((MetadataValidationException) ex).getValidationErrors();
                    assertThat(errors.get("email")).contains("EMAIL");
                });
    }

    @Test
    void shouldPassWhenAllConstraintsAreSatisfied() {
        TableMetadata meta = TableMetadata.builder()
                .tableName("test_users")
                .columns(List.of(
                        ColumnMetadata.builder().name("email").dataType("VARCHAR").nullable(false).validationType("EMAIL").build(),
                        ColumnMetadata.builder().name("phone").dataType("VARCHAR").nullable(true).validationType("PHONE").build()
                ))
                .build();

        when(catalogService.getTableMetadata("test_users")).thenReturn(Optional.of(meta));

        Map<String, Object> record = Map.of(
                "email", "test@layermark.com",
                "phone", "+905551234567"
        );

        metadataValidator.validate("test_users", record); // should not throw
    }
}
