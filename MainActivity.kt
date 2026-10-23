package com.pedrin.ialivre

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val bg = Color.rgb(11, 16, 32)
    private val panel = Color.rgb(23, 32, 51)
    private val cyan = Color.rgb(56, 189, 248)
    private val white = Color.rgb(241, 245, 249)
    private val muted = Color.rgb(148, 163, 184)
    private lateinit var transcript: LinearLayout
    private lateinit var input: EditText
    private lateinit var send: Button
    private lateinit var status: TextView
    private val executor = Executors.newSingleThreadExecutor()
    private val history = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
        addMessage("IA Livre", "Oi! Sou sua IA. Escreva uma pergunta para começar. Preciso de internet para responder.")
        status.text = "Pronta para conversar"
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = radius }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(10))
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(14))
        }
        val logo = TextView(this).apply {
            text = "IA"; textSize = 20f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(8, 47, 73)); gravity = Gravity.CENTER
            background = rounded(cyan, dp(14).toFloat())
        }
        header.addView(logo, LinearLayout.LayoutParams(dp(48), dp(48)))
        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        titles.addView(TextView(this).apply {
            text = "IA Livre"; textSize = 21f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white)
        })
        titles.addView(TextView(this).apply {
            text = "Assistente de IA"; textSize = 12f; setTextColor(muted)
        })
        header.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        status = TextView(this).apply { textSize = 11f; setTextColor(cyan); gravity = Gravity.END }
        header.addView(status)
        root.addView(header)

        val divider = View(this).apply { setBackgroundColor(Color.rgb(41, 50, 68)) }
        root.addView(divider, LinearLayout.LayoutParams(-1, dp(1)))

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        transcript = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(16))
        }
        scroll.addView(transcript)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.rgb(17, 24, 39), dp(16).toFloat())
            setPadding(dp(8), dp(5), dp(5), dp(5))
        }
        input = EditText(this).apply {
            hint = "Pergunte qualquer coisa…"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 16f
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            minHeight = dp(48)
            maxLines = 4
            imeOptions = EditorInfo.IME_ACTION_SEND
            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
            }
        }
        composer.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
        send = Button(this).apply {
            text = "Enviar"; textSize = 14f; isAllCaps = false
            setTextColor(Color.rgb(8, 47, 73))
            background = rounded(cyan, dp(12).toFloat())
            setOnClickListener { sendMessage() }
        }
        composer.addView(send, LinearLayout.LayoutParams(-2, dp(46)))
        val cparams = LinearLayout.LayoutParams(-1, -2)
        cparams.topMargin = dp(10)
        root.addView(composer, cparams)

        val footer = TextView(this).apply {
            text = "Requer internet • Respostas podem conter erros"
            textSize = 11f; setTextColor(muted); gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun addMessage(who: String, text: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(if (who == "Você") Color.rgb(7, 89, 133) else panel, dp(14).toFloat())
            setPadding(dp(13), dp(10), dp(13), dp(10))
        }
        card.addView(TextView(this).apply {
            this.text = who; textSize = 12f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (who == "Você") Color.WHITE else cyan)
        })
        card.addView(TextView(this).apply {
            this.text = text; textSize = 15f; setTextColor(white)
            setTextIsSelectable(true); setPadding(0, dp(5), 0, 0)
        })
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        transcript.addView(card, lp)
    }

    private fun sendMessage() {
        val prompt = input.text.toString().trim()
        if (prompt.isEmpty() || !send.isEnabled) return
        input.setText("")
        addMessage("Você", prompt)
        history.add("Usuário: $prompt")
        status.text = "Pensando…"
        send.isEnabled = false
        send.text = "..."
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(input.windowToken, 0)

        val conversation = history.takeLast(12).joinToString("\n")
        val fullPrompt = "Você é uma assistente de IA útil e clara. Responda em português quando apropriado. " +
                "Esta é a conversa até agora:\n$conversation\nAssistente:"
        executor.execute {
            try {
                val encoded = URLEncoder.encode(fullPrompt, "UTF-8")
                val url = URL("https://text.pollinations.ai/$encoded?model=openai")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20000
                    readTimeout = 90000
                    setRequestProperty("Accept", "text/plain")
                    setRequestProperty("User-Agent", "IALivreAndroid/1.0")
                }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                conn.disconnect()
                if (code !in 200..299) throw Exception("Serviço respondeu HTTP $code: ${body.take(180)}")
                runOnUiThread {
                    addMessage("IA Livre", body.ifBlank { "O serviço não retornou texto. Tente novamente." })
                    history.add("Assistente: $body")
                    status.text = "Resposta recebida"
                    send.isEnabled = true
                    send.text = "Enviar"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    addMessage("Aviso", "Não consegui obter resposta agora. Verifique a internet ou tente novamente mais tarde. Detalhe: ${e.message ?: "erro de conexão"}")
                    status.text = "Falha na conexão"
                    send.isEnabled = true
                    send.text = "Enviar"
                }
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
