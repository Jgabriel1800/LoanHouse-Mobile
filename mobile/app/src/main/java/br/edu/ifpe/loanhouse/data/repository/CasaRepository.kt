package br.edu.ifpe.loanhouse.data.repository

import br.edu.ifpe.loanhouse.data.model.Casa
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration

/** Acesso à coleção "casas", compartilhada com o site. */
class CasaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    /**
     * Escuta todas as casas em tempo real. Se alguém mudar algo no site, a tela do app atualiza sozinha.
     * A ordem (pendência, em dia, vaga) é aplicada na tela, com a regra de domain/Pendencias.
     * Chame remove() no retorno quando a tela for destruída.
     */
    fun escutarCasas(
        aoReceber: (List<Casa>) -> Unit,
        aoFalhar: (FirebaseFirestoreException) -> Unit,
    ): ListenerRegistration =
        db.collection(COLECAO).addSnapshotListener { snapshot, erro ->
            if (erro != null) {
                aoFalhar(erro)
                return@addSnapshotListener
            }
            aoReceber(snapshot?.documents.orEmpty().mapNotNull { converter(it.id, it.toObject(Casa::class.java)) })
        }

    /** Escuta uma casa só (tela de detalhes). Recebe null se ela for excluída. */
    fun escutarCasa(
        codigo: String,
        aoReceber: (Casa?) -> Unit,
        aoFalhar: (FirebaseFirestoreException) -> Unit,
    ): ListenerRegistration =
        db.collection(COLECAO).document(codigo).addSnapshotListener { doc, erro ->
            if (erro != null) {
                aoFalhar(erro)
                return@addSnapshotListener
            }
            aoReceber(if (doc != null && doc.exists()) converter(doc.id, doc.toObject(Casa::class.java)) else null)
        }

    fun excluir(codigo: String): Task<Void> = db.collection(COLECAO).document(codigo).delete()

    /**
     * Só para a versão de teste: grava 5 casas inventadas, com e sem pendência,
     * para as telas não ficarem vazias. Usa os mesmos campos que o site.
     */
    fun criarCasasDeExemplo(): Task<Void> {
        val lote = db.batch()
        EXEMPLOS.forEach { dados ->
            val ref = db.collection(COLECAO).document(dados["codigo"] as String)
            lote.set(ref, dados + mapOf("criadoEm" to FieldValue.serverTimestamp(), "atualizadoEm" to FieldValue.serverTimestamp()))
        }
        return lote.commit()
    }

    private fun converter(id: String, casa: Casa?): Casa? =
        casa?.apply { if (codigo.isBlank()) codigo = id }

    companion object {
        const val COLECAO = "casas"

        private fun casa(
            codigo: String, rua: String, numero: String, bairro: String, cep: String,
            situacao: String, valor: Double, extras: Map<String, Any?> = emptyMap(),
        ): Map<String, Any?> = mapOf(
            "codigo" to codigo, "rua" to rua, "numero" to numero, "bairro" to bairro, "cep" to cep,
            "cidade" to "Belo Jardim", "uf" to "PE", "situacao" to situacao, "valorAluguel" to valor,
            "iptuAno" to 2026, "iptuPago" to true, "fotos" to emptyList<Any>(), "documentos" to emptyList<Any>(),
        ) + extras

        private val EXEMPLOS = listOf(
            casa(
                "TAN-210", "Rua das Flores", "210", "Tancredo Neves", "55150-000", "alugada", 1200.0,
                mapOf(
                    "aluguelAtrasado" to true, "vencimentoAtrasado" to "05/11/2026",
                    "inquilinoNome" to "Maria Souza", "inquilinoTelefone" to "81900000001",
                ),
            ),
            casa(
                "BOA-205", "Rua do Sol", "205", "Boa Vista", "55150-010", "alugada", 950.0,
                mapOf("aguaPendente" to true, "iptuPago" to false, "inquilinoNome" to "Pedro Lima", "inquilinoTelefone" to "81900000002"),
            ),
            casa(
                "SAO-123", "Rua da Casa 1", "123", "São Pedro", "55020-010", "alugada", 1900.0,
                mapOf("inquilinoNome" to "João Carlos da Silva", "inquilinoTelefone" to "81900000003"),
            ),
            casa("CEN-45", "Rua Cel. Antônio Marinho", "45", "Centro", "55150-020", "vaga", 800.0),
            casa("SAN-124", "Rua Santo Antônio", "124", "Santo Antônio", "55150-030", "vaga", 750.0),
        )
    }
}
