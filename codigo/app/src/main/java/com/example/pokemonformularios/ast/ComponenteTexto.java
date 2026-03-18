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
        if (ent.getContexto() != null && ent.getLayoutActual() != null) {
            TextView textViewAndroid = new TextView(ent.getContexto());
            textViewAndroid.setText(contenido);
            textViewAndroid.setTextColor(Color.BLACK);
            textViewAndroid.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textViewAndroid.setPadding(0, 8, 0, 8);
            ent.getLayoutActual().addView(textViewAndroid);
        }
        else
        {
            System.out.println(" Generando Componente de TEXTO: " + contenido);
        }
        return null;
    }
}