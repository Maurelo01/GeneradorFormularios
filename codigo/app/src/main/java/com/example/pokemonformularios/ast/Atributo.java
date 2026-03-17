package com.example.pokemonformularios.ast;

public class Atributo
{
    private String nombre;
    private Object valor;

    public Atributo(String nombre, Object valor)
    {
        this.nombre = nombre;
        this.valor = valor;
    }
    public String getNombre()
    {
        return nombre;
    }
    public Object getValor()
    {
        return valor;
    }
}