package org.tajiro.terms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TermsDetailDTO {
    private Long id;
    private String type;
    private String title;
    private String version;
    private String content;
}
