package br.edu.ifpe.loanhouse.domain

import androidx.annotation.StringRes
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.data.model.Casa

/** Tipos de pendência: os mesmos do site (web/js/utils/situacao.js, listarPendenciasCasa). */
enum class TipoPendencia(val valor: String, @StringRes val rotulo: Int) {
    ALUGUEL("aluguel", R.string.pendencia_aluguel),
    AGUA("agua", R.string.pendencia_agua),
    LUZ("luz", R.string.pendencia_luz),
    IPTU("iptu", R.string.pendencia_iptu);

    companion object {
        fun de(valor: String): TipoPendencia? = entries.firstOrNull { it.valor == valor.lowercase() }
    }
}

/** Como a casa aparece nas telas. A ordem do enum é a ordem da lista na aba Casas. */
enum class EstadoCasa { COM_PENDENCIA, ALUGADA, VAGA }

/**
 * Regras de pendência, portadas de web/js/utils/situacao.js para o app calcular igual ao site.
 * Qualquer mudança aqui precisa ser feita lá também (e vice-versa).
 */
object Pendencias {

    /** Lista as pendências da casa, sem repetir, na ordem: lista "pendencias", aluguel, água, luz, IPTU. */
    fun listar(casa: Casa): List<TipoPendencia> {
        val lista = mutableListOf<TipoPendencia>()
        fun adicionar(tipo: TipoPendencia?) {
            if (tipo != null && tipo !in lista) lista += tipo
        }

        casa.pendencias.orEmpty().forEach { adicionar(TipoPendencia.de(it)) }
        if (casa.situacao == "atrasada" || casa.aluguelAtrasado == true) adicionar(TipoPendencia.ALUGUEL)
        if (casa.aguaPendente == true) adicionar(TipoPendencia.AGUA)
        if (casa.luzPendente == true) adicionar(TipoPendencia.LUZ)
        if (casa.iptuPendente == true || ((casa.iptuAno ?: 0L) != 0L && casa.iptuPago == false)) {
            adicionar(TipoPendencia.IPTU)
        }
        return lista
    }

    fun temPendencia(casa: Casa): Boolean = listar(casa).isNotEmpty()

    fun estado(casa: Casa): EstadoCasa = when {
        temPendencia(casa) -> EstadoCasa.COM_PENDENCIA
        casa.situacao == "alugada" -> EstadoCasa.ALUGADA
        else -> EstadoCasa.VAGA
    }

    /** Pendência -> Alugada em dia -> Vaga; dentro de cada grupo, pelo código (como no site). */
    fun ordenar(casas: List<Casa>): List<Casa> =
        casas.sortedWith(compareBy<Casa>({ estado(it).ordinal }, { it.codigo }))
}
