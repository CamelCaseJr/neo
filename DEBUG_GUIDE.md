# 🐛 Guia de Debug - Projeto NEO

Este guia explica como configurar e usar o debug no VS Code para o projeto NEO (Quarkus multi-módulo).

---

## 📋 Pré-requisitos

1. **VS Code** instalado
2. **Extension Pack for Java** instalado (Microsoft)
3. **Quarkus Extension Pack** instalado (Red Hat)
4. **Java 17** configurado
5. **Maven** configurado

---

## 🚀 Como Debugar

### **Método 1: Usando Tasks do VS Code (RECOMENDADO)**

#### **Passo 1: Iniciar o Serviço em Modo Debug**

1. Pressione `Ctrl+Shift+P` para abrir o Command Palette
2. Digite `Tasks: Run Task`
3. Selecione uma das opções:
   - **Start neo-core (Debug Mode)** → Inicia neo-core na porta 5005 (HTTP: 8080)
   - **Start modelo-ia (Debug Mode)** → Inicia modelo-ia na porta 5006 (HTTP: 8081)

**Aguarde** até ver no terminal:

```
Listening for transport dt_socket at address: 5005
...
Quarkus X.XX.X started in X.XXXs
```

#### **Passo 2: Conectar o Debugger**

1. Vá para a aba **Run and Debug** (Ctrl+Shift+D)
2. Selecione a configuração correspondente:
   - **Debug neo-core (Attach)** → Conecta na porta 5005
   - **Debug modelo-ia (Attach)** → Conecta na porta 5006
3. Clique no botão **▶️ Start Debugging** (F5)

✅ **Pronto!** Agora você pode colocar breakpoints e debugar.

---

### **Método 2: Linha de Comando Manual**

#### **Iniciar neo-core em modo debug:**

```powershell
./mvnw -pl neo-core quarkus:dev -Ddebug=5005
```

#### **Iniciar modelo-ia em modo debug:**

```powershell
./mvnw -pl modelo-ia quarkus:dev -Ddebug=5006
```

Depois, conecte o debugger usando o método acima (Passo 2).

---

## 🎯 Colocando Breakpoints

1. Abra o arquivo Java que você quer debugar
2. Clique na margem esquerda (ao lado do número da linha)
3. Um círculo vermelho aparecerá indicando o breakpoint

### **Exemplos de Arquivos Úteis:**

**neo-core:**

- `neo-core/src/main/java/org/acme/controller/NeoController.java`
- `neo-core/src/main/java/org/acme/service/NeoService.java`
- `neo-core/src/main/java/org/acme/repository/NeoRepository.java`

**modelo-ia:**

- `modelo-ia/src/main/java/org/acme/ia/controller/MLResource.java`
- `modelo-ia/src/main/java/org/acme/ia/service/MLTrainingService.java`
- `modelo-ia/src/main/java/org/acme/ia/service/MLInferenceService.java`

---

## 🧪 Testando o Debug

### **1. Debug no neo-core (porta 5005)**

1. Inicie o neo-core em modo debug
2. Conecte o debugger
3. Coloque um breakpoint em `NeoController.java` (linha do método GET)
4. Acesse no navegador: http://localhost:8080/hello
5. O VS Code vai pausar no breakpoint ✅

### **2. Debug no modelo-ia (porta 5006)**

1. Inicie o modelo-ia em modo debug
2. Conecte o debugger
3. Coloque um breakpoint em `MLResource.java` (linha do método POST /ml/train/all)
4. Execute via Postman/Insomnia:
   ```http
   POST http://localhost:8081/ml/train/all
   ```
5. O VS Code vai pausar no breakpoint ✅

---

## 🔧 Controles do Debugger

| Botão       | Atalho        | Descrição                                  |
| ----------- | ------------- | ------------------------------------------ |
| ▶️ Continue | F5            | Continua execução até o próximo breakpoint |
| ⏸️ Pause    | F6            | Pausa a execução                           |
| ↷ Step Over | F10           | Executa a linha atual e vai para a próxima |
| ↓ Step Into | F11           | Entra dentro do método chamado             |
| ↑ Step Out  | Shift+F11     | Sai do método atual                        |
| 🔄 Restart  | Ctrl+Shift+F5 | Reinicia o debug                           |
| ⏹️ Stop     | Shift+F5      | Para o debug                               |

---

## 📊 Painéis do Debug

### **1. VARIABLES**

Mostra todas as variáveis locais e seus valores no escopo atual.

### **2. WATCH**

Permite adicionar expressões customizadas para monitorar. Exemplo:

```java
neoDto.getName()
classifier.toString()
```

### **3. CALL STACK**

