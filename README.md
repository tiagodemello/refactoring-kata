# Refactoring Kata: Nota Fiscal Manager (Projeto Fictício)

Este projeto **não é funcional** e foi criado **apenas para fins educacionais e experimentação** em katas de refatoração de código legado.

## Objetivo
O objetivo deste repositório é servir de base para exercícios de refatoração, análise de código ruim, identificação de más práticas e demonstração de técnicas para melhorar código legado.

## Aviso
- **Não utilize este código em produção.**
- O projeto contém métodos propositalmente complexos, confusos e difíceis de manter.
- Não há garantia de funcionamento, cobertura de casos reais ou integração com sistemas externos.

## Como usar
- Use este repositório para treinar refatoração, testes, identificação de code smells e boas práticas.
- Sinta-se livre para clonar, modificar, propor melhorias e praticar técnicas de refatoração.

## Exercícios de Refatoração

### Exercício 1: Correção na Descrição da Nota Fiscal
**Cenário:**
Recentemente, a legislação de São Paulo mudou e agora, para notas fiscais com valor total acima de R$ 100.000, 
a descrição deve obrigatoriamente conter o texto "GRANDE CONTRIBUINTE". Além disso, para notas sem ICMS, 
a descrição deve informar explicitamente "SEM ICMS POR ISENÇÃO". Sua tarefa é implementar essas mudanças 
na lógica de descrição no método `adicionarNotaFiscal`, garantindo que as novas regras sejam atendidas 
sem quebrar os fluxos existentes.

**Desafio:**
- Refatore a lógica de descrição para suportar as novas regras.
- Use técnicas do livro _Working Effectively with Legacy Code_ (Michael Feathers), como "brote uma classe", "envolva método" ou "extraia método".
- Explique suas decisões e justifique as escolhas de design.

### Exercício 2: Alteração nas Regras de Cálculo de Imposto
**Cenário:**
A área fiscal identificou que, para o estado do Rio de Janeiro (RJ), a partir deste ano, o cálculo do ICMS deve considerar uma alíquota extra de 2% para clientes cujo nome contenha "INDUSTRIA" e valor da nota superior a R$ 200.000. Além disso, para clientes do tipo MEI em qualquer estado, o IPI deve ser zerado independentemente do produto.

**Desafio:**
- Implemente as novas regras de negócio no método `calcularImpostos`.
- Refatore o método para facilitar a manutenção e garantir que as regras específicas estejam claras e isoladas.
- Use técnicas do livro do Michael Feathers para facilitar a alteração e garantir testabilidade.
- Justifique suas escolhas de refatoração e design.

## Recomendações para o Kata
- Antes de modificar o código, gere testes automatizados para cobrir os fluxos principais. Use IA para sugerir casos de teste se desejar.
- Utilize ferramentas de cobertura de testes (ex: JaCoCo) e mutação (ex: PITEST) para garantir a qualidade dos testes.
- Aproveite os recursos de refatoração da sua IDE (IntelliJ, Eclipse, VSCode etc) para extrair métodos, mover classes, renomear variáveis etc.
- Documente as decisões de design e refatoração tomadas.

---

Este repositório é parte de um exercício didático sobre como lidar com código legado em projetos Java.
