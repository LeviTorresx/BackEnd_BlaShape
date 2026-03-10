package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.SvgRequest;
import com.blashape.backend_blashape.DTOs.SvgResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.mapper.CuttingMapper;
import com.blashape.backend_blashape.mapper.PieceMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.CuttingRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CuttingService {

    private final CuttingRepository cuttingRepository;
    private final FurnitureRepository furnitureRepository;
    private final CuttingMapper cuttingMapper;
    private final CarpenterRepository carpenterRepository;
    private final JwtUtil jwtUtil;
    private final PieceMapper pieceMapper;

    @Transactional
    public SvgResponse createCuttingPreview(SvgRequest request) {

        GuillotineAlgorithm.PackingResult result = GuillotineAlgorithm.pack(
                request.getContainerWidth(),
                request.getContainerHeight(),
                request.getItems(),
                request.getKerf()
        );

        List<String> svgs = SvgPreviewGenerator.generateAll(
                result.sheets(),
                result.wastePercents(),
                request.getContainerWidth(),
                request.getContainerHeight(),
                request.getKerf(),
                request.getPreviewWidth(),
                request.getPreviewHeight()
        );

        SvgResponse response = new SvgResponse();
        response.setSvgs(svgs);
        response.setWastePercents(result.wastePercents());
        response.setSheetCount(svgs.size());
        return response;
    }

}
