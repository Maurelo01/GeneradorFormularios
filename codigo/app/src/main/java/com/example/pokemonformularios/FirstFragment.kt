package com.example.pokemonformularios

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.*
import android.text.style.ForegroundColorSpan
import android.content.ContentValues
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.ColorUtils
import com.example.pokemonformularios.analizador.LexerFormulario
import com.example.pokemonformularios.analizador.ParserFormulario
import com.example.pokemonformularios.ast.Entorno
import com.example.pokemonformularios.ast.Instruccion
import com.example.pokemonformularios.reportes.ErrorCompi
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.util.Date
import java.util.Locale

class FirstFragment : Fragment()
{
    private var listaErroresActuales: List<ErrorCompi> = emptyList()

    private val abrirArchivoForm = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri ->
        uri?.let {
            try
            {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val contenido = inputStream?.bufferedReader().use { reader -> reader?.readText() }
                if (contenido != null)
                {
                    val entradaCodigo = view?.findViewById<EditText>(R.id.entradaCodigo)
                    val contenedorFormulario = view?.findViewById<LinearLayout>(R.id.contenedorFormulario)
                    entradaCodigo?.setText(contenido)
                    if (contenedorFormulario != null && (uri.toString().lowercase().endsWith(".pkm") || contenido.contains("<table=") || contenido.contains("<section=") || contenido.contains("<text=") || contenido.contains("<open=") || contenido.contains("<select=") || contenido.contains("<multiple=") || contenido.contains("<drop=")))
                    {
                        contenedorFormulario.removeAllViews()
                        mostrarArchivoPKM(contenido, contenedorFormulario)
                    }
                    Toast.makeText(requireContext(), "Archivo cargado exitosamente", Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al leer el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        val btnExportarPKM = view.findViewById<Button>(R.id.btnExportarPKM)
        val btnCargarArchivo = view.findViewById<Button>(R.id.btnCargarArchivo)
        val btnIrAContestar = view.findViewById<Button>(R.id.btnIrAContestar)
        btnErrores.isEnabled = false
        btnExportarPKM.visibility = View.GONE
        entradaCodigo.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?)
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
            mostrarSelectorDeColor(entradaCodigo)
        }

        btnCargarArchivo.setOnClickListener{
            abrirArchivoForm.launch("*/*")
        }

        btnCompilar.setOnClickListener{
            procesarCompilacion(entradaCodigo, contenedorFormulario, btnErrores, btnExportarPKM)
        }

        btnErrores.setOnClickListener{
            mostrarTablaErrores()
        }

        btnIrAContestar.setOnClickListener{
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun colorearSintaxis(editable: Editable?)
    {
        if (editable == null) return
        val texto = editable.toString()
        val spans = editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        for (span in spans)
        {
            editable.removeSpan(span)
        }
        fun aplicarColor(regex: String, colorHex: String)
        {
            val patron = Pattern.compile(regex)
            val matcher = patron.matcher(texto)
            while (matcher.find())
            {
                editable.setSpan(ForegroundColorSpan(Color.parseColor(colorHex)), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        aplicarColor("(\\$.*)|(/\\*[\\s\\S]*?\\*/)", "#808080")
        aplicarColor("[+\\-*/^%]", "#88DC65")
        aplicarColor("\"[^\"]*\"", "#FF8000")
        aplicarColor("\\b\\d+(\\.\\d+)?\\b", "#A3D5EB")
        aplicarColor("\\b(SECTION|TABLE|TEXT|OPEN_QUESTION|DROP_QUESTION|SELECT_QUESTION|MULTIPLE_QUESTION|IF|ELSE|WHILE|DO|FOR|in|number|string|special)\\b", "#8800FF")
        aplicarColor("[{}\\[\\]()]", "#0000FF")
        aplicarColor("@\\[.*?\\]", "#FFDE21")
    }

    private fun generarEmojis(textoOriginal: String): String
    {
        var texto = textoOriginal
        texto = texto.replace(Regex("@\\[:\\)+\\]|@\\[:smile:\\]"), "\uD83D\uDE00")
        texto = texto.replace(Regex("@\\[:\\(+\\]|@\\[:sad:\\]"), "\uD83E\uDD72")
        texto = texto.replace(Regex("@\\[:\\]+\\]|@\\[:serious:\\]"), "\uD83D\uDE10")
        texto = texto.replace(Regex("@\\[<+3+\\]|@\\[:heart:\\]"), "❤\uFE0F")
        texto = texto.replace(Regex("@\\[:\\^\\^:\\]|@\\[:cat:\\]"), "\uD83D\uDE3A")
        texto = texto.replace(Regex("@\\[:star:\\]"), "⭐")
        val regexStar = Regex("@\\[:star[:-](\\d+):?\\]")
        texto = regexStar.replace(texto)
        { matchResult ->
            val cantidad = matchResult.groupValues[1].toIntOrNull() ?: 1
            "⭐".repeat(cantidad)
        }
        return texto
    }

    private fun procesarCompilacion(entradaCodigo: EditText, contenedorFormulario: LinearLayout, btnErrores: Button, btnExportarPKM: Button)
    {
        contenedorFormulario.removeAllViews()
        listaErroresActuales = emptyList()
        btnErrores.isEnabled = false
        btnExportarPKM.visibility = View.GONE
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
                btnExportarPKM.visibility = View.VISIBLE
                btnExportarPKM.setOnClickListener{
                    mostrarDialogoGuardarPKM(entornoGlobal)
                }
                val btnEnviar = Button(requireContext())
                btnEnviar.text = "Enviar Formulario"
                btnEnviar.setBackgroundColor(Color.parseColor("#29446F"))
                btnEnviar.setTextColor(Color.WHITE)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 32, 0, 32)
                btnEnviar.layoutParams = params
                btnEnviar.setOnClickListener{
                    val preguntas = entornoGlobal.preguntasFormulario
                    var totalPuntos = 0
                    var totalCalificables = 0
                    for (pregunta in preguntas)
                    {
                        if (pregunta.esCalificable())
                        {
                            totalCalificables++
                            totalPuntos += pregunta.evaluarPuntos()
                        }
                    }
                    if (totalCalificables > 0)
                    {
                        AlertDialog.Builder(requireContext()).setTitle("Formulario Enviado").setMessage("Tu puntuación es:\n$totalPuntos de $totalCalificables correctas.").setPositiveButton("Aceptar", null).show()
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "Formulario enviado exitosamente.", Toast.LENGTH_LONG).show()
                    }
                }
                contenedorFormulario.addView(btnEnviar)
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

    private fun guardarArchivoPKM(entorno: Entorno, nombreArchivo: String, autor: String, descripcion: String)
    {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val metadatos = """
            ###
            ###
            Author: $autor
            Fecha: $fecha
            Hora: $hora
            Description: $descripcion
            Total de Secciones: ${entorno.totalSecciones}
            Total de Preguntas: ${entorno.totalPreguntas}
            Abiertas: ${entorno.abiertas}
            Desplegables: ${entorno.desplegables}
            Selección: ${entorno.seleccion}
            Múltiples: ${entorno.multiples}
            
        """.trimIndent()
        val contenidoFinal = metadatos + "\n" + entorno.pkmBuilder.toString()
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply{
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null)
                {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(contenidoFinal.toByteArray())
                    }
                    Toast.makeText(requireContext(), "Guardado en Descargas:\n$nombreArchivo", Toast.LENGTH_LONG).show()
                    println(" Archivo .PKM Generado \n$contenidoFinal\n ")
                }
                else
                {
                    Toast.makeText(requireContext(), "Error al crear el archivo en Descargas", Toast.LENGTH_SHORT).show()
                }

            }
            else
            {
                val carpetaDestino = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(carpetaDestino, nombreArchivo)
                file.writeText(contenidoFinal)
                Toast.makeText(requireContext(), "Guardado en Descargas:\n$nombreArchivo", Toast.LENGTH_LONG).show()
                println(" Archivo .PKM Generado \n$contenidoFinal\n ")
            }
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al guardar .PKM: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarDialogoGuardarPKM(entorno: Entorno)
    {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(64, 32, 64, 16)
        val inputNombreArchivo = EditText(requireContext())
        inputNombreArchivo.hint = "Nombre del archivo .PKM"
        val inputAutor = EditText(requireContext())
        inputAutor.hint = "Nombre del Autor"
        val inputDesc = EditText(requireContext())
        inputDesc.hint = "Descripción del formulario"
        layout.addView(inputNombreArchivo)
        layout.addView(inputAutor)
        layout.addView(inputDesc)
        AlertDialog.Builder(requireContext()).setTitle("Guardar Archivo .PKM").setMessage("Ingresa los metadatos del formulario:").setView(layout).setPositiveButton("Guardar")
        { _, _ ->
            val nombreIngresado = inputNombreArchivo.text.toString().trim()
            val nombreDefinitivo = if (nombreIngresado.isNotBlank())
            {
                if (nombreIngresado.endsWith(".pkm")) nombreIngresado else "$nombreIngresado.pkm"
            }
            else
            {
                val fechaHoraArchivo = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault()).format(Date())
                "pokemon_form_$fechaHoraArchivo.pkm"
            }
            val autor = inputAutor.text.toString().ifBlank{ "Autor Desconocido" }
            val descripcion = inputDesc.text.toString().ifBlank{ "Sin descripción" }
            guardarArchivoPKM(entorno, nombreDefinitivo, autor, descripcion)
        }.setNegativeButton("Cancelar", null).show()
    }

    private fun mostrarSelectorDeColor(entradaCodigo: EditText)
    {
        val colores = arrayOf(
            "#F80000", "#C6DA52", "#3F48F4", "#FFFF00",
            "#8800FF", "#FFA500", "#DDF4F5", "#CF3476",
            "#000000", "#FFFFFF", "#808080", "#FFB5C0"
        )
        val nombresBase = mapOf(
            "#F80000" to "RED", "#C6DA52" to "GREEN", "#3F48F4" to "BLUE", "#FFFF00" to "YELLOW",
            "#8800FF" to "PURPLE", "#DDF4F5" to "SKY", "#000000" to "BLACK", "#FFFFFF" to "WHITE"
        )
        val contenedor = LinearLayout(requireContext()).apply{
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER
        }
        val gridLayout = GridLayout(requireContext()).apply{
            columnCount = 4
            rowCount = 3
        }
        val dialog = AlertDialog.Builder(requireContext()).setTitle("Elige un Color").setView(contenedor).setNegativeButton("Cancelar", null).create()
        for (colorHex in colores)
        {
            val colorView = View(requireContext()).apply{
                val size = 130
                val params = GridLayout.LayoutParams().apply{
                    width = size
                    height = size
                    setMargins(16, 16, 16, 16)
                }
                layoutParams = params
                val border = GradientDrawable()
                val hexLimpio = colorHex.trim()
                var colorInt = Color.GRAY
                try
                {
                    colorInt = Color.parseColor(hexLimpio)
                    border.setColor(colorInt)
                }
                catch (e: Exception)
                {
                    border.setColor(colorInt)
                }
                border.setStroke(3, Color.DKGRAY)
                background = border
                setOnClickListener{
                    val r = Color.red(colorInt)
                    val g = Color.green(colorInt)
                    val b = Color.blue(colorInt)
                    val hsl = FloatArray(3)
                    ColorUtils.colorToHSL(colorInt, hsl)
                    val h = hsl[0].toInt()
                    val s = (hsl[1] * 100).toInt()
                    val l = (hsl[2] * 100).toInt()
                    val opcionesList = mutableListOf("Hexadecimal: $hexLimpio", "RGB: ($r,$g,$b)", "HSL: <$h,$s,$l>")
                    if (nombresBase.containsKey(hexLimpio))
                    {
                        opcionesList.add("Predefinido: ${nombresBase[hexLimpio]}")
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle("¿En qué formato deseas insertarlo?").setItems(opcionesList.toTypedArray())
                        { _, which ->
                            val formatoElegido = opcionesList[which]
                            val valorAInsertar = formatoElegido.split(": ")[1]
                            val cursor = entradaCodigo.selectionStart
                            if (cursor >= 0)
                            {
                                entradaCodigo.text.insert(cursor, valorAInsertar)
                            }
                            else
                            {
                                entradaCodigo.append(valorAInsertar)
                            }
                            dialog.dismiss()
                        }.show()
                }
            }
            gridLayout.addView(colorView)
        }
        contenedor.addView(gridLayout)
        dialog.show()
    }

    private fun mostrarArchivoPKM(codigoPKM: String, contenedorFormulario: LinearLayout)
    {
        contenedorFormulario.removeAllViews()
        var seccionActual: LinearLayout? = null
        var tablaActual: TableLayout? = null
        var filaActual: TableRow? = null
        var ultimoElementoCreado: View? = null
        var ultimoLabel: TextView? = null
        val fondosEstilos = mutableMapOf<View, GradientDrawable>()
        val lineas = codigoPKM.split("\n")
        val regexBackgroundColor = Regex("<background color=(.*?)/>")
        val regexColor = Regex("<color=(.*?)/>")
        val regexFont = Regex("<font family=(.*?)/>")
        val regexSize = Regex("<text size=(.*?)>")
        val regexBorder = Regex("""<border=\((.*?),(.*?),(.*?)\)/>""")

        for (linea in lineas)
        {
            val l = linea.trim()
            val esMeta = listOf(
                "Author:", "Fecha:", "Hora:", "Description:",
                "Total de Secciones:", "Total de Preguntas:",
                "Abiertas:", "Desplegables:", "Selección:", "Múltiples:"
            ).any { l.startsWith(it) }

            if (l.isEmpty() || l.startsWith("###") || l == "<style>" || l == "</style>"
                || l == "<content>" || l == "</content>" || esMeta)
            {
                continue
            }

            if (l == "</section>") { seccionActual = null; continue }
            if (l == "</table>") { tablaActual = null; continue }
            if (l == "</line>") { filaActual = null; continue }
            if (l.startsWith("</")) { continue }

            if (regexBackgroundColor.matches(l))
            {
                val colorStr = regexBackgroundColor.find(l)!!.groupValues[1]
                val gd = fondosEstilos.getOrPut(ultimoElementoCreado!!) { GradientDrawable() }
                gd.setColor(parsearColores(colorStr))
                ultimoElementoCreado?.background = gd
                continue
            }
            else if (regexBorder.matches(l))
            {
                val match = regexBorder.find(l)!!
                val grosor = match.groupValues[1].trim().toFloat().toInt()
                val tipo = match.groupValues[2].trim()
                val colorBorde = parsearColores(match.groupValues[3].trim())
                val gd = fondosEstilos.getOrPut(ultimoElementoCreado!!) { GradientDrawable() }
                if (tipo == "DOTTED") gd.setStroke(grosor, colorBorde, 15f, 10f)
                else gd.setStroke(grosor, colorBorde)
                ultimoElementoCreado?.background = gd
                continue
            }
            else if (regexColor.matches(l))
            {
                val colorStr = regexColor.find(l)!!.groupValues[1]
                ultimoLabel?.setTextColor(parsearColores(colorStr))
                continue
            }
            else if (regexFont.matches(l))
            {
                val fontStr = regexFont.find(l)!!.groupValues[1]
                if (fontStr.contains("MONO")) ultimoLabel?.typeface = Typeface.MONOSPACE
                else if (fontStr.contains("SANS_SERIF")) ultimoLabel?.typeface = Typeface.SANS_SERIF
                else if (fontStr.contains("CURSIVE")) ultimoLabel?.typeface = Typeface.create("cursive", Typeface.NORMAL)
                continue
            }
            else if (regexSize.matches(l))
            {
                val sizeStr = regexSize.find(l)!!.groupValues[1]
                ultimoLabel?.textSize = sizeStr.toFloat()
                continue
            }

            val obtenerDestino: () -> LinearLayout = lambda@
            {
                if (filaActual != null)
                {
                    val celda = LinearLayout(requireContext())
                    celda.orientation = LinearLayout.VERTICAL
                    val border = GradientDrawable()
                    border.setColor(Color.WHITE)
                    border.setStroke(2, Color.parseColor("#CCCCCC"))
                    celda.background = border
                    celda.setPadding(16, 16, 16, 16)
                    val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    params.setMargins(4, 4, 4, 4)
                    celda.layoutParams = params

                    filaActual!!.addView(celda)
                    return@lambda celda
                }
                else
                {
                    return@lambda seccionActual ?: contenedorFormulario
                }
            }

            when {
                l.startsWith("<section=") ->
                {
                    seccionActual = LinearLayout(requireContext()).apply{
                        orientation = LinearLayout.VERTICAL
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 16, 0, 48)
                        layoutParams = params
                        setPadding(32, 32, 32, 32)
                    }
                    contenedorFormulario.addView(seccionActual)
                    ultimoElementoCreado = seccionActual
                    ultimoLabel = null
                }
                l.startsWith("<table=") ->
                {
                    tablaActual = TableLayout(requireContext()).apply{
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 16, 0, 16)
                        layoutParams = params
                        setPadding(16, 16, 16, 16)
                    }
                    (seccionActual ?: contenedorFormulario).addView(tablaActual)
                    ultimoElementoCreado = tablaActual
                    ultimoLabel = null
                }
                l == "<line>" ->
                {
                    filaActual = TableRow(requireContext())
                    tablaActual?.addView(filaActual)
                }
                l.startsWith("<text=") ->
                {
                    val contenido = extraerTexto(l)
                    val tv = TextView(requireContext()).apply{
                        text = generarEmojis(contenido)
                        textSize = 16f
                        setTextColor(Color.parseColor("#333333"))
                        setPadding(16, 16, 16, 16)
                    }
                    obtenerDestino().addView(tv)
                    ultimoElementoCreado = tv
                    ultimoLabel = tv
                }
                l.startsWith("<open=") || l.startsWith("<select=") || l.startsWith("<multiple=") || l.startsWith("<drop=") ->
                {
                    val labelLimpio = extraerTexto(l)
                    val layoutPregunta = LinearLayout(requireContext()).apply{
                        orientation = LinearLayout.VERTICAL
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 16, 0, 32)
                        layoutParams = params
                        setPadding(24, 24, 24, 24)
                    }
                    val tvLabel = TextView(requireContext()).apply {
                        textSize = 18f
                        setTextColor(Color.parseColor("#111111"))
                        setPadding(0, 0, 0, 16)
                        text = generarEmojis(labelLimpio)
                    }
                    layoutPregunta.addView(tvLabel)

                    when {
                        l.startsWith("<open=") ->
                        {
                            val et = EditText(requireContext())
                            et.hint = "Tu respuesta..."
                            layoutPregunta.addView(et)
                        }
                        l.startsWith("<select=") ->
                        {
                            val opciones = extraerOpciones(l)
                            val correctaStr = extraerRespuestaCorrecta(l)
                            val rg = RadioGroup(requireContext())
                            layoutPregunta.addView(rg)
                            if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                            {
                                cargarPokeApiLectura(rg, opciones[0], "SELECT")
                            }
                            else
                            {
                                opciones.forEach{ opt ->
                                    val rb = RadioButton(requireContext())
                                    rb.text = generarEmojis(opt)
                                    rg.addView(rb)
                                }
                            }
                        }
                        l.startsWith("<multiple=") ->
                        {
                            val opciones = extraerOpciones(l)
                            val listaCheckboxes = mutableListOf<CheckBox>()
                            if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                            {
                                cargarPokeApiLectura(layoutPregunta, opciones[0], "MULTIPLE", listaCheckboxes)
                            }
                            else
                            {
                                opciones.forEach { opt ->
                                    val cb = CheckBox(requireContext())
                                    cb.text = generarEmojis(opt)
                                    layoutPregunta.addView(cb)
                                    listaCheckboxes.add(cb)
                                }
                            }
                        }
                        l.startsWith("<drop=") ->
                        {
                            val opciones = extraerOpciones(l)
                            val spinner = Spinner(requireContext())
                            if (opciones.size == 1 && opciones[0].startsWith("POKEAPI:"))
                            {
                                cargarPokeApiLectura(spinner, opciones[0], "DROP")
                            }
                            else
                            {
                                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(" Selecciona ") + opciones.map { generarEmojis(it) })
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinner.adapter = adapter
                            }
                            layoutPregunta.addView(spinner)
                        }
                    }
                    obtenerDestino().addView(layoutPregunta)
                    ultimoElementoCreado = layoutPregunta
                    ultimoLabel = tvLabel
                }
            }
        }

        val btnEnviar = Button(requireContext()).apply{
            text = "ENVIAR FORMULARIO"
            setBackgroundColor(Color.parseColor("#29446F"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply{
                setMargins(0, 48, 0, 32)
            }
        }
        contenedorFormulario.addView(btnEnviar)
        Toast.makeText(requireContext(), "Formulario cargado desde PKM", Toast.LENGTH_SHORT).show()
    }

    private fun extraerTexto(linea: String): String
    {
        val inicio = linea.indexOf("\"")
        if (inicio == -1) return ""
        val fin = linea.indexOf("\"", inicio + 1)
        if (fin == -1) return ""
        return linea.substring(inicio + 1, fin)
    }

    private fun extraerOpciones(linea: String): List<String>
    {
        val inicio = linea.indexOf("{")
        val fin = linea.indexOf("}")
        if (inicio == -1 || fin == -1) return emptyList()
        val contenido = linea.substring(inicio + 1, fin)
        return contenido.split(",").map { it.replace("\"", "").trim() }
    }

    private fun extraerRespuestaCorrecta(linea: String): String
    {
        val lastComma = linea.lastIndexOf(",")
        if (lastComma == -1) return ""
        var finStr = linea.indexOf("/>")
        if (finStr == -1) finStr = linea.indexOf(">")
        if (finStr == -1 || finStr <= lastComma) return ""
        return linea.substring(lastComma + 1, finStr).trim()
    }

    private fun parsearColores(colorOriginal: String): Int
    {
        val c = colorOriginal.trim().replace("\"", "").uppercase()
        return when (c)
        {
            "RED" -> Color.parseColor("#F80000")
            "BLUE" -> Color.parseColor("#3F48F4")
            "GREEN" -> Color.parseColor("#C6DA52")
            "YELLOW" -> Color.parseColor("#FFFF00")
            "PURPLE" -> Color.parseColor("#8800FF")
            "SKY" -> Color.parseColor("#DDF4F5")
            "BLACK" -> Color.parseColor("#000000")
            "WHITE" -> Color.parseColor("#FFFFFF")
            else ->
            {
                try
                {
                    if (c.startsWith("(") && c.endsWith(")"))
                    {
                        val rgb = c.replace("(", "").replace(")", "").split(",")
                        Color.rgb(rgb[0].trim().toInt(), rgb[1].trim().toInt(), rgb[2].trim().toInt())
                    }
                    else if (c.startsWith("<") && c.endsWith(">"))
                    {
                        val hsl = c.replace("<", "").replace(">", "").split(",")
                        ColorUtils.HSLToColor(floatArrayOf(hsl[0].trim().toFloat(), hsl[1].trim().toFloat() / 100f, hsl[2].trim().toFloat() / 100f))
                    }
                    else
                    {
                        Color.parseColor(c)
                    }
                }
                catch (e: Exception)
                {
                    Color.TRANSPARENT
                }
            }
        }
    }

    private fun cargarPokeApiLectura(vistaContenedor: View, comando: String, tipoPregunta: String, listaCheckboxes: MutableList<CheckBox>? = null)
    {
        val partes = comando.split(":")
        val inicio = partes[1].toInt()
        val fin = partes[2].toInt()
        val tvCargando = TextView(vistaContenedor.context).apply{
            text = "Cargando Pokemon..."
            setTextColor(Color.GRAY)
        }
        if (vistaContenedor is ViewGroup && vistaContenedor !is Spinner)
        {
            vistaContenedor.addView(tvCargando)
        }
        Thread{
            val descargados = mutableListOf<String>()
            for (i in inicio..fin)
            {
                try
                {
                    val url = java.net.URL("https://pokeapi.co/api/v2/pokemon/$i")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val nombre = org.json.JSONObject(response).getString("name")
                    descargados.add(nombre.replaceFirstChar { it.uppercase() })
                }
                catch (_: Exception)
                {
                    descargados.add("Error #$i")
                }
            }
            activity?.runOnUiThread{
                if (vistaContenedor is ViewGroup && vistaContenedor !is Spinner)
                {
                    vistaContenedor.removeView(tvCargando)
                }
                when {
                    tipoPregunta == "DROP" && vistaContenedor is Spinner ->
                    {
                        val adapter = ArrayAdapter(vistaContenedor.context, android.R.layout.simple_spinner_item, listOf(" Selecciona ") + descargados)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        vistaContenedor.adapter = adapter
                    }
                    tipoPregunta == "SELECT" && vistaContenedor is RadioGroup ->
                    {
                        descargados.forEach{ rb ->
                            vistaContenedor.addView(RadioButton(vistaContenedor.context).apply { text = rb })
                        }
                    }
                    tipoPregunta == "MULTIPLE" && vistaContenedor is LinearLayout ->
                    {
                        descargados.forEach{ cb ->
                            val check = CheckBox(vistaContenedor.context).apply { text = cb }
                            vistaContenedor.addView(check)
                            listaCheckboxes?.add(check)
                        }
                    }
                }
            }
        }.start()
    }
}