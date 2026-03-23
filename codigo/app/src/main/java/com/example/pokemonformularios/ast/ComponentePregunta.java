package com.example.pokemonformularios.ast;

import java.util.List;
import java.util.ArrayList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.core.graphics.ColorUtils;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;
import android.os.Looper;

public class ComponentePregunta implements Instruccion
{
    private String tipoPregunta;
    private List<Atributo> atributos;
    private Object respuestaCorrecta = null;
    private View vistaEntrada = null;
    private List<CheckBox> listaCheckboxes = new ArrayList<>();

    public ComponentePregunta(String tipoPregunta, List<Atributo> atributos)
    {
        this.tipoPregunta = tipoPregunta;
        this.atributos = atributos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object ejecutar(Entorno ent)
    {
        String label = "Pregunta sin título";
        List<Object> opciones = new ArrayList<>();
        List<Atributo> listaEstilos = null;
        String width = "-1";
        String height = "-1";
        if (atributos != null)
        {
            for (Atributo attr : atributos)
            {
                if (attr.getNombre().equals("label"))
                {
                    Object val = ((Expresion) attr.getValor()).evaluar(ent);
                    if (val != null) label = val.toString();
                }
                else if (attr.getNombre().equals("options"))
                {
                    opciones = (List<Object>) attr.getValor();
                }
                else if (attr.getNombre().equals("correct"))
                {
                    respuestaCorrecta = ((Expresion) attr.getValor()).evaluar(ent);
                }
                else if (attr.getNombre().equals("correct_multiple"))
                {
                    List<Object> expresiones = (List<Object>) attr.getValor();
                    List<Integer> correctasEvaluadas = new ArrayList<>();
                    for (Object exp : expresiones)
                    {
                        if (exp instanceof Expresion)
                        {
                            Object val = ((Expresion) exp).evaluar(ent);
                            if (val instanceof Double)
                            {
                                correctasEvaluadas.add(Double.valueOf(val.toString()).intValue());
                            }
                        }
                    }
                    respuestaCorrecta = correctasEvaluadas;
                }
                else if (attr.getNombre().equals("styles"))
                {
                    listaEstilos = (List<Atributo>) attr.getValor();
                }
                else if (attr.getNombre().equals("width"))
                {
                    Object w = ((Expresion) attr.getValor()).evaluar(ent);
                    if (w != null) width = w.toString();
                }
                else if (attr.getNombre().equals("height"))
                {
                    Object h = ((Expresion) attr.getValor()).evaluar(ent);
                    if (h != null) height = h.toString();
                }
            }
        }
        LinearLayout layoutPregunta = null;
        TextView tvLabel = null;
        if (ent.getContexto() != null && ent.getLayoutActual() != null)
        {
            layoutPregunta = new LinearLayout(ent.getContexto());
            layoutPregunta.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 48);
            layoutPregunta.setLayoutParams(params);
            tvLabel = new TextView(ent.getContexto());
            tvLabel.setText(generarEmojisJava(label));
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            tvLabel.setTextColor(Color.parseColor("#333333"));
            tvLabel.setPadding(0, 0, 0, 16);
            layoutPregunta.addView(tvLabel);
            boolean requiereApi = false;
            int pokeInicio = 1;
            int pokeFin = 1;
            List<String> opcionesNormis = new ArrayList<>();
            for (Object obj : opciones)
            {
                if (obj instanceof Expresion)
                {
                    Object val = ((Expresion) obj).evaluar(ent);
                    if (val != null && val.toString().startsWith("POKEAPI:"))
                    {
                        requiereApi = true;
                        String[] parts = val.toString().split(":");
                        pokeInicio = Integer.parseInt(parts[1]);
                        pokeFin = Integer.parseInt(parts[2]);
                    }
                    else
                    {
                        opcionesNormis.add(val != null ? val.toString() : "");
                    }
                }
            }
            switch (tipoPregunta)
            {
                case "OPEN":
                {
                    EditText editText = new EditText(ent.getContexto());
                    editText.setHint("Tu respuesta...");
                    layoutPregunta.addView(editText);
                    this.vistaEntrada = editText;
                    break;
                }
                case "SELECT":
                {
                    RadioGroup radioGroup = new RadioGroup(ent.getContexto());
                    if (requiereApi)
                    {
                        cargarPokemon(pokeInicio, pokeFin, radioGroup, "SELECT");
                    }
                    else
                    {
                        for (String opt : opcionesNormis)
                        {
                            RadioButton rb = new RadioButton(ent.getContexto());
                            rb.setText(generarEmojisJava(opt));
                            radioGroup.addView(rb);
                        }
                    }
                    layoutPregunta.addView(radioGroup);
                    this.vistaEntrada = radioGroup;
                    break;
                }
                case "MULTIPLE":
                {
                    if (requiereApi)
                    {
                        cargarPokemon(pokeInicio, pokeFin, layoutPregunta, "MULTIPLE");
                    }
                    else
                    {
                        for (String opt : opcionesNormis)
                        {
                            CheckBox cb = new CheckBox(ent.getContexto());
                            cb.setText(generarEmojisJava(opt));
                            layoutPregunta.addView(cb);
                            this.listaCheckboxes.add(cb);
                        }
                    }
                    break;
                }
                case "DROP":
                {
                    Spinner spinner = new Spinner(ent.getContexto());
                    if (requiereApi)
                    {
                        List<String> cargando = new ArrayList<>();
                        cargando.add("Cargando Pokémon...");
                        ArrayAdapter<String> adapterTemp = new ArrayAdapter<>(ent.getContexto(), android.R.layout.simple_spinner_item, cargando);
                        adapterTemp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapterTemp);
                        cargarPokemon(pokeInicio, pokeFin, spinner, "DROP");
                    }
                    else
                    {
                        List<String> opcionesStr = new ArrayList<>();
                        opcionesStr.add(" Selecciona ");
                        for (String opt : opcionesNormis) opcionesStr.add(generarEmojisJava(opt));
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ent.getContexto(), android.R.layout.simple_spinner_item, opcionesStr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    }
                    layoutPregunta.addView(spinner);
                    this.vistaEntrada = spinner;
                    break;
                }
            }
            boolean tieneEstilos = listaEstilos != null && !listaEstilos.isEmpty();
            if (tieneEstilos)
            {
                GradientDrawable fondoYBorde = new GradientDrawable();
                fondoYBorde.setCornerRadius(16f);
                boolean aplicarFondoYBorde = false;
                for (Atributo estilo : listaEstilos)
                {
                    String nombreEstilo = estilo.getNombre().replace("\"", "");
                    Object valorEstiloObj = estilo.getValor();
                    String valorStr = valorEstiloObj != null ? valorEstiloObj.toString() : "";
                    if (layoutPregunta != null && tvLabel != null)
                    {
                        try
                        {
                            switch (nombreEstilo)
                            {
                                case "color":
                                {
                                    tvLabel.setTextColor(parsearColores(valorStr));
                                    break;
                                }
                                case "background color":
                                {
                                    fondoYBorde.setColor(parsearColores(valorStr));
                                    aplicarFondoYBorde = true;
                                    break;
                                }
                                case "font family":
                                {
                                    if (valorStr.contains("MONO")) tvLabel.setTypeface(Typeface.MONOSPACE);
                                    else if (valorStr.contains("SANS_SERIF")) tvLabel.setTypeface(Typeface.SANS_SERIF);
                                    else if (valorStr.contains("CURSIVE")) tvLabel.setTypeface(Typeface.create("cursive", Typeface.NORMAL));
                                    break;
                                }
                                case "text size":
                                {
                                    tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(valorStr));
                                    break;
                                }
                                case "border":
                                {
                                    String limpio = valorStr.replace("(", "").replace(")", "").replace(" ", "");
                                    String[] partes = limpio.split(",");
                                    if (partes.length == 3)
                                    {
                                        int grosor = Float.valueOf(partes[0]).intValue();
                                        String tipo = partes[1];
                                        int colorBorde = parsearColores(partes[2]);

                                        if (tipo.equals("DOTTED"))
                                        {
                                            fondoYBorde.setStroke(grosor, colorBorde, 15f, 10f);
                                        }
                                        else
                                        {
                                            fondoYBorde.setStroke(grosor, colorBorde);
                                        }
                                        aplicarFondoYBorde = true;
                                    }
                                    break;
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            System.err.println("Error aplicando estilo a PREGUNTA: " + e.getMessage());
                        }
                    }
                }
                if (layoutPregunta != null && aplicarFondoYBorde)
                {
                    layoutPregunta.setBackground(fondoYBorde);
                    layoutPregunta.setPadding(32, 32, 32, 32);
                }
            }
            ent.getLayoutActual().addView(layoutPregunta);
            ent.registrarPregunta(this);
        }
        ent.registrarPreguntaPKM(tipoPregunta);
        StringBuilder strOptions = new StringBuilder("{");
        for (int i = 0; i < opciones.size(); i++)
        {
            Object exp = opciones.get(i);
            if (exp instanceof Expresion)
            {
                Object val = ((Expresion) exp).evaluar(ent);
                strOptions.append("\"").append(val != null ? val.toString() : "").append("\"");
                if (i < opciones.size() - 1) strOptions.append(", ");
            }
        }
        strOptions.append("}");
        String strCorrect = respuestaCorrecta != null ? respuestaCorrecta.toString() : "-1";
        if (tipoPregunta.equals("MULTIPLE") && respuestaCorrecta != null)
        {
            strCorrect = respuestaCorrecta.toString().replace("[", "{").replace("]", "}");
        }
        switch (tipoPregunta)
        {
            case "OPEN":
            {
                ent.getPkmBuilder().append("<open=").append(width).append(", ").append(height).append(", \"").append(label).append("\"/>\n");
                break;
            }
            case "SELECT":
            {
                ent.getPkmBuilder().append("<select=").append(width).append(", ").append(height).append(", \"").append(label).append("\", ").append(strOptions).append(", ").append(strCorrect).append("/>\n");
                break;
            }
            case "MULTIPLE":
            {
                ent.getPkmBuilder().append("<multiple=").append(width).append(", ").append(height).append(", \"").append(label).append("\", ").append(strOptions).append(", ").append(strCorrect).append("/>\n");
                break;
            }
            case "DROP":
            {
                ent.getPkmBuilder().append("<drop=").append(width).append(", ").append(height).append(", \"").append(label).append("\", ").append(strOptions).append(", ").append(strCorrect).append("/>\n");
                break;
            }
        }

        return null;
    }