Mostra a pilha de chamadas de métodos que levaram até o ponto atual.

### **4. BREAKPOINTS**

Lista todos os breakpoints ativos. Você pode desabilitar temporariamente sem remover.

---

## 🛑 Tipos de Breakpoints

### **1. Breakpoint Simples**

Clique na margem esquerda da linha.

### **2. Conditional Breakpoint**

1. Clique com botão direito na margem
2. Selecione **Add Conditional Breakpoint**
3. Digite a condição. Exemplo:
   ```java
   neoId == 123
   csvCount > 20
   ```

### **3. Logpoint**

Para logar sem pausar:

1. Clique com botão direito na margem
2. Selecione **Add Logpoint**
3. Digite a mensagem. Exemplo:
   ```
   CSV Count: {csvCount}, Bucket: {bucketName}
   ```

---

## 🔥 Hot Code Replace (Live Reload)

O Quarkus suporta **hot reload** automático:

1. Faça alterações no código Java
2. **Salve o arquivo** (Ctrl+S)
3. O Quarkus detecta e recompila automaticamente
4. Sem necessidade de reiniciar!

⚠️ **Limitações:**

- Mudanças de assinatura de método requerem restart
- Adição de novas classes pode precisar restart

---

## 🐞 Troubleshooting

### **Problema: "Cannot attach to port 5005"**

**Solução:**

1. Certifique-se que o serviço está rodando em modo debug
2. Verifique se apareceu: `Listening for transport dt_socket at address: 5005`
3. Confirme que nenhum outro processo está usando a porta:
   ```powershell
   netstat -ano | findstr "5005"
   ```

### **Problema: "Breakpoint não para a execução"**

**Solução:**

1. Verifique se o código foi compilado (deve ter `.class` em `target/classes`)
2. Confirme que não há código otimizado pelo compilador
3. Tente limpar e recompilar:
   ```powershell
   ./mvnw clean compile -pl neo-core
   ```

### **Problema: "Source code não aparece no debug"**

**Solução:**

1. Verifique `settings.json`:
   ```json
   "java.project.sourcePaths": [
       "neo-core/src/main/java",
       "modelo-ia/src/main/java"
   ]
   ```
2. Reabra o workspace: `Ctrl+K Ctrl+O`

### **Problema: "Múltiplas instâncias rodando"**

**Solução:**
Use a task **Stop All Quarkus Services**:

```powershell
Get-Process | Where-Object {$_.ProcessName -eq 'java' -and $_.CommandLine -like '*quarkus:dev*'} | Stop-Process -Force
```

---

## 📂 Estrutura de Arquivos de Debug

```
.vscode/
├── launch.json       → Configurações de debug (attach ports 5005/5006)
├── tasks.json        → Tasks para iniciar serviços em modo debug
└── settings.json     → Configurações Java/Quarkus
```

---

## 🎓 Dicas Pro

### **1. Debug Concorrente (Ambos Módulos)**

Se precisar debugar **neo-core** E **modelo-ia** ao mesmo tempo:

1. Abra **2 terminais** no VS Code
2. Terminal 1:
   ```powershell
   ./mvnw -pl neo-core quarkus:dev -Ddebug=5005
   ```
3. Terminal 2:
   ```powershell
   ./mvnw -pl modelo-ia quarkus:dev -Ddebug=5006
   ```
4. Conecte **ambos os debuggers** na aba Run and Debug

### **2. Debug de Testes**

Para debugar testes JUnit:

1. Abra o arquivo de teste (ex: `NeoServiceTest.java`)
2. Clique com botão direito no método de teste
3. Selecione **Debug Test**

### **3. Evaluate Expression**

Durante o debug, você pode avaliar expressões:

1. Selecione uma expressão no código
2. Clique com botão direito → **Evaluate Expression**
3. Ou use o atalho: `Shift+Alt+E`

### **4. Drop Frame (Voltar no Stack)**

Se você passou de um ponto importante:

1. Na aba **CALL STACK**
2. Clique com botão direito no frame anterior
3. Selecione **Drop Frame**
4. A execução volta para aquele ponto!

---

## 🔗 Recursos Adicionais

- [Quarkus Debugging Guide](https://quarkus.io/guides/maven-tooling#debugging)
- [VS Code Java Debugging](https://code.visualstudio.com/docs/java/java-debugging)
- [Weka API Documentation](https://weka.sourceforge.io/doc.stable-3-8/)

---

## 📞 Suporte

Se tiver problemas, verifique:

1. Logs do terminal (erros de compilação)
2. VS Code Output → Java/Quarkus
3. Versões: Java 17, Maven 3.8+, Quarkus 3.28.1

---

**Happy Debugging! 🎉**
