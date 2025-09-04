package com.blashape.backend_blashape.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Edges {
    @Column(name = "edge_top")
    private Boolean top;

    @Column(name = "edge_bottom")
    private Boolean bottom;

    @Column(name = "edge_left")
    private Boolean left;

    @Column(name = "edge_right")
    private Boolean right;
}
