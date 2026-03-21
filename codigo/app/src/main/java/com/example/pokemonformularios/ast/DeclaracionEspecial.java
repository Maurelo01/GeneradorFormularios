package com.example.pokemonformularios.ast;

public class DeclaracionEspecial implements Instruccion
{
    private String id;
    private Instruccion componente;

    public DeclaracionEspecial(String id, Instruccion componente)
    {
        this.id = id;
        this.componente = componente;
    }

    @Override
    public Object ejecutar(Entorno ent)
    {
        ent.agregarVariable(id, "special", componente);
        return null;
    }
}