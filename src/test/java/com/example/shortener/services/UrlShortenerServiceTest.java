package com.example.shortener.services;

import com.example.shortener.model.InvalidUrlException;
import com.example.shortener.model.RandomKeyGen;
import com.example.shortener.model.RedirectionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.shortener.model.Redirection;

import java.util.Optional;

import com.example.shortener.model.RedirectionNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private RedirectionRepo repo;

    @Mock
    private RandomKeyGen gen;

    @Mock
    private UrlValidator validator;

    @InjectMocks
    private UrlShortenerService service;
    //Проверяет, что при ошибочном URL сервис выбрасывает InvalidUrlException
    @Test
    void shortenThrowsInvalidUrlExceptionWhenValidationFails() {
        String url = "bad-url";

        when(validator.validateAndGetError(url))
                .thenReturn("validation error");

        assertThrows(
                InvalidUrlException.class,
                () -> service.shorten(url)
        );
    }
    //Проверяет успешный поиск по shortKey, увеличение счётчика переходов и сохранение обновлённой записи
    @Test
    void resolveReturnsRedirectionAndIncrementsUsageCount() {
        Redirection redirection =
                new Redirection("https://example.com", "abc", "secret");

        when(repo.findByShortKey("abc"))
                .thenReturn(Optional.of(redirection));

        Redirection result = service.resolve("abc");

        assertSame(redirection, result);
        assertEquals(1, result.getUsageCount());
        verify(repo).save(redirection);
    }
    //Проверяет наличие исключения если ссылка по shortKey не найдена
    @Test
    void resolveThrowsExceptionWhenRedirectionNotFound() {
        when(repo.findByShortKey("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                RedirectionNotFoundException.class,
                () -> service.resolve("missing")
        );
    }
    //Проверяет успешный поиск ссылки по secretKey
    @Test
    void findBySecretKeyReturnsRedirectionWhenFound() {
        Redirection redirection =
                new Redirection("https://example.com", "abc", "secret");

        when(repo.findBySecretKey("secret"))
                .thenReturn(Optional.of(redirection));

        Redirection result = service.findBySecretKey("secret");

        assertSame(redirection, result);
    }
    //Проверяет исключение, если ссылка по secretKey не найдена
    @Test
    void findBySecretKeyThrowsExceptionWhenNotFound() {
        when(repo.findBySecretKey("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                RedirectionNotFoundException.class,
                () -> service.findBySecretKey("missing")
        );
    }
    //Проверяет, что найденная по secretKey ссылка передаётся репозиторию на удаление
    @Test
    void deleteRedirectionDeletesFoundRedirection() {
        Redirection redirection =
                new Redirection("https://example.com", "abc", "secret");

        when(repo.findBySecretKey("secret"))
                .thenReturn(Optional.of(redirection));

        service.deleteRedirection("secret");

        verify(repo).delete(redirection);
    }
    //Проверяет успешное сокращение валидного URL, сохранение записи и возврат shortUrl и secretKey
    @Test
    void shortenReturnsShortUrlAndSecretKeyWhenUrlIsValid() {
        String url = "https://example.com";

        ReflectionTestUtils.setField(service, "serverPort", "8080");

        when(validator.validateAndGetError(url))
                .thenReturn(null);
        when(gen.generateKey(3))
                .thenReturn("abc");
        when(gen.generateKey(10))
                .thenReturn("secret1234");

        Pair<String, String> result = service.shorten(url);

        assertEquals("http://localhost:8080/abc", result.getFirst());
        assertEquals("secret1234", result.getSecond());
        verify(repo).save(any(Redirection.class));
    }
}