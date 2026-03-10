package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.Item;
import java.util.*;

/**
 * GuillotineAlgorithm v3 (Post-Migración)
 * ─────────────────────────────────────────────────────────────────
 * Nuevas mejoras sobre v2 (Pre-Migración):
 *  #4 Múltiples órdenes de entrada
 *      Corre el algoritmo con 5 estrategias de ordenamiento y devuelve
 *      la que produce menor desperdicio total. Costo: O(5 * N log N).
 *  #1 Lookahead K=2
 *      Cuando quedan ≤ LOOKAHEAD_THRESHOLD piezas, en lugar de colocar
 *      greedy, evalúa los TOP_N mejores encajes para la pieza actual y
 *      simula K pasos hacia adelante con cada uno. Elige el encaje cuya
 *      proyección futura deja menos desperdicio.
 */

public class GuillotineAlgorithm {

    // -Espacio libre
    private record Space(double x, double y, double w, double h) {
        double area() { return w * h; }
    }

    // -Resultado público
    public record PackingResult(List<List<Item>> sheets, List<Double> wastePercents) {}

    // -Parámetros del lookahead
    private static final int LOOKAHEAD_K         = 2;   // pasos de simulación
    private static final int LOOKAHEAD_THRESHOLD = 12;  // activar cuando quedan ≤N piezas
    private static final int TOP_N               = 3;   // candidatos a evaluar

    private static final double MIN_SPACE = 10.0;       // mm mínimos para un espacio útil

    // ════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ════════════════════════════════════════════════════════
    public static PackingResult pack(double cW, double cH,
                                     List<Item> itemsIn, double kerf) {
        // #4 — Probar 5 estrategias de ordenamiento, quedarse con la mejor
        List<Comparator<Item>> orders = List.of(
                // S1: área descendente (baseline original)
                Comparator.comparingDouble((Item i) -> -(i.width * i.height)),
                // S2: lado más largo descendente
                Comparator.comparingDouble((Item i) -> -Math.max(i.width, i.height)),
                // S3: lado más corto descendente (favorece piezas cuadradas)
                Comparator.comparingDouble((Item i) -> -Math.min(i.width, i.height)),
                // S4: ratio W/H descendente (piezas más anchas primero)
                Comparator.comparingDouble((Item i) -> -(double) i.width / i.height),
                // S5: perímetro descendente
                Comparator.comparingDouble((Item i) -> -(i.width + i.height))
        );

        PackingResult best = null;
        for (Comparator<Item> order : orders) {
            List<Item> sorted = new ArrayList<>(itemsIn);
            sorted.sort(order);
            PackingResult r = packOrdered(cW, cH, sorted, kerf);
            if (best == null || totalWastePct(r) < totalWastePct(best)) {
                best = r;
            }
        }
        return best;
    }

    // ════════════════════════════════════════════════════════
    // ALGORITMO PRINCIPAL (una pasada con orden dado)
    // ════════════════════════════════════════════════════════
    private static PackingResult packOrdered(double cW, double cH,
                                             List<Item> itemsIn, double kerf) {
        List<List<Item>> sheets = new ArrayList<>();
        List<Double>     wastes = new ArrayList<>();
        List<Item>    remaining = new ArrayList<>(itemsIn);

        while (!remaining.isEmpty()) {
            List<Space> spaces  = new ArrayList<>();
            spaces.add(new Space(0, 0, cW, cH));

            List<Item> placed   = new ArrayList<>();
            List<Item> notFit   = new ArrayList<>();
            double     usedArea = 0;

            for (int idx = 0; idx < remaining.size(); idx++) {
                Item item = remaining.get(idx);

                Placement p;
                int piecesLeft = remaining.size() - idx;

                if (piecesLeft <= LOOKAHEAD_THRESHOLD) {
                    // #1 — Lookahead: evalúa TOP_N candidatos simulando K pasos
                    p = choosePlacementLookahead(
                            item, spaces, remaining, idx, kerf, cW, cH);
                } else {
                    // Greedy estándar
                    p = choosePlacementGreedy(item, spaces, kerf);
                }

                if (p == null) { notFit.add(item); continue; }

                // Colocar
                Space sp = spaces.get(p.spaceIdx);
                double iw = p.rotated ? item.height : item.width;
                double ih = p.rotated ? item.width  : item.height;

                placed.add(item.placed(sp.x(), sp.y(), p.rotated));
                usedArea += (iw + kerf) * (ih + kerf);

                // Split guillotine
                spaces.remove(p.spaceIdx);
                addSplitSpaces(spaces, sp, iw, ih, kerf);

                spaces.sort(Comparator.comparingDouble(Space::y)
                        .thenComparingDouble(Space::x));
                spaces = pruneSpaces(spaces);
                spaces = mergeSpaces(spaces);
            }

            double wastePct = (cW * cH - usedArea) / (cW * cH) * 100.0;
            sheets.add(placed);
            wastes.add(wastePct);
            remaining = notFit;
        }

        return new PackingResult(sheets, wastes);
    }

