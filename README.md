# Conversor de Arquivos 📄

Uma aplicação web moderna e eficiente para conversão de arquivos, similar ao ILovePDF e Sejda. Desenvolvida com Spring Boot e seguindo as melhores práticas de desenvolvimento.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

## 🎯 Funcionalidades

- ✅ Conversão de PDF para Word (DOCX)
- ✅ Conversão de PDF para Texto (TXT)
- ✅ Conversão de PDF para Imagem (JPG/PNG)
- ✅ Conversão de Imagem para PDF
- ✅ Conversão entre formatos de imagem (JPG ↔ PNG)
- ✅ Conversão de Texto para PDF
- ✅ Interface web intuitiva e responsiva
- ✅ Suporte para drag and drop
- ✅ Download direto dos arquivos convertidos
- ✅ Tamanho máximo de arquivo: 50MB

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem de programação
- **Spring Boot 3.2.0** - Framework principal
- **Spring MVC** - Arquitetura web
- **Thymeleaf** - Template engine
- **Apache PDFBox 3.0.1** - Manipulação de PDF
- **Apache POI 5.2.5** - Manipulação de documentos Office
- **Maven** - Gerenciamento de dependências

### Frontend
- **Bootstrap 5** - Framework CSS
- **Bootstrap Icons** - Ícones
- **JavaScript** - Interatividade
- **HTML5/CSS3** - Estrutura e estilo

## 📋 Pré-requisitos

- Java JDK 17 ou superior
- Maven 3.6 ou superior
- Navegador web moderno

## 🔧 Instalação e Execução

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/ConversordeArquivos.git
cd ConversordeArquivos
```

### 2. Compile o projeto

```bash
mvn clean install
```

### 3. Execute a aplicação

```bash
mvn spring-boot:run
```

Ou execute o JAR gerado:

```bash
java -jar target/conversor-arquivos-1.0.0.jar
```

### 4. Acesse a aplicação

Abra seu navegador e acesse:
```
http://localhost:8080
```

## 📁 Estrutura do Projeto

```
ConversordeArquivos/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── conversor/
│   │   │           ├── config/          # Configurações
│   │   │           ├── controller/      # Controllers MVC e REST
│   │   │           ├── dto/             # Data Transfer Objects
│   │   │           ├── exception/       # Tratamento de exceções
│   │   │           ├── model/           # Modelos de domínio
│   │   │           ├── service/         # Lógica de negócio
│   │   │           ├── util/            # Utilitários
│   │   │           └── ConversorApplication.java
│   │   └── resources/
│   │       ├── static/                  # Arquivos estáticos
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       ├── templates/               # Templates Thymeleaf
│   │       │   ├── index.html
│   │       │   ├── about.html
│   │       │   └── conversions.html
│   │       └── application.properties
│   └── test/                            # Testes
├── uploads/                             # Arquivos enviados (gerado em runtime)
├── converted/                           # Arquivos convertidos (gerado em runtime)
├── pom.xml
└── README.md
```

## 🏗️ Arquitetura

A aplicação segue uma arquitetura em camadas (Layered Architecture):

### Controller Layer
- **WebController**: Gerencia as páginas web
- **FileConversionController**: API REST para conversão de arquivos

### Service Layer
- **FileConversionService**: Lógica de conversão entre formatos
- **FileStorageService**: Gerenciamento de armazenamento de arquivos

### Model Layer
- **ConvertedFile**: Representa um arquivo convertido
- **FileFormat**: Enum dos formatos suportados
- **ConversionType**: Enum dos tipos de conversão

### Exception Handling
- **GlobalExceptionHandler**: Tratamento centralizado de exceções
- **FileConversionException**: Exceção de conversão
- **FileStorageException**: Exceção de armazenamento

## 🔄 Conversões Suportadas

### De PDF para:
- DOCX (Word)
- TXT (Texto)
- JPG (JPEG)
- PNG

### Para PDF:
- DOCX → PDF
- TXT → PDF
- JPG → PDF
- PNG → PDF

### Entre Imagens:
- JPG ↔ PNG

### Outros:
- DOCX → TXT

## 🌐 API REST

### Endpoints Disponíveis

#### 1. Converter Arquivo
```http
POST /api/files/convert
Content-Type: multipart/form-data

Parameters:
  - file: MultipartFile (arquivo a converter)
  - targetFormat: String (formato de destino)

Response: ConversionResponse
```

#### 2. Download do Arquivo Convertido
```http
GET /api/files/download/{fileId}

Response: Resource (arquivo para download)
```

#### 3. Status da Conversão
```http
GET /api/files/status/{fileId}

Response: ConversionResponse
```

#### 4. Formatos Suportados
```http
GET /api/files/formats

Response: FileFormat[]
```

## ⚙️ Configurações

As configurações podem ser ajustadas no arquivo `application.properties`:

```properties
# Porta do servidor
server.port=8080

# Tamanho máximo de upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Diretórios de armazenamento
file.upload-dir=uploads
file.converted-dir=converted
```

## 🎨 Interface do Usuário

A interface foi desenvolvida com foco em:
- **Usabilidade**: Interface intuitiva e fácil de usar
- **Responsividade**: Funciona em desktop, tablet e mobile
- **Feedback Visual**: Indicadores de progresso e mensagens claras
- **Design Moderno**: Visual atraente e profissional

### Funcionalidades da Interface:
- Upload por clique ou drag and drop
- Seleção visual de formato de destino
- Indicador de progresso durante conversão
- Download direto do arquivo convertido
- Mensagens de erro claras e informativas

## 🧪 Testes

Para executar os testes:

```bash
mvn test
```

## 📝 Melhores Práticas Implementadas

- ✅ **SOLID Principles**: Código modular e manutenível
- ✅ **Clean Code**: Código limpo e bem documentado
- ✅ **Exception Handling**: Tratamento robusto de erros
- ✅ **Layered Architecture**: Separação clara de responsabilidades
- ✅ **DTOs**: Transferência de dados otimizada
- ✅ **Logging**: Sistema de logs configurado
- ✅ **Validation**: Validação de entrada de dados
- ✅ **REST API**: API RESTful bem estruturada
- ✅ **Responsive Design**: Interface adaptável

## 🔒 Segurança

- Validação de tipos de arquivo
- Limite de tamanho de upload
- Sanitização de nomes de arquivo
- Validação de entrada de dados
- Tratamento seguro de exceções

## 🚧 Roadmap

Funcionalidades planejadas para versões futuras:

- [ ] Conversão de XLSX/XLS (Excel)
- [ ] Conversão de PPTX (PowerPoint)
- [ ] Compressão de PDF
- [ ] Mesclagem de PDFs
- [ ] Divisão de PDF
- [ ] Autenticação de usuários
- [ ] Histórico de conversões
- [ ] API com rate limiting
- [ ] Suporte a conversão em lote
- [ ] Preview de arquivos

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👥 Autores

- **Conversor de Arquivos Team**

## 🙏 Agradecimentos

- Apache PDFBox por fornecer excelente biblioteca de manipulação de PDF
- Apache POI por suporte a documentos Office
- Spring Boot pela facilidade de desenvolvimento
- Bootstrap pela interface responsiva

## 📞 Suporte

Para suporte, abra uma issue no GitHub ou entre em contato através do email: support@conversorapp.com

---

**Desenvolvido com ❤️ e Java**
