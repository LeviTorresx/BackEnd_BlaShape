package com.blashape.backend_blashape.services;

public class RenderOptions {

    public enum ColorMode { SINGLE, MULTI }

    // ── SVG ──────────────────────────────────────────────────────────────────
    private ColorMode colorMode       = ColorMode.SINGLE;
    private boolean   showBorders     = true;
    private boolean   showLabels      = true;
    private boolean   showDimensions  = true;
    private boolean   showEdgeBanding = true;
    private boolean   showWatermark   = false;
    private String    watermarkText   = "DEMO";

    // ── PDF ──────────────────────────────────────────────────────────────────
    private boolean   showCutLines    = true;   // líneas guillotina azules
    private boolean   showKerfContour = true;   // contorno naranja de corte (kerf)
    private boolean   showCutArrows   = true;   // flechas de inicio de corte
    private boolean   showCutOrder    = true;   // numeración de orden de corte
    private boolean   showPieceList   = true;   // página 2: lista de piezas
    private boolean   showDimLabels   = true;   // cotas del tablero (mm)
    private boolean   showGrid        = true;   // cuadrícula de referencia
    private boolean   pdfWatermark    = false;  // marca de agua en PDF
    private String    pdfWatermarkText= "DEMO";

    // ── Planes ───────────────────────────────────────────────────────────────

    public static RenderOptions freePlan() {
        RenderOptions o = new RenderOptions();
        // SVG
        o.colorMode       = ColorMode.SINGLE;
        o.showBorders     = false;
        o.showLabels      = true;
        o.showDimensions  = false;
        o.showEdgeBanding = false;
        o.showWatermark   = true;
        o.watermarkText   = "BLASHAPE";
        // PDF
        o.showCutLines    = false;   // sin líneas guillotina
        o.showKerfContour = false;   // sin kerf
        o.showCutArrows   = false;   // sin flechas
        o.showCutOrder    = false;   // sin numeración
        o.showPieceList   = false;   // sin página de piezas
        o.showDimLabels   = false;   // sin cotas
        o.showGrid        = false;   // sin cuadrícula
        o.pdfWatermark    = true;
        o.pdfWatermarkText= "BLASHAPE";
        return o;
    }

    public static RenderOptions basicPlan() {
        RenderOptions o = new RenderOptions();
        // SVG
        o.colorMode       = ColorMode.MULTI;
        o.showBorders     = true;
        o.showLabels      = true;
        o.showDimensions  = true;
        o.showEdgeBanding = false;
        o.showWatermark   = false;
        // PDF
        o.showCutLines    = true;
        o.showKerfContour = false;   // sin kerf
        o.showCutArrows   = false;   // sin flechas
        o.showCutOrder    = false;   // sin numeración
        o.showPieceList   = true;
        o.showDimLabels   = true;
        o.showGrid        = true;
        o.pdfWatermark    = false;
        return o;
    }

    public static RenderOptions proPlan() {
        RenderOptions o = new RenderOptions();
        // SVG
        o.colorMode       = ColorMode.MULTI;
        o.showBorders     = true;
        o.showLabels      = true;
        o.showDimensions  = true;
        o.showEdgeBanding = true;
        o.showWatermark   = false;
        // PDF — todo habilitado
        o.showCutLines    = true;
        o.showKerfContour = true;
        o.showCutArrows   = true;
        o.showCutOrder    = true;
        o.showPieceList   = true;
        o.showDimLabels   = true;
        o.showGrid        = true;
        o.pdfWatermark    = false;
        return o;
    }

    public static RenderOptions resolveOptions(String plan) {
        if (plan == null) return RenderOptions.freePlan();
        return switch (plan.toUpperCase()) {
            case "PRO"   -> RenderOptions.proPlan();
            case "BASIC" -> RenderOptions.basicPlan();
            default      -> RenderOptions.freePlan();
        };
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public ColorMode getColorMode()             { return colorMode; }
    public void setColorMode(ColorMode c)       { this.colorMode = c; }

    public boolean isShowBorders()              { return showBorders; }
    public void setShowBorders(boolean v)       { this.showBorders = v; }

    public boolean isShowLabels()               { return showLabels; }
    public void setShowLabels(boolean v)        { this.showLabels = v; }

    public boolean isShowDimensions()           { return showDimensions; }
    public void setShowDimensions(boolean v)    { this.showDimensions = v; }

    public boolean isShowEdgeBanding()          { return showEdgeBanding; }
    public void setShowEdgeBanding(boolean v)   { this.showEdgeBanding = v; }

    public boolean isShowWatermark()            { return showWatermark; }
    public void setShowWatermark(boolean v)     { this.showWatermark = v; }

    public String getWatermarkText()            { return watermarkText; }
    public void setWatermarkText(String t)      { this.watermarkText = t; }

    // PDF getters/setters
    public boolean isShowCutLines()             { return showCutLines; }
    public void setShowCutLines(boolean v)      { this.showCutLines = v; }

    public boolean isShowKerfContour()          { return showKerfContour; }
    public void setShowKerfContour(boolean v)   { this.showKerfContour = v; }

    public boolean isShowCutArrows()            { return showCutArrows; }
    public void setShowCutArrows(boolean v)     { this.showCutArrows = v; }

    public boolean isShowCutOrder()             { return showCutOrder; }
    public void setShowCutOrder(boolean v)      { this.showCutOrder = v; }

    public boolean isShowPieceList()            { return showPieceList; }
    public void setShowPieceList(boolean v)     { this.showPieceList = v; }

    public boolean isShowDimLabels()            { return showDimLabels; }
    public void setShowDimLabels(boolean v)     { this.showDimLabels = v; }

    public boolean isShowGrid()                 { return showGrid; }
    public void setShowGrid(boolean v)          { this.showGrid = v; }

    public boolean isPdfWatermark()             { return pdfWatermark; }
    public void setPdfWatermark(boolean v)      { this.pdfWatermark = v; }

    public String getPdfWatermarkText()         { return pdfWatermarkText; }
    public void setPdfWatermarkText(String t)   { this.pdfWatermarkText = t; }
}