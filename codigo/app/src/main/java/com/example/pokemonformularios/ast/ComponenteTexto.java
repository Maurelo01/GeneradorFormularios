package com.example.pokemonformularios.ast;
import java.util.List;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import androidx.core.graphics.ColorUtils;


public class ComponenteTexto implements Instruccion
{
    private List<Atributo> atributos;

    public ComponenteTexto(List<Atributo> atributos)
    {
        this.atributos = atributos;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        String contenido = null;
        List<Atributo> listaEstilos = null;
        for (Atributo attr : atributos)
        {
            if (attr.getNombre().equals("content"))
            {
                Object val = ((Expresion) attr.getValor()).evaluar(ent);
                if (val != null)
                {
                    contenido = val.toString();
                }
            }
            else if (attr.getNombre().equals("styles"))
            {
                listaEstilos = (List<Atributo>) attr.getValor();
            }
        }
        if (contenido == null)
        {
            System.err.println("Error Semántico: El componente TEXT requiere el atributo 'content'.");
            return null;
        }
        TextView tv = null;
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            tv = new TextView(ent.getContexto());
            tv.setText(generarEmojisJava(contenido));
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setPadding(0, 8, 0, 8);
        }
        StringBuilder pkmBuilder = new StringBuilder();
        if (listaEstilos != null && !listaEstilos.isEmpty())
        {
            pkmBuilder.append("<text=-1, -1, \"").append(contenido).append("\">\n");
            pkmBuilder.append("<style>\n");
            GradientDrawable fondoYBorde = new GradientDrawable();
            boolean aplicarFondoYBorde = false;
            for (Atributo estilo : listaEstilos)
            {
                String nombreEstilo = estilo.getNombre().replace("\"", "");
                Object valorEstiloObj = estilo.getValor();
                String valorStr = valorEstiloObj != null ? valorEstiloObj.toString() : "";
                if (tv != null)
                {
                    try
                    {
                        switch (nombreEstilo)
                        {
                            case "color":
                                tv.setTextColor(parsearColores(valorStr));
                                pkmBuilder.append("<color=").append(valorStr).append("/>\n");
                                break;
                            case "background color":
                                fondoYBorde.setColor(parsearColores(valorStr));
                                aplicarFondoYBorde = true;
                                pkmBuilder.append("<background color=").append(valorStr).append("/>\n");
                                break;
                            case "font family":
                                if (valorStr.contains("MONO")) tv.setTypeface(Typeface.MONOSPACE);
                                else if (valorStr.contains("SANS_SERIF")) tv.setTypeface(Typeface.SANS_SERIF);
                                else if (valorStr.contains("CURSIVE")) tv.setTypeface(Typeface.create("cursive", Typeface.NORMAL));
                                pkmBuilder.append("<font family=").append(valorStr).append("/>\n");
                                break;
                            case "text size":
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(valorStr));
                                pkmBuilder.append("<text size=").append(valorStr).append(">\n");
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
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error aplicando estilo " + nombreEstilo + ": " + e.getMessage());
                    }
                }
            }
            if (tv != null && aplicarFondoYBorde)
            {
                tv.setBackground(fondoYBorde);
            }
            pkmBuilder.append("</style>\n</text>\n");
        }
        else
        {
            pkmBuilder.append("<text=-1, -1, \"").append(contenido).append("\"/>\n");
        }
        if (tv != null)
        {
            ent.getLayoutActual().addView(tv);
        }
        ent.getPkmBuilder().append(pkmBuilder.toString());
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

    private String generarEmojisJava(String textoOriginal)
    {
        if (textoOriginal == null) return "";
        String texto = textoOriginal;
        texto = texto.replaceAll("@\\[:\\)+\\]|@\\[:smile:\\]", "\uD83D\uDE00");
        texto = texto.replaceAll("@\\[:\\(+\\]|@\\[:sad:\\]", "\uD83E\uDD72️");
        texto = texto.replaceAll("@\\[:\\]+\\]|@\\[:serious:\\]", "\uD83D\uDE10");
        texto = texto.replaceAll("@\\[<+3+\\]|@\\[:heart:\\]", "❤\uFE0F");
        texto = texto.replaceAll("@\\[:\\^\\^:\\]|@\\[:cat:\\]", "\uD83D\uDE3A");
        texto = texto.replaceAll("@\\[:star:\\]", "⭐");
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@\\[:star[:-](\\d+):?\\]");
        java.util.regex.Matcher matcher = pattern.matcher(texto);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
        {
            int cantidad = Integer.parseInt(matcher.group(1));
            StringBuilder estrellas = new StringBuilder();
            for (int i = 0; i < cantidad; i++) estrellas.append("⭐");
            matcher.appendReplacement(sb, estrellas.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}