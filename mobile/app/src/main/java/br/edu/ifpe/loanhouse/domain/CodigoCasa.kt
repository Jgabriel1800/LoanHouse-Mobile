package br.edu.ifpe.loanhouse.domain

import java.text.Normalizer

/**
 * Código da casa: 3 primeiras letras do bairro (sem acento, maiúsculas) + "-" + número.
 * Mesma regra de web/js/utils/codigo-casa.js e docs/regras-de-negocio.md.
 * Ex.: "Boa Viagem", "123" -> "BOA-123"
 */
object CodigoCasa {

    fun siglaDoBairro(bairro: String): String {
        val letras = Normalizer.normalize(bairro, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .uppercase()
            .replace(Regex("[^A-Z]"), "")
        require(letras.isNotEmpty()) { "Bairro inválido para gerar o código" }
        return letras.take(3).padEnd(3, 'X')
    }

    fun normalizarNumero(numero: String): String {
        val n = numero.uppercase().replace(Regex("[^0-9A-Z]"), "")
        require(n.isNotEmpty()) { "Número inválido para gerar o código" }
        return n
    }

    fun gerar(bairro: String, numero: String): String = "${siglaDoBairro(bairro)}-${normalizarNumero(numero)}"
}
