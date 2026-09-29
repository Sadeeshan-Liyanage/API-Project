package com.example.api_project.service;

import com.example.api_project.entity.*;
import com.example.api_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;





@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final GeminiService geminiService;

    public String reply(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Ask me about products, prices, stock, or discounts!";
        }

        String context = buildContext();
        String prompt = """
                You are the friendly shop assistant for "Greenline Grocery".
                Answer the customer's question ONLY using the shop data below.
                If the answer isn't in the data, say you don't have that information.
                Keep answers short (1-3 sentences), conversational, and never invent prices or stock numbers.

                SHOP DATA:
                %s

                CUSTOMER QUESTION: %s
                """.formatted(context, userMessage);

        return geminiService.ask(prompt);
    }

    private String buildContext() {
        StringBuilder sb = new StringBuilder();

        sb.append("Products:\n");
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getActive() == null || p.getActive())
                .collect(Collectors.toList());
        for (Product p : products) {
            int stock = inventoryRepository.findByProductId(p.getId()).stream()
                    .mapToInt(Inventory::getQuantity).sum();
            sb.append(String.format("- %s (SKU %s): Rs %.2f per %s, %d in stock%n",
                    p.getName(), p.getSku(), p.getUnitPrice(),
                    p.getUnit() != null ? p.getUnit() : "unit", stock));
        }

        sb.append("\nCategories: ");
        sb.append(categoryRepository.findAll().stream()
                .map(Category::getName).collect(Collectors.joining(", ")));

        sb.append("\n\nActive discounts:\n");
        List<Discount> discounts = discountRepository.findAll().stream()
                .filter(d -> d.getActive() == null || d.getActive())
                .collect(Collectors.toList());
        if (discounts.isEmpty()) {
            sb.append("(none currently)\n");
        } else {
            for (Discount d : discounts) {
                sb.append(String.format("- %s: %s%n", d.getCode(),
                        d.getType() == Discount.DiscountType.PERCENTAGE
                                ? d.getValue() + "%% off" : "Rs " + d.getValue() + " off"));
            }
        }

        return sb.toString();
    }
}


