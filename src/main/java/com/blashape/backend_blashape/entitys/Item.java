package com.blashape.backend_blashape.entitys;

public class Item {
    public final int    id;
    public final String label;
    public final double width;
    public final double height;

    // Resultado tras empaquetar
    public double  x       = 0;
    public double  y       = 0;
    public boolean rotated = false;

    public Item(int id, String label, double width, double height) {
        this.id     = id;
        this.label  = label;
        this.width  = width;
        this.height = height;
    }

    /** Ancho físico según orientación colocada */
    public double placedW() { return rotated ? height : width;  }
    /** Alto físico según orientación colocada */
    public double placedH() { return rotated ? width  : height; }

    /** Copia con posición y rotación ya asignadas */
    public Item placed(double x, double y, boolean rotated) {
        Item copy    = new Item(id, label, width, height);
        copy.x       = x;
        copy.y       = y;
        copy.rotated = rotated;
        return copy;
    }

    @Override
    public String toString() {
        return String.format("Item{id=%d, label='%s', x=%.0f, y=%.0f, w=%.0f, h=%.0f, rot=%b}",
                id, label, x, y, placedW(), placedH(), rotated);
    }
}
