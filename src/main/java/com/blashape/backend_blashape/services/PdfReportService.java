package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;


public class PdfReportService {
    private static final DeviceRgb AZUL_OSCURO  = new DeviceRgb(0x1a, 0x2b, 0x4a);
    private static final DeviceRgb AZUL_MEDIO   = new DeviceRgb(0x2e, 0x6d, 0xa8);
    private static final DeviceRgb AZUL_CLARO   = new DeviceRgb(0xe6, 0xf1, 0xfb);
    private static final DeviceRgb VERDE_ACENTO = new DeviceRgb(0x1d, 0x9e, 0x75);
    private static final DeviceRgb GRIS_CLARO   = new DeviceRgb(0xf5, 0xf5, 0xf3);
    private static final DeviceRgb GRIS_BORDE   = new DeviceRgb(0xcc, 0xcb, 0xc5);
    private static final DeviceRgb ROJO_TAPAC   = new DeviceRgb(0xe7, 0x4c, 0x3c);
    private static final DeviceRgb PIEZA_FILL   = new DeviceRgb(0xa8, 0xd8, 0xea);
    private static final DeviceRgb PIEZA_ROT    = new DeviceRgb(0xb8, 0xe4, 0xc9);
    private static final DeviceRgb PIEZA_BORDE  = new DeviceRgb(0x2c, 0x3e, 0x50);

    /**
            * Genera el PDF completo y lo devuelve como array de bytes.
     *
     * @param resultado   resultado de la optimizacion
     * @param tapacanto   resumen de tapacanto calculado
     * @param proyecto    nombre del proyecto (puede ser null)
     * @return bytes del PDF
     */

    private void drawHeaderSeccion(PdfCanvas canvas, float W, float H,
                                          String titulo, PdfFont fontR, PdfFont fontB) {
        canvas.setFillColor(AZUL_OSCURO)
                .rectangle(0, H - 50, W, 50).fill();
        canvas.setFillColor(VERDE_ACENTO)
                .rectangle(0, H - 53, W, 3).fill();
        canvas.beginText().setFontAndSize(fontB, 16).setFillColor(ColorConstants.WHITE)
                .moveText(40, H - 32).showText(titulo).endText();
        canvas.beginText().setFontAndSize(fontR, 9).setFillColor(new DeviceRgb(0xb5,0xd4,0xf4))
                .moveText(40, H - 44).showText("Blashape  |  Plan de Corte").endText();
    }

