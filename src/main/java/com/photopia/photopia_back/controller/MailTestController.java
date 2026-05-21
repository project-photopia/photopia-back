package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mail-test")
@RequiredArgsConstructor
@Tag(name = "Mail Test", description = "Endpoints pour tester l'intégration de Mailgun")
public class MailTestController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Envoyer un email de test", description = "Envoie un email via Mailgun à l'adresse spécifiée. ATTENTION : avec un compte Sandbox, l'email doit être vérifié sur Mailgun.")
    public ResponseEntity<String> sendTestMail(
            @Parameter(description = "L'adresse email qui recevra le test", required = true, example = "louis.yang@edu.esiee-it.fr") @RequestParam String email) {

        emailService.sendNewMediaNotification(List.of(email), "Swagger Test Capsule", "Swagger User");

        return ResponseEntity.ok("Commande d'envoi exécutée ! Vérifiez vos logs et votre boîte mail : " + email);
    }
}
