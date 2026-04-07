// PreviewGeneratorSVG.java
package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.CuttingPosition;
import com.blashape.backend_blashape.entitys.CuttingSheet;
import com.blashape.backend_blashape.entitys.Sheet;
import org.springframework.stereotype.Service;

@Service
public class PreviewGeneratorSVG {

    private static final int    SVG_W       = 900;
    private static final int    SVG_H       = 500;
    private static final int    PADDING_TOP = 28;
    private static final int    PADDING_BOT = 30;
    private static final int    CANVAS_H    = SVG_H - PADDING_TOP - PADDING_BOT;

    private static final String COLOR_FONDO = "#f5f0e8";
    private static final String COLOR_BORDE = "#2c3e50";
    private static final String COLOR_TEXTO = "#1a1a2e";
    private static final String COLOR_TAPAC = "#e74c3c";
    private static final int    TAPAC_THICKNESS = 3; // grosor franja interna tapacanto

    private static final String[] PALETA = {
            "#a8d8ea", "#b8e4c9", "#f9e4b7", "#d5c8f0",
            "#f4b8c1", "#bbe3f5", "#c8e6c9", "#ffe0b2",
            "#e1bee7", "#b3e5fc"
    };

    // ─────────────────────────────────────────────────────────────────────────

    public String generateSVG(CuttingSheet sheet) {
        return generateSVG(sheet, new RenderOptions());
    }