    // ════════════════════════════════════════════════════════
    // GREEDY — elige el mejor encaje para UNA pieza
    // ════════════════════════════════════════════════════════
    private static Placement choosePlacementGreedy(Item item,
                                                   List<Space> spaces,
                                                   double kerf) {
        int     bestIdx   = -1;
        boolean bestRot   = false;
        double  bestScore = Double.MAX_VALUE;

        double iw = item.width  + kerf;
        double ih = item.height + kerf;

        for (int i = 0; i < spaces.size(); i++) {
            Space sp = spaces.get(i);

            if (iw <= sp.w() && ih <= sp.h()) {
                double s = score(iw, ih, sp, false);
                if (s < bestScore) { bestScore = s; bestIdx = i; bestRot = false; }
            }
            if (ih <= sp.w() && iw <= sp.h()) {
                double s = score(ih, iw, sp, true);
                // Rotar solo si mejora ≥5 % (evita rotaciones innecesarias)
                if (s < bestScore * 0.95) { bestScore = s; bestIdx = i; bestRot = true; }
            }
        }

        return bestIdx == -1 ? null : new Placement(bestIdx, bestRot, bestScore);
    }

    // ════════════════════════════════════════════════════════
    // #1 LOOKAHEAD K=2
    // ════════════════════════════════════════════════════════
    private static Placement choosePlacementLookahead(
            Item item, List<Space> spaces,
            List<Item> remaining, int currentIdx,
            double kerf, double cW, double cH) {

        // Reunir todos los encajes válidos para esta pieza
        List<Placement> candidates = getAllPlacements(item, spaces, kerf);
        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);

        // Ordenar por score greedy y tomar los TOP_N mejores
        candidates.sort(Comparator.comparingDouble(p -> p.score));
        int take = Math.min(TOP_N, candidates.size());

        Placement bestP      = candidates.get(0);
        double    bestFuture = Double.MAX_VALUE;

        for (int ci = 0; ci < take; ci++) {
            Placement p = candidates.get(ci);

            // Simular esta colocación
            List<Space> simSpaces = new ArrayList<>(spaces);
            Space sp  = simSpaces.get(p.spaceIdx);
            double iw = p.rotated ? item.height : item.width;
            double ih = p.rotated ? item.width  : item.height;

            simSpaces.remove(p.spaceIdx);
            addSplitSpaces(simSpaces, sp, iw, ih, kerf);
            simSpaces = pruneSpaces(mergeSpaces(simSpaces));

            // Simular K pasos greedy hacia adelante
            double futureWaste = simulateAhead(
                    simSpaces, remaining, currentIdx + 1, kerf, cW, cH, LOOKAHEAD_K);

            if (futureWaste < bestFuture) {
                bestFuture = futureWaste;
                bestP      = p;
            }
        }

