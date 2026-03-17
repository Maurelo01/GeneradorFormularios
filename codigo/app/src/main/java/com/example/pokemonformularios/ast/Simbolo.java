package com.example.pokemonformularios.ast;

public class Simbolo
{
    private String identificador;
    private String tipo;
    private Object valor;

    public Simbolo(String identificador, String tipo, Object valor)
    {
        this.identificador = identificador;
        this.tipo = tipo;
        this.valor = valor;
    }
    public String getIdentificador()
    {
        return identificador;
    }
    public String getTipo()
    {
        return tipo;
    }
    public Object getValor()
    {
        return valor;
    }
    public void setValor(Object valor)
    {
        this.valor = valor;
    }
}