    public int evaluarPuntos()
    {
        if (respuestaCorrecta == null) return 0;
        try
        {
            if (tipoPregunta.equals("SELECT") && vistaEntrada instanceof RadioGroup)
            {
                RadioGroup rg = (RadioGroup) vistaEntrada;
                int radioButtonID = rg.getCheckedRadioButtonId();
                if (radioButtonID != -1)
                {
                    View radioButton = rg.findViewById(radioButtonID);
                    int indiceSeleccionado = rg.indexOfChild(radioButton);
                    int indiceCorrecto = Double.valueOf(respuestaCorrecta.toString()).intValue();
                    if (indiceSeleccionado == indiceCorrecto) return 1;
                }
            }
            else if (tipoPregunta.equals("DROP") && vistaEntrada instanceof Spinner)
            {
                Spinner sp = (Spinner) vistaEntrada;
                int indiceSeleccionado = sp.getSelectedItemPosition();
                int indiceCorrecto = Double.valueOf(respuestaCorrecta.toString()).intValue();
                if (indiceSeleccionado - 1 == indiceCorrecto) return 1;
            }
            else if (tipoPregunta.equals("MULTIPLE"))
            {
                @SuppressWarnings("unchecked")
                List<Integer> correctas = (List<Integer>) respuestaCorrecta;
                int opcionesMarcadas = 0;
                int aciertos = 0;
                for (int i = 0; i < listaCheckboxes.size(); i++)
                {
                    if (listaCheckboxes.get(i).isChecked())
                    {
                        opcionesMarcadas++;
                        if (correctas.contains(i))
                        {
                            aciertos++;
                        }
                    }
                }
                if (aciertos == correctas.size() && opcionesMarcadas == correctas.size())
                {
                    return 1;
                }
                return 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean esCalificable()
    {
        return respuestaCorrecta != null && !tipoPregunta.equals("OPEN");
    }

    private void cargarPokemon(int inicio, int fin, View contenedor, String tipoPregunta)
    {
        new Thread(() ->
        {
            List<String> pokemones = new ArrayList<>();
            for (int i = inicio; i <= fin; i++)
            {
                try
                {
                    URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + i);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) response.append(linea);
                    reader.close();
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String nombre = jsonObject.getString("name");
                    nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
                    pokemones.add(nombre);
                }
                catch (Exception e)
                {
                    pokemones.add("Error #" + i);
                }
            }
            new Handler(Looper.getMainLooper()).post(() ->
            {
                if (tipoPregunta.equals("DROP") && contenedor instanceof Spinner)
                {
                    Spinner spinner = (Spinner) contenedor;
                    List<String> opciones = new ArrayList<>();
                    opciones.add(" Selecciona ");
                    opciones.addAll(pokemones);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, opciones);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }
                else if (tipoPregunta.equals("SELECT") && contenedor instanceof RadioGroup)
                {
                    RadioGroup rg = (RadioGroup) contenedor;
                    for (String p : pokemones)
                    {
                        RadioButton rb = new RadioButton(rg.getContext());
                        rb.setText(p);
                        rg.addView(rb);
                    }
                }
                else if (tipoPregunta.equals("MULTIPLE") && contenedor instanceof LinearLayout)
                {
                    LinearLayout ll = (LinearLayout) contenedor;
                    for (String p : pokemones)
                    {
                        CheckBox cb = new CheckBox(ll.getContext());
                        cb.setText(p);
                        ll.addView(cb);
                        listaCheckboxes.add(cb);
                    }
                }
            });
        }).start();
    }

