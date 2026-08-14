package org.tajiro.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingLedgerItem {
    private String mgmBldrgstPk;
    private String dongNm;
    private String bldNm;
}