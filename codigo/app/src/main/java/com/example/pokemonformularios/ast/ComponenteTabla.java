package com.example.pokemonformularios.ast;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import androidx.core.graphics.ColorUtils;

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
        List<Atributo> listaEstilos = null;
        String width = "-1", height = "-1";
        for (Atributo attr : atributos)
        {
            switch (attr.getNombre())
            {
                case "elements_table":
                    filas = (List<Object>) attr.getValor();
                    break;
                case "styles":
                    listaEstilos = (List<Atributo>) attr.getValor();
                    break;
                case "width":
                    Object w = ((Expresion) attr.getValor()).evaluar(ent);
                    if (w != null) width = w.toString();
                    break;
                case "height":
                    Object h = ((Expresion) attr.getValor()).evaluar(ent);
                    if (h != null) height = h.toString();
                    break;
            }
        }

        if (filas.isEmpty()) return null;
        TableLayout tableLayout = null;
        LinearLayout layoutOriginal = null;
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            tableLayout = new TableLayout(ent.getContexto());
            layoutOriginal = ent.getLayoutActual();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 32);
            tableLayout.setLayoutParams(params);
            tableLayout.setStretchAllColumns(true);
            tableLayout.setPadding(16, 16, 16, 16);
        }
        StringBuilder pkmBuilder = new StringBuilder();
        pkmBuilder.append("<table=").append(width).append(", ").append(height).append(">\n");
        if (listaEstilos != null && !listaEstilos.isEmpty())
        {
            pkmBuilder.append("<style>\n");
            GradientDrawable fondoYBorde = new GradientDrawable();
            fondoYBorde.setCornerRadius(16f);
            boolean aplicarFondoYBorde = false;
            for (Atributo estilo : listaEstilos)
            {
                String nombreEstilo = estilo.getNombre().replace("\"", "");
                Object valorEstiloObj = estilo.getValor();
                String valorStr = valorEstiloObj != null ? valorEstiloObj.toString() : "";
                if (tableLayout != null)
                {
                    try
                    {
                        switch (nombreEstilo)
                        {
                            case "background color":
                                fondoYBorde.setColor(parsearColores(valorStr));
                                aplicarFondoYBorde = true;
                                pkmBuilder.append("<background color=").append(valorStr).append("/>\n");
                                break;
                            case "border":
                                String limpio = valorStr.replace("(", "").replace(")", "").replace(" ", "");
                                String[] partes = limpio.split(",");
                                if (partes.length == 3)
                                {
                                    int grosor = Float.valueOf(partes[0]).intValue();
                                    String tipo = partes[1];
                                    int colorBorde = parsearColores(partes[2]);
                                    if (tipo.equals("DOTTED"))
                                    {
                                        fondoYBorde.setStroke(grosor, colorBorde, 15f, 10f);
                                    }
                                    else
                                    {
                                        fondoYBorde.setStroke(grosor, colorBorde);
                                    }
                                    aplicarFondoYBorde = true;
                                }
                                pkmBuilder.append("<border=").append(valorStr).append("/>\n");
                                break;
                            case "color":
                                pkmBuilder.append("<color=").append(valorStr).append("/>\n");
                                break;
                            case "font family":
                                pkmBuilder.append("<font family=").append(valorStr).append("/>\n");
                                break;
                            case "text size":
                                pkmBuilder.append("<text size=").append(valorStr).append(">\n");
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error aplicando estilo a TABLA: " + e.getMessage());
                    }
                }
            }
            if (tableLayout != null && aplicarFondoYBorde)
            {
                tableLayout.setBackground(fondoYBorde);
            }
            pkmBuilder.append("</style>\n");
        }
        if (tableLayout != null)
        {
            layoutOriginal.addView(tableLayout);
        }

        ent.getPkmBuilder().append(pkmBuilder.toString());
        for (int i = 0; i < filas.size(); i++)
        {
            Object filaObj = filas.get(i);
            if (filaObj instanceof List)
            {
                List<Instruccion> componentesFila = (List<Instruccion>) filaObj;
                TableRow tableRow = null;
                if (tableLayout != null)
                {
                    tableRow = new TableRow(ent.getContexto());
                    tableLayout.addView(tableRow);
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
                            android.graphics.drawable.GradientDrawable borderCelda = new android.graphics.drawable.GradientDrawable();
                            borderCelda.setColor(android.graphics.Color.WHITE);
                            borderCelda.setStroke(2, android.graphics.Color.parseColor("#CCCCCC"));
                            celda.setBackground(borderCelda);
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
        return null;
    }

    private int parsearColores(String colorOriginal)
    {
        String c = colorOriginal.trim().replace("\"", "").toUpperCase();
        switch (c)
        {
            case "RED": return Color.parseColor("#F80000");
            case "BLUE": return Color.parseColor("#3F48F4");
            case "GREEN": return Color.parseColor("#C6DA52");
            case "YELLOW": return Color.parseColor("#FFFF00");
            case "PURPLE": return Color.parseColor("#8800FF");
            case "SKY": return Color.parseColor("#DDF4F5");
            case "BLACK": return Color.parseColor("#000000");
            case "WHITE": return Color.parseColor("#FFFFFF");
        }
        try
        {
            if (c.startsWith("(") && c.endsWith(")"))
            {
                String[] rgb = c.replace("(", "").replace(")", "").split(",");
                return Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim()));
            }
            else if (c.startsWith("<") && c.endsWith(">"))
            {
                String[] hsl = c.replace("<", "").replace(">", "").split(",");
                float h = Float.parseFloat(hsl[0].trim());
                float s = Float.parseFloat(hsl[1].trim()) / 100f;
                float l = Float.parseFloat(hsl[2].trim()) / 100f;
                return ColorUtils.HSLToColor(new float[]{h, s, l});
            }
            else
            {
                return Color.parseColor(c);
            }
        }
        catch (Exception e)
        {
            return Color.BLACK;
        }
    }
}