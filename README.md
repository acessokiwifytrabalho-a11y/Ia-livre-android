# IA Livre para Android

Projeto de aplicativo Android nativo (Kotlin), com interface própria de chat. Não exige conta nem chave de API no código: usa o endpoint de texto público legado do Pollinations. É necessário ter internet, e o serviço remoto pode impor limites, mudar ou parar de aceitar chamadas sem chave. Não há promessa de disponibilidade permanente nem de ausência total de restrições.

## Como gerar o APK
1. Instale Android Studio: https://developer.android.com/studio
2. Extraia o ZIP.
3. No Android Studio, escolha **Open** e selecione a pasta `IA_Livre_Android`.
4. Aguarde o Gradle sincronizar e instalar os componentes solicitados.
5. Para testar, conecte o celular com depuração USB ativada ou use um emulador e pressione **Run**.
6. Para gerar APK: **Build > Build APK(s)**. O APK de depuração ficará normalmente em `app/build/outputs/apk/debug/app-debug.apk`.

## Observações
- O projeto fonte foi preparado, mas não foi possível compilar/assinar um APK neste ambiente por falta do Android SDK/Gradle instalado.
- A IA usa um serviço online; mensagens são enviadas ao provedor para gerar respostas. Não use para compartilhar senhas, documentos ou dados privados.
- O endpoint gratuito legado pode sofrer limites ou indisponibilidade. Se deixar de funcionar, será necessário adaptar a conexão a um provedor atual.
- Nenhum aplicativo de IA pode garantir respostas perfeitas ou literalmente sem restrições.
