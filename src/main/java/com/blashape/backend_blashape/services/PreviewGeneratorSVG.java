package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.CuttingPosition;
import com.blashape.backend_blashape.entitys.CuttingSheet;
import com.blashape.backend_blashape.entitys.Sheet;

public class PreviewGeneratorSVG {

    private static final double SVG_MAX_W   = 900;
    private static final double SVG_MAX_H   = 500;
    private static final String COLOR_FONDO = "#f5f0e8";
    private static final String COLOR_PIEZA = "#a8d8ea";
    private static final String COLOR_BORDE = "#2c3e50";
    private static final String COLOR_TEXTO = "#1a1a2e";
    private static final String COLOR_TAPAC = "#e74c3c";
    private static final int    PADDING_TOP = 28;
    private static final int    PADDING_BOT = 30;

    private String line(int x1, int y1, int x2, int y2, int grosor) {
        return "<line x1='" + x1 + "' y1='" + y1 + "' x2='" + x2 + "' y2='" + y2 +
                "' stroke='" + COLOR_TAPAC + "' stroke-width='" + grosor + "'/>";
    }

    private void dibujarPieza(StringBuilder sb, CuttingPosition c, double scale) {
        int px = (int)(c.getX()              * scale) + 1;
        int py = (int)(c.getY()              * scale) + PADDING_TOP;
        int pw = (int)(c.getEffectiveWidth()  * scale);
        int ph = (int)(c.getEffectiveHeight()  * scale);

        String fill = c.isRotated() ? "#b8e4c9" : COLOR_PIEZA;

        sb.append("<rect x='").append(px).append("' y='").append(py).append("'");
        sb.append(" width='").append(pw).append("' height='").append(ph).append("'");
        sb.append(" fill='").append(fill).append("'");
        sb.append(" stroke='").append(COLOR_BORDE).append("' stroke-width='1' opacity='0.9'/>");

        if (pw > 25 && ph > 18) {
            int cx = px + pw / 2;
            int cy = py + ph / 2;
            int fs = Math.min(13, Math.max(8, Math.min(pw / 6, ph / 2)));
            String nombre = c.getPiece().getName() == null ? "" : c.getPiece().getName();
            String sufijo = c.isRotated() ? " [R]" : "";

            int tyNombre = (pw > 40 && ph > 30) ? cy - fs / 2 : cy;
            sb.append("<text x='").append(cx).append("' y='").append(tyNombre).append("'");
            sb.append(" font-size='").append(fs).append("' font-weight='bold'");
            sb.append(" text-anchor='middle' dominant-baseline='central'");
            sb.append(" fill='").append(COLOR_TEXTO).append("'>");
            sb.append(nombre).append(sufijo).append("</text>");

            if (pw > 40 && ph > 30) {
                String dims = (int)c.getEffectiveWidth() + "x" + (int)c.getEffectiveHeight();
                int tyDims = cy + fs;
                sb.append("<text x='").append(cx).append("' y='").append(tyDims).append("'");
                sb.append(" font-size='").append(Math.max(7, fs - 2)).append("'");
                sb.append(" text-anchor='middle' dominant-baseline='central'");
                sb.append(" fill='#445'>").append(dims).append("</text>");
            }
        }

        // Tapacanto
        int gt = Math.max(2, Math.min(3, (int)(scale * 5)));
        if (c.isEdgeBandingX1()) sb.append(line(px, py,      px+pw, py,      gt));
        if (c.isEdgeBandingX2()) sb.append(line(px, py+ph,   px+pw, py+ph,   gt));
        if (c.isEdgeBandingY1()) sb.append(line(px, py,      px,    py+ph,   gt));
        if (c.isEdgeBandingY2()) sb.append(line(px+pw, py,   px+pw, py+ph,   gt));
    }

