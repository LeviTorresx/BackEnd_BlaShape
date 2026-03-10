package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SvgPreviewGenerator  v1
 * ────────────────────────────────────────────────────────────────
 * Usa viewBox normalizado (0 0 100 H) donde 100 = ancho del tablero.
 * Las coordenadas de las piezas se escalan a [0..100] antes de escribir,
 * y la altura del viewBox mantiene el ratio real del contenedor.
 */
public class SvgPreviewGenerator {

    // Paleta de colores
    private static final String[] PALETTE = {
            "#FFB300", "#00ACA1", "#AB47BC", "#78909C",
            "#00BCD4", "#EC4079", "#FF7043", "#4296FA",
            "#66BB6A", "#EF5350", "#5C6BC0", "#26A69A",
            "#FFCA28", "#8D6E63",
    };

    // -Colores UI
    private static final String C_BG     = "#1A0A2E";
    private static final String C_BOARD  = "#F8F5FF";
    private static final String C_BORDER = "#3A1860";
    private static final String C_BARB   = "#2D124E";  // fondo barra
    private static final String C_BARF   = "#00C070";  // relleno barra
    private static final String C_LBL    = "#C0A0E8";  // texto etiqueta
    private static final String C_TXT    = "#1A0A2E";  // texto piezas

    // viewBox ancho fijo = 100 unidades (el contenedor siempre ocupa 0..100)
    private static final double VW = 100.0;
    // Alto extra para la zona de etiqueta (% del alto del tablero)
    private static final double LABEL_PCT = 0.08;

