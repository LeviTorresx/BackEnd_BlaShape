package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SvgRequest {
    private List<Item> items;
    private int containerWidth;
    private int containerHeight;
    private int kerf;

    private int previewWidth;
    private int previewHeight;
}
