<div align="center">

# ⚔️ NEXORA Project

### Lineage II Interlude Development Project

Servidor baseado em Java 11 e MariaDB com ferramentas auxiliares para cliente, launcher, atualizador e painel web.

![Java](https://img.shields.io/badge/Java-11-orange)
![MariaDB](https://img.shields.io/badge/MariaDB-10.4-blue)
![Platform](https://img.shields.io/badge/Platform-Windows-success)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

</div>

---

# 📖 Sobre o Projeto

O **NEXORA Project** é uma base de desenvolvimento para servidores **Lineage II Interlude**, desenvolvida com foco em:

- 🚀 Performance
- 🔒 Segurança
- 🛠️ Facilidade de manutenção
- 📦 Distribuição simplificada
- 🌐 Integração com Website e Launcher
- 🔄 Sistema de atualização automática

---

# ✨ Recursos

| Recurso | Status |
|----------|----------|
| Login Server | ✅ |
| Game Server | ✅ |
| Sistema de Atualização | ✅ |
| Launcher Customizado | ✅ |
| Proteção Cliente | ✅ |
| InterfaceBlock | ✅ |
| Painel Web | ✅ |
| Banco MariaDB | ✅ |
| Java 11 | ✅ |

---

# 📥 Downloads Oficiais

## Arquivos do Projeto

| Componente | Link |
|------------|------------|
| 🛡️ DStupe.dll (Proteção Cliente) | https://github.com/JulioPradoL2j/L2Protection |
| 🎮 Cliente Interlude + Patch | https://www.mediafire.com/file/8bwfzaco9k7jqv4/Lineage_II_-_Chronicle_Interlude.zip/file |
| 🖥️ InterfaceBlock | https://github.com/JulioPradoL2j/InterfaceBlock |
| 🚀 Launcher | https://github.com/JulioPradoL2j/L2Updater |
| 🌐 Website / Painel Web | https://github.com/JulioPradoL2j/L2UpdaterWeb |
| 🔄 Atualizador Compilado | https://github.com/JulioPradoL2j/CompiledFiles |

---

## Dependências

| Software | Versão |
|------------|------------|
| ☕ Java JDK | 11 |
| 🛢️ MariaDB | 10.4+ |
| 🖥️ Eclipse IDE | Última versão |

### Downloads

Java 11

https://mega.nz/file/V7tj1arS#OKWaTzaCqYK0m3iMmR0kW3TddfAJoiu8a20kOFEKShk

MariaDB 10.4

https://mega.nz/file/1jEykRgL#DDuIGktiFbmE-M0jMzhUvYVckw1U0ov-OnZEYS5vopU

---

# 🖥️ Requisitos do Sistema

## Ambiente de Desenvolvimento

| Item | Recomendado |
|---------|---------|
| Windows | 10 ou 11 |
| Processador | Intel i5 / Ryzen 5 |
| RAM | 8 GB |
| SSD | 20 GB livres |
| Java | JDK 11 |
| Banco | MariaDB 10.4 |

## Ambiente de Produção

| Item | Recomendado |
|---------|---------|
| Windows Server | 2019 ou superior |
| CPU | Xeon / Ryzen |
| RAM | 16 GB |
| SSD | Recomendado |
| Rede | Conexão dedicada |

---

# ⚙️ Instalação Rápida

## 1. Instalar Java 11

Após instalar o Java configure a variável de ambiente:

### JAVA_HOME

```text
C:\Program Files\Java\jdk-11
```

Adicionar ao PATH:

```text
%JAVA_HOME%\bin
```

Verificar instalação:

```cmd
java -version
```

Resultado esperado:

```text
openjdk version "11"
```

---

## 2. Instalar MariaDB

Instale o MariaDB e configure:

```text
Usuário: root
Senha: root
```

Confirme que o serviço está iniciado.

---

## 3. Criar Banco de Dados

Abra a pasta:

```text
tools\
```

Execute:

```bat
install_db.bat
```

O instalador criará automaticamente:

- Databases
- Tabelas
- Dados iniciais
- Estrutura necessária

---

## 4. Importar Projeto no Eclipse

Abra:

```text
File
 └── Import
      └── Existing Projects into Workspace
```

Selecione a pasta raiz do projeto.

---

## 5. Iniciar o Servidor

Dentro da pasta:

```text
launcher\
```

Execute:

```text
LoginServer.launch
```

Depois:

```text
GameServer.launch
```

Ou:

```text
Run As → Java Application
```

---

# 🔨 Compilação

Para gerar os binários:

```text
build.xml
```

Para gerar o pacote final:

```text
amount.xml
```

Estrutura gerada:

```text
Zip/
├── LoginServer
├── GameServer
├── libs
├── tools
├── images
└── configs
```

---

# 🌐 Portas Utilizadas

| Serviço | Porta |
|----------|----------|
| Login Server | 2106 |
| Game Server | 7777 |
| MariaDB | 3306 |

Caso utilize VPS ou dedicado, libere essas portas no Firewall.

---

# ❗ Problemas Comuns

### Java não encontrado

Verifique:

```cmd
java -version
```

e confirme a configuração do:

```text
JAVA_HOME
```

---

### Access denied for user 'root'

Verifique usuário e senha do banco configurados corretamente.

---

### Nenhuma tabela criada

Confirme a existência dos arquivos SQL em:

```text
tools\sql\
```

---

### Servidor não conecta ao banco

Verifique:

- MariaDB iniciado
- Porta 3306 aberta
- Usuário configurado corretamente
- Configurações do database.properties

---

# 📚 Repositórios Relacionados

| Projeto | Link |
|----------|----------|
| L2Protection | https://github.com/JulioPradoL2j/L2Protection |
| InterfaceBlock | https://github.com/JulioPradoL2j/InterfaceBlock |
| L2Updater | https://github.com/JulioPradoL2j/L2Updater |
| L2UpdaterWeb | https://github.com/JulioPradoL2j/L2UpdaterWeb |
| CompiledFiles | https://github.com/JulioPradoL2j/CompiledFiles |

---

# 📞 Suporte

Ao reportar um problema informe:

- Sistema Operacional
- Versão do Java
- Mensagem de erro completa
- Log do LoginServer
- Log do GameServer

📧 E-mail:

juliopradol2j@gmail.com

---

# 📜 Licença

Este projeto é disponibilizado para fins de estudo, desenvolvimento e manutenção de servidores Lineage II.

Verifique os termos definidos pelos autores antes da redistribuição.

---

<div align="center">

### © NEXORA Project

Desenvolvido para a comunidade Lineage II

</div>