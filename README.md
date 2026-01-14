# Fallen Hero - READ ME

## Estrutura do Projeto
A estrutura do projeto está em MVVM, dividindo-se em vários packages, sendo estes "model" e "view".

Dentro do "model", temos as entidades todas, o sound manager e o GameView;

E dentro do "view" tem todos os menus.

## Lista de Funcionalidades da Aplicação

### Jogador:
* Jogadro voa ao pressionar a tela;
* Enquanto está a voar e tem stamina disponível lança um raio que mata qualquer inimigo.

### Inimigos:
* Pode matar os mobs com o lazer ao voar por cima deles;
* Um mob a voar;
* Outro mob no chão que dispara balas na direção do jogador.

### Background:
* Cidade destruída em loop infinito.

### Power-ups:
* Após apanhar 3 orbs azuis para o carregar, o botão fica carregado, ao clicar usa o raio super hiper mega forte da morte;
* Um escudo que fica ativo após o apanhar e é destruído quando o jogador leva dano.

### Jogo:
* Registo de high-scores em firebase;
* Colisões.

### UI:
* Ecrã inicial com opções para iniciar jogo e ver pontuações;
* Interface durante a partida;
* Apresentação da pontuação final;
* Ecrã de High Scores.

### Sons:
* Vários efeitos sonoros para melhor imersão do jogo.


## Desenhos, esquemas e protótipos da aplicação

### Personagem principal:

![Jogador1](/images/Jogador1.png)
![Jogador2](/images/Jogador2.png)

### Mobs:
Inspirados nas personagens de DOOM Eternal _Cacodemon_ e _Arachnotron_

![Cacodemon](/images/Cacodemon.png)
![Arachnotron](/images/Arachnotron.png)

### 1ª Versão dos Mobs:
![Mob1](/images/mob1.png)
![Mob2](/images/mob2.png)


### 1ªs Versões do Background
![Background1](/images/bg1.png)
![Background2](/images/bg2.png)

Esta versão foi posteriormente ajustada para que consiga dar sidescroll infinitamente sem que a imagem fique cortada.

## Modelo de dados
Para o armazenamento de dados é utilizado o Firestore do Firebase, onde são guardadas informações dos jogadores sendo elas Nome e ID e os seus respectivos melhores scores.

![Firestore](/images/firebase.png)

Aqui, o “playerId” serve para indicar à base de dados se o dispositivo em que foi registado o highscore já existe na base de dados ou não. O nome é escolhido pelo jogador sempre que bate o seu recorde.

## Implementação do projeto
* Foi utilizada a linguagem Kotlin e seguindo o padrão de arquitetura MVVM;
* Também foi utilizado o Firebase, onde são armazenadas as pontuações obtidas pelos jogadores.


## Tecnologias usadas

### Linguagem:
Kotlin

### Arquitetura:
MVVM

### IDE:
Android Studio

## Dificuldades
As nossas maiores dificuldades neste projeto foram o uso de alguns sons e integração do segundo inimigo, pois este era inicialmente demasiado difícil de esquivar para o jogador e tivemos que o modificar para que seja mais fácil evitá-lo.

## Conclusão
Neste projeto aplicamos os conhecimentos adquiridos no decurso da unidade curricular e fez com que percebamos melhor o desenvolvimento completo de um jogo um pouco mais complexo de início ao fim.
