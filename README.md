# Quiz App Starter (Kotlin + Compose + Hilt + Room + Firebase-ready)

Este é um projeto inicial para o seu trabalho de PDM:
- Login (e-mail/senha) via **Firebase Auth** (adicione o `google-services.json`).
- Sincronização de quizzes/perguntas do **Firestore** para **Room**.
- Execução do quiz (Compose), histórico local e estrutura para ranking.

## Passos
1. No [Firebase Console](https://console.firebase.google.com/), crie um app Android com o `applicationId` **com.seuapp.quiz** e baixe o `google-services.json`. Coloque o arquivo em `app/google-services.json`.
2. Abra o projeto no Android Studio **Ladybug+** (ou mais novo).
3. Sincronize o Gradle e rode o app (Debug).
4. No Firestore, crie as coleções conforme o README do chat.

> Observação: o plugin do Google Services só é aplicado se o `google-services.json` existir. Assim o projeto compila mesmo antes de configurar o Firebase.

## Estrutura
- `app/src/main/java/com/seuapp/quiz`
  - `QuizApp.kt` (Hilt)
  - `di/AppModule.kt` (Hilt Providers)
  - `data/db/*` (Room + DAOs + Entities)
  - `data/repo/*` (Repositórios Firestore/Room)
  - `ui/*` (Compose + ViewModels + Navegação)

## Próximos passos sugeridos
- Completar telas de Resultado, Ranking e melhorar UI.
- Validar inputs, tratamento de erros e estados vazios.
- Escrever regras do Firestore (segurança).
- Gravar vídeo e gerar APK para entrega.
