package com.example.pokemonformularios.ast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class ComponenteSeccion implements Instruccion
{
    private List<Atributo> atributos;

    public ComponenteSeccion(List<Atributo> atributos)
    {
        this.atributos = atributos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object ejecutar(Entorno ent)
    {
        System.out.println("Dibujando SECCION...");
        ent.registrarSeccionPKM();
        List<Instruccion> elementosHijos = new ArrayList<>();
        List<Atributo> listaEstilos = null;
        String orientation = "VERTICAL";
        String width = "-1", height = "-1", pointX = "-1", pointY = "-1";
        for (Atributo attr : atributos)
        {
            String nombreAttr = attr.getNombre().replace("\"", "").trim();
            switch (nombreAttr)
            {
                case "elements":
                    elementosHijos = (List<Instruccion>) attr.getValor();
                    break;
                case "styles":
                    listaEstilos = (List<Atributo>) attr.getValor();
                    break;
                case "orientation":
                    orientation = attr.getValor().toString();
                    break;
                case "width":
                    Object w = ((Expresion) attr.getValor()).evaluar(ent);
                    if (w != null) width = w.toString();
                    break;
                case "height":
                    Object h = ((Expresion) attr.getValor()).evaluar(ent);
                    if (h != null) height = h.toString();
                    break;
                case "pointX":
                    Object px = ((Expresion) attr.getValor()).evaluar(ent);
                    if (px != null) pointX = px.toString();
                    break;
                case "pointY":
                    Object py = ((Expresion) attr.getValor()).evaluar(ent);
                    if (py != null) pointY = py.toString();
                    break;
            }
        }
        LinearLayout seccionLayout = null;
        LinearLayout layoutOriginal = null;
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            seccionLayout = new LinearLayout(ent.getContexto());
            layoutOriginal = ent.getLayoutActual();
            if (orientation.equals("HORIZONTAL"))
            {
                seccionLayout.setOrientation(LinearLayout.HORIZONTAL);
            }
            else
            {
                seccionLayout.setOrientation(LinearLayout.VERTICAL);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 24, 16, 32);
            seccionLayout.setLayoutParams(params);
            seccionLayout.setPadding(40, 40, 40, 40);
        }
        StringBuilder pkmBuilder = new StringBuilder();
        pkmBuilder.append("<section=").append(width).append(", ").append(height).append(", ").append(pointX).append(", ").append(pointY).append(", ").append(orientation).append(">\n");
        if (listaEstilos != null && !listaEstilos.isEmpty())
        {
            pkmBuilder.append("<style>\n");
            GradientDrawable fondoYBorde = new GradientDrawable();
            boolean aplicarFondoYBorde = false;
            for (Atributo estilo : listaEstilos)
            {
                String nombreEstilo = estilo.getNombre().replace("\"", "");
                Object valorEstiloObj = estilo.getValor();
                String valorStr = valorEstiloObj != null ? valorEstiloObj.toString() : "";
                if (seccionLayout != null)
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
                        System.err.println("Error aplicando estilo a SECTION: " + e.getMessage());
                    }
                }
            }
            if (seccionLayout != null && aplicarFondoYBorde)
            {
                seccionLayout.setBackground(fondoYBorde);
            }
            pkmBuilder.append("</style>\n");
        }
        if (seccionLayout != null)
        {
            layoutOriginal.addView(seccionLayout);
            ent.setLayoutActual(seccionLayout);
        }
        pkmBuilder.append("<content>\n");
        ent.getPkmBuilder().append(pkmBuilder.toString());
        if (!elementosHijos.isEmpty())
        {
            System.out.println(" Contiene " + elementosHijos.size() + " subelementos. Procesando...");
            for (Instruccion hijo : elementosHijos)
            {
                if (hijo != null)
                {
                    hijo.ejecutar(ent);
                }
            }
        }
        ent.getPkmBuilder().append("</content>\n</section>\n");
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