package br.edu.ifpe.loanhouse.domain

import br.edu.ifpe.loanhouse.data.model.Casa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test


class RegrasTest {

    @Test
    fun geraCodigoDaCasa() {
        assertEquals("BOA-123", CodigoCasa.gerar("Boa Viagem", "123"))
        assertEquals("SAO-45A", CodigoCasa.gerar("São José", "45a"))
        assertEquals("OXX-7", CodigoCasa.gerar("Ó", "7"))
    }

    @Test
    fun pendenciasEOrdenacaoIguaisAoSite() {
        val casaVaga = Casa(codigo = "SAN-124", situacao = "vaga")
        val casaEmDia = Casa(codigo = "SAO-123", situacao = "alugada", iptuAno = 2026, iptuPago = true)
        val casaAguaIptu = Casa(codigo = "BOA-205", situacao = "alugada", aguaPendente = true, iptuAno = 2026, iptuPago = false)
        val casaAluguel = Casa(codigo = "TAN-210", situacao = "atrasada")

        assertFalse(Pendencias.temPendencia(casaVaga))
        assertFalse(Pendencias.temPendencia(casaEmDia))
        assertEquals(listOf(TipoPendencia.AGUA, TipoPendencia.IPTU), Pendencias.listar(casaAguaIptu))
        assertEquals(listOf(TipoPendencia.ALUGUEL), Pendencias.listar(casaAluguel))

        val ordenadas = Pendencias.ordenar(listOf(casaVaga, casaEmDia, casaAluguel, casaAguaIptu))
        assertEquals(listOf("BOA-205", "TAN-210", "SAO-123", "SAN-124"), ordenadas.map { it.codigo })
    }

    @Test
    fun listaDePendenciasDoBancoNaoRepete() {
        val casa = Casa(codigo = "X-1", situacao = "alugada", pendencias = listOf("LUZ", "luz"), luzPendente = true)
        assertEquals(listOf(TipoPendencia.LUZ), Pendencias.listar(casa))
    }

    @Test
    fun iniciaisDoUsuario() {
        assertEquals("JS", Formatos.iniciais("João Carlos da Silva"))
        assertEquals("MA", Formatos.iniciais("marcos"))
        assertEquals("marcos", Formatos.nomeDoUsuario(null, "marcos@email.com"))
    }
}