    private int parsearColores(String colorOriginal)
    {
        String c = colorOriginal.trim().replace("\"", "").toUpperCase();
        switch (c)
        {
            case "RED": return Color.parseColor("#F80000");
            case "BLUE": return Color.parseColor("#3F48F4");
            case "GREEN": return Color.parseColor("#C6DA52");
            case "YELLOW": return Color.parseColor("#FFFF00");
            case "PURPLE": return Color.parseColor("#8800FF");
            case "SKY": return Color.parseColor("#DDF4F5");
            case "BLACK": return Color.parseColor("#000000");
            case "WHITE": return Color.parseColor("#FFFFFF");
        }
        try
        {
            if (c.startsWith("(") && c.endsWith(")"))
            {
                String[] rgb = c.replace("(", "").replace(")", "").split(",");
                return Color.rgb(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim()));
            }
            else if (c.startsWith("<") && c.endsWith(">"))
            {
                String[] hsl = c.replace("<", "").replace(">", "").split(",");
                float h = Float.parseFloat(hsl[0].trim());
                float s = Float.parseFloat(hsl[1].trim()) / 100f;
                float l = Float.parseFloat(hsl[2].trim()) / 100f;
                return ColorUtils.HSLToColor(new float[]{h, s, l});
            }
            else
            {
                return Color.parseColor(c);
            }
        }
        catch (Exception e)
        {
            return Color.BLACK;
        }
    }

    private String generarEmojisJava(String textoOriginal)
    {
        if (textoOriginal == null) return "";
        String texto = textoOriginal;
        texto = texto.replaceAll("@\\[:\\)+\\]|@\\[:smile:\\]", "\uD83D\uDE00");
        texto = texto.replaceAll("@\\[:\\(+\\]|@\\[:sad:\\]", "\uD83E\uDD72️");
        texto = texto.replaceAll("@\\[:\\]+\\]|@\\[:serious:\\]", "\uD83D\uDE10");
        texto = texto.replaceAll("@\\[<+3+\\]|@\\[:heart:\\]", "❤\uFE0F");
        texto = texto.replaceAll("@\\[:\\^\\^:\\]|@\\[:cat:\\]", "\uD83D\uDE3A");
        texto = texto.replaceAll("@\\[:star:\\]", "⭐");
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@\\[:star[:-](\\d+):?\\]");
        java.util.regex.Matcher matcher = pattern.matcher(texto);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
        {
            int cantidad = Integer.parseInt(matcher.group(1));
            StringBuilder estrellas = new StringBuilder();
            for (int i = 0; i < cantidad; i++) estrellas.append("⭐");
            matcher.appendReplacement(sb, estrellas.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}