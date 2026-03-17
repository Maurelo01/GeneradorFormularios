package com.example.pokemonformularios.ast;
import java.util.List;

public class ComponentePregunta implements Instruccion
{
    private String tipoPregunta;
    private List<Atributo> atributos;

    public ComponentePregunta(String tipoPregunta, List<Atributo> atributos)
    {
        this.tipoPregunta = tipoPregunta;
        this.atributos = atributos;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        String labelStr = "Sin etiqueta";
        boolean tieneOptions = false;
        for (Atributo attr : atributos)
        {
            if (attr.getNombre().equals("label"))
            {
                Object val = ((Expresion) attr.getValor()).evaluar(ent);
                if (val != null) labelStr = val.toString();
            }
            if (attr.getNombre().equals("options"))
            {
                tieneOptions = true;
            }
        }
        System.out.println(" Dibujando PREGUNTA [" + tipoPregunta + "]: " + labelStr);
        if (tipoPregunta.equals("DROP") || tipoPregunta.equals("SELECT") || tipoPregunta.equals("MULTIPLE"))
        {
            if (!tieneOptions)
            {
                System.err.println("Error Semántico: La pregunta '" + labelStr + "' requiere el atributo 'options'.");
            }
        }
        return null;
    }
}