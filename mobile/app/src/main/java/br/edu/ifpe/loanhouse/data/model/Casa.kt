package br.edu.ifpe.loanhouse.data.model

import com.google.firebase.firestore.Exclude

/**
 * Documento da coleção "casas" no Firestore. Os nomes dos campos são os mesmos que o site usa
 * (web/js/services/casas.js e web/js/components/cartao-casa.js).
 * Todos têm valor padrão para o Firestore conseguir criar o objeto com toObject();
 * campos que o app ainda não usa (fotos, documentos, datas) são ignorados.
 */
data class Casa(
    var codigo: String = "",
    var cep: String = "",
    var rua: String = "",
    var numero: String = "",
    var bairro: String = "",
    var cidade: String = "",
    var uf: String = "",
    /** "vaga" ou "alugada" ("atrasada" ainda é aceito, por compatibilidade com dados antigos). */
    var situacao: String = "vaga",
    var valorAluguel: Double? = null,
    var valor: Double? = null,

    // Pendências (mesmos campos lidos por listarPendenciasCasa no site)
    var pendencias: List<String>? = null,
    var aluguelAtrasado: Boolean? = null,
    var aguaPendente: Boolean? = null,
    var luzPendente: Boolean? = null,
    var iptuPendente: Boolean? = null,
    var iptuAno: Long? = null,
    var iptuPago: Boolean? = null,
    /** Data do aluguel vencido, no formato DD/MM/AAAA (usada no alerta e na cobrança). */
    var vencimentoAtrasado: String? = null,

    // Inquilino atual (repetido na casa para a Home poder cobrar sem outra consulta)
    var inquilinoAtualId: String? = null,
    var inquilinoNome: String? = null,
    var inquilinoTelefone: String? = null,
    var contratoAtivoId: String? = null,
) {
    /** Valor mostrado no card: valorAluguel, ou o campo antigo "valor". */
    @get:Exclude
    val valorExibido: Double get() = valorAluguel ?: valor ?: 0.0
}
