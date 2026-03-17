package com.example.pokemonformularios.ast;
import java.util.List;

public class Bloque implements Instruccion
{
    private List<Instruccion> instrucciones;

    public Bloque(List<Instruccion> instrucciones)
    {
        this.instrucciones = instrucciones;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        for (Instruccion instr : instrucciones)
        {
            if (instr != null)
            {
                instr.ejecutar(ent);
            }
        }
        return null;
    }
}