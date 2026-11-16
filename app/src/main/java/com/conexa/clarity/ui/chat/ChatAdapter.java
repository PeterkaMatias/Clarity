package com.conexa.clarity.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_USUARIO = 1;
    private static final int TIPO_LUMI = 2;

    private final List<Mensaje> mensajes;

    public ChatAdapter(
            List<Mensaje> mensajes
    ) {
        this.mensajes = mensajes;
    }

    @Override
    public int getItemViewType(
            int position
    ) {

        Mensaje mensaje =
                mensajes.get(position);

        if (mensaje.esDelUsuario()) {
            return TIPO_USUARIO;
        }

        return TIPO_LUMI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LayoutInflater inflater =
                LayoutInflater.from(
                        parent.getContext()
                );

        if (viewType == TIPO_USUARIO) {

            View view =
                    inflater.inflate(
                            R.layout.item_mensaje_usuario,
                            parent,
                            false
                    );

            return new UsuarioViewHolder(
                    view
            );
        }

        View view =
                inflater.inflate(
                        R.layout.item_mensaje_lumi,
                        parent,
                        false
                );

        return new LumiViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        Mensaje mensaje =
                mensajes.get(position);

        if (holder instanceof UsuarioViewHolder) {

            UsuarioViewHolder usuarioHolder =
                    (UsuarioViewHolder) holder;

            usuarioHolder.tvMensajeUsuario.setText(
                    mensaje.getTexto()
            );

            usuarioHolder.itemView.setOnLongClickListener(
                    null
            );

        } else if (holder instanceof LumiViewHolder) {

            LumiViewHolder lumiHolder =
                    (LumiViewHolder) holder;

            lumiHolder.tvMensajeLumi.setText(
                    aplicarFormatoMarkdown(
                            mensaje.getTexto()
                    )
            );

            lumiHolder.itemView.setOnLongClickListener(
                    view -> {

                        copiarTexto(
                                view.getContext(),
                                mensaje.getTexto()
                        );

                        return true;
                    }
            );
        }
    }

    private CharSequence aplicarFormatoMarkdown(
            String texto
    ) {

        if (texto == null
                || texto.isEmpty()) {

            return "";
        }

        SpannableStringBuilder resultado =
                new SpannableStringBuilder(
                        texto
                );

        Pattern patronNegrita =
                Pattern.compile(
                        "\\*\\*(.+?)\\*\\*",
                        Pattern.DOTALL
                );

        Matcher matcher =
                patronNegrita.matcher(
                        resultado
                );

        while (matcher.find()) {

            int inicio =
                    matcher.start();

            int fin =
                    matcher.end();

            String contenido =
                    matcher.group(1);

            if (contenido == null) {
                contenido = "";
            }

            resultado.replace(
                    inicio,
                    fin,
                    contenido
            );

            resultado.setSpan(
                    new StyleSpan(
                            Typeface.BOLD
                    ),
                    inicio,
                    inicio + contenido.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            matcher =
                    patronNegrita.matcher(
                            resultado
                    );
        }

        return resultado;
    }

    private void copiarTexto(
            Context context,
            String texto
    ) {

        if (texto == null
                || texto.trim().isEmpty()) {

            return;
        }

        if (texto.startsWith(
                "Lumi está pensando"
        )) {

            return;
        }

        ClipboardManager clipboardManager =
                (ClipboardManager)
                        context.getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

        if (clipboardManager == null) {
            return;
        }

        ClipData clipData =
                ClipData.newPlainText(
                        "Respuesta de Lumi",
                        texto
                );

        clipboardManager.setPrimaryClip(
                clipData
        );

        Toast.makeText(
                context,
                "Respuesta copiada",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public int getItemCount() {

        return mensajes != null
                ? mensajes.size()
                : 0;
    }

    public void agregarMensaje(
            Mensaje mensaje
    ) {

        if (mensaje == null) {
            return;
        }

        mensajes.add(
                mensaje
        );

        notifyItemInserted(
                mensajes.size() - 1
        );
    }

    public void actualizarMensaje(
            int posicion,
            String nuevoTexto
    ) {

        if (posicion < 0
                || posicion >= mensajes.size()) {

            return;
        }

        mensajes.get(posicion)
                .setTexto(
                        nuevoTexto
                );

        notifyItemChanged(
                posicion
        );
    }

    static class UsuarioViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvMensajeUsuario;

        public UsuarioViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvMensajeUsuario =
                    itemView.findViewById(
                            R.id.tvMensajeUsuario
                    );
        }
    }

    static class LumiViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvMensajeLumi;

        public LumiViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvMensajeLumi =
                    itemView.findViewById(
                            R.id.tvMensajeLumi
                    );
        }
    }
}