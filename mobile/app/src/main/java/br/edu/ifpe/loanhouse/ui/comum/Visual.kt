package br.edu.ifpe.loanhouse.ui.comum

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.databinding.ViewCabecalhoBinding
import br.edu.ifpe.loanhouse.domain.EstadoCasa
import br.edu.ifpe.loanhouse.domain.Formatos
import br.edu.ifpe.loanhouse.domain.TipoPendencia
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth

/** Cores e textos de cada estado da casa (os mesmos do card do site). */
data class AparenciaEstado(
    @ColorRes val cor: Int,
    @ColorRes val fundo: Int,
    @ColorRes val borda: Int,
    @DrawableRes val icone: Int,
    val rotulo: Int,
)

fun aparencia(estado: EstadoCasa): AparenciaEstado = when (estado) {
    EstadoCasa.COM_PENDENCIA -> AparenciaEstado(R.color.vermelho, R.color.vermelho_fundo, R.color.vermelho_borda, R.drawable.ic_alerta, R.string.badge_pendencia)
    EstadoCasa.ALUGADA -> AparenciaEstado(R.color.verde, R.color.verde_fundo, R.color.borda, R.drawable.ic_ok, R.string.badge_alugada)
    EstadoCasa.VAGA -> AparenciaEstado(R.color.cinza, R.color.cinza_fundo, R.color.borda, R.drawable.ic_casa, R.string.badge_vaga)
}

private fun corDe(context: Context, @ColorRes id: Int) = ContextCompat.getColor(context, id)

/** Etiqueta colorida no canto do card ("Com Pendência", "Alugada", "Vaga"). */
fun TextView.mostrarBadge(estado: EstadoCasa) {
    val a = aparencia(estado)
    text = context.getString(a.rotulo)
    setTextColor(corDe(context, a.cor))
    background = GradientDrawable().apply {
        cornerRadius = resources.getDimension(R.dimen.raio_botao)
        setColor(corDe(context, a.fundo))
    }
    setCompoundDrawablesRelativeWithIntrinsicBounds(a.icone, 0, 0, 0)
    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(corDe(context, a.cor)))
}

/** Ícone da casa pintado com a cor do estado. */
fun ImageView.pintar(estado: EstadoCasa) {
    imageTintList = ColorStateList.valueOf(corDe(context, aparencia(estado).cor))
}

/** Badges de pendência (vermelhos) ou, se não houver, um verde: "Aluguel Pago" ou "Sem Pendências". */
fun ChipGroup.mostrarPendencias(pendencias: List<TipoPendencia>, alugada: Boolean) {
    removeAllViews()
    if (pendencias.isEmpty()) {
        addView(chip(context, context.getString(if (alugada) R.string.pagamento_em_dia else R.string.sem_pendencias), R.color.verde, R.color.verde_fundo, R.drawable.ic_ok))
    } else {
        pendencias.forEach { addView(chip(context, context.getString(it.rotulo), R.color.vermelho, R.color.vermelho_fundo, R.drawable.ic_alerta)) }
    }
}

private fun chip(context: Context, texto: String, @ColorRes cor: Int, @ColorRes fundo: Int, @DrawableRes icone: Int) =
    Chip(context).apply {
        text = texto
        isClickable = false
        isCheckable = false
        setTextColor(corDe(context, cor))
        chipBackgroundColor = ColorStateList.valueOf(corDe(context, fundo))
        chipStrokeWidth = 0f
        setChipIconResource(icone)
        chipIconTint = ColorStateList.valueOf(corDe(context, cor))
        isChipIconVisible = true
    }

/** Preenche o cabeçalho das abas: título (ou logo) e avatar com as iniciais do usuário logado. */
fun ViewCabecalhoBinding.preencher(titulo: String?) {
    if (titulo == null) {
        logo.root.visibility = android.view.View.VISIBLE
        this.titulo.visibility = android.view.View.GONE
    } else {
        logo.root.visibility = android.view.View.GONE
        this.titulo.visibility = android.view.View.VISIBLE
        this.titulo.text = titulo
    }
    val usuario = FirebaseAuth.getInstance().currentUser
    avatar.text = Formatos.iniciais(Formatos.nomeDoUsuario(usuario?.displayName, usuario?.email))
}

/** Abre o WhatsApp com a mensagem pronta. Quem envia é sempre o usuário. */
fun abrirWhatsApp(context: Context, telefone: String, mensagem: String) {
    var numero = telefone.filter { it.isDigit() }
    if (numero.length <= 11) numero = "55$numero" // sem código do país: assume Brasil (igual ao site)
    val uri = Uri.parse("https://wa.me/$numero?text=${Uri.encode(mensagem)}")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.sem_whatsapp, Toast.LENGTH_SHORT).show()
    }
}
