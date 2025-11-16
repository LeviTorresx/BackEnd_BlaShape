package com.blashape.backend_blashape.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Color {
    @Column (name = "color_name")
    private String name;

    @Column (name = "color_hex")
    private String hex;
}
