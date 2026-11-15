package com.conectebem.backend.service;

import com.conectebem.backend.dto.ContatoRequestDTO;
import com.conectebem.backend.exception.ContatoEnvioException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContatoServiceTest {
    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final ContatoService service = new ContatoService(sender, "destino@exemplo.com", "site@exemplo.com");
    private final ContatoRequestDTO dados = new ContatoRequestDTO("Ana", "ana@exemplo.com", "Quero ajudar.");

    @Test
    void enviaParaDestinoConfiguradoComRespostaAoVisitante() {
        service.enviar(dados);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        SimpleMailMessage email = captor.getValue();
        assertArrayEquals(new String[]{"destino@exemplo.com"}, email.getTo());
        assertEquals("site@exemplo.com", email.getFrom());
        assertEquals(dados.email(), email.getReplyTo());
        assertTrue(email.getText().contains(dados.nome()));
        assertTrue(email.getText().contains(dados.mensagem()));
    }

    @Test
    void preservaCausaSemExporDetalhesSmtpNaMensagem() {
        MailSendException causa = new MailSendException("detalhe privado do SMTP");
        doThrow(causa).when(sender).send(any(SimpleMailMessage.class));
        ContatoEnvioException erro = assertThrows(ContatoEnvioException.class, () -> service.enviar(dados));
        assertSame(causa, erro.getCause());
        assertFalse(erro.getMessage().contains("SMTP"));
    }
}
