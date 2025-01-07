# Batalha Naval

https://github.com/mig4lha/BatalhaNaval

Bem-vindo ao projeto **Batalha Naval**, um jogo clássico desenvolvido para dispositivos Android, utilizando o **Kotlin** e o **Jetpack Compose**. Este projeto inclui funcionalidades de login, registro, gerenciamento de jogos online e um placar de líderes para acompanhar os melhores jogadores.

## 🚀 Funcionalidades

- **Login e Registro de Usuários:**
  - Registro seguro com verificação de nicknames disponíveis.
  - Login com autenticação para acessar o sistema de jogos.

- **Jogos Online:**
  - Criação e gerenciamento de jogos ativos.
  - Interface intuitiva para configurar o tabuleiro e posicionar os navios.
  - Mecanismo de disparos, troca de turnos e sistema de vitória.
  - Tela de jogos ativos, permitindo aos jogadores continuar partidas em andamento.

- **Leaderboard:**
  - Exibição dos 10 melhores jogadores com base no menor número de turnos para ganhar.

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Framework de UI:** Jetpack Compose
- **Banco de Dados:** Firebase Firestore
- **Arquitetura:** MVVM (Model-View-ViewModel)
- **Gerenciamento de Estado:** StateFlow

## 📂 Estrutura do Projeto

```
📁 src/
 ├── 📁 data/                # Serviços e repositórios
 │     ├── 📁 firebase/      # Conexões com o Firebase
 │     ├── 📁 model/         # Modelos de dados
 │     └── 📁 repository/    # Lógica de repositório
 ├── 📁 ui/
 │     ├── 📁 screens/       # Telas do aplicativo
 │     ├── 📁 components/    # Componentes reutilizáveis
 │     ├── 📁 theme/         # Estilo e cores
 ├── 📁 MainActivity.kt      # Entrada principal do app
```

## 🖥️ Telas Principais

1. **Login e Registro:**
   - Entrar no sistema ou criar uma nova conta.
2. **Menu Principal:**
   - Iniciar um novo jogo.
   - Acessar jogos ativos.
   - Visualizar o leaderboard.
3. **Configuração do Tabuleiro:**
   - Posicionar navios de forma estratégica antes do início do jogo.
4. **Tela de Jogo:**
   - Jogar contra outro jogador, alternando entre turnos.
5. **Leaderboard:**
   - Visualizar os 10 melhores jogadores.
6. **Jogos Ativos:**
   - Continuar partidas em andamento e verificar o estado atual dos jogos.

## 🔧 Instalação

1. Clone o repositório:
   ```bash
   git clone https://github.com/seuusuario/batalha-naval.git
   ```

2. Abra o projeto no Android Studio.

3. Configure o Firebase:
   - Adicione o arquivo `google-services.json` no diretório `app`.
   - Certifique-se de configurar o Firestore no console do Firebase.

4. Compile e execute o aplicativo em um emulador ou dispositivo físico.

## 🧩 Configuração do Firebase

Certifique-se de que as coleções necessárias estão configuradas no Firestore:

### Estrutura de Dados

#### Coleção: `users`
```json
{
  "nick": "string",
  "password": "string",
  "playerId": "string"
}
```

#### Coleção: `games`
```json
{
  "player1": "string",
  "player2": "string",
  "status": "string",
  "winner": "string",
  "turn": "number",
  "boards": "array"
}
```

#### Coleção: `leaderboard`
```json
{
  "playerId": "string",
  "score": "number"
}
```

Este projeto está sob a licença MIT. Consulte o arquivo `LICENSE` para mais informações.

---

Desenvolvido por Miguel, João e Margarida para a cadeira de DPJM.
