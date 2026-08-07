package org.tajiro.terms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TermsDTO {
    private Long id;
    private String title;
    private Boolean required;
    private String version;
}
