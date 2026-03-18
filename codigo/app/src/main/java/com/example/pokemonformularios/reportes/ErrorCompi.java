package com.example.pokemonformularios.reportes;

public class ErrorCompi
{
    private String tipo;
    private String descripcion;
    private int linea;
    private int columna;

    public ErrorCompi(String tipo, String descripcion, int linea, int columna)
    {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.linea = linea;
        this.columna = columna;
    }

    public String getTipo()
    {
        return tipo;
    }
    public String getDescripcion()
    {
        return descripcion;
    }
    public int getLinea()
    {
        return linea;
    }
    public int getColumna()
    {
        return columna;
    }
}