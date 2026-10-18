package com.example.shortener.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomKeyGenTest {
    //Проверяет, что генератор создаёт ключ заданной длины
    @Test
    void generateKeyReturnsRequestedLength() {
        RandomKeyGen generator = new RandomKeyGen();

        String key = generator.generateKey(10);

        assertEquals(10, key.length());
    }
    //Проверяет, что ключ содержит буквы и цифры из перечня
    @Test
    void generateKeyUsesOnlyAllowedCharacters() {
        RandomKeyGen generator = new RandomKeyGen();

        String key = generator.generateKey(100);

        assertTrue(key.matches("[a-zA-Z0-9]+"));
    }
    //Проверяет, что при длине строки 0, возвращается пустая строка
    @Test
    void generateKeyWithZeroLengthReturnsEmptyString() {
        RandomKeyGen generator = new RandomKeyGen();

        String key = generator.generateKey(0);

        assertEquals("", key);
    }


}

