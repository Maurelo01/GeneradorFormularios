package com.example.pokemonformularios

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.pokemonformularios.analizador.LexerFormulario
import com.example.pokemonformularios.analizador.ParserFormulario
import com.example.pokemonformularios.ast.Entorno
import com.example.pokemonformularios.ast.Instruccion
import com.example.pokemonformularios.reportes.ErrorCompi
import java.io.StringReader

class FirstFragment : Fragment()
{
    private var listaErroresActuales: List<ErrorCompi> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val btnCompilar = view.findViewById<Button>(R.id.btnCompilar)
        val btnErrores = view.findViewById<Button>(R.id.btnErrores)
        val entradaCodigo = view.findViewById<EditText>(R.id.entradaCodigo)
        val contenedorFormulario = view.findViewById<LinearLayout>(R.id.contenedorFormulario)
        val btnPlantilla = view.findViewById<Button>(R.id.btnPlantilla)
        val btnColor = view.findViewById<Button>(R.id.btnColor)
        btnErrores.isEnabled = false

        entradaCodigo.addTextChangedListener(object : android.text.TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?)
            {
                entradaCodigo.removeTextChangedListener(this)
                colorearSintaxis(s)
                entradaCodigo.addTextChangedListener(this)
            }
        })

        btnPlantilla.setOnClickListener{
            val codigoPlantilla = """
                number titulo = 1
                WHILE (titulo <= 3) {
                    SECTION [
                        elements: {
                            TEXT [ content: "Texto generado numero: " + titulo ]
                        }
                    ]
                    titulo = titulo + 1
                }
            """.trimIndent()
            entradaCodigo.setText(codigoPlantilla)
            Toast.makeText(requireContext(), "Plantilla cargada", Toast.LENGTH_SHORT).show()
        }

        btnColor.setOnClickListener{
            val cursor = entradaCodigo.selectionStart
            val textoAInsertar = "\"#5A438F\""
            if (cursor >= 0)
            {
                entradaCodigo.text.insert(cursor, textoAInsertar)
            }
            else
            {
                entradaCodigo.append(textoAInsertar)
            }
        }

        btnCompilar.setOnClickListener{
            procesarCompilacion(entradaCodigo, contenedorFormulario, btnErrores)
        }

        btnErrores.setOnClickListener{
            mostrarTablaErrores()
        }
    }

    private fun colorearSintaxis(editable: android.text.Editable?)
    {
        if (editable == null) return
        val texto = editable.toString()
        val spans = editable.getSpans(0, editable.length, android.text.style.ForegroundColorSpan::class.java)
        for (span in spans)
        {
            editable.removeSpan(span)
        }
        val patronComentarios = java.util.regex.Pattern.compile("(\\$.*)|(/\\*[\\s\\S]*?\\*/)")
        var matcher = patronComentarios.matcher(texto)
        while (matcher.find())
        {
            editable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#808080")), matcher.start(), matcher.end(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val patronReservadas = java.util.regex.Pattern.compile("\\b(SECTION|TABLE|TEXT|OPEN_QUESTION|IF|ELSE|WHILE|FOR|number|string)\\b")
        matcher = patronReservadas.matcher(texto)
        while (matcher.find())
        {
            editable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#000080")), matcher.start(), matcher.end(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val patronAtributos = java.util.regex.Pattern.compile("\\b(content|width|height|elements|label)\\b\\s*:")
        matcher = patronAtributos.matcher(texto)
        while (matcher.find())
        {
            editable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#F27C07")), matcher.start(), matcher.end() - 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val patronCadenas = java.util.regex.Pattern.compile("\"[^\"\\n]*\"")
        matcher = patronCadenas.matcher(texto)
        while (matcher.find())
        {
            editable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#00913F")), matcher.start(), matcher.end(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val patronNumeros = java.util.regex.Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
        matcher = patronNumeros.matcher(texto)
        while (matcher.find())
        {
            editable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#E5BE01")), matcher.start(), matcher.end(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun procesarCompilacion(entradaCodigo: EditText, contenedorFormulario: LinearLayout, btnErrores: Button)
    {
        contenedorFormulario.removeAllViews()
        listaErroresActuales = emptyList()
        btnErrores.isEnabled = false
        val codigoStr = entradaCodigo.text.toString()
        if (codigoStr.isBlank()) return
        var lexer: LexerFormulario? = null
        var parser: ParserFormulario? = null
        try
        {
            lexer = LexerFormulario(StringReader(codigoStr))
            parser = ParserFormulario(lexer)
            val sym = parser.parse()
            val todosLosErrores = mutableListOf<ErrorCompi>()
            todosLosErrores.addAll(lexer.erroresLexicos)
            todosLosErrores.addAll(parser.erroresSintacticos)
            if (todosLosErrores.isEmpty())
            {
                val raizAST = sym.value as? Instruccion
                val entornoGlobal = Entorno(null, requireContext(), contenedorFormulario)
                raizAST?.ejecutar(entornoGlobal)
                if (entornoGlobal.erroresSemanticos.isNotEmpty())
                {
                    todosLosErrores.addAll(entornoGlobal.erroresSemanticos)
                    listaErroresActuales = todosLosErrores
                    btnErrores.isEnabled = true
                    Toast.makeText(requireContext(), "Formulario generado, pero con Errores Semánticos", Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(requireContext(), "¡Formulario Generado con éxito!", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                listaErroresActuales = todosLosErrores
                btnErrores.isEnabled = true
                Toast.makeText(requireContext(), "Hay ${todosLosErrores.size} error(es). Revisa la tabla.", Toast.LENGTH_LONG).show()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            val erroresRescatados = mutableListOf<ErrorCompi>()
            lexer?.erroresLexicos?.let { erroresRescatados.addAll(it) }
            parser?.erroresSintacticos?.let { erroresRescatados.addAll(it) }

            if (erroresRescatados.isNotEmpty())
            {
                listaErroresActuales = erroresRescatados
                btnErrores.isEnabled = true
                Toast.makeText(requireContext(), "Caida Sintáctico. Se rescataron ${erroresRescatados.size} errores.", Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(requireContext(), "Error grave al compilar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarTablaErrores()
    {
        val dialogView = layoutInflater.inflate(R.layout.dialog_errores, null)
        val tablaErrores = dialogView.findViewById<TableLayout>(R.id.tablaErrores)
        for (error in listaErroresActuales)
        {
            val fila = crearFilaError(error)
            tablaErrores.addView(fila)
        }
        AlertDialog.Builder(requireContext()).setView(dialogView).setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun crearFilaError(error: ErrorCompi): TableRow
    {
        val fila = TableRow(requireContext())
        fila.setPadding(0, 16, 0, 16)
        val tvTipo = TextView(requireContext()).apply{
            text = error.tipo
            setPadding(0, 0, 16, 0)
        }
        val tvDesc = TextView(requireContext()).apply{
            text = error.descripcion
            setPadding(0, 0, 16, 0)
            maxWidth = 400
        }
        val tvLinea = TextView(requireContext()).apply{
            text = error.linea.toString()
            setPadding(0, 0, 16, 0)
        }
        val tvColumna = TextView(requireContext()).apply{
            text = error.columna.toString()
        }
        fila.addView(tvTipo)
        fila.addView(tvDesc)
        fila.addView(tvLinea)
        fila.addView(tvColumna)

        return fila
    }
}