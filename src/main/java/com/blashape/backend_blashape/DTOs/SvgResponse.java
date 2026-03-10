package com.blashape.backend_blashape.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SvgResponse {
    private List<String> svgs;
    private List<Double> wastePercents;
    private int sheetCount;
}
