# Conecta Disparos

Aplicativo Android nativo privado para conduzir lotes locais de disparos assistidos pelo WhatsApp Business. O app nao envia mensagens sozinho: ele abre a conversa com a mensagem preenchida e aguarda uma confirmacao externa, normalmente enviada pelo MacroDroid.

## Arquitetura

- Kotlin, Jetpack Compose, Material 3 e Navigation Compose.
- MVVM com Clean Architecture simplificada.
- Room para lotes, contatos, mensagens, historico e resultados.
- Kotlin Serialization para parser JSON.
- Coroutines e StateFlow para estado de execucao.
- Foreground Service para manter a operacao visivel.
- Broadcasts Android para integracao com MacroDroid.

## Build

Requisitos: Android Studio atual, JDK 17 e Android SDK 35.

```bash
./gradlew clean
./gradlew test
./gradlew assembleDebug
```

APK debug esperado:

`app/build/outputs/apk/debug/app-debug.apk`

Para release, copie `keystore.properties.example` para `keystore.properties`, preencha com sua chave local e rode:

```bash
./gradlew assembleRelease
```

Nao coloque chaves reais no Git.

## Formato do lote

Veja `sample-lote.json`. Telefones sao normalizados removendo caracteres nao numericos. O app aceita telefones com DDI e valida preferencialmente Brasil no formato `55 + DDD + numero`.

## Status

Contatos: `PENDENTE`, `PREPARANDO`, `AGUARDANDO_ENVIO`, `ENVIADO`, `IGNORADO`, `ERRO`, `CANCELADO`.

Lotes: `IMPORTADO`, `PRONTO`, `EM_EXECUCAO`, `PAUSADO`, `AGUARDANDO_HORARIO`, `EM_PAUSA_DE_BLOCO`, `CONCLUIDO`, `CANCELADO`, `ERRO`.

## MacroDroid

Broadcasts emitidos:

- `br.com.conectatecnologia.conectadisparos.ACTION_CONTACT_OPENED`
- `br.com.conectatecnologia.conectadisparos.ACTION_WAITING_EXTERNAL_CONFIRMATION`
- `br.com.conectatecnologia.conectadisparos.ACTION_BATCH_PAUSED`
- `br.com.conectatecnologia.conectadisparos.ACTION_BATCH_FINISHED`

Extras: `batchId`, `contactId`, `nome`, `telefone`, `mensagem`, `posicao`, `total`, `timestamp`.

Broadcasts recebidos:

- `br.com.conectatecnologia.conectadisparos.ACTION_MESSAGE_SENT`
- `br.com.conectatecnologia.conectadisparos.ACTION_MESSAGE_FAILED`
- `br.com.conectatecnologia.conectadisparos.ACTION_SKIP_CONTACT`

Configure o MacroDroid para detectar que o WhatsApp foi aberto, executar sua automacao externa e enviar `ACTION_MESSAGE_SENT` com `batchId` e `contactId`. Para erro, envie `ACTION_MESSAGE_FAILED` com `erro`. Para ignorar, envie `ACTION_SKIP_CONTACT` com `motivo`.

## Limitacoes

O projeto nao implementa Accessibility Service, cliques automaticos, leitura da interface do WhatsApp, backend, Supabase, Firebase, n8n ou painel web. Todo o estado principal e local.

## Solucao de problemas

- Se a fila parar, confira se o MacroDroid enviou o broadcast de confirmacao.
- Se o WhatsApp nao abrir, instale o WhatsApp Business ou habilite fallback para WhatsApp comum.
- Desative otimizacao de bateria e permita notificacoes para maior estabilidade.
