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
            String descripcion = "La variable '" + id + "' no ha sido declarada.";
            ent.reportarErrorSemantico(descripcion);
            System.err.println("Error Semántico: " + descripcion);
            return null;
        }
        return sim.getValor();
    }
}