        return bestP;
    }

    /**
     * Simula `depth` pasos greedy y devuelve el área libre normalizada.
     * Menor = mejor (menos desperdicio proyectado).
     */
    private static double simulateAhead(List<Space> spaces, List<Item> remaining,
                                        int fromIdx, double kerf,
                                        double cW, double cH, int depth) {
        if (depth == 0 || fromIdx >= remaining.size()) {
            double free = spaces.stream().mapToDouble(Space::area).sum();
            return free / (cW * cH);
        }

        List<Space> sim = new ArrayList<>(spaces);
        double penalty  = 0;

        int limit = Math.min(fromIdx + depth, remaining.size());
        for (int i = fromIdx; i < limit; i++) {
            Placement p = choosePlacementGreedy(remaining.get(i), sim, kerf);
            if (p == null) {
                penalty += 0.4; // pieza que no cabe → penalizar
                continue;
            }
            Space sp  = sim.get(p.spaceIdx);
            double iw = p.rotated ? remaining.get(i).height : remaining.get(i).width;
            double ih = p.rotated ? remaining.get(i).width  : remaining.get(i).height;
            sim.remove(p.spaceIdx);
            addSplitSpaces(sim, sp, iw, ih, kerf);
            sim = pruneSpaces(mergeSpaces(sim));
        }

        double free = sim.stream().mapToDouble(Space::area).sum();
        return penalty + free / (cW * cH);
    }

    /** Devuelve todos los encajes válidos (normal + rotado) para una pieza. */
    private static List<Placement> getAllPlacements(Item item,
                                                    List<Space> spaces,
                                                    double kerf) {
        List<Placement> result = new ArrayList<>();
        double iw = item.width  + kerf;
        double ih = item.height + kerf;

        for (int i = 0; i < spaces.size(); i++) {
            Space sp = spaces.get(i);
            if (iw <= sp.w() && ih <= sp.h())
                result.add(new Placement(i, false, score(iw, ih, sp, false)));
            if (ih <= sp.w() && iw <= sp.h() && item.width != item.height)
                result.add(new Placement(i, true,  score(ih, iw, sp, true)));
        }
        return result;
    }

    // ════════════════════════════════════════════════════════
    // SPLIT GUILLOTINE (igual que v2)
    // ════════════════════════════════════════════════════════
    private static void addSplitSpaces(List<Space> spaces, Space sp,
                                       double iw, double ih, double kerf) {
        double lw = sp.w() - iw - kerf;
        double lh = sp.h() - ih - kerf;

        Space first, second;
        if (iw > ih * 1.5) {
            first  = new Space(sp.x(),          sp.y() + ih + kerf, sp.w(), lh);
            second = new Space(sp.x() + iw + kerf, sp.y(),           lw,   ih + kerf);
        } else if (ih > iw * 1.5) {
            first  = new Space(sp.x() + iw + kerf, sp.y(),           lw,   sp.h());
            second = new Space(sp.x(),          sp.y() + ih + kerf, iw + kerf, lh);
        } else {
            if (lh >= lw) {
                first  = new Space(sp.x(),          sp.y() + ih + kerf, sp.w(), lh);
                second = new Space(sp.x() + iw + kerf, sp.y(),           lw,   ih + kerf);
            } else {
                first  = new Space(sp.x() + iw + kerf, sp.y(),           lw,   sp.h());
                second = new Space(sp.x(),          sp.y() + ih + kerf, iw + kerf, lh);
            }
        }

        if (first.w()  >= MIN_SPACE && first.h()  >= MIN_SPACE) spaces.add(first);
        if (second.w() >= MIN_SPACE && second.h() >= MIN_SPACE) spaces.add(second);
    }

    // ════════════════════════════════════════════════════════
    // SCORE DE ENCAJE
    // ════════════════════════════════════════════════════════
    private static double score(double iw, double ih, Space sp, boolean rotated) {
        double areaLeft  = sp.area() - iw * ih;
        double lw        = sp.w() - iw;
        double lh        = sp.h() - ih;
        double narrowPen = (lw > 0 && lw < 20 ? 1000 : 0)
                + (lh > 0 && lh < 20 ? 1000 : 0);
        double ratioPen  = Math.abs(iw / ih - sp.w() / sp.h()) * 1000;
        double rotPen    = (rotated && iw / ih > 1.8) ? areaLeft * 0.5 : 0;
        return areaLeft + narrowPen + ratioPen + rotPen;
    }

    // ════════════════════════════════════════════════════════
    // GESTIÓN DE ESPACIOS (sin cambios respecto a v1)
    // ════════════════════════════════════════════════════════
    private static List<Space> pruneSpaces(List<Space> spaces) {
        List<Space> result = new ArrayList<>();
        for (int i = 0; i < spaces.size(); i++) {
            Space s = spaces.get(i);
            boolean dominated = false;
            for (int j = 0; j < spaces.size(); j++) {
                if (i == j) continue;
                Space o = spaces.get(j);
                if (s.x() >= o.x() && s.y() >= o.y()
                        && s.x() + s.w() <= o.x() + o.w()
                        && s.y() + s.h() <= o.y() + o.h()) {
                    dominated = true; break;
                }
            }
            if (!dominated) result.add(s);
        }
        return result;
    }

    private static List<Space> mergeSpaces(List<Space> spaces) {
        boolean changed = true;
        while (changed) {
            changed = false;
            outer:
            for (int i = 0; i < spaces.size(); i++) {
                for (int j = i + 1; j < spaces.size(); j++) {
                    Space a = spaces.get(i), b = spaces.get(j);
                    if (a.x() == b.x() && a.w() == b.w()) {
                        if (a.y() + a.h() == b.y()) {
                            spaces.set(i, new Space(a.x(), a.y(), a.w(), a.h() + b.h()));
                            spaces.remove(j); changed = true; break outer;
                        }
                        if (b.y() + b.h() == a.y()) {
                            spaces.set(i, new Space(b.x(), b.y(), b.w(), a.h() + b.h()));
                            spaces.remove(j); changed = true; break outer;
                        }
                    }
                    if (a.y() == b.y() && a.h() == b.h()) {
                        if (a.x() + a.w() == b.x()) {
                            spaces.set(i, new Space(a.x(), a.y(), a.w() + b.w(), a.h()));
                            spaces.remove(j); changed = true; break outer;
                        }
                        if (b.x() + b.w() == a.x()) {
                            spaces.set(i, new Space(b.x(), b.y(), a.w() + b.w(), a.h()));
                            spaces.remove(j); changed = true; break outer;
                        }
                    }
                }
            }
        }
        return spaces;
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    /** Suma de porcentajes de desperdicio — métrica para comparar estrategias. */
    private static double totalWastePct(PackingResult r) {
        return r.wastePercents().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Tupla interna: índice de espacio + orientación + score. */
    private record Placement(int spaceIdx, boolean rotated, double score) {}
}