    public String generarSVG(CuttingSheet sheet) {
        Sheet t = sheet.getSheet();

        double scale = Math.min(SVG_MAX_W / t.getWidth(), SVG_MAX_H / t.getHeight());
        int    sw     = (int)(t.getWidth() * scale);
        int    sh     = (int)(t.getHeight() * scale);
        int    svgH   = sh + PADDING_TOP + PADDING_BOT;
        int    escalaInt = (int) Math.round(1.0 / scale);
        String mat    = t.getMaterial() == null ? "Tablero" : t.getMaterial().getName();

        StringBuilder sb = new StringBuilder(8192);

        // Apertura SVG
        sb.append("<svg xmlns='http://www.w3.org/2000/svg'");
        sb.append(" width='").append(sw + 2).append("'");
        sb.append(" height='").append(svgH).append("'");
        sb.append(" style='background:").append(COLOR_FONDO);
        sb.append(";font-family:Arial,sans-serif'>");

        // Encabezado — usar solo un String.format simple para el porcentaje
        String pct = String.format("%.1f", sheet.getPercentageUtilized());
        sb.append("<text x='6' y='18' font-size='13' font-weight='bold' fill='");
        sb.append(COLOR_TEXTO).append("'>");
        sb.append(mat).append(" ").append((int)t.getWidth()).append("x");
        sb.append((int)t.getHeight()).append(" mm - Plano ");
        sb.append(sheet.getPlaneNumber()).append(" (").append(pct).append("% aprovechado)");
        sb.append("</text>");

        // Fondo del tablero
        sb.append("<rect x='1' y='").append(PADDING_TOP).append("'");
        sb.append(" width='").append(sw).append("'");
        sb.append(" height='").append(sh).append("'");
        sb.append(" fill='").append(COLOR_FONDO).append("'");
        sb.append(" stroke='").append(COLOR_BORDE).append("'");
        sb.append(" stroke-width='2'/>");

        // Piezas
        for (CuttingPosition c : sheet.getCuts()) {
            dibujarPieza(sb, c, scale);
        }

        // Leyenda
        int ly = PADDING_TOP + sh + 14;
        sb.append("<line x1='6' y1='").append(ly).append("' x2='26' y2='").append(ly).append("'");
        sb.append(" stroke='").append(COLOR_TAPAC).append("' stroke-width='3'/>");
        sb.append("<text x='30' y='").append(ly + 4).append("' font-size='11' fill='");
        sb.append(COLOR_TEXTO).append("'>Tapacanto</text>");
        sb.append("<text x='130' y='").append(ly + 4).append("' font-size='11' fill='#888'>");
        sb.append("Escala 1:").append(escalaInt).append("</text>");

        sb.append("</svg>");
        return sb.toString();
    }

    public String generarGCode(CuttingSheet sheet, double thicknessMm, double velocityMm) {
        StringBuilder sb = new StringBuilder(2048);
        Sheet t = sheet.getSheet();
        String mat = t.getMaterial() == null ? "Tablero" : t.getMaterial().getName();

        sb.append("; ============================================================\n");
        sb.append("; CNC Optimizer - G-code generado automaticamente\n");
        sb.append("; Tablero: ").append(mat)
                .append(" ").append((int)t.getWidth()).append("x").append((int)t.getHeight()).append(" mm\n");
        sb.append(String.format("; Profundidad: %.2f mm  Velocidad: %.0f mm/min\n",
                thicknessMm, velocityMm));
        sb.append("; Piezas: ").append(sheet.getCuts().size()).append("\n");
        sb.append("; ============================================================\n\n");

        sb.append("G21       ; unidades en milimetros\n");
        sb.append("G90       ; posicionamiento absoluto\n");
        sb.append("G17       ; plano XY\n");
        sb.append("G94       ; avance por minuto\n");
        sb.append("M03 S18000 ; encender husillo\n");
        sb.append("G0 Z5     ; seguridad inicial\n\n");

        int num = 1;
        for (CuttingPosition c : sheet.getCuts()) {
            double x = c.getX();
            double y = c.getY();
            double w = c.getEffectiveWidth();
            double h = c.getEffectiveHeight();
            String nombre = c.getPiece().getName() == null ? "Pieza" : c.getPiece().getName();

            sb.append("; --- Pieza ").append(num++).append(": ").append(nombre);
            sb.append(" (").append((int)w).append("x").append((int)h).append(" mm)");
            if (c.isRotated()) sb.append(" [rotada 90 grados]");
            sb.append(" ---\n");

            sb.append(String.format("G0 X%.3f Y%.3f\n", x, y));
            sb.append(String.format("G1 Z%.3f F300\n", -thicknessMm));
            sb.append(String.format("G1 X%.3f Y%.3f F%.0f\n", x + w, y,     velocityMm));
            sb.append(String.format("G1 X%.3f Y%.3f\n",        x + w, y + h));
            sb.append(String.format("G1 X%.3f Y%.3f\n",        x,     y + h));
            sb.append(String.format("G1 X%.3f Y%.3f\n",        x,     y));
            sb.append("G0 Z5\n\n");
        }

        sb.append("M05       ; apagar husillo\n");
        sb.append("G0 X0 Y0  ; volver al origen\n");
        sb.append("M30       ; fin de programa\n");
        return sb.toString();
    }

}
