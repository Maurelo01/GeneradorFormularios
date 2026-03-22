package com.example.pokemonformularios.ast;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

public class ComponenteTabla implements Instruccion
{
    private List<Atributo> atributos;

    public ComponenteTabla(List<Atributo> atributos)
    {
        this.atributos = atributos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object ejecutar(Entorno ent)
    {
        System.out.println(" Dibujando TABLA...");
        List<Object> filas = new ArrayList<>();
        for (Atributo attr : atributos)
        {
            if (attr.getNombre().equals("elements_table"))
            {
                filas = (List<Object>) attr.getValor();
            }
        }
        if (filas.isEmpty()) return null;
        System.out.println(" La tabla tiene " + filas.size() + " filas.");
        TableLayout tableLayout = null;
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            tableLayout = new TableLayout(ent.getContexto());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 32);
            tableLayout.setLayoutParams(params);
            tableLayout.setStretchAllColumns(true);
            ent.getLayoutActual().addView(tableLayout);
        }
        ent.getPkmBuilder().append("<table=-1, -1>\n");
        LinearLayout layoutOriginal = ent.getLayoutActual();
        for (int i = 0; i < filas.size(); i++)
        {
            System.out.println(" Fila " + (i + 1) + " ");
            Object filaObj = filas.get(i);
            if (filaObj instanceof List)
            {
                List<Instruccion> componentesFila = (List<Instruccion>) filaObj;
                TableRow tableRow = null;
                if (tableLayout != null)
                {
                    tableRow = new TableRow(ent.getContexto());
                    tableLayout.addView(tableRow);
                    ent.setLayoutActual(tableRow);
                }
                ent.getPkmBuilder().append("<line>\n");
                for (Instruccion componente : componentesFila)
                {
                    if (componente != null)
                    {
                        if (tableRow != null)
                        {
                            LinearLayout celda = new LinearLayout(ent.getContexto());
                            celda.setOrientation(LinearLayout.VERTICAL);
                            android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                            border.setColor(android.graphics.Color.WHITE);
                            border.setStroke(3, Color.BLACK);
                            celda.setBackground(border);
                            celda.setPadding(16, 16, 16, 16);
                            TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            cellParams.setMargins(4, 4, 4, 4);
                            celda.setLayoutParams(cellParams);
                            tableRow.addView(celda);
                            ent.setLayoutActual(celda);
                        }
                        componente.ejecutar(ent);
                    }
                }
                ent.getPkmBuilder().append("</line>\n");
            }
        }
        ent.getPkmBuilder().append("</table>\n");
        if (layoutOriginal != null)
        {
            ent.setLayoutActual(layoutOriginal);
        }
        System.out.println(" Fin de TABLA");
        return null;
    }
}