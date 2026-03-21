package com.example.pokemonformularios.ast;
import java.util.List;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;

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
        }
        if (contenido == null)
        {
            System.err.println("Error Semántico: El componente TEXT requiere el atributo 'content'.");
            return null;
        }
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            TextView tv = new TextView(ent.getContexto());
            tv.setText(generarEmojisJava(contenido));
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setPadding(0, 8, 0, 8);
            ent.getLayoutActual().addView(tv);
        }
        else
        {
            System.out.println(" Generando Componente de TEXTO: " + contenido);
        }
        ent.getPkmBuilder().append("<text=-1, -1, \"").append(contenido).append("\"/>\n");
        return null;
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