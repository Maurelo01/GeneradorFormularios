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
        Object valInicio = inicio.evaluar(ent);
        Object valFin = fin.evaluar(ent);
        int inicio = 1;
        int fin = 10;
        if (valInicio instanceof Double) inicio = ((Double) valInicio).intValue();
        if (valFin instanceof Double) fin = ((Double) valFin).intValue();
        return "POKEAPI:" + inicio + ":" + fin;
    }
}
