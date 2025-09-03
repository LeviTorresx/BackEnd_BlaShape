package com.blashape.backend_blashape.entitys;

import jakarta.persistence.Embeddable;

@Embeddable
public class Edges {
    private Boolean top;
    private Boolean bottom;
    private Boolean left;
    private Boolean right;
}
