# WePayU

# WePayU - Sistema de Folha de Pagamento

Implementacao das User Stories do projeto de Programacao 2 usando Java + EasyAccept.

## Estrutura

- `src/` - codigo-fonte
- `src/br/ufal/ic/p2/wepayu/Facade.java` - fachada usada pelos testes
- `src/br/ufal/ic/p2/wepayu/models/` - modelos do dominio
- `tests/` - testes EasyAccept fornecidos
- `ok/` - folhas de pagamento esperadas
- `lib/easyaccept.jar` - biblioteca EasyAccept

## Como executar no IntelliJ

1. Abra a pasta `WePayU`.
2. Verifique se `src` esta marcado como Sources Root.
3. Execute `Main.java`.
4. Para trocar o teste, informe como argumento de programa, por exemplo:
   `tests/us7.txt`

Tambem e possivel executar diretamente:

```text
java -cp "out/production/WePayU;lib/easyaccept.jar" easyaccept.EasyAccept br.ufal.ic.p2.wepayu.Facade tests/us7.txt
```

No Linux/macOS, troque `;` por `:` no classpath.

## Persistencia

O sistema grava o estado em `wepayu.dat` quando `encerrarSistema` e executado.
Esse arquivo e criado automaticamente na pasta em que o programa e executado.

## Observacao sobre os testes fornecidos

O `tests/us9.txt` original possui um erro de digitacao na linha do teste da agenda `semanal 2 5`:
`expect 1292,30totalFolha ...`. O correto e haver um espaco antes de `totalFolha`.
A implementacao foi validada com essa pequena correcao local.

## Validacao realizada

Os testes US1 a US8 foram executados com sucesso, assim como os testes de persistencia.
US9 e US10 tambem passaram quando executados com a correcao de digitacao mencionada acima.
