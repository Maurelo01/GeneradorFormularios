package com.example.pokemonformularios.ast;

public class Valor implements Expresion
{
    private Object valor;
    private String tipo;

    public Valor(Object valor, String tipo)
    {
        this.valor = valor;
        this.tipo = tipo;
    }
    @Override
    public Object evaluar(Entorno ent)
    {
        return this.valor;
    }
    public String getTipo()
    {
        return tipo;
    }
}