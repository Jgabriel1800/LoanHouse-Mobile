package br.edu.ifpe.loanhouse.domain

import java.text.NumberFormat
import java.util.Locale

object Formatos {

    private val brasil = Locale("pt", "BR")

    /** 1200.0 -> "R$ 1.200,00" */
    fun moeda(valor: Double): String = NumberFormat.getCurrencyInstance(brasil).format(valor)

    /** "João Carlos da Silva" -> "JS"; "marcos" -> "MA" (igual ao iniciaisDe do site). */
    fun iniciais(nome: String?): String {
        val partes = nome.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            partes.isEmpty() -> ""
            partes.size == 1 -> partes[0].take(2).uppercase(brasil)
            else -> "${partes.first().first()}${partes.last().first()}".uppercase(brasil)
        }
    }

    /** Nome para mostrar a partir do usuário logado: nome de exibição ou a parte antes do @. */
    fun nomeDoUsuario(nomeExibicao: String?, email: String?): String =
        nomeExibicao?.takeIf { it.isNotBlank() } ?: email.orEmpty().substringBefore("@")
}
