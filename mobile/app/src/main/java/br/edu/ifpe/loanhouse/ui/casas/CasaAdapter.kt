package br.edu.ifpe.loanhouse.ui.casas

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.data.model.Casa
import br.edu.ifpe.loanhouse.databinding.ItemCasaBinding
import br.edu.ifpe.loanhouse.domain.EstadoCasa
import br.edu.ifpe.loanhouse.domain.Formatos
import br.edu.ifpe.loanhouse.domain.Pendencias
import br.edu.ifpe.loanhouse.ui.comum.aparencia
import br.edu.ifpe.loanhouse.ui.comum.mostrarBadge
import br.edu.ifpe.loanhouse.ui.comum.mostrarPendencias
import br.edu.ifpe.loanhouse.ui.comum.pintar

/** Card de casa do Figma. É o mesmo na Home e na aba Casas. */
class CasaAdapter(
    private val aoAbrir: (Casa) -> Unit,
) : ListAdapter<Casa, CasaAdapter.CasaViewHolder>(Diferenca) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CasaViewHolder =
        CasaViewHolder(ItemCasaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CasaViewHolder, position: Int) = holder.mostrar(getItem(position))

    inner class CasaViewHolder(private val b: ItemCasaBinding) : RecyclerView.ViewHolder(b.root) {

        fun mostrar(casa: Casa) {
            val contexto = b.root.context
            val estado = Pendencias.estado(casa)
            val visual = aparencia(estado)

            b.cartaoCasa.strokeColor = ContextCompat.getColor(contexto, visual.borda)
            b.cartaoCasa.setCardBackgroundColor(
                ContextCompat.getColor(contexto, if (estado == EstadoCasa.COM_PENDENCIA) R.color.vermelho_fundo else R.color.fundo),
            )
            b.iconeCasa.pintar(estado)
            b.badgeSituacao.mostrarBadge(estado)
            b.textoCodigo.text = casa.codigo
            b.textoEndereco.text = contexto.getString(R.string.casa_endereco, casa.rua.ifBlank { "Rua não informada" }, casa.numero.ifBlank { "S/N" })
            b.textoBairro.text = contexto.getString(
                R.string.casa_bairro,
                casa.bairro.ifBlank { "Centro" },
                casa.cidade.ifBlank { "Belo Jardim" },
                casa.uf.ifBlank { "PE" },
            )
            b.textoValor.text = Formatos.moeda(casa.valorExibido)
            b.grupoBadges.mostrarPendencias(Pendencias.listar(casa), alugada = casa.situacao != "vaga")
            b.botaoDetalhes.iconTint = ColorStateList.valueOf(ContextCompat.getColor(contexto, R.color.azul))
            b.botaoDetalhes.setOnClickListener { aoAbrir(casa) }
            b.root.setOnClickListener { aoAbrir(casa) }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<Casa>() {
        override fun areItemsTheSame(oldItem: Casa, newItem: Casa) = oldItem.codigo == newItem.codigo
        override fun areContentsTheSame(oldItem: Casa, newItem: Casa) = oldItem == newItem
    }
}