    /**
     * Genera el SVG miniatura de UNA lámina.
     *
     * @param idx    indice 0-based (para "Hoja N")
     * @param sheet  piezas colocadas por el algoritmo
     * @param waste  desperdicio %
     * @param cW     ancho contenedor mm
     * @param cH     alto  contenedor mm
     * @param kerf   sangria mm
     * @param svgW   ancho SVG px
     * @param svgH   alto  SVG px
     */
    public static String generate(
            int idx, List<Item> sheet, double waste,
            double cW, double cH, double kerf,
            int svgW, int svgH) {

        double eff = 100.0 - waste;

        // Factor de escala: convierte mm reales → unidades del viewBox
        double scX = VW / cW;           // escala X
        double scY = scX;               // misma escala (sin distorsión)
        double VH  = cH * scY;          // alto del tablero en unidades viewBox
        double LH  = VH * LABEL_PCT;    // alto zona etiqueta
        double TVH = VH + LH;           // alto total del viewBox

        // Tamaños de fuente proporcionales al viewBox
        double fs   = VH * 0.045;       // nombre pieza
        double fs2  = fs * 0.72;        // dimensiones pieza
        double fsL  = VH * 0.036;       // etiqueta inferior
        double sw   = VW * 0.003;       // stroke-width piezas
        double swB  = VW * 0.004;       // stroke-width borde tablero

        // Umbral mínimo de tamaño para mostrar etiqueta (en unidades viewBox)
        double minW = VW * 0.06;
        double minH = VH * 0.06;

        StringBuilder s = new StringBuilder(4096);

        // -SVG con viewBox normalizado
        s.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\"\n" +
                        "     width=\"%d\" height=\"%d\"\n" +
                        "     viewBox=\"0 0 %.4f %.4f\"\n" +
                        "     shape-rendering=\"crispEdges\"\n" +
                        "     font-family=\"Arial,Helvetica,sans-serif\">\n\n",
                svgW, svgH, VW, TVH));

        // - Defs: patron hatch por color (piezas rotadas)
        s.append("  <defs>\n");
        double hp = VW * 0.012;  // paso del patron hatch
        for (int i = 0; i < PALETTE.length; i++) {
            s.append(String.format(Locale.US,
                    "    <pattern id=\"h%d\" patternUnits=\"userSpaceOnUse\"\n" +
                            "      width=\"%.3f\" height=\"%.3f\" patternTransform=\"rotate(45)\">\n" +
                            "      <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"%.3f\"\n" +
                            "        stroke=\"%s\" stroke-width=\"%.3f\"/>\n" +
                            "    </pattern>\n",
                    i, hp, hp, hp, lighten(PALETTE[i], 0.5f), hp * 0.4));
        }
        s.append("  </defs>\n\n");

        // -Fondo completo
        s.append(String.format(Locale.US,
                "  <rect width=\"%.4f\" height=\"%.4f\" fill=\"%s\"/>\n\n",
                VW, TVH, C_BG));

        // -Fondo del tablero
        s.append(String.format(Locale.US,
                "  <rect x=\"0\" y=\"0\" width=\"%.4f\" height=\"%.4f\"\n" +
                        "    fill=\"%s\" stroke=\"%s\" stroke-width=\"%.3f\"/>\n\n",
                VW, VH, C_BOARD, C_BORDER, sw));

        // -Piezas
        s.append("  <!-- piezas -->\n");
        for (Item it : sheet) {
            int    ci   = (it.id - 1) % PALETTE.length;
            String fill = PALETTE[ci];
            String edge = darken(fill, 0.28f);

            // Escalar coordenadas mm → viewBox
            double px = it.x          * scX;
            double py = it.y          * scY;
            double pw = it.placedW()  * scX;
            double ph = it.placedH()  * scY;
            double cx = px + pw / 2.0;
            double cy = py + ph / 2.0;

            String tip = xml(String.format(Locale.US, "ID %d%s  %.0fx%.0f mm%s",
                    it.id,
                    it.label != null ? " - " + it.label : "",
                    it.placedW(), it.placedH(),
                    it.rotated ? " [R]" : ""));

            s.append(String.format(Locale.US, "  <g><title>%s</title>\n", tip));

            // Rect relleno
            s.append(String.format(Locale.US,
                    "    <rect x=\"%.4f\" y=\"%.4f\" width=\"%.4f\" height=\"%.4f\"\n" +
                            "      fill=\"%s\" stroke=\"%s\" stroke-width=\"%.3f\"/>\n",
                    px, py, pw, ph, fill, edge, sw));

            // Hatch si rotada
            if (it.rotated) {
                s.append(String.format(Locale.US,
                        "    <rect x=\"%.4f\" y=\"%.4f\" width=\"%.4f\" height=\"%.4f\"\n" +
                                "      fill=\"url(#h%d)\" opacity=\"0.4\"/>\n",
                        px, py, pw, ph, ci));
            }

            // Etiqueta si hay espacio
            if (pw > minW && ph > minH) {
                String lbl = (it.label != null ? it.label : "#" + it.id)
                        + (it.rotated ? " [R]" : "");
                s.append(String.format(Locale.US,
                        "    <text x=\"%.4f\" y=\"%.4f\" text-anchor=\"middle\"\n" +
                                "      font-size=\"%.3f\" font-weight=\"bold\" fill=\"%s\">%s</text>\n",
                        cx, cy + fs * 0.15, fs, C_TXT, xml(lbl)));
                s.append(String.format(Locale.US,
                        "    <text x=\"%.4f\" y=\"%.4f\" text-anchor=\"middle\"\n" +
                                "      font-size=\"%.3f\" fill=\"%s\">%.0fx%.0f</text>\n",
                        cx, cy + fs * 0.15 + fs2 * 1.4, fs2, C_TXT,
                        it.placedW(), it.placedH()));
            }

            s.append("  </g>\n");
        }

        // Borde exterior encima de las piezas
        s.append(String.format(Locale.US,
                "\n  <rect x=\"0\" y=\"0\" width=\"%.4f\" height=\"%.4f\"\n" +
                        "    fill=\"none\" stroke=\"%s\" stroke-width=\"%.3f\"/>\n\n",
                VW, VH, C_BORDER, swB));

        // -Zona inferior: barra + etiqueta
        double zY    = VH;
        double barPX = VW * 0.025;
        double barW  = VW * 0.95;
        double barH  = LH * 0.30;
        double barY  = zY + LH * 0.10;

        // Fondo barra
        s.append(String.format(Locale.US,
                "  <rect x=\"%.4f\" y=\"%.4f\" width=\"%.4f\" height=\"%.4f\"\n" +
                        "    fill=\"%s\" rx=\"%.3f\"/>\n",
                barPX, barY, barW, barH, C_BARB, barH * 0.45));

        // Relleno barra (eficiencia)
        s.append(String.format(Locale.US,
                "  <rect x=\"%.4f\" y=\"%.4f\" width=\"%.4f\" height=\"%.4f\"\n" +
                        "    fill=\"%s\" rx=\"%.3f\"/>\n",
                barPX, barY, barW * eff / 100.0, barH, C_BARF, barH * 0.45));

        // Etiqueta "Hoja N  |  XX%"
        s.append(String.format(Locale.US,
                "  <text x=\"%.4f\" y=\"%.4f\" text-anchor=\"middle\"\n" +
                        "    font-size=\"%.3f\" fill=\"%s\">Hoja %d  |  %.0f%%</text>\n",
                VW / 2.0, zY + LH * 0.80, fsL, C_LBL, idx + 1, eff));

        s.append("</svg>\n");
        return s.toString();
    }

    /**
     * Genera SVGs para TODAS las laminas.
     * Tamaño por defecto: 600 x proporcional al ratio del contenedor.
     */
    public static List<String> generateAll(
            List<List<Item>> sheets, List<Double> wastes,
            double cW, double cH, double kerf) {
        int w = 600;
        int h = (int)(w * cH / cW * (1 + LABEL_PCT)) + 1;
        return generateAll(sheets, wastes, cW, cH, kerf, w, h);
    }

    /** Genera SVGs para TODAS las laminas con tamaño personalizado. */
    public static List<String> generateAll(
            List<List<Item>> sheets, List<Double> wastes,
            double cW, double cH, double kerf,
            int svgW, int svgH) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < sheets.size(); i++)
            out.add(generate(i, sheets.get(i), wastes.get(i), cW, cH, kerf, svgW, svgH));
        return out;
    }

    // -Utilidades de color

    private static String lighten(String hex, float r) {
        int[] c = rgb(hex);
        return hx((int)(c[0]+(255-c[0])*r), (int)(c[1]+(255-c[1])*r), (int)(c[2]+(255-c[2])*r));
    }
    private static String darken(String hex, float r) {
        int[] c = rgb(hex);
        return hx((int)(c[0]*(1-r)), (int)(c[1]*(1-r)), (int)(c[2]*(1-r)));
    }
    private static int[] rgb(String h) {
        return new int[]{
                Integer.parseInt(h.substring(1,3),16),
                Integer.parseInt(h.substring(3,5),16),
                Integer.parseInt(h.substring(5,7),16)};
    }
    private static String hx(int r,int g,int b) {
        return String.format(Locale.US, "#%02X%02X%02X", r, g, b);
    }
    private static String xml(String s) {
        if (s==null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }
}
