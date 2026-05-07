package com.gestaofinanceira.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import com.gestaofinanceira.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class StatementProcessorService {
    private static final Logger log = LoggerFactory.getLogger(StatementProcessorService.class);

    private final GeminiApiService geminiApiService;
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;
    private final com.gestaofinanceira.repository.UserRepository userRepository;

    public StatementProcessorService(GeminiApiService geminiApiService, ObjectMapper objectMapper, TransactionRepository transactionRepository, com.gestaofinanceira.repository.UserRepository userRepository) {
        this.geminiApiService = geminiApiService;
        this.objectMapper = objectMapper;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<Transaction> processStatement(MultipartFile file, String email) throws Exception {
        byte[] fileBytes = file.getBytes();
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/pdf";
        String base64Data = Base64.getEncoder().encodeToString(fileBytes);

        com.gestaofinanceira.model.User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String textPrompt = """
                Você é um especialista em finanças. Leia o documento em anexo que é um extrato bancário ou fatura de cartão de crédito.
                Extraia TODAS as transações financeiras (compras, pagamentos, recebimentos, salário, pix recebido).
                Ignore transferências entre contas da mesma titularidade, se for o caso. Ignore avisos do banco.
                Retorne ESTREITAMENTE um JSON Array válido, onde cada objeto representa um gasto extraído e contenha os seguintes campos:
                - "date": string (data da transação no formato exato YYYY-MM-DD)
                - "description": string (nome do estabelecimento ou tipo de transação)
                - "amount": number (o valor numérico da transação, SEMPRE positivo)
                - "type": string (DEVE ser exatamente "ENTRADA" para rendimentos ou "SAIDA" para gastos)
                - "recurring": boolean (true se for custo fixo como Netflix, Luz, Água)
                - "category": string (DEVE ser exatamente um destes quatro: "Alimentação", "Transporte", "Lazer", ou "Outros")
                - "confidence": number (de 0.0 a 1.0)

                NÃO retorne markdown, apenas o texto do array JSON puro.
                """;

        String aiResponse = geminiApiService.generateContent(textPrompt, mimeType, base64Data);
        return parseAndSaveTransactions(aiResponse, user);
    }

    private List<Transaction> parseAndSaveTransactions(String aiResponse, com.gestaofinanceira.model.User user) {
        try {
            log.info("Resposta bruta da IA: {}", aiResponse);

            int startIndex = aiResponse.indexOf('[');
            int endIndex = aiResponse.lastIndexOf(']');

            if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
                log.error("IA não retornou um array JSON válido.");
                throw new RuntimeException("Formato inválido retornado pela IA.");
            }

            String jsonArrayStr = aiResponse.substring(startIndex, endIndex + 1);

            JsonNode rootArray = objectMapper.readTree(jsonArrayStr);
            List<Transaction> transactions = new ArrayList<>();

            if (rootArray.isArray()) {
                for (JsonNode node : rootArray) {
                    try {
                        Transaction t = new Transaction();
                        t.setDescription(node.path("description").asText("Transação Indefinida"));
                        t.setAmount(BigDecimal.valueOf(Math.abs(node.path("amount").asDouble(0.0))));
                        t.setCategory(Category.fromText(node.path("category").asText("Outros")));
                        t.setType(com.gestaofinanceira.model.TransactionType.fromText(node.path("type").asText("SAIDA")));
                        t.setRecurring(node.path("recurring").asBoolean(false));
                        t.setConfidence(BigDecimal.valueOf(node.path("confidence").asDouble(0.0)));
                        t.setUser(user);

                        if (node.has("date")) {
                            try {
                                t.setDate(java.time.LocalDate.parse(node.get("date").asText()).atStartOfDay());
                            } catch (Exception dateEx) {
                                t.setDate(java.time.LocalDateTime.now());
                            }
                        }

                        transactions.add(t);
                    } catch (Exception e) {
                        log.warn("Falha ao ler um dos gastos do extrato.");
                    }
                }
            }

            if (!transactions.isEmpty()) {
                return transactionRepository.saveAll(transactions);
            }
            return transactions;

        } catch (Exception e) {
            log.error("Erro ao fazer parsing do resultado da IA para o extrato: '{}'", aiResponse, e);
            throw new RuntimeException("Erro ao processar extrato bancário.");
        }
    }
}

