package com.example.pokemonformularios.ast;
import java.util.List;

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
        return null;
    }
}