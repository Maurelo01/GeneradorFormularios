package com.example.pokemonformularios.ast;

public class AccesoVariable implements Expresion
{
    private String id;
    public AccesoVariable(String id)
    {
        this.id = id;
    }

    @Override
    public Object evaluar(Entorno ent)
    {
        Simbolo sim = ent.obtenerVariable(id);
        if (sim == null)
        {
            System.err.println("Error Semántico: La variable '" + id + "' no ha sido declarada.");
            return null;
        }
        return sim.getValor();
    }
}