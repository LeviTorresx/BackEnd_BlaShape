package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PdfReportService {

    private static final DeviceRgb VIOLETA_OSCURO = new DeviceRgb(0x2e, 0x1a, 0x47);
    private static final DeviceRgb VIOLETA_MEDIO  = new DeviceRgb(0x6d, 0x3f, 0xa8);
    private static final DeviceRgb VIOLETA_CLARO  = new DeviceRgb(0xf3, 0xe8, 0xff);
    private static final DeviceRgb MORADO_ACENTO  = new DeviceRgb(0x8e, 0x44, 0xad);
    private static final DeviceRgb GRIS_CLARO     = new DeviceRgb(0xf5, 0xf5, 0xf3);
    private static final DeviceRgb GRIS_BORDE     = new DeviceRgb(0xcc, 0xcb, 0xc5);
    private static final DeviceRgb ROJO_TAPAC     = new DeviceRgb(0xe7, 0x4c, 0x3c);
    private static final DeviceRgb PIEZA_FILL     = new DeviceRgb(0xd6, 0xc2, 0xf0);
    private static final DeviceRgb PIEZA_ROT      = new DeviceRgb(0xe0, 0xd4, 0xf7);
    private static final DeviceRgb PIEZA_BORDE    = new DeviceRgb(0x3d, 0x2c, 0x5a);

    // ── API pública ───────────────────────────────────────────────────────────

    /** Genera con opciones por defecto. */
    public byte[] generate(CuttingResult result,
                           BandingService.BandingSummary banding,
                           String proyecto) throws IOException {
        return generate(result, banding, proyecto, RenderOptions.freePlan());
    }

    /** Genera respetando el plan del usuario. */
    public byte[] generate(CuttingResult result,
                           BandingService.BandingSummary banding,
                           String proyecto,
                           RenderOptions opts) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter   writer = new PdfWriter(baos);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A4);
        doc.setMargins(0, 0, 0, 0);

        PdfFont fontR = PdfFontFactory.createFont("Helvetica");
        PdfFont fontB = PdfFontFactory.createFont("Helvetica-Bold");

        String nombre = (proyecto != null && !proyecto.isBlank())
                ? proyecto : "Plan de Corte CNC";

        // Página 1: portada (siempre)
        drawCover(pdf.addNewPage(PageSize.A4), result, banding, nombre, fontR, fontB, opts);

        // Página 2: lista de piezas (según plan)
        if (opts.isShowPieceList()) {
            drawPieceList(pdf.addNewPage(PageSize.A4), result, banding, fontR, fontB);
        }

        // Páginas de planos
        int planoNum = 1;
        int total    = result.getSheets().size();
        for (CuttingSheet plano : result.getSheets()) {
            drawCutting(pdf.addNewPage(PageSize.A4.rotate()),
                    plano, planoNum++, total, fontR, fontB, opts);
        }

        doc.close();
        return baos.toByteArray();
    }

    // ── Helpers compartidos ───────────────────────────────────────────────────

    private void drawHeaderSeccion(PdfCanvas canvas, float W, float H,
                                   String titulo, PdfFont fontR, PdfFont fontB) {
        canvas.setFillColor(VIOLETA_OSCURO).rectangle(0, H - 50, W, 50).fill();
        canvas.setFillColor(MORADO_ACENTO).rectangle(0, H - 53, W, 3).fill();
        canvas.beginText().setFontAndSize(fontB, 16).setFillColor(ColorConstants.WHITE)
                .moveText(40, H - 32).showText(titulo).endText();
        canvas.beginText().setFontAndSize(fontR, 9)
                .setFillColor(new DeviceRgb(0xb5, 0xd4, 0xf4))
                .moveText(40, H - 44).showText("Blashape  |  Plan de Corte").endText();
    }

    private void drawFooter(PdfCanvas canvas, PdfPage page, PdfFont fontR,
                            int numPage, int totalPage, String seccion) {
        float W = page.getPageSize().getWidth();
        canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.5f)
                .moveTo(40, 22).lineTo(W - 40, 22).stroke();
        canvas.beginText().setFontAndSize(fontR, 8)
                .setFillColor(new DeviceRgb(0x88, 0x87, 0x80))
                .moveText(40, 10).showText("Blashape  |  " + seccion).endText();
        String pagTxt = totalPage > 0
                ? "Pagina " + numPage + " de " + totalPage
                : "Pagina " + numPage;
        canvas.beginText().setFontAndSize(fontR, 8)
                .setFillColor(new DeviceRgb(0x88, 0x87, 0x80))
                .moveText(W - 80, 10).showText(pagTxt).endText();
    }

    /** Marca de agua diagonal sobre toda la página PDF. */
    private void drawPdfWatermark(PdfCanvas canvas, float W, float H,
                                  String text, PdfFont fontB) throws IOException {
        // Crear el estado gráfico con opacidad ANTES de dibujar
        com.itextpdf.kernel.pdf.extgstate.PdfExtGState gs =
                new com.itextpdf.kernel.pdf.extgstate.PdfExtGState();
        gs.setFillOpacity(0.07f); // ← ajusta aquí: 0.05 muy tenue, 0.15 más visible

        canvas.saveState();
        canvas.setExtGState(gs); // ← aplicar ANTES de dibujar el texto
        canvas.setFillColor(new DeviceRgb(0xc0, 0x39, 0x2b));

        float fs = 42f;
        float stepX = 220f, stepY = 130f;

        for (float y = 0; y < H + stepY; y += stepY) {
            for (float x = -100; x < W + stepX; x += stepX) {
                canvas.saveState();
                canvas.concatMatrix(
                        Math.cos(Math.toRadians(-35)), Math.sin(Math.toRadians(-35)),
                        -Math.sin(Math.toRadians(-35)), Math.cos(Math.toRadians(-35)),
                        x, y);
                canvas.beginText()
                        .setFontAndSize(fontB, fs)
                        .setFillColor(new DeviceRgb(0xc0, 0x39, 0x2b))
                        .moveText(0, 0)
                        .showText(text)
                        .endText();
                canvas.restoreState();
            }
        }

        canvas.restoreState(); // restaura el estado gráfico (opacidad vuelve a 1)
    }

    // ── Portada ───────────────────────────────────────────────────────────────

    private void drawCover(PdfPage page,
                           CuttingResult result,
                           BandingService.BandingSummary banding,
                           String proyect,
                           PdfFont fontR, PdfFont fontB,
                           RenderOptions opts) throws IOException {
        PdfCanvas canvas = new PdfCanvas(page);
        float W = page.getPageSize().getWidth();
        float H = page.getPageSize().getHeight();

        float sectionH = H * 0.42f;
        canvas.setFillColor(VIOLETA_OSCURO)
                .rectangle(0, H - sectionH, W, sectionH).fill();
        canvas.setFillColor(MORADO_ACENTO)
                .rectangle(0, H - sectionH - 6, W, 6).fill();

        canvas.setStrokeColor(new DeviceRgb(0x2e, 0x4a, 0x7a)).setLineWidth(0.5f);
        for (int i = 0; i < 8; i++) {
            float xStart = W * 0.55f + i * 35;
            canvas.moveTo(xStart, H).lineTo(xStart + 80, H - sectionH).stroke();
        }

        float iconX = 52, iconY = H - sectionH + sectionH * 0.45f;
        canvas.setFillColor(MORADO_ACENTO)
                .rectangle(iconX,      iconY,      60, 4).fill()
                .rectangle(iconX,      iconY + 10, 40, 4).fill()
                .rectangle(iconX,      iconY + 20, 50, 4).fill()
                .rectangle(iconX + 65, iconY,       4, 28).fill();

        canvas.beginText().setFontAndSize(fontB, 28).setFillColor(ColorConstants.WHITE)
                .moveText(52, H - sectionH + sectionH * 0.35f).showText(proyect).endText();
        canvas.beginText().setFontAndSize(fontR, 13)
                .setFillColor(new DeviceRgb(0xb5, 0xd4, 0xf4))
                .moveText(52, H - sectionH + sectionH * 0.22f)
                .showText("Plan de Corte  |  Blashape").endText();

        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"));
        canvas.beginText().setFontAndSize(fontR, 10)
                .setFillColor(new DeviceRgb(0x85, 0xb7, 0xeb))
                .moveText(52, H - sectionH + sectionH * 0.10f)
                .showText("Generado: " + fecha).endText();

        // Tarjetas de métricas
        float cardY   = H - sectionH - 130;
        float cardW   = (W - 104) / 4f;
        float cardH   = 90;
        float cardGap = 12;

        String[] labels = {"Tableros usados", "Aprovechamiento", "Desperdicio", "Tapacanto total"};
        String[] values = {
                String.valueOf(result.getUsedSheets()),
                String.format("%.1f%%", result.getAverageUtilizationPercentage()),
                String.format("%.2f m²", result.getTotalWastedM2()),
                String.format("%.1f ml", banding.totalMlGeneral())
        };
        DeviceRgb[] accents = {VIOLETA_MEDIO, MORADO_ACENTO, ROJO_TAPAC, VIOLETA_MEDIO};

        for (int i = 0; i < 4; i++) {
            float cx = 52 + i * (cardW + cardGap);
            canvas.setFillColor(GRIS_CLARO)
                    .roundRectangle(cx, cardY - cardH, cardW, cardH, 8).fill();
            canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.5f)
                    .roundRectangle(cx, cardY - cardH, cardW, cardH, 8).stroke();
            canvas.setFillColor(accents[i])
                    .rectangle(cx, cardY - 4, cardW, 4).fill();
            canvas.beginText().setFontAndSize(fontB, 22).setFillColor(VIOLETA_OSCURO)
                    .moveText(cx + 14, cardY - 38).showText(values[i]).endText();
            canvas.beginText().setFontAndSize(fontR, 9)
                    .setFillColor(new DeviceRgb(0x88, 0x87, 0x80))
                    .moveText(cx + 14, cardY - 55).showText(labels[i]).endText();
        }

        float detY = cardY - cardH - 40;
        canvas.beginText().setFontAndSize(fontB, 11).setFillColor(VIOLETA_OSCURO)
                .moveText(52, detY).showText("Detalle de tapacanto").endText();
        canvas.setFillColor(MORADO_ACENTO).rectangle(52, detY - 4, 32, 2).fill();

        float rowY = detY - 22;
        String[] tapRows = {
                "Metros lineales eje ancho (X): " + String.format("%.2f ml", banding.totalMlWidth()),
                "Metros lineales eje largo (Y): " + String.format("%.2f ml", banding.totalMlHeight()),
                "Total general:                 " + String.format("%.2f ml", banding.totalMlGeneral()),
        };
        for (String row : tapRows) {
            canvas.beginText().setFontAndSize(fontR, 10).setFillColor(VIOLETA_OSCURO)
                    .moveText(52, rowY).showText(row).endText();
            rowY -= 16;
        }

        if (!result.getSheets().isEmpty()) {
            Sheet t = result.getSheets().get(0).getSheet();
            assert t.getMaterial() != null;
            String[] infoRows = {
                    "Material:  " + t.getMaterial(),
                    "Dimension: " + (int)t.getWidth() + " x " + (int)t.getHeight() + " mm",
                    "Piezas:    " + result.getTotalPiecesLocated() + " ubicadas"
                            + (result.getMissingParts() > 0
                            ? "  /  " + result.getMissingParts() + " sin ubicar" : "")
            };
            float col2X = W / 2f + 10;
            canvas.beginText().setFontAndSize(fontB, 11).setFillColor(VIOLETA_OSCURO)
                    .moveText(col2X, detY).showText("Datos del tablero").endText();
            canvas.setFillColor(VIOLETA_MEDIO)
                    .rectangle(col2X, detY - 4, 32, 2).fill();
            float rRowY = detY - 22;
            for (String row : infoRows) {
                canvas.beginText().setFontAndSize(fontR, 10).setFillColor(VIOLETA_OSCURO)
                        .moveText(col2X, rRowY).showText(row).endText();
                rRowY -= 16;
            }
        }

        // Marca de agua encima de todo al final
        if (opts.isPdfWatermark()) {
            drawPdfWatermark(canvas, W, H, opts.getPdfWatermarkText(), fontB);
        }

        drawFooter(canvas, page, fontR, 1, -1, "Portada");
    }

    // ── Lista de piezas ───────────────────────────────────────────────────────

    private static class SummaryPiece {
        final Piece piece;
        int quantity = 0;
        int rotated  = 0;
        SummaryPiece(Piece p) { this.piece = p; }
        void increment(boolean r) { quantity++; if (r) rotated++; }
    }

    private void drawPieceList(PdfPage page,
                               CuttingResult result,
                               BandingService.BandingSummary banding,
                               PdfFont fontR, PdfFont fontB) {
        PdfCanvas canvas = new PdfCanvas(page);
        float W = page.getPageSize().getWidth();
        float H = page.getPageSize().getHeight();

        drawHeaderSeccion(canvas, W, H, "Lista de piezas", fontR, fontB);

        Map<String, Double> mlPorPieza = banding.toMlPiece();
        Map<String, SummaryPiece> resumen = new LinkedHashMap<>();
        for (CuttingSheet sheet : result.getSheets()) {
            for (CuttingPosition c : sheet.getCuts()) {
                String nombre = c.getPiece().getName();
                resumen.computeIfAbsent(nombre, k -> new SummaryPiece(c.getPiece()))
                        .increment(c.isRotated());
            }
        }

        float tableY = H - 90;
        float[] colX  = {40, 160, 210, 265, 315, 365, 420, 480, 530};
        String[] heads = {"Pieza", "Ancho", "Largo", "Cant.", "T.A1", "T.A2", "T.L1", "T.L2", "Tapacanto ml"};

        canvas.setFillColor(VIOLETA_OSCURO)
                .rectangle(40, tableY - 18, W - 80, 20).fill();
        for (int i = 0; i < heads.length; i++) {
            canvas.beginText().setFontAndSize(fontB, 9).setFillColor(ColorConstants.WHITE)
                    .moveText(colX[i], tableY - 12).showText(heads[i]).endText();
        }

        float rowH = 16f;
        float rowY = tableY - 20;
        boolean alt = false;
        for (Map.Entry<String, SummaryPiece> entry : resumen.entrySet()) {
            SummaryPiece pr = entry.getValue();
            Piece p = pr.piece;
            if (alt) {
                canvas.setFillColor(VIOLETA_CLARO)
                        .rectangle(40, rowY - rowH + 3, W - 80, rowH).fill();
            }
            String nombre = p.getName() == null ? "-" : p.getName();
            if (nombre.length() > 18) nombre = nombre.substring(0, 16) + "..";
            double ml = mlPorPieza.getOrDefault(p.getName(), 0.0);

            String[] cells = {
                    nombre,
                    (int)p.getWidth() + " mm",
                    (int)p.getHeight() + " mm",
                    String.valueOf(pr.quantity),
                    p.getEdges().getTop()    ? "SI" : "-",
                    p.getEdges().getBottom() ? "SI" : "-",
                    p.getEdges().getLeft()   ? "SI" : "-",
                    p.getEdges().getRight()  ? "SI" : "-",
                    String.format("%.2f", ml)
            };

            for (int i = 0; i < cells.length; i++) {
                boolean esTap = i >= 4 && i <= 7 && cells[i].equals("SI");
                canvas.beginText().setFontAndSize(fontR, 9)
                        .setFillColor(esTap ? MORADO_ACENTO : VIOLETA_OSCURO)
                        .moveText(colX[i], rowY - 8).showText(cells[i]).endText();
            }

            canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.3f)
                    .moveTo(40, rowY - rowH + 3).lineTo(W - 40, rowY - rowH + 3).stroke();
            rowY -= rowH;
            alt = !alt;
        }

        rowY -= 8;
        canvas.setFillColor(VIOLETA_OSCURO).rectangle(40, rowY - 16, W - 80, 18).fill();
        canvas.beginText().setFontAndSize(fontB, 9).setFillColor(ColorConstants.WHITE)
                .moveText(colX[0], rowY - 8)
                .showText("TOTAL  —  " + result.getTotalPiecesLocated() + " piezas ubicadas").endText();
        canvas.beginText().setFontAndSize(fontB, 9).setFillColor(ColorConstants.WHITE)
                .moveText(colX[8], rowY - 8)
                .showText(String.format("%.2f ml", banding.totalMlGeneral())).endText();

        float leyY = rowY - 40;
        canvas.beginText().setFontAndSize(fontB, 9).setFillColor(VIOLETA_OSCURO)
                .moveText(40, leyY).showText("Leyenda tapacanto:").endText();
        canvas.beginText().setFontAndSize(fontR, 8).setFillColor(new DeviceRgb(0x44, 0x44, 0x41))
                .moveText(40, leyY - 14)
                .showText("T.A1 = Cara ancho frontal  |  T.A2 = Cara ancho trasera  |  T.L1 = Cara largo izquierda  |  T.L2 = Cara largo derecha")
                .endText();

        drawFooter(canvas, page, fontR, 2, -1, "Lista de piezas");
    }

    /**
     * Un corte vertical en X es guillotina real si ninguna pieza lo cruza,
     * es decir, ninguna pieza tiene su inicio antes de X y su fin después de X.
     */
    private boolean esCorteVerticalCompleto(CuttingSheet sheet, float x, float tol) {
        double xD   = x;
        double tolD = tol;
        for (CuttingPosition c : sheet.getCuts()) {
            double inicio = c.getX();
            double fin    = c.getX() + c.getEffectiveWidth();
            if (inicio < xD - tolD && fin > xD + tolD) return false;
        }
        return true;
    }

    private boolean esCorteHorizontalCompleto(CuttingSheet sheet, float y, float tol) {
        double yD   = y;
        double tolD = tol;
        for (CuttingPosition c : sheet.getCuts()) {
            double inicio = c.getY();
            double fin    = c.getY() + c.getEffectiveHeight();
            if (inicio < yD - tolD && fin > yD + tolD) return false;
        }
        return true;
    }

    // ── Plano de corte ────────────────────────────────────────────────────────

    private void drawCutting(PdfPage page, CuttingSheet sheet,
                             int numSheet, int totalSheets,
                             PdfFont fontR, PdfFont fontB,
                             RenderOptions opts) throws IOException {
        PdfCanvas canvas = new PdfCanvas(page);
        float W = page.getPageSize().getWidth();
        float H = page.getPageSize().getHeight();

        float headerH = 50f, footerH = 30f, margin = 30f;

        // Encabezado
        canvas.setFillColor(VIOLETA_OSCURO).rectangle(0, H - headerH, W, headerH).fill();
        canvas.setFillColor(MORADO_ACENTO).rectangle(0, H - headerH - 3, W, 3).fill();
        canvas.beginText().setFontAndSize(fontB, 14).setFillColor(ColorConstants.WHITE)
                .moveText(margin, H - 28)
                .showText("Plano de corte " + numSheet + " / " + totalSheets).endText();

        Sheet t   = sheet.getSheet();
        String mat = t.getMaterial() == null ? "" : t.getMaterial().getName();
        String info = mat + "  " + (int)t.getWidth() + "x" + (int)t.getHeight() + " mm  |  "
                + sheet.getCuts().size() + " piezas  |  "
                + String.format("%.1f%% aprovechado", sheet.getPercentageUtilized());
        canvas.beginText().setFontAndSize(fontR, 9)
                .setFillColor(new DeviceRgb(0xb5, 0xd4, 0xf4))
                .moveText(margin, H - 42).showText(info).endText();

        // Área de dibujo
        float drawX = margin, drawY = footerH + 10;
        float drawW = W - margin * 2;
        float drawH = H - headerH - footerH - 20;

        double escalaX = drawW / t.getWidth();
        double escalaY = drawH / t.getHeight();
        double escala  = Math.min(escalaX, escalaY);

        float tw = (float)(t.getWidth()  * escala);
        float th = (float)(t.getHeight() * escala);
        float offsetX = drawX + (drawW - tw) / 2f;
        float offsetY = drawY + (drawH - th) / 2f;

        // Fondo tablero
        canvas.setFillColor(new DeviceRgb(0xf5, 0xf0, 0xe8))
                .rectangle(offsetX, offsetY, tw, th).fill();
        canvas.setStrokeColor(PIEZA_BORDE).setLineWidth(1.5f)
                .rectangle(offsetX, offsetY, tw, th).stroke();

        // Cuadrícula (según plan)
        if (opts.isShowGrid()) {
            canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.2f);
            float gridStep = (float)(200 * escala);
            if (gridStep > 10) {
                for (float gx = gridStep; gx < tw; gx += gridStep)
                    canvas.moveTo(offsetX + gx, offsetY)
                            .lineTo(offsetX + gx, offsetY + th).stroke();
                for (float gy = gridStep; gy < th; gy += gridStep)
                    canvas.moveTo(offsetX, offsetY + gy)
                            .lineTo(offsetX + tw, offsetY + gy).stroke();
            }
        }

        // Paso 1: relleno de piezas
        for (CuttingPosition c : sheet.getCuts()) {
            float px = offsetX + (float)(c.getX()            * escala);
            float py = offsetY + (float)(c.getY()            * escala);
            float pw = (float)(c.getEffectiveWidth()  * escala);
            float ph = (float)(c.getEffectiveHeight() * escala);
            canvas.setFillColor(c.isRotated() ? PIEZA_ROT : PIEZA_FILL)
                    .rectangle(px, py, pw, ph).fill();
        }

        // Paso 2: líneas guillotina (según plan)
        // Paso 2: líneas guillotina (solo las que atraviesan el tablero completo)
        if (opts.isShowCutLines()) {
            java.util.Set<Float> cortesX = new java.util.TreeSet<>();
            java.util.Set<Float> cortesY = new java.util.TreeSet<>();

            for (CuttingPosition c : sheet.getCuts()) {
                float px = (float)(c.getX()             * escala);
                float py = (float)(c.getY()             * escala);
                float pw = (float)(c.getEffectiveWidth() * escala);
                float ph = (float)(c.getEffectiveHeight()* escala);
                cortesX.add(px); cortesX.add(px + pw);
                cortesY.add(py); cortesY.add(py + ph);
            }

            // Tolerancia: cuántos px puede faltar para considerarse "que atraviesa"
            float tolerancia = (float)(5 * escala); // 5mm en escala

            canvas.setStrokeColor(new DeviceRgb(0x0a, 0x84, 0xff)).setLineWidth(0.6f)
                    .setLineDash(new float[]{4f, 3f}, 0);

            for (float cx : cortesX) {
                if (cx <= 1 || cx >= tw - 1) continue; // ignorar bordes del tablero

                // Verificar que ninguna pieza "interrumpe" esta línea vertical
                // Es decir, que no haya piezas a ambos lados sin un corte que las separe
                if (esCorteVerticalCompleto(sheet, cx / (float) escala, (float)(5.0 / escala))) {
                    canvas.moveTo(offsetX + cx, offsetY)
                            .lineTo(offsetX + cx, offsetY + th).stroke();
                }
            }

            for (float cy : cortesY) {
                if (cy <= 1 || cy >= th - 1) continue;

                if (esCorteHorizontalCompleto(sheet, cy / (float) escala, (float)(5.0 / escala))) {
                    canvas.moveTo(offsetX, offsetY + cy)
                            .lineTo(offsetX + tw, offsetY + cy).stroke();
                }
            }

            canvas.setLineDash(new float[]{}, 0);
        }

        // Paso 3: contorno kerf + flecha inicio (según plan)
        for (CuttingPosition c : sheet.getCuts()) {
            float px = offsetX + (float)(c.getX()            * escala);
            float py = offsetY + (float)(c.getY()            * escala);
            float pw = (float)(c.getEffectiveWidth()  * escala);
            float ph = (float)(c.getEffectiveHeight() * escala);

            if (opts.isShowKerfContour()) {
                float kerf  = Math.max(0.8f, (float)(3.0 * escala));
                float inset = kerf / 2f;
                canvas.setStrokeColor(new DeviceRgb(0xe6, 0x7e, 0x22)).setLineWidth(kerf);
                canvas.moveTo(px + inset,      py + inset)
                        .lineTo(px + pw - inset, py + inset)
                        .lineTo(px + pw - inset, py + ph - inset)
                        .lineTo(px + inset,      py + ph - inset)
                        .closePath().stroke();
            }

            if (opts.isShowCutArrows()) {
                float inset = Math.max(0.8f, (float)(3.0 * escala)) / 2f;
                float aLen  = Math.min(8f, pw * 0.15f);
                canvas.setFillColor(new DeviceRgb(0xe6, 0x7e, 0x22))
                        .moveTo(px + inset,        py + inset)
                        .lineTo(px + inset + aLen, py + inset + aLen * 0.5f)
                        .lineTo(px + inset + aLen, py + inset - aLen * 0.5f)
                        .closePath().fill();
            }

            // Tapacanto como franja interna (igual que en SVG)
            float gt  = (float)(4.0 * escala);  // grosor de la franja
            float gap = (float)(8.0 * escala);  // separación del borde

            // Proteger que el gap no sea mayor que 1/6 del lado más pequeño
            float minLado = Math.min(pw, ph);
            float safeGap = Math.min(gap, minLado / 6f);
            float safeGt  = Math.min(gt,  minLado / 6f);

            canvas.setFillColor(ROJO_TAPAC);

            if (c.isEdgeBandingX1()) // top
                canvas.rectangle(px + safeGap, py + ph - safeGap - safeGt,
                        pw - safeGap * 2, safeGt).fill();

            if (c.isEdgeBandingX2()) // bottom
                canvas.rectangle(px + safeGap, py + safeGap,
                        pw - safeGap * 2, safeGt).fill();

            if (c.isEdgeBandingY1()) // left
                canvas.rectangle(px + safeGap, py + safeGap,
                        safeGt, ph - safeGap * 2).fill();

            if (c.isEdgeBandingY2()) // right
                canvas.rectangle(px + pw - safeGap - safeGt, py + safeGap,
                        safeGt, ph - safeGap * 2).fill();
        }

        // Paso 4: etiquetas
        for (CuttingPosition c : sheet.getCuts()) {
            float px = offsetX + (float)(c.getX()            * escala);
            float py = offsetY + (float)(c.getY()            * escala);
            float pw = (float)(c.getEffectiveWidth()  * escala);
            float ph = (float)(c.getEffectiveHeight() * escala);

            if (pw > 22 && ph > 14) {
                float fs = Math.min(8f, Math.max(5f, Math.min(pw / 8f, ph / 3.5f)));
                String nombre = c.getPiece().getName() == null ? "" : c.getPiece().getName();
                if (nombre.length() > 12 && pw < 60) nombre = nombre.substring(0, 10) + "..";

                float textX = px + pw / 2f - (nombre.length() * fs * 0.3f);
                float textY = py + ph / 2f + (pw > 40 && ph > 22 ? fs * 0.5f : 0);

                canvas.beginText().setFontAndSize(fontB, fs).setFillColor(VIOLETA_OSCURO)
                        .moveText(textX, textY)
                        .showText(nombre + (c.isRotated() ? " [R]" : "")).endText();

                if (pw > 40 && ph > 22) {
                    String dims = (int)c.getEffectiveWidth() + "x" + (int)c.getEffectiveHeight();
                    float dimFs = Math.max(4.5f, fs - 1.5f);
                    canvas.beginText().setFontAndSize(fontR, dimFs)
                            .setFillColor(new DeviceRgb(0x44, 0x44, 0x41))
                            .moveText(px + pw/2f - dims.length() * dimFs * 0.28f, textY - fs * 1.4f)
                            .showText(dims).endText();
                }
            }

            // Número de orden (según plan)
            if (opts.isShowCutOrder() && pw > 16 && ph > 16) {
                // pequeño círculo con número en la esquina superior derecha
                float cx = px + pw - 8f;
                float cy = py + ph - 8f;
                canvas.setFillColor(VIOLETA_OSCURO).circle(cx, cy, 5f).fill();
                // el orden coincide con la posición en la lista (1-based)
                int orden = sheet.getCuts().indexOf(c) + 1;
                canvas.beginText().setFontAndSize(fontB, 4.5f).setFillColor(ColorConstants.WHITE)
                        .moveText(cx - 3f, cy - 1.5f)
                        .showText(String.valueOf(orden)).endText();
            }
        }

        // Cotas del tablero (según plan)
        if (opts.isShowDimLabels()) {
            canvas.setStrokeColor(new DeviceRgb(0x44, 0x44, 0x41)).setLineWidth(0.4f);
            canvas.moveTo(offsetX, offsetY - 8).lineTo(offsetX + tw, offsetY - 8).stroke();
            canvas.moveTo(offsetX, offsetY - 5).lineTo(offsetX, offsetY - 11).stroke();
            canvas.moveTo(offsetX + tw, offsetY - 5).lineTo(offsetX + tw, offsetY - 11).stroke();
            canvas.beginText().setFontAndSize(fontR, 7)
                    .setFillColor(new DeviceRgb(0x44, 0x44, 0x41))
                    .moveText(offsetX + tw/2 - 15, offsetY - 18)
                    .showText((int)t.getWidth() + " mm").endText();
            canvas.moveTo(offsetX - 8, offsetY).lineTo(offsetX - 8, offsetY + th).stroke();
            canvas.moveTo(offsetX - 5, offsetY).lineTo(offsetX - 11, offsetY).stroke();
            canvas.moveTo(offsetX - 5, offsetY + th).lineTo(offsetX - 11, offsetY + th).stroke();
            canvas.beginText().setFontAndSize(fontR, 7)
                    .setFillColor(new DeviceRgb(0x44, 0x44, 0x41))
                    .moveText(offsetX - 22, offsetY + th/2 - 8)
                    .showText((int)t.getHeight() + " mm").endText();
        }

        // Leyenda lateral (solo si hay espacio y el plan lo permite)
        float leyX = offsetX + tw + 15;
        if (leyX + 85 < W - margin) {
            float leyY = offsetY + th;

            canvas.beginText().setFontAndSize(fontB, 8).setFillColor(VIOLETA_OSCURO)
                    .moveText(leyX, leyY).showText("Leyenda").endText();
            leyY -= 14;

            // Pieza normal
            canvas.setFillColor(PIEZA_FILL).rectangle(leyX, leyY - 8, 14, 10).fill();
            canvas.setStrokeColor(PIEZA_BORDE).setLineWidth(0.4f)
                    .rectangle(leyX, leyY - 8, 14, 10).stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Pieza normal").endText();
            leyY -= 15;

            // Pieza rotada
            canvas.setFillColor(PIEZA_ROT).rectangle(leyX, leyY - 8, 14, 10).fill();
            canvas.setStrokeColor(PIEZA_BORDE).setLineWidth(0.4f)
                    .rectangle(leyX, leyY - 8, 14, 10).stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Pieza rotada 90°").endText();
            leyY -= 15;

            // Líneas guillotina (solo si el plan las muestra)
            if (opts.isShowCutLines()) {
                canvas.setStrokeColor(new DeviceRgb(0x0a, 0x84, 0xff)).setLineWidth(0.8f)
                        .setLineDash(new float[]{4f, 3f}, 0)
                        .moveTo(leyX, leyY - 4).lineTo(leyX + 14, leyY - 4).stroke();
                canvas.setLineDash(new float[]{}, 0);
                canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                        .moveText(leyX + 18, leyY - 1).showText("Linea guillotina").endText();
                leyY -= 15;
            }

            // Kerf (solo si el plan lo muestra)
            if (opts.isShowKerfContour()) {
                canvas.setStrokeColor(new DeviceRgb(0xe6, 0x7e, 0x22)).setLineWidth(1.5f)
                        .moveTo(leyX, leyY - 8).lineTo(leyX + 14, leyY - 8)
                        .lineTo(leyX + 14, leyY - 1).lineTo(leyX, leyY - 1)
                        .closePath().stroke();
                canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                        .moveText(leyX + 18, leyY - 1).showText("Contorno corte (kerf)").endText();
                leyY -= 15;
            }

            // Flecha inicio (solo si el plan la muestra)
            if (opts.isShowCutArrows()) {
                canvas.setFillColor(new DeviceRgb(0xe6, 0x7e, 0x22))
                        .moveTo(leyX,     leyY - 4).lineTo(leyX + 8, leyY - 1)
                        .lineTo(leyX + 8, leyY - 7).closePath().fill();
                canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                        .moveText(leyX + 18, leyY - 1).showText("Inicio de corte").endText();
                leyY -= 15;
            }

            // Orden de corte (solo si el plan lo muestra)
            if (opts.isShowCutOrder()) {
                canvas.setFillColor(VIOLETA_OSCURO).circle(leyX + 5, leyY - 4, 5).fill();
                canvas.beginText().setFontAndSize(fontB, 5).setFillColor(ColorConstants.WHITE)
                        .moveText(leyX + 3, leyY - 6).showText("1").endText();
                canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                        .moveText(leyX + 18, leyY - 1).showText("Orden de corte").endText();
                leyY -= 15;
            }

            // Tapacanto (siempre en leyenda si hay piezas con tapacanto)
            canvas.setStrokeColor(ROJO_TAPAC).setLineWidth(2f)
                    .moveTo(leyX, leyY - 4).lineTo(leyX + 14, leyY - 4).stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(VIOLETA_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Tapacanto").endText();
        }

        // Marca de agua encima de todo (al final)
        if (opts.isPdfWatermark()) {
            drawPdfWatermark(canvas, W, H, opts.getPdfWatermarkText(), fontB);
        }

        drawFooter(canvas, page, fontR, numSheet + (opts.isShowPieceList() ? 2 : 1),
                totalSheets + (opts.isShowPieceList() ? 2 : 1),
                "Plano " + numSheet + "/" + totalSheets);
    }
}