    private void drawFooter(PdfCanvas canvas, PdfPage page, PdfFont fontR,
                            int numPage, int totalPage, String seccion) {
        float W = page.getPageSize().getWidth();
        canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.5f)
                .moveTo(40, 22).lineTo(W - 40, 22).stroke();
        canvas.beginText().setFontAndSize(fontR, 8)
                .setFillColor(new DeviceRgb(0x88,0x87,0x80))
                .moveText(40, 10).showText("CNC Optimizer  |  " + seccion).endText();
        String pagTxt = totalPage > 0
                ? "Pagina " + numPage + " de " + totalPage
                : "Pagina " + numPage;
        canvas.beginText().setFontAndSize(fontR, 8)
                .setFillColor(new DeviceRgb(0x88,0x87,0x80))
                .moveText(W - 80, 10).showText(pagTxt).endText();
    }

    private static class SummaryPiece {
        final Piece piece;
        int quantity  = 0;
        int rotated   = 0;

        SummaryPiece(Piece piece) { this.piece = piece; }

        void increment(boolean isRotated) {
            quantity++;
            if (isRotated) rotated++;
        }
    }

    private void drawCover(PdfPage page,
                                CuttingResult result,
                                BandingService.BandingSummary banding,
                                String proyect,
                                PdfFont fontR, PdfFont fontB) {
        PdfCanvas canvas = new PdfCanvas(page);
        float W = page.getPageSize().getWidth();
        float H = page.getPageSize().getHeight();

        // Bloque superior azul oscuro (40% de la pagina)
        float sectionH = H * 0.42f;
        canvas.setFillColor(AZUL_OSCURO)
                .rectangle(0, H - sectionH, W, sectionH)
                .fill();

        // Franja de acento verde
        canvas.setFillColor(VERDE_ACENTO)
                .rectangle(0, H - sectionH - 6, W, 6)
                .fill();

        // Decoracion geometrica — lineas diagonales sutiles en el bloque
        canvas.setStrokeColor(new DeviceRgb(0x2e, 0x4a, 0x7a))
                .setLineWidth(0.5f);
        for (int i = 0; i < 8; i++) {
            float xStart = W * 0.55f + i * 35;
            canvas.moveTo(xStart, H).lineTo(xStart + 80, H - sectionH).stroke();
        }

        // Logo / icono CNC (rectangulos simbolicos de corte)
        float iconX = 52, iconY = H - sectionH + sectionH * 0.45f;
        canvas.setFillColor(VERDE_ACENTO)
                .rectangle(iconX,      iconY,      60, 4).fill()
                .rectangle(iconX,      iconY + 10, 40, 4).fill()
                .rectangle(iconX,      iconY + 20, 50, 4).fill()
                .rectangle(iconX + 65, iconY,       4, 28).fill();

        // Titulo principal
        canvas.beginText()
                .setFontAndSize(fontB, 28)
                .setFillColor(ColorConstants.WHITE)
                .moveText(52, H - sectionH + sectionH * 0.35f)
                .showText(proyect)
                .endText();

        // Subtitulo
        canvas.beginText()
                .setFontAndSize(fontR, 13)
                .setFillColor(new DeviceRgb(0xb5, 0xd4, 0xf4))
                .moveText(52, H - sectionH + sectionH * 0.22f)
                .showText("Plan de Corte CNC  |  Optimizacion 2D")
                .endText();

        // Fecha y hora
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"));
        canvas.beginText()
                .setFontAndSize(fontR, 10)
                .setFillColor(new DeviceRgb(0x85, 0xb7, 0xeb))
                .moveText(52, H - sectionH + sectionH * 0.10f)
                .showText("Generado: " + fecha)
                .endText();

        // ── Tarjetas de metricas (4 columnas) ─────────────────────────────
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
        DeviceRgb[] accents = {AZUL_MEDIO, VERDE_ACENTO, ROJO_TAPAC, AZUL_MEDIO};

        for (int i = 0; i < 4; i++) {
            float cx = 52 + i * (cardW + cardGap);
            // Sombra/fondo tarjeta
            canvas.setFillColor(GRIS_CLARO)
                    .roundRectangle(cx, cardY - cardH, cardW, cardH, 8)
                    .fill();
            canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.5f)
                    .roundRectangle(cx, cardY - cardH, cardW, cardH, 8)
                    .stroke();
            // Borde superior de color
            canvas.setFillColor(accents[i])
                    .rectangle(cx, cardY - 4, cardW, 4)
                    .fill();
            // Valor grande
            canvas.beginText()
                    .setFontAndSize(fontB, 22)
                    .setFillColor(AZUL_OSCURO)
                    .moveText(cx + 14, cardY - 38)
                    .showText(values[i])
                    .endText();
            // Etiqueta
            canvas.beginText()
                    .setFontAndSize(fontR, 9)
                    .setFillColor(new DeviceRgb(0x88, 0x87, 0x80))
                    .moveText(cx + 14, cardY - 55)
                    .showText(labels[i])
                    .endText();
        }
        float detY = cardY - cardH - 40;
        canvas.beginText()
                .setFontAndSize(fontB, 11)
                .setFillColor(AZUL_OSCURO)
                .moveText(52, detY)
                .showText("Detalle de tapacanto")
                .endText();
        canvas.setFillColor(VERDE_ACENTO)
                .rectangle(52, detY - 4, 32, 2).fill();

        float rowY = detY - 22;
        String[] tapRows = {
                "Metros lineales eje ancho (X): " + String.format("%.2f ml", banding.totalMlWidth()),
                "Metros lineales eje largo (Y): " + String.format("%.2f ml", banding.totalMlHeight()),
                "Total general:                 " + String.format("%.2f ml", banding.totalMlGeneral()),
        };
        for (String row : tapRows) {
            canvas.beginText().setFontAndSize(fontR, 10).setFillColor(AZUL_OSCURO)
                    .moveText(52, rowY).showText(row).endText();
            rowY -= 16;
        }

        // ── Resumen de tablero ─────────────────────────────────────────────
        if (!result.getSheets().isEmpty()) {
            Sheet t = result.getSheets().get(0).getSheet();
            float rY = detY - 22;
            assert t.getMaterial() != null;
            String[] infoRows = {
                    "Material:  " + (t.getMaterial() == null ? "-" : t.getMaterial()),
                    "Dimension: " + (int)t.getWidth() + " x " + (int)t.getHeight() + " mm",
                    "Grosor:    " + t.getMaterial().getThickness().get(0) + " mm",
                    "Piezas:    " + result.getTotalPiecesLocated() + " ubicadas"
                            + (result.getMissingParts() > 0
                            ? "  /  " + result.getMissingParts() + " sin ubicar" : "")
            };
            float col2X = W / 2f + 10;
            canvas.beginText().setFontAndSize(fontB, 11).setFillColor(AZUL_OSCURO)
                    .moveText(col2X, detY).showText("Datos del tablero").endText();
            canvas.setFillColor(AZUL_MEDIO)
                    .rectangle(col2X, detY - 4, 32, 2).fill();
            float rRowY = detY - 22;
            for (String row : infoRows) {
                canvas.beginText().setFontAndSize(fontR, 10).setFillColor(AZUL_OSCURO)
                        .moveText(col2X, rRowY).showText(row).endText();
                rRowY -= 16;
            }
        }

        // ── Pie de pagina ──────────────────────────────────────────────────
        drawFooter(canvas, page, fontR, 1, -1, "Portada");
    }

    private void drawPieceList(PdfPage page,
                                    CuttingResult result,
                                    BandingService.BandingSummary banding,
                                    PdfFont fontR, PdfFont fontB) {
        PdfCanvas canvas = new PdfCanvas(page);
        float W = page.getPageSize().getWidth();
        float H = page.getPageSize().getHeight();

        drawHeaderSeccion(canvas, W, H, "Lista de piezas", fontR, fontB);

        // Construir mapa pieza -> ml tapacanto
        Map<String, Double> mlPorPieza = banding.toMlPiece();

        // Consolidar piezas por nombre
        Map<String, SummaryPiece> resumen = new LinkedHashMap<>();
        for (CuttingSheet sheet : result.getSheets()) {
            for (CuttingPosition c : sheet.getCuts()) {
                String nombre = c.getPiece().getName();
                resumen.computeIfAbsent(nombre, k -> new SummaryPiece(c.getPiece()))
                        .increment(c.isRotated());
            }
        }

        // Encabezado tabla
        float tableY = H - 90;
        float[] colX  = {40, 160, 210, 265, 315, 365, 420, 480, 530};
        String[] heads = {"Pieza", "Ancho", "Largo", "Cant.", "T.A1", "T.A2", "T.L1", "T.L2", "Tapacanto ml"};

        canvas.setFillColor(AZUL_OSCURO)
                .rectangle(40, tableY - 18, W - 80, 20)
                .fill();
        for (int i = 0; i < heads.length; i++) {
            canvas.beginText().setFontAndSize(fontB, 9).setFillColor(ColorConstants.WHITE)
                    .moveText(colX[i], tableY - 12).showText(heads[i]).endText();
        }

        // Filas
        float rowH  = 16f;
        float rowY  = tableY - 20;
        boolean alt = false;
        for (Map.Entry<String, SummaryPiece> entry : resumen.entrySet()) {
            SummaryPiece pr = entry.getValue();
            Piece p = pr.piece;
            if (alt) {
                canvas.setFillColor(AZUL_CLARO)
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
                    p.getEdges().getTop() ? "SI" : "-",
                    p.getEdges().getBottom() ? "SI" : "-",
                    p.getEdges().getLeft() ? "SI" : "-",
                    p.getEdges().getRight() ? "SI" : "-",
                    String.format("%.2f", ml)
            };

            for (int i = 0; i < cells.length; i++) {
                boolean esTap = i >= 4 && i <= 7 && cells[i].equals("SI");
                canvas.beginText()
                        .setFontAndSize(fontR, 9)
                        .setFillColor(esTap ? VERDE_ACENTO : AZUL_OSCURO)
                        .moveText(colX[i], rowY - 8)
                        .showText(cells[i])
                        .endText();
            }

            canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.3f)
                    .moveTo(40, rowY - rowH + 3).lineTo(W - 40, rowY - rowH + 3).stroke();

            rowY -= rowH;
            alt = !alt;
        }

        // Totales
        rowY -= 8;
        canvas.setFillColor(AZUL_OSCURO)
                .rectangle(40, rowY - 16, W - 80, 18).fill();
        canvas.beginText().setFontAndSize(fontB, 9).setFillColor(ColorConstants.WHITE)
                .moveText(colX[0], rowY - 8)
                .showText("TOTAL  —  " + result.getTotalPiecesLocated() + " piezas ubicadas")
                .endText();
        canvas.beginText().setFontAndSize(fontB, 9).setFillColor(VERDE_ACENTO)
                .moveText(colX[8], rowY - 8)
                .showText(String.format("%.2f ml", banding.totalMlGeneral()))
                .endText();

        // Leyenda tapacanto
        float leyY = rowY - 40;
        canvas.beginText().setFontAndSize(fontB, 9).setFillColor(AZUL_OSCURO)
                .moveText(40, leyY).showText("Leyenda tapacanto:").endText();
        canvas.beginText().setFontAndSize(fontR, 8).setFillColor(new DeviceRgb(0x44,0x44,0x41))
                .moveText(40, leyY - 14)
                .showText("T.A1 = Cara ancho frontal  |  T.A2 = Cara ancho trasera  |  T.L1 = Cara largo izquierda  |  T.L2 = Cara largo derecha")
                .endText();

        drawFooter(canvas, page, fontR, 2, -1, "Lista de piezas");
    }

    // ─── Pagina de plano de corte ─────────────────────────────────────────────

    private void dibujarPlanoCortePage(PdfPage page, CuttingSheet sheet,
                                       int numSheet, int totalSheets,
                                       PdfFont fontR, PdfFont fontB) {
        PdfCanvas canvas = new PdfCanvas(page);
        float W = page.getPageSize().getWidth();  // A4 apaisado = 841.89
        float H = page.getPageSize().getHeight(); // 595.28

        float headerH = 50f;
        float footerH = 30f;
        float margin   = 30f;

        // ── Encabezado ──────────────────────────────────────────────────────
        canvas.setFillColor(AZUL_OSCURO)
                .rectangle(0, H - headerH, W, headerH).fill();
        canvas.setFillColor(VERDE_ACENTO)
                .rectangle(0, H - headerH - 3, W, 3).fill();

        canvas.beginText().setFontAndSize(fontB, 14).setFillColor(ColorConstants.WHITE)
                .moveText(margin, H - 28)
                .showText("Plano de corte " + numSheet + " / " + totalSheets)
                .endText();

        Sheet t = sheet.getSheet();
        String mat = t.getMaterial() == null ? "" : t.getMaterial().getName();
        String info = mat + "  " + (int)t.getWidth() + "x" + (int)t.getHeight() + " mm  |  "
                + sheet.getCuts().size() + " piezas  |  "
                + String.format("%.1f%% aprovechado", sheet.getPercentageUtilized());

        canvas.beginText().setFontAndSize(fontR, 9).setFillColor(new DeviceRgb(0xb5,0xd4,0xf4))
                .moveText(margin, H - 42).showText(info).endText();

        // ── Area de dibujo del tablero ────────────────────────────────────
        float drawX = margin;
        float drawY = footerH + 10;
        float drawW = W - margin * 2;
        float drawH = H - headerH - footerH - 20;

        // Calcular escala para que el tablero quepa en el area de dibujo
        double escalaX = drawW / t.getWidth();
        double escalaY = drawH / t.getHeight();
        double escala  = Math.min(escalaX, escalaY);

        float tw = (float)(t.getWidth() * escala);
        float th = (float)(t.getHeight() * escala);

        // Centrar el tablero en el area de dibujo
        float offsetX = drawX + (drawW - tw) / 2f;
        float offsetY = drawY + (drawH - th) / 2f;

        // Fondo del tablero
        canvas.setFillColor(new DeviceRgb(0xf5, 0xf0, 0xe8))
                .rectangle(offsetX, offsetY, tw, th).fill();
        canvas.setStrokeColor(PIEZA_BORDE).setLineWidth(1.5f)
                .rectangle(offsetX, offsetY, tw, th).stroke();

        // Cuadricula de referencia sutil
        canvas.setStrokeColor(GRIS_BORDE).setLineWidth(0.2f);
        float gridStep = (float)(200 * escala);
        if (gridStep > 10) {
            for (float gx = gridStep; gx < tw; gx += gridStep) {
                canvas.moveTo(offsetX + gx, offsetY)
                        .lineTo(offsetX + gx, offsetY + th).stroke();
            }
            for (float gy = gridStep; gy < th; gy += gridStep) {
                canvas.moveTo(offsetX,      offsetY + gy)
                        .lineTo(offsetX + tw, offsetY + gy).stroke();
            }
        }

        // ── Paso 1: relleno de piezas (capa base) ────────────────────────
        for (CuttingPosition c : sheet.getCuts()) {
            float px = offsetX + (float)(c.getX()            * escala);
            float py = offsetY + (float)(c.getY()            * escala);
            float pw = (float)(c.getEffectiveWidth() * escala);
            float ph = (float)(c.getEffectiveHeight() * escala);
            DeviceRgb fill = c.isRotated() ? PIEZA_ROT : PIEZA_FILL;
            canvas.setFillColor(fill).rectangle(px, py, pw, ph).fill();
        }

        // ── Paso 2: lineas de corte guillotina (atraviesan el tablero) ────
        // Recopilar todas las coordenadas X e Y de los bordes de piezas
        java.util.Set<Float> cortesX = new java.util.TreeSet<>();
        java.util.Set<Float> cortesY = new java.util.TreeSet<>();
        for (CuttingPosition c : sheet.getCuts()) {
            float px = (float)(c.getX()            * escala);
            float py = (float)(c.getY()            * escala);
            float pw = (float)(c.getEffectiveWidth() * escala);
            float ph = (float)(c.getEffectiveHeight() * escala);
            cortesX.add(px); cortesX.add(px + pw);
            cortesY.add(py); cortesY.add(py + ph);
        }
        // Lineas verticales de corte (excepto bordes del tablero)
        canvas.setStrokeColor(new DeviceRgb(0x0a, 0x84, 0xff)).setLineWidth(0.6f);
        float[] dashV = {4f, 3f};
        canvas.setLineDash(dashV, 0);
        for (float cx : cortesX) {
            if (cx > 1 && cx < tw - 1) {
                canvas.moveTo(offsetX + cx, offsetY)
                        .lineTo(offsetX + cx, offsetY + th).stroke();
            }
        }
        // Lineas horizontales de corte
        for (float cy : cortesY) {
            if (cy > 1 && cy < th - 1) {
                canvas.moveTo(offsetX,      offsetY + cy)
                        .lineTo(offsetX + tw, offsetY + cy).stroke();
            }
        }
        canvas.setLineDash(new float[]{}, 0); // resetear dash

        // ── Paso 3: contorno de corte por pieza + kerf + flecha inicio ────
        int orden = 1;
        for (CuttingPosition c : sheet.getCuts()) {
            float px = offsetX + (float)(c.getX()            * escala);
            float py = offsetY + (float)(c.getY()            * escala);
            float pw = (float)(c.getEffectiveWidth() * escala);
            float ph = (float)(c.getEffectiveHeight() * escala);

            // Kerf (ancho de sierra ~3mm → escala a px)
            float kerf = Math.max(0.8f, (float)(3.0 * escala));

            // Contorno de corte: linea solida naranja, ligeramente dentro del borde
            float inset = kerf / 2f;
            canvas.setStrokeColor(new DeviceRgb(0xe6, 0x7e, 0x22)).setLineWidth(kerf);
            canvas.moveTo(px + inset,      py + inset)
                    .lineTo(px + pw - inset, py + inset)
                    .lineTo(px + pw - inset, py + ph - inset)
                    .lineTo(px + inset,      py + ph - inset)
                    .closePath().stroke();

            // Flecha de inicio de corte (esquina inferior-izquierda → derecha)
            float arrowX = px + inset;
            float arrowY = py + inset;
            float aLen   = Math.min(8f, pw * 0.15f);
            canvas.setFillColor(new DeviceRgb(0xe6, 0x7e, 0x22))
                    .moveTo(arrowX,         arrowY)
                    .lineTo(arrowX + aLen,  arrowY + aLen * 0.5f)
                    .lineTo(arrowX + aLen,  arrowY - aLen * 0.5f)
                    .closePath().fill();

            // Marcas de tapacanto (lineas rojas en bordes)
            float gt = Math.max(1f, Math.min(2.5f, (float)(escala * 4)));
            canvas.setStrokeColor(ROJO_TAPAC).setLineWidth(gt);
            if (c.isEdgeBandingX1())
                canvas.moveTo(px, py + ph).lineTo(px + pw, py + ph).stroke();
            if (c.isEdgeBandingX2())
                canvas.moveTo(px, py).lineTo(px + pw, py).stroke();
            if (c.isEdgeBandingY1())
                canvas.moveTo(px, py).lineTo(px, py + ph).stroke();
            if (c.isEdgeBandingY2())
                canvas.moveTo(px + pw, py).lineTo(px + pw, py + ph).stroke();
        }

        // Cotas del tablero
        canvas.setStrokeColor(new DeviceRgb(0x44,0x44,0x41)).setLineWidth(0.4f);
        // Cota horizontal (abajo)
        canvas.moveTo(offsetX, offsetY - 8).lineTo(offsetX + tw, offsetY - 8).stroke();
        canvas.moveTo(offsetX, offsetY - 5).lineTo(offsetX, offsetY - 11).stroke();
        canvas.moveTo(offsetX + tw, offsetY - 5).lineTo(offsetX + tw, offsetY - 11).stroke();
        canvas.beginText().setFontAndSize(fontR, 7).setFillColor(new DeviceRgb(0x44,0x44,0x41))
                .moveText(offsetX + tw/2 - 15, offsetY - 18)
                .showText((int)t.getWidth() + " mm").endText();
        // Cota vertical (izquierda)
        canvas.moveTo(offsetX - 8, offsetY).lineTo(offsetX - 8, offsetY + th).stroke();
        canvas.moveTo(offsetX - 5, offsetY).lineTo(offsetX - 11, offsetY).stroke();
        canvas.moveTo(offsetX - 5, offsetY + th).lineTo(offsetX - 11, offsetY + th).stroke();
        // Texto cota vertical (rotado a mano con posicion manual)
        canvas.beginText().setFontAndSize(fontR, 7).setFillColor(new DeviceRgb(0x44,0x44,0x41))
                .moveText(offsetX - 22, offsetY + th/2 - 8)
                .showText((int)t.getHeight() + " mm").endText();

        // ── Leyenda del plano ──────────────────────────────────────────────
        float leyX = offsetX + tw + 15;
        if (leyX + 85 < W - margin) {
            float leyY = offsetY + th;

            canvas.beginText().setFontAndSize(fontB, 8).setFillColor(AZUL_OSCURO)
                    .moveText(leyX, leyY).showText("Leyenda").endText();
            leyY -= 14;

            // Pieza normal
            canvas.setFillColor(PIEZA_FILL).rectangle(leyX, leyY - 8, 14, 10).fill();
            canvas.setStrokeColor(PIEZA_BORDE).setLineWidth(0.4f)
                    .rectangle(leyX, leyY - 8, 14, 10).stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Pieza normal").endText();
            leyY -= 15;

            // Pieza rotada
            canvas.setFillColor(PIEZA_ROT).rectangle(leyX, leyY - 8, 14, 10).fill();
            canvas.setStrokeColor(PIEZA_BORDE).setLineWidth(0.4f)
                    .rectangle(leyX, leyY - 8, 14, 10).stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Pieza rotada 90deg").endText();
            leyY -= 15;

            // Linea de corte guillotina (azul punteada)
            float[] dash = {4f, 3f};
            canvas.setStrokeColor(new DeviceRgb(0x0a, 0x84, 0xff)).setLineWidth(0.8f)
                    .setLineDash(dash, 0)
                    .moveTo(leyX, leyY - 4).lineTo(leyX + 14, leyY - 4).stroke();
            canvas.setLineDash(new float[]{}, 0);
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Linea guillotina").endText();
            leyY -= 15;

            // Contorno de corte (naranja / kerf)
            canvas.setStrokeColor(new DeviceRgb(0xe6, 0x7e, 0x22)).setLineWidth(1.5f)
                    .moveTo(leyX, leyY - 8).lineTo(leyX + 14, leyY - 8)
                    .lineTo(leyX + 14, leyY - 1).lineTo(leyX, leyY - 1)
                    .closePath().stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Contorno corte (kerf)").endText();
            leyY -= 15;

            // Flecha inicio de corte
            canvas.setFillColor(new DeviceRgb(0xe6, 0x7e, 0x22))
                    .moveTo(leyX,     leyY - 4)
                    .lineTo(leyX + 8, leyY - 1)
                    .lineTo(leyX + 8, leyY - 7)
                    .closePath().fill();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Inicio de corte").endText();
            leyY -= 15;

            // Numero de orden
            canvas.setFillColor(AZUL_OSCURO).circle(leyX + 5, leyY - 4, 5).fill();
            canvas.beginText().setFontAndSize(fontB, 5).setFillColor(ColorConstants.WHITE)
                    .moveText(leyX + 3, leyY - 6).showText("1").endText();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Orden de corte").endText();
            leyY -= 15;

            // Tapacanto
            canvas.setStrokeColor(ROJO_TAPAC).setLineWidth(2f)
                    .moveTo(leyX, leyY - 4).lineTo(leyX + 14, leyY - 4).stroke();
            canvas.beginText().setFontAndSize(fontR, 7).setFillColor(AZUL_OSCURO)
                    .moveText(leyX + 18, leyY - 1).showText("Tapacanto").endText();
        }

        drawFooter(canvas, page, fontR, numSheet + 2, totalSheets + 2,
                "Plano " + numSheet + "/" + totalSheets);
    }
}
