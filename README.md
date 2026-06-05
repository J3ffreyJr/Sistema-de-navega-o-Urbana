# Urban Nav System

**Urban Nav System** é um motor de navegação urbana para cidades africanas, baseado em dados reais do OpenStreetMap, com suporte a rotas, POIs e análise de conectividade.

## Visão geral

Urban Nav System é um projeto de navegação urbana para Maputo, desenvolvido em Java 11 com Spring Boot, que transforma dados reais do OpenStreetMap em uma rede viária navegável. O sistema já suporta carregamento do ficheiro `.osm`, construção do grafo, cálculo de rotas e operações de busca sobre a malha urbana.

O objetivo é criar uma base reutilizável para outras cidades africanas, oferecendo navegação, exploração de pontos de interesse e análise estrutural da rede de ruas.

## Funcionalidades

- Leitura e parsing de ficheiro `.osm` do OpenStreetMap.
- Construção de grafo viário real.
- Rotas mais curtas com Dijkstra.
- Exploração da rede com BFS e DFS.
- API REST para consultas de rotas.
- Estruturas de dados customizadas para suportar o motor do sistema.
- Base preparada para frontend web responsivo.

## Tecnologias

- Java 11
- Spring Boot 2.7.18
- Maven
- OpenStreetMap `.osm`
- HTML/CSS/JavaScript no frontend
- React ou outra stack web em implementação

## Estruturas de dados e porquê

### Grafo
A rede urbana é modelada como um **grafo ponderado** em lista de adjacência, onde:
- cada cruzamento ou nó geográfico é um vértice;
- cada rua ou segmento viário é uma aresta;
- o peso representa distância ou custo de deslocação.

Essa estrutura é ideal para representar redes de ruas e executar algoritmos de caminho mínimo.

### Dijkstra
Usado para encontrar a rota mais curta entre dois pontos.
É a base do cálculo de navegação no sistema.

### BFS
Usado para explorar locais próximos a partir de um ponto de origem, útil para encontrar serviços num raio definido.

### DFS
Usado para analisar conectividade e detectar zonas desconexas ou isoladas no mapa.

### Trie
Usada para autocomplete de nomes de ruas, bairros e outros nomes de locais.

### Fila, lista ligada, pilha e nós
Estas estruturas sustentam operações internas do parser, das buscas e da navegação sobre o grafo, mantendo o projeto alinhado com os requisitos académicos de estruturas de dados.

## Arquitetura do projeto

```text

```

## Requisitos

- Java 11
- Maven 3.x
- Spring Boot 2.7.x
- Ficheiro `.osm` de Maputo dentro de `src/main/resources/data/` ou `data/`

## Como executar o backend

### 1. Clonar o repositório

```bash
git clone https://github.com/J3ffreyJr/Sistema-de-navega-o-Urban.git
cd Sistema-de-navega-o-Urban
```

### 2. Adicionar o ficheiro OSM

Coloca o ficheiro `maputo.osm` em:

```text
src/main/resources/data/maputo.osm
```

O projeto foi validado com dados reais do OpenStreetMap de Maputo.

### 3. Compilar o projeto

```bash
mvn clean install
```

### 4. Executar a aplicação

```bash
mvn spring-boot:run
```

Ou executar a classe principal `UrbanNavApplication` a partir da tua IDE.

## Resultado esperado ao iniciar

Ao arrancar, o sistema carrega o ficheiro OSM, extrai os nós e as vias, constrói o grafo e expõe a API REST.

Exemplo de log:

```text
=== Urban Nav: Iniciando carregamento do OSM ===
A carregar OSM: src/main/resources/data/maputo.osm
Nos OSM lidos: 2391094
Ways de highway encontradas: 40788
Grafo construido: 258866 nos, 287978 arestas
=== Grafo pronto em 7961ms ===
```

## API REST

A API já inclui o `RouteController` para navegação entre pontos.

### Exemplos de endpoints
> Ajusta os caminhos abaixo aos nomes reais do teu controller.

```http
GET /api/routes
POST /api/routes
GET /api/routes/shortest
```

### Exemplo de uso
```bash
curl "http://localhost:8080/api/routes/shortest?from=1&to=2"
```

## Interface gráfica / frontend

O frontend web ainda está em implementação.

A intenção do projeto é disponibilizar:
- versão para laptop;
- versão para celular;
- mapa responsivo;
- busca por rua, bairro e POIs;
- visualização de rotas no mapa.

A recomendação atual é usar uma interface web responsiva com mapa interativo, para manter uma única base de código e funcionar bem em diferentes tamanhos de ecrã.

## Limitações atuais

- O frontend ainda não está concluído.
- A visualização do mapa ainda está em desenvolvimento.
- A indexação completa de POIs pode ser expandida.
- O suporte para múltiplas cidades ainda não foi generalizado.
- Benchmarks formais ainda podem ser melhorados.

## Próximos passos

- Implementar frontend web responsivo.
- Integrar o mapa com a API REST.
- Adicionar autocomplete com Trie no frontend.
- Indexar POIs como hospitais, escolas e paragens.
- Melhorar o suporte a raio geográfico para buscas locais.
- Criar benchmarks de desempenho.
- Preparar deploy demonstrável do sistema.

## Motivação

O projeto foi criado para apoiar mobilidade urbana e acesso à informação geográfica em cidades africanas, usando dados abertos e estruturas de dados clássicas aplicadas a um problema real.

A ideia é servir como base para navegação urbana, exploração de POIs e análise de conectividade em Maputo e em outras cidades com dados OpenStreetMap.

## Licença

Definir de acordo com a tua preferência antes de publicar.

## Autor

Jeffrey Jr.
Maputo, Moçambique
