package com.example.pokemonformularios.ast;

public class FuncionPkmAPI implements Expresion
{
    private Expresion tipo;
    private Expresion inicio;
    private Expresion fin;

    public FuncionPkmAPI(Expresion tipo, Expresion inicio, Expresion fin)
    {
        this.tipo = tipo;
        this.inicio = inicio;
        this.fin = fin;
    }

    @Override
    public Object evaluar(Entorno ent)
    {
        try
        {
            Object valInicio = inicio != null ? inicio.evaluar(ent) : 1.0;
            Object valFin = fin != null ? fin.evaluar(ent) : 1.0;

            int numInicio = 1;
            int numFin = 10;

            if (valInicio instanceof Double) numInicio = ((Double) valInicio).intValue();
            if (valFin instanceof Double) numFin = ((Double) valFin).intValue();

            return "POKEAPI:" + numInicio + ":" + numFin;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "POKEAPI:1:1";
        }
    }
}