    public String generateSVG(CuttingSheet sheet, RenderOptions opts) {
        Sheet t = sheet.getSheet();

        double scaleX = (double) SVG_W    / t.getWidth();
        double scaleY = (double) CANVAS_H / t.getHeight();
        double scale  = Math.min(scaleX, scaleY);

        int sw       = (int)(t.getWidth()  * scale);
        int sh       = (int)(t.getHeight() * scale);
        int offsetX  = (SVG_W - sw) / 2;
        int escalaInt= Math.max(1, (int) Math.round(1.0 / scale));
        String mat   = t.getMaterial() == null ? "Tablero" : t.getMaterial().getName();

        StringBuilder sb = new StringBuilder(8192);

        // ── Apertura SVG ─────────────────────────────────────────────────────
        sb.append("<svg xmlns='http://www.w3.org/2000/svg'")
                .append(" width='").append(SVG_W).append("'")
                .append(" height='").append(SVG_H).append("'")
                .append(" viewBox='0 0 ").append(SVG_W).append(" ").append(SVG_H).append("'")
                .append(" style='background:").append(COLOR_FONDO).append(";font-family:Arial,sans-serif'>");

        // ── Encabezado ───────────────────────────────────────────────────────
        String pct = String.format("%.1f", sheet.getPercentageUtilized());
        sb.append("<text x='6' y='18' font-size='13' font-weight='bold' fill='")
                .append(COLOR_TEXTO).append("'>")
                .append(escape(mat)).append(" ").append((int)t.getWidth()).append("x")
                .append((int)t.getHeight()).append(" mm — Plano ")
                .append(sheet.getPlaneNumber()).append(" (").append(pct).append("% aprovechado)")
                .append("</text>");

        // ── Fondo lámina ─────────────────────────────────────────────────────
        sb.append("<rect x='").append(offsetX).append("' y='").append(PADDING_TOP).append("'")
                .append(" width='").append(sw).append("' height='").append(sh).append("'")
                .append(" fill='").append(COLOR_FONDO).append("'")
                .append(" stroke='").append(COLOR_BORDE).append("' stroke-width='2'/>");

        // ── Piezas ───────────────────────────────────────────────────────────
        int idx = 0;
        for (CuttingPosition c : sheet.getCuts()) {
            dibujarPieza(sb, c, scale, offsetX, idx++, opts);
        }

        // ── Leyenda ──────────────────────────────────────────────────────────
        int ly = PADDING_TOP + sh + 14;
        if (opts.isShowEdgeBanding()) {
            // muestra franja interna como ejemplo en la leyenda
            sb.append("<rect x='6' y='").append(ly - 8).append("' width='20' height='12'")
                    .append(" fill='#a8d8ea' stroke='").append(COLOR_BORDE).append("' stroke-width='1'/>");
            sb.append("<rect x='6' y='").append(ly - 8).append("' width='20' height='")
                    .append(TAPAC_THICKNESS).append("' fill='").append(COLOR_TAPAC).append("'/>");
            sb.append("<text x='30' y='").append(ly + 4).append("' font-size='11' fill='")
                    .append(COLOR_TEXTO).append("'>Tapacanto</text>");
        }
        sb.append("<text x='130' y='").append(ly + 4).append("' font-size='11' fill='#888'>")
                .append("Escala 1:").append(escalaInt).append("</text>");

        // ── Marca de agua AL FINAL (encima de todo) ──────────────────────────
        // Va aquí al final para que quede sobre piezas y etiquetas
        if (opts.isShowWatermark()) {
            appendWatermark(sb, opts.getWatermarkText(), SVG_W, SVG_H);
        }

        sb.append("</svg>");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void dibujarPieza(StringBuilder sb, CuttingPosition c,
                              double scale, int offsetX,
                              int idx, RenderOptions opts) {

        int px = offsetX + (int)(c.getX() * scale);
        int py = PADDING_TOP  + (int)(c.getY() * scale);
        int pw = Math.max(2, (int)(c.getEffectiveWidth()  * scale));
        int ph = Math.max(2, (int)(c.getEffectiveHeight() * scale));

        // ── Color de relleno ─────────────────────────────────────────────────
        String fill;
        if (opts.getColorMode() == RenderOptions.ColorMode.MULTI) {
            int base = idx % PALETA.length;
            fill = c.isRotated()
                    ? PALETA[(base + PALETA.length / 2) % PALETA.length]
                    : PALETA[base];
        } else {
            fill = c.isRotated() ? "#b8e4c9" : "#a8d8ea";
        }

        // ── Borde: invisible en free plan, normal en el resto ─────────────────
        // En free plan el stroke es del mismo color que el fill → no se ve
        String strokeColor = opts.isShowBorders() ? COLOR_BORDE : fill;

        sb.append("<rect x='").append(px).append("' y='").append(py).append("'")
                .append(" width='").append(pw).append("' height='").append(ph).append("'")
                .append(" fill='").append(fill).append("'")
                .append(" stroke='").append(strokeColor).append("' stroke-width='1' opacity='0.92'/>");

        // ── Tapacanto como franja INTERNA (no sobre el borde) ─────────────────
        // Se dibuja como rectángulos de color tapacanto pegados al interior
        // del borde correspondiente, sin tocar el borde exterior de la pieza.
        if (opts.isShowEdgeBanding()) {
            int t = TAPAC_THICKNESS;
            int gap = 2;

            // top
            if (c.isEdgeBandingX1())
                sb.append("<rect x='").append(px + gap).append("' y='").append(py + gap).append("'")
                        .append(" width='").append(pw - gap * 2).append("' height='").append(t).append("'")
                        .append(" fill='").append(COLOR_TAPAC).append("' opacity='0.85'/>");
            // bottom
            if (c.isEdgeBandingX2())
                sb.append("<rect x='").append(px + gap).append("' y='").append(py + ph - gap - t).append("'")
                        .append(" width='").append(pw - gap * 2).append("' height='").append(t).append("'")
                        .append(" fill='").append(COLOR_TAPAC).append("' opacity='0.85'/>");
            // left
            if (c.isEdgeBandingY1())
                sb.append("<rect x='").append(px + gap).append("' y='").append(py + gap).append("'")
                        .append(" width='").append(t).append("' height='").append(ph - gap * 2).append("'")
                        .append(" fill='").append(COLOR_TAPAC).append("' opacity='0.85'/>");
            // right
            if (c.isEdgeBandingY2())
                sb.append("<rect x='").append(px + pw - gap - t).append("' y='").append(py + gap).append("'")
                        .append(" width='").append(t).append("' height='").append(ph - gap * 2).append("'")
                        .append(" fill='").append(COLOR_TAPAC).append("' opacity='0.85'/>");
        }

        // ── Etiquetas ─────────────────────────────────────────────────────────
        if ((opts.isShowLabels() || opts.isShowDimensions()) && pw > 20 && ph > 14) {
            int cx = px + pw / 2;
            int cy = py + ph / 2;
            int fs = Math.min(13, Math.max(7, Math.min(pw / 6, ph / 2)));

            boolean mostrarNombre = opts.isShowLabels();
            boolean mostrarDims   = opts.isShowDimensions() && pw > 36 && ph > 26;
            int tyNombre = mostrarDims ? cy - fs / 2 : cy;

            if (mostrarNombre) {
                String nombre = c.getPiece().getName() == null ? "" : c.getPiece().getName();
                String sufijo = c.isRotated() ? " [R]" : "";
                sb.append("<text x='").append(cx).append("' y='").append(tyNombre).append("'")
                        .append(" font-size='").append(fs).append("' font-weight='bold'")
                        .append(" text-anchor='middle' dominant-baseline='central'")
                        .append(" fill='").append(COLOR_TEXTO).append("'>")
                        .append(escape(nombre)).append(sufijo).append("</text>");
            }

            if (mostrarDims) {
                String dims = (int)c.getEffectiveWidth() + "×" + (int)c.getEffectiveHeight();
                sb.append("<text x='").append(cx).append("' y='").append(tyNombre + fs + 2).append("'")
                        .append(" font-size='").append(Math.max(7, fs - 2)).append("'")
                        .append(" text-anchor='middle' dominant-baseline='central'")
                        .append(" fill='#445'>").append(dims).append("</text>");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /** Marca de agua diagonal, llamada AL FINAL del SVG para quedar encima. */
    private void appendWatermark(StringBuilder sb, String text, int w, int h) {
        sb.append("<g opacity='0.18'>");
        for (int iy = 0; iy < h + 150; iy += 110) {
            for (int ix = -100; ix < w + 100; ix += 190) {
                sb.append("<text")
                        .append(" x='").append(ix).append("'")
                        .append(" y='").append(iy).append("'")
                        .append(" font-size='38' font-weight='bold' fill='#c0392b'")
                        .append(" font-family='Arial,sans-serif'")
                        .append(" transform='rotate(-35 ").append(ix).append(" ").append(iy).append(")'")
                        .append(">").append(escape(text)).append("</text>");
            }
        }
        sb.append("</g>");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // G-Code
    // ─────────────────────────────────────────────────────────────────────────

    public String generarGCode(CuttingSheet sheet, double thicknessMm, double velocityMm) {
        StringBuilder sb = new StringBuilder(2048);
        Sheet t   = sheet.getSheet();
        String mat = t.getMaterial() == null ? "Tablero" : t.getMaterial().getName();

        sb.append("; ============================================================\n")
                .append("; CNC Optimizer - G-code generado automaticamente\n")
                .append("; Tablero: ").append(mat)
                .append(" ").append((int)t.getWidth()).append("x").append((int)t.getHeight()).append(" mm\n")
                .append(String.format("; Profundidad: %.2f mm  Velocidad: %.0f mm/min\n", thicknessMm, velocityMm))
                .append("; Piezas: ").append(sheet.getCuts().size()).append("\n")
                .append("; ============================================================\n\n")
                .append("G21\nG90\nG17\nG94\nM03 S18000\nG0 Z5\n\n");

        int num = 1;
        for (CuttingPosition c : sheet.getCuts()) {
            double x = c.getX(), y = c.getY();
            double w = c.getEffectiveWidth(), h = c.getEffectiveHeight();
            String nombre = c.getPiece().getName() == null ? "Pieza" : c.getPiece().getName();

            sb.append("; --- Pieza ").append(num++).append(": ").append(nombre)
                    .append(" (").append((int)w).append("x").append((int)h).append(" mm)")
                    .append(c.isRotated() ? " [rotada 90 grados]" : "").append(" ---\n")
                    .append(String.format("G0 X%.3f Y%.3f\n", x, y))
                    .append(String.format("G1 Z%.3f F300\n", -thicknessMm))
                    .append(String.format("G1 X%.3f Y%.3f F%.0f\n", x+w, y, velocityMm))
                    .append(String.format("G1 X%.3f Y%.3f\n", x+w, y+h))
                    .append(String.format("G1 X%.3f Y%.3f\n", x,   y+h))
                    .append(String.format("G1 X%.3f Y%.3f\n", x,   y))
                    .append("G0 Z5\n\n");
        }

        return sb.append("M05\nG0 X0 Y0\nM30\n").toString();
    }
}