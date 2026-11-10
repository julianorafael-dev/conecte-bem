package com.conectebem.backend.service;

import com.conectebem.backend.dto.ContatoRequestDTO;
import com.conectebem.backend.exception.ContatoEnvioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ContatoService {

    private final JavaMailSender mailSender;
    private final String destino;
    private final String remetente;

    public ContatoService(JavaMailSender mailSender,
                          @Value("${contato.email.destino}") String destino,
                          @Value("${contato.email.remetente:contato@conectebem.local}") String remetente) {
        this.mailSender = mailSender;
        this.destino = destino;
        this.remetente = remetente;
    }

    public void enviar(ContatoRequestDTO dados) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(destino);
        email.setFrom(remetente);
        email.setReplyTo(dados.email());
        email.setSubject("Nova mensagem pelo site Conecte-Bem");
        email.setText("Nome: " + dados.nome() + "\nE-mail: " + dados.email()
                + "\n\n" + dados.mensagem());
        try {
            mailSender.send(email);
        } catch (MailException ex) {
            throw new ContatoEnvioException(ex);
        }
    }